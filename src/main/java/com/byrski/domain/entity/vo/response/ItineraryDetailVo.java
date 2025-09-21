package com.byrski.domain.entity.vo.response;

import com.byrski.domain.entity.dto.Doc;
import com.byrski.domain.enums.ProductType;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;
import lombok.Data;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@AllArgsConstructor
@Data
@Builder
public class ItineraryDetailVo {
    private String name;
    private ProductType type;
    private String skiResortLocation;
    private String beginDate;
    private String busNumber;
    private String busName;
    private String toArea;
    private String busMoveTime;
    private String arrivalTime;
    private String school;
    private String campus;
    private String location;
    private String arrivalLocation;
    private String returnTime;
    private String returnLocation;
    private Doc schedule;
    private Doc attention;
    private int itineraryStatus;
    private String hotel;
    private String ticketIntro;
    private String roomCode;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long stationId;

    // 用户使用字段
    private String qrCode;
    private LeaderInfo leaderInfo;
    private Boolean busMoveAvailable;

    // 领队使用字段
    private Doc leaderNotice;
    private List<StationInfo> stationInfoList;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long busId;

    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    public static class LeaderInfo {
        @JsonSerialize(using = ToStringSerializer.class)
        private Long id;
        private String name;
        private String phone;
        private String profile;
        private String intro;
    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    public static class StationInfo {
        @JsonSerialize(using = ToStringSerializer.class)
        private long id;
        private String school;
        private String campus;
        private String location;
        private Boolean goFinished;
        private LocalDateTime time;
    }
}
