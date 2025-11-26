package com.bfc.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class WordDto {
    private Integer id;
    private String word;
    private String meaning;
    private Integer day;
    private String lang;
    private LocalDateTime createdAt;
    private List<WordExtensionDto> extensions;
    /**
     * 顽固单词星标（0/1），普通列表可为空
     */
    private Integer isStar;
}
