package com.bfc.controller;

import com.bfc.entity.ReviewRecord;
import com.bfc.entity.User;
import com.bfc.entity.Word;
import com.bfc.service.ReviewRecordServiceImpl;
import com.bfc.service.UserServiceImpl;
import com.bfc.service.WordServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/review-record")
public class ReviewRecordController {

    @Autowired
    private ReviewRecordServiceImpl reviewRecordService;

    @Autowired
    private UserServiceImpl userService;

    @Autowired
    private WordServiceImpl wordService;

    /**
     * 查询某用户所有复习记录（可按语言过滤）
     * /api/review-record/user/{username}?lang=EN   或  ?lang=KO
     */
    @GetMapping("/user/{username}")
    public List<ReviewRecord> getUserRecords(@PathVariable String username,
                                             @RequestParam(required = false) String lang) {

        User user = userService.findByUsername(username);
        if (user == null) {
            return List.of();
        }

        return reviewRecordService.findByUser(user)
                .stream()
                .filter(r -> {
                    if (lang == null || lang.isEmpty()) {
                        return true; // 不过滤
                    }
                    Word w = r.getWord();
                    return w != null && lang.equalsIgnoreCase(w.getLang());
                })
                .toList();
    }

    /**
     * 查询某用户某单词的复习记录
     * 这个接口不需要语言过滤，因为查的是具体某个 wordId
     */
    @GetMapping("/user/{username}/word/{wordId}")
    public ReviewRecord getRecord(@PathVariable String username, @PathVariable Integer wordId) {
        User user = userService.findByUsername(username);
        Optional<Word> wordOpt = wordService.findById(wordId);
        if (user == null || wordOpt.isEmpty()) {
            return null;
        }
        return reviewRecordService.findByUserAndWord(user, wordOpt.get()).orElse(null);
    }

    /**
     * 新增或更新复习记录
     */
    @PostMapping("/save")
    public String saveRecord(@RequestParam String username,
                             @RequestParam Integer wordId,
                             @RequestParam String status,
                             @RequestParam Integer reviewTimes) {

        User user = userService.findByUsername(username);
        Optional<Word> wordOpt = wordService.findById(wordId);

        if (user == null || wordOpt.isEmpty()) {
            return "用户或单词不存在";
        }

        ReviewRecord record = reviewRecordService
                .findByUserAndWord(user, wordOpt.get())
                .orElse(new ReviewRecord());

        record.setUser(user);
        record.setWord(wordOpt.get());
        record.setStatus(status);
        record.setReviewTimes(reviewTimes);
        // lastReview 和 createdAt 由 Service 自动设置

        reviewRecordService.saveOrUpdate(record);

        return "保存成功";
    }

    /**
     * 删除复习记录
     */
    @DeleteMapping("/{id}")
    public String deleteRecord(@PathVariable Integer id) {
        reviewRecordService.deleteById(id);
        return "删除成功";
    }
}
