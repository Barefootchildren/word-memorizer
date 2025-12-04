package com.bfc.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(
        name = "wrong_word",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"user_id", "word_id", "day", "lang"})
        }
)
public class WrongWord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /** 用户外键 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private User user;

    /** 单词外键 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "word_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Word word;

    /** 记录该错词属于第几天 */
    @Column(nullable = false)
    private Integer day;

    /** 单词语言：EN / KO */
    @Column(nullable = false, length = 10)
    private String lang;

    /** 加入错词本的时间 */
    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    public WrongWord() {}

    public WrongWord(User user, Word word, Integer day, String lang) {
        this.user = user;
        this.word = word;
        this.day = day;
        this.lang = lang;
        this.createdAt = LocalDateTime.now();
    }
}
