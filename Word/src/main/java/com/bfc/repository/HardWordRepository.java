package com.bfc.repository;

import com.bfc.entity.HardWord;
import com.bfc.entity.User;
import com.bfc.entity.Word;
import org.springframework.data.jpa.repository.JpaRepository;

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
}
