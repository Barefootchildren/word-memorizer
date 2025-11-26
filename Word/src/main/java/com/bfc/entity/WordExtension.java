package com.bfc.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "word_extension")
/**
 * type 取值：SIMILAR(近义词)、RELATED(关联词)、IDIOM(惯用语)、ANTONYM(反义词)、SENTENCE(例句)
 */
public class WordExtension {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "word_id")
    private Word word;

    @Column(nullable = false, length = 20)
    private String type;

    @Column(name = "text_kor", nullable = false)
    private String textKor;

    @Column(name = "text_cn", nullable = false)
    private String textCn;

    @Column(name = "sort_order")
    private Integer sortOrder = 0;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
