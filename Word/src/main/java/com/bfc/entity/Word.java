package com.bfc.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
@Table(name = "word")
public class Word {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // 单词内容：可以是英文单词，也可以是韩语单词
    @Column(nullable = false)
    private String word;

    // 释义：你现在存的是中文解释，可以带 <br> 等
    @Column(nullable = false, columnDefinition = "TEXT")
    private String meaning;

    // 第几天
    @Column(nullable = false)
    private Integer day;

    // 语言：EN = 英语单词，KO = 韩语单词
    @Column(nullable = false, length = 10)
    private String lang;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @JsonIgnore
    @OneToMany(mappedBy = "word", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<WordExtension> extensions;
}
