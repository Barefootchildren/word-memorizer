package com.bfc.repository;

import com.bfc.entity.Word;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface WordRepository extends JpaRepository<Word, Integer> {

    /**
     * 按天数和语言查询单词
     */
    List<Word> findByDayAndLang(Integer day, String lang);

    /**
     * 按语言查询全部单词
     */
    List<Word> findByLang(String lang);

    /**
     * 查询某语言下所有的天数（去重 + 排序）
     */
    @Query("SELECT DISTINCT w.day FROM Word w WHERE w.lang = :lang ORDER BY w.day")
    List<Integer> findDistinctDayByLang(@Param("lang") String lang);

    /**
     * 按单词内容精确查找
     */
    Word findByWord(String word);
}
