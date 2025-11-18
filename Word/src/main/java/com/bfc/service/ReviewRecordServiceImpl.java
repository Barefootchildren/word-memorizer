package com.bfc.service;

import com.bfc.entity.ReviewRecord;
import com.bfc.entity.User;
import com.bfc.entity.Word;
import com.bfc.repository.ReviewRecordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ReviewRecordServiceImpl {

    @Autowired
    private ReviewRecordRepository reviewRecordRepository;

    /**
     * 查询指定用户的所有复习记录
     */
    public List<ReviewRecord> findByUser(User user) {
        return reviewRecordRepository.findByUser(user);
    }

    /**
     * 查询指定用户某个单词的复习记录
     */
    public Optional<ReviewRecord> findByUserAndWord(User user, Word word) {
        return reviewRecordRepository.findByUserAndWord(user, word);
    }

    /**
     * 查询指定用户某状态（如"hard"）的复习记录
     */
    public List<ReviewRecord> findByUserAndStatus(User user, String status) {
        return reviewRecordRepository.findByUserAndStatus(user, status);
    }

    /**
     * 新增或更新复习记录
     */
    public ReviewRecord saveOrUpdate(ReviewRecord record) {
        // 设置最后复习时间
        record.setLastReview(LocalDateTime.now());
        return reviewRecordRepository.save(record);
    }

    /**
     * 删除复习记录
     */
    public void deleteById(Integer id) {
        reviewRecordRepository.deleteById(id);
    }
}
