package com.bfc.repository;

import com.bfc.entity.ReviewRecord;
import com.bfc.entity.User;
import com.bfc.entity.Word;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReviewRecordRepository extends JpaRepository<ReviewRecord, Integer> {

    /**
     * 查询指定用户的所有复习记录
     */
    List<ReviewRecord> findByUser(User user);

    /**
     * 查询指定用户某个单词的复习记录
     */
    Optional<ReviewRecord> findByUserAndWord(User user, Word word);

    /**
     * 查询指定用户、某种状态（如"hard"）的复习记录
     */
    List<ReviewRecord> findByUserAndStatus(User user, String status);

    // 你可以按需继续扩展其它方法
}
