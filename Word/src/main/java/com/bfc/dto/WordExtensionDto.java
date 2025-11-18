package com.bfc.dto;

import lombok.Data;

@Data
public class WordExtensionDto {
    private Integer id;
    private String type;
    private String textKor;
    private String textCn;
    private Integer sortOrder;
}
