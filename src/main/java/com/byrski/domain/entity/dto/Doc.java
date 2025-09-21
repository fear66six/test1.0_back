package com.byrski.domain.entity.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "docs")
public class Doc {
    @Id
    private String id;
    private Integer type;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long activityTemplateId;
    private List<Content> contents;

    @Data
    public static class Content {
        private String title;
        private String imageUrl;
        private List<String> paragraphs;
    }
}