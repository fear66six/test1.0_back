package com.byrski;

import com.byrski.common.utils.RoomAllocUtils;
import com.byrski.domain.entity.dto.Account;
import com.byrski.domain.entity.dto.Room;
import com.byrski.domain.entity.dto.Trade;
import com.byrski.domain.entity.dto.TradeTickets;
import com.byrski.domain.enums.Gender;
import com.byrski.domain.enums.ProductType;
import com.byrski.domain.enums.TicketType;
import com.byrski.infrastructure.mapper.impl.AccountMapperService;
import com.byrski.infrastructure.mapper.impl.RoomMapperService;
import com.byrski.infrastructure.mapper.impl.TradeMapperService;
import com.byrski.infrastructure.mapper.impl.TradeTicketsMapperService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.*;

@SpringBootTest
@Slf4j
public class RoomAllocTest {
    @Autowired
    private RoomMapperService roomMapperService;
    @Autowired
    private TradeMapperService tradeMapperService;
    @Autowired
    private RoomAllocUtils roomAllocUtils;
    @Autowired
    private AccountMapperService accountMapperService;
    @Autowired
    private TradeTicketsMapperService tradeTicketsMapperService;

    public static class RoomAllocMeta {
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

    public static List<RoomAllocMeta> combineRoomsBacktrack(List<RoomAllocMeta> rooms) {
        // Group rooms by composite type instead of simple type
        Map<String, List<RoomAllocMeta>> roomGroups = new HashMap<>();
        for (RoomAllocMeta room : rooms) {
            roomGroups.computeIfAbsent(room.getCompositeType(), k -> new ArrayList<>()).add(room);
        }

        List<RoomAllocMeta> result = new ArrayList<>();

        // Process each room type separately
        for (Map.Entry<String, List<RoomAllocMeta>> entry : roomGroups.entrySet()) {
            List<RoomAllocMeta> typeRooms = entry.getValue();

            // Get the roomTicketId and gender from the first room in the group
            // (they'll all be the same within a group)
            Long roomTicketId = typeRooms.get(0).roomTicketId;
            Gender gender = typeRooms.get(0).gender;

            // Get max capacity for current room type
            int maxCapacity = typeRooms.stream()
                    .mapToInt(room -> room.maxPeopleNum)
                    .max()
                    .orElse(0);

            List<RoomPeople> roomPeople = typeRooms.stream()
                    .map(room -> new RoomPeople(room.tradeIds))
                    .sorted((a, b) -> Integer.compare(b.ids.size(), a.ids.size()))
                    .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);

            List<List<RoomPeople>> bestSolution = solveBacktrack(roomPeople, maxCapacity);

            // Convert solution back to RoomAllocMeta objects using the same roomTicketId and gender
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

    private static class RoomPeople {
        List<Long> ids;

        RoomPeople(List<Long> ids) {
            this.ids = new ArrayList<>(ids);
        }

        RoomPeople copy() {
            return new RoomPeople(new ArrayList<>(ids));
        }
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

        // Pruning: if current room count exceeds known minimum
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

        // Try adding to current room
        if (!currentRoom.isEmpty() &&
                currentRoom.stream().mapToInt(r -> r.ids.size()).sum() + current.ids.size() <= maxCapacity) {
            currentRoom.add(current);
            backtrack(pos + 1, currentRooms, currentRoom, roomPeople, maxCapacity, minRooms, bestSolution);
            currentRoom.remove(currentRoom.size() - 1);
        }

        // Try starting a new room
        List<RoomPeople> newRoom = new ArrayList<>();
        newRoom.add(current);
        List<List<RoomPeople>> newCurrentRooms = new ArrayList<>(currentRooms);
        if (!currentRoom.isEmpty()) {
            newCurrentRooms.add(new ArrayList<>(currentRoom));
        }
        backtrack(pos + 1, newCurrentRooms, newRoom, roomPeople, maxCapacity, minRooms, bestSolution);

        // Try adding to existing rooms
        for (List<RoomPeople> existingRoom : currentRooms) {
            if (existingRoom.stream().mapToInt(r -> r.ids.size()).sum() + current.ids.size() <= maxCapacity) {
                existingRoom.add(current);
                backtrack(pos + 1, currentRooms, currentRoom, roomPeople, maxCapacity, minRooms, bestSolution);
                existingRoom.remove(existingRoom.size() - 1);
            }
        }
    }


    public static void main(String[] args) {
        long idCounter = 1;
        List<List<RoomAllocMeta>> testCases = new ArrayList<>();

        // Test case 1: Simple case
        List<RoomAllocMeta> testCase1 = new ArrayList<>();
        testCase1.add(new RoomAllocMeta(1L, Gender.MALE, generateIds(idCounter, 2), 4)); idCounter += 2;
        testCase1.add(new RoomAllocMeta(1L, Gender.MALE, generateIds(idCounter, 1), 4)); idCounter += 1;
        testCase1.add(new RoomAllocMeta(1L, Gender.MALE, generateIds(idCounter, 1), 4)); idCounter += 1;
        testCase1.add(new RoomAllocMeta(2L, Gender.FEMALE, generateIds(idCounter, 3), 6)); idCounter += 3;
        testCase1.add(new RoomAllocMeta(2L, Gender.FEMALE, generateIds(idCounter, 2), 6)); idCounter += 2;
        testCases.add(testCase1);

        // Test case 2: More complex case
        List<RoomAllocMeta> testCase2 = new ArrayList<>();
        testCase2.add(new RoomAllocMeta(1L, Gender.MALE, generateIds(idCounter, 2), 4)); idCounter += 2;
        testCase2.add(new RoomAllocMeta(1L, Gender.MALE, generateIds(idCounter, 1), 4)); idCounter += 1;
        testCase2.add(new RoomAllocMeta(1L, Gender.MALE, generateIds(idCounter, 2), 4)); idCounter += 2;
        testCase2.add(new RoomAllocMeta(1L, Gender.MALE, generateIds(idCounter, 1), 4)); idCounter += 1;
        testCase2.add(new RoomAllocMeta(2L, Gender.FEMALE, generateIds(idCounter, 3), 6)); idCounter += 3;
        testCase2.add(new RoomAllocMeta(2L, Gender.FEMALE, generateIds(idCounter, 2), 6)); idCounter += 2;
        testCase2.add(new RoomAllocMeta(2L, Gender.FEMALE, generateIds(idCounter, 1), 6)); idCounter += 1;
        testCases.add(testCase2);

        // Test case 3
        List<RoomAllocMeta> testCase3 = new ArrayList<>();
        testCase3.add(new RoomAllocMeta(1L, Gender.MALE, generateIds(idCounter, 4), 6)); idCounter += 4;
        testCase3.add(new RoomAllocMeta(1L, Gender.MALE, generateIds(idCounter, 3), 6)); idCounter += 3;
        testCase3.add(new RoomAllocMeta(2L, Gender.FEMALE, generateIds(idCounter, 1), 4)); idCounter += 1;
        testCase3.add(new RoomAllocMeta(2L, Gender.FEMALE, generateIds(idCounter, 2), 4)); idCounter += 2;
        testCase3.add(new RoomAllocMeta(2L, Gender.FEMALE, generateIds(idCounter, 2), 4)); idCounter += 2;
        testCases.add(testCase3);

        // Test case 4
        List<RoomAllocMeta> testCase4 = new ArrayList<>();
        testCase4.add(new RoomAllocMeta(1L, Gender.MALE, generateIds(idCounter, 3), 6)); idCounter += 3;
        testCase4.add(new RoomAllocMeta(1L, Gender.MALE, generateIds(idCounter, 4), 6)); idCounter += 4;
        testCase4.add(new RoomAllocMeta(2L, Gender.FEMALE, generateIds(idCounter, 1), 4)); idCounter += 1;
        testCase4.add(new RoomAllocMeta(2L, Gender.FEMALE, generateIds(idCounter, 2), 4)); idCounter += 2;
        testCase4.add(new RoomAllocMeta(2L, Gender.FEMALE, generateIds(idCounter, 2), 4)); idCounter += 2;
        testCases.add(testCase4);

        // Test case 5
        List<RoomAllocMeta> testCase5 = new ArrayList<>();
        testCase5.add(new RoomAllocMeta(1L, Gender.MALE, generateIds(idCounter, 2), 8)); idCounter += 2;
        testCase5.add(new RoomAllocMeta(2L, Gender.FEMALE, generateIds(idCounter, 3), 7)); idCounter += 3;
        testCase5.add(new RoomAllocMeta(2L, Gender.FEMALE, generateIds(idCounter, 1), 6)); idCounter += 1;
        testCase5.add(new RoomAllocMeta(1L, Gender.MALE, generateIds(idCounter, 2), 4)); idCounter += 2;
        testCase5.add(new RoomAllocMeta(1L, Gender.MALE, generateIds(idCounter, 2), 5)); idCounter += 2;
        testCase5.add(new RoomAllocMeta(3L, Gender.MALE, generateIds(idCounter, 1), 5)); idCounter += 1;
        testCase5.add(new RoomAllocMeta(1L, Gender.MALE, generateIds(idCounter, 2), 5)); idCounter += 2;
        testCase5.add(new RoomAllocMeta(1L, Gender.MALE, generateIds(idCounter, 1), 7)); idCounter += 1;
        testCase5.add(new RoomAllocMeta(3L, Gender.MALE, generateIds(idCounter, 2), 7)); idCounter += 2;
        testCases.add(testCase5);

        // Run all test cases
        for (int i = 0; i < testCases.size(); i++) {
            System.out.println("\nTest Case " + (i + 1) + ":");
            System.out.println("Original rooms:");
            for (RoomAllocMeta room : testCases.get(i)) {
                System.out.printf("RoomTicketId: %d, Gender: %s, People: %d, IDs: %s, Max capacity: %d%n",
                        room.roomTicketId, room.gender, room.peopleNum, room.tradeIds, room.maxPeopleNum);
            }

            List<RoomAllocMeta> result = combineRoomsBacktrack(testCases.get(i));

            System.out.println("\nCombined rooms:");
            for (RoomAllocMeta room : result) {
                System.out.printf("RoomTicketId: %d, Gender: %s, People: %d, IDs: %s, Max capacity: %d%n",
                        room.roomTicketId, room.gender, room.peopleNum, room.tradeIds, room.maxPeopleNum);
            }
            System.out.println("----------------------------------------");
        }
    }

    // Helper method to generate sequential IDs
    private static List<Long> generateIds(long startId, int count) {
        List<Long> ids = new ArrayList<>();
        for (long i = startId; i < startId + count; i++) {
            ids.add(i);
        }
        return ids;
    }

    @Test
    public void testSave() {
        Room room = Room.builder()
                .id(123L)
                .name("Room 123")
                .build();
        roomMapperService.save(room);
    }

    @Test
    public void testUpdate() {
        Trade trade = Trade.builder().id(1891097247610933249L).roomId(7444444L).build();
        tradeMapperService.updateById(trade);
    }

    @Test
    public void generateUser() {
        for (int i = 1; i <= 12; ++i) {
            Account account = new Account();
            account.setId((long) i);
            account.setUsername("测试者" + i);
            account.setPhone(randomPhone());
            account.setIdCardNumber(randomIdCard());
            account.setIdentity(0);
            account.setIsStudent(true);
            account.setIsActive(true);
            account.setSchoolId(1L);
            account.setOpenid("test_openid" + i);
            accountMapperService.save(account);
        }
    }

    @Test
    public void generateTrade() {
        for (int i = 1; i <= 12; ++i) {
            Trade trade = Trade.builder()
                    .id((long) i)
                    .outTradeNo("test_trade_no" + i)
                    .type(ProductType.PACKAGE)
                    .status(3)
                    .busId(1891098547505434626L)
                    .stationId(1890996849617244161L)
                    .busMoveId(1891098547518017538L)
                    .productId("67b1772afba0f10b21717bf3")
                    .activityId(1890995723345956866L)
                    .activityTemplateId(2L)
                    .snowfieldId(2L)
                    .userId((long) i)
                    .wxgroupId(1890997205155811329L)
                    .total(345)
                    .costTicket(345)
                    .build();
            tradeMapperService.save(trade);
        }
    }

    @Test
    public void generateTradeTicket() {

        final List<Long> ticketIds = Arrays.asList(1891029753462362114L, 1890996048568094722L, 1891737826766196737L);

        // 写一个lambda方法，用于从ticketIds中随机获取一个id
        final Random random = new Random();
        final int size = ticketIds.size();
        final List<Long> randomTicketIds = new ArrayList<>();
        for (int i = 1; i <= 12; ++i) {
            randomTicketIds.add(ticketIds.get(random.nextInt(size)));
        }

        for (int i = 1; i <= 12; ++i) {
            TradeTickets tradeTickets = TradeTickets.builder()
                    .id((long) i)
                    .tradeId((long) i)
                    .ticketId(randomTicketIds.get(i - 1))
                    .type(TicketType.ROOM)
                    .build();
            tradeTicketsMapperService.save(tradeTickets);
        }
    }

    @Test
    public void allocRoom() {
        roomAllocUtils.allocateRoom(1890995723345956866L);
    }

    @Test
    public void cleanTest() {
        tradeTicketsMapperService.removeBatchByIds(Arrays.asList(1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L, 11L, 12L));
        tradeMapperService.removeBatchByIds(Arrays.asList(1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L, 11L, 12L));
    }

    private String randomPhone() {
        StringBuilder sb = new StringBuilder();
        sb.append("1");
        for (int i = 0; i < 10; ++i) {
            sb.append((int) (Math.random() * 10));
        }
        return sb.toString();
    }

    private String randomIdCard() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 18; ++i) {
            sb.append((int) (Math.random() * 10));
        }
        return sb.toString();
    }
}
