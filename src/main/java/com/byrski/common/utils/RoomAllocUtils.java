package com.byrski.common.utils;

import com.baomidou.mybatisplus.core.toolkit.Sequence;
import com.byrski.domain.entity.dto.Account;
import com.byrski.domain.entity.dto.Room;
import com.byrski.domain.entity.dto.Trade;
import com.byrski.domain.entity.dto.ticket.RoomTicket;
import com.byrski.domain.enums.Gender;
import com.byrski.domain.enums.TicketType;
import com.byrski.infrastructure.mapper.impl.AccountMapperService;
import com.byrski.infrastructure.mapper.impl.RoomMapperService;
import com.byrski.infrastructure.mapper.impl.TradeMapperService;
import com.byrski.infrastructure.mapper.impl.TradeTicketsMapperService;
import com.byrski.strategy.factory.TicketStrategyFactory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class RoomAllocUtils {

    private final TradeMapperService tradeMapperService;
    private final RoomMapperService roomMapperService;
    private final AccountMapperService accountMapperService;
    private final TradeTicketsMapperService tradeTicketsMapperService;
    private final TicketStrategyFactory ticketStrategyFactory;
    private final TradeUtils tradeUtils;
    private final Sequence sequence;

    public RoomAllocUtils(TradeMapperService tradeMapperService, RoomMapperService roomMapperService, AccountMapperService accountMapperService, TradeTicketsMapperService tradeTicketsMapperService, TicketStrategyFactory ticketStrategyFactory, TradeUtils tradeUtils) {
        this.tradeMapperService = tradeMapperService;
        this.roomMapperService = roomMapperService;
        this.accountMapperService = accountMapperService;
        this.tradeTicketsMapperService = tradeTicketsMapperService;
        this.ticketStrategyFactory = ticketStrategyFactory;
        this.tradeUtils = tradeUtils;
        this.sequence = new Sequence(null);
    }

    /**
     * 分房内部实体类
     */
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    private static class RoomAllocMeta {
        Long roomTicketId;
        Gender gender;
        List<Long> tradeIds;
        int peopleNum;
        int maxPeopleNum;

        public RoomAllocMeta(Long roomTicketId, Gender gender, List<Long> tradeIds, int maxPeopleNum) {
            this.roomTicketId = roomTicketId;
            this.gender = gender;
            this.tradeIds = tradeIds;
            this.peopleNum = tradeIds.size();
            this.maxPeopleNum = maxPeopleNum;
        }

        public RoomAllocMeta(Long roomTicketId, Gender gender, int maxPeopleNum) {
            this.roomTicketId = roomTicketId;
            this.gender = gender;
            this.tradeIds = new ArrayList<>();
            this.peopleNum = 0;
            this.maxPeopleNum = maxPeopleNum;
        }

        private String getCompositeType() {
            return roomTicketId.toString() + "_" + gender;
        }
    }

    private static class RoomPeople {
        List<Long> ids;

        RoomPeople(List<Long> ids) {
            this.ids = new ArrayList<>(ids);
        }

        RoomPeople copy() {
            return new RoomPeople(new ArrayList<>(ids));
        }
    }

    public void allocateRoom(Long activityId) {
        List<RoomAllocMeta> roomAllocMetas = getRoomAllocMetas(activityId);
        List<RoomAllocMeta> result = combineRoomsBacktrack(roomAllocMetas);
        updateAllocData(activityId, result);
    }

    /**
     * 获取活动中所有需要分配房间的订单，具体为过滤掉无效订单（取消、退款、删除）
     * @param activityId 活动ID
     * @return 需要分配房间的订单
     */
    private List<RoomAllocMeta> getRoomAllocMetas(Long activityId) {
        // 获取所有需要拼房的订单trades
        List<Trade> trades = tradeMapperService.getAllocRoomTradeByActivityId(activityId);

        // 分配前，获取该活动下的所有已经创建的房间
        List<Room> initialRoomList = roomMapperService.getRoomListByActivityId(activityId);
        List<RoomAllocMeta> roomAllocMetas = new ArrayList<>();

        // 寻找未满的房间，添加当前房间到待分房列表中
        for (Room room : initialRoomList) {
            if (room.getPeopleNum() < room.getMaxPeopleNum()) {
                List<Long> tradesIdInRoom = tradeMapperService.getByRoomId(room.getId()).stream().map(Trade::getId).toList();
                Account owner = accountMapperService.getById(room.getOwnerId());
                roomAllocMetas.add(RoomAllocMeta.builder()
                        .roomTicketId(room.getTicketId())
                                .gender(Gender.fromCode(owner.getGender()))
                        .tradeIds(tradesIdInRoom)
                        .peopleNum(tradesIdInRoom.size())
                        .maxPeopleNum(room.getMaxPeopleNum())
                        .build());
                roomMapperService.deleteRoom(room.getId());
            }
        }

        // 对没有创建房间的订单，自动创建一个新房间
        for (Trade trade : trades) {
            RoomTicket roomTicket = getRoomTicketWithTradeId(trade.getId());
            if (roomTicket != null) {
                if (trade.getRoomId() == null) {
                    Account owner = accountMapperService.getById(trade.getUserId());
                    roomAllocMetas.add(RoomAllocMeta.builder()
                            .roomTicketId(roomTicket.getTicketId())
                            .gender(Gender.fromCode(owner.getGender()))
                            .tradeIds(List.of(trade.getId()))
                            .peopleNum(1)
                            .maxPeopleNum(roomTicket.getMaxPeopleNum())
                            .build());
                }
            }
        }
        return roomAllocMetas;
    }

    /**
     * 更新分房结果
     * @param activityId 活动ID
     * @param result 分房结果
     */
    private void updateAllocData(Long activityId, List<RoomAllocMeta> result) {
        List<Room> saveRoomBatch = new ArrayList<>();
        List<Trade> updateTradeBatch = new ArrayList<>();
        for (RoomAllocMeta roomAllocMeta : result) {
            Trade ownerTrade = tradeMapperService.getById(roomAllocMeta.tradeIds.get(0));
            Account owner = accountMapperService.getById(ownerTrade.getUserId());
            RoomTicket roomTicket = (RoomTicket) ticketStrategyFactory.getStrategy(TicketType.ROOM).getTicketById(roomAllocMeta.roomTicketId);
            Room room = Room.builder()
                    .id(sequence.nextId())
                    .name(roomTicket.getDescription())
                    .coed(roomTicket.getCoed())
                    .peopleNum(roomAllocMeta.peopleNum)
                    .maxPeopleNum(roomTicket.getMaxPeopleNum())
                    .code(tradeUtils.generateNumericVerificationCode())
                    .tradeId(ownerTrade.getId())
                    .activityId(activityId)
                    .ownerId(owner.getId())
                    .ownerName(owner.getUsername())
                    .ticketId(roomAllocMeta.roomTicketId)
                    .build();
            saveRoomBatch.add(room);
            for (Long tradeId : roomAllocMeta.tradeIds) {
                Trade trade = tradeMapperService.getById(tradeId);
                trade.setRoomId(room.getId());
                updateTradeBatch.add(trade);
            }
        }
        roomMapperService.saveBatch(saveRoomBatch);
        tradeMapperService.updateBatchById(updateTradeBatch);
    }

    private List<RoomAllocMeta> combineRoomsBacktrack(List<RoomAllocMeta> rooms) {
        // 按组合类型而不是简单类型划分的团体房间
        Map<String, List<RoomAllocMeta>> roomGroups = new HashMap<>();
        for (RoomAllocMeta room : rooms) {
            roomGroups.computeIfAbsent(room.getCompositeType(), k -> new ArrayList<>()).add(room);
        }

        List<RoomAllocMeta> result = new ArrayList<>();

        // 分别处理每种房型
        for (Map.Entry<String, List<RoomAllocMeta>> entry : roomGroups.entrySet()) {
            List<RoomAllocMeta> typeRooms = entry.getValue();

            // 从组中的第一个房间获取房间TicketId和性别
            // （他们在一个群体中都是一样的）
            Long roomTicketId = typeRooms.get(0).roomTicketId;
            Gender gender = typeRooms.get(0).gender;

            // 获得当前房间类型的最大容量
            int maxCapacity = typeRooms.stream()
                    .mapToInt(room -> room.maxPeopleNum)
                    .max()
                    .orElse(0);

            List<RoomPeople> roomPeople = typeRooms.stream()
                    .map(room -> new RoomPeople(room.tradeIds))
                    .sorted((a, b) -> Integer.compare(b.ids.size(), a.ids.size()))
                    .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);

            List<List<RoomPeople>> bestSolution = solveBacktrack(roomPeople, maxCapacity);

            // 使用相同的房间TicketId和性别将解决方案转换回RoomAllocMeta对象
            for (List<RoomPeople> combination : bestSolution) {
                List<Long> combinedIds = new ArrayList<>();
                for (RoomPeople rp : combination) {
                    combinedIds.addAll(rp.ids);
                }
                result.add(new RoomAllocMeta(roomTicketId, gender, combinedIds, maxCapacity));
            }
        }

        return result;
    }

    private static List<List<RoomPeople>> solveBacktrack(List<RoomPeople> roomPeople, int maxCapacity) {
        int n = roomPeople.size();
        int[] minRooms = {Integer.MAX_VALUE};
        List<List<RoomPeople>> bestSolution = new ArrayList<>();

        backtrack(0, new ArrayList<>(), new ArrayList<>(), roomPeople, maxCapacity, minRooms, bestSolution);

        return bestSolution;
    }

    private static void backtrack(
            int pos,
            List<List<RoomPeople>> currentRooms,
            List<RoomPeople> currentRoom,
            List<RoomPeople> roomPeople,
            int maxCapacity,
            int[] minRooms,
            List<List<RoomPeople>> bestSolution) {

        // 修剪：如果当前房间数超过已知最小值
        if (currentRooms.size() + (currentRoom.isEmpty() ? 0 : 1) >= minRooms[0]) {
            return;
        }

        // All rooms have been allocated
        if (pos == roomPeople.size()) {
            List<List<RoomPeople>> totalRooms = new ArrayList<>(currentRooms);
            if (!currentRoom.isEmpty()) {
                totalRooms.add(new ArrayList<>(currentRoom));
            }
            if (totalRooms.size() < minRooms[0]) {
                minRooms[0] = totalRooms.size();
                bestSolution.clear();
                for (List<RoomPeople> room : totalRooms) {
                    bestSolution.add(new ArrayList<>(room));
                }
            }
            return;
        }

        RoomPeople current = roomPeople.get(pos);

        // 尝试添加到当前房间
        if (!currentRoom.isEmpty() &&
                currentRoom.stream().mapToInt(r -> r.ids.size()).sum() + current.ids.size() <= maxCapacity) {
            currentRoom.add(current);
            backtrack(pos + 1, currentRooms, currentRoom, roomPeople, maxCapacity, minRooms, bestSolution);
            currentRoom.remove(currentRoom.size() - 1);
        }

        // 试着开始一个新房间
        List<RoomPeople> newRoom = new ArrayList<>();
        newRoom.add(current);
        List<List<RoomPeople>> newCurrentRooms = new ArrayList<>(currentRooms);
        if (!currentRoom.isEmpty()) {
            newCurrentRooms.add(new ArrayList<>(currentRoom));
        }
        backtrack(pos + 1, newCurrentRooms, newRoom, roomPeople, maxCapacity, minRooms, bestSolution);

        // 尝试添加到现有房间
        for (List<RoomPeople> existingRoom : currentRooms) {
            if (existingRoom.stream().mapToInt(r -> r.ids.size()).sum() + current.ids.size() <= maxCapacity) {
                existingRoom.add(current);
                backtrack(pos + 1, currentRooms, currentRoom, roomPeople, maxCapacity, minRooms, bestSolution);
                existingRoom.remove(existingRoom.size() - 1);
            }
        }
    }

    private RoomTicket getRoomTicketWithTradeId(Long tradeId) {
        Long roomTicketId = tradeTicketsMapperService.getRoomTicketIdByTradeId(tradeId);
        RoomTicket roomTicket = null;
        if (roomTicketId != null) {
            roomTicket = (RoomTicket) ticketStrategyFactory.getStrategy(TicketType.ROOM).getTicketById(roomTicketId);
        }
        return roomTicket;
    }


}
