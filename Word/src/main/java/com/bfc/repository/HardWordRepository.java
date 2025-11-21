package com.bfc.repository;

import com.bfc.entity.HardWord;
import com.bfc.entity.User;
import com.bfc.entity.Word;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface HardWordRepository extends JpaRepository<HardWord, Integer> {

    /**
     * 查询某用户的所有顽固单词记录
     */
    List<HardWord> findByUser(User user);

    /**
     * 查询某用户某单词的顽固记录（用于判重、判断是否标记）
     */
    Optional<HardWord> findByUserAndWord(User user, Word word);

    /**
     * 删除某用户某单词的顽固标记
     */
    void deleteByUserAndWord(User user, Word word);

    /**
     * 按用户 + 语言 + 天数查询顽固单词列表（返回 HardWord 实体）
     */
    @Query(value = "select hw.* from hard_word hw join word w on hw.word_id = w.id where hw.user_id = :userId and w.lang = :lang and w.day = :day", nativeQuery = true)
    List<HardWord> findHardWords(@Param("userId") Integer userId, @Param("lang") String lang, @Param("day") Integer day);

    @Modifying
    @Query(value = "UPDATE hard_word SET is_star = :star WHERE user_id = :userId AND word_id = :wordId", nativeQuery = true)
    void updateStar(@Param("userId") int userId, @Param("wordId") int wordId, @Param("star") int star);
}
