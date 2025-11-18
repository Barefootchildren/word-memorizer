package com.bfc.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "review_record")
public class ReviewRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // 关联用户
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 关联单词
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "word_id", nullable = false)
    private Word word;

    // 学习状态（如 new, familiar, hard, mastered）
    @Column(length = 20)
    private String status;

    // 复习次数
    @Column(name = "review_times")
    private Integer reviewTimes;

    // 最后复习时间
    @Column(name = "last_review")
    private LocalDateTime lastReview;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
