package com.bfc.service;

import com.bfc.entity.HardWord;
import com.bfc.entity.User;
import com.bfc.entity.Word;
import com.bfc.repository.HardWordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.List;
import java.util.Optional;

@Service
public class HardWordServiceImpl {

    @Autowired
    private HardWordRepository hardWordRepository;

    /**
     * 查询某用户所有的顽固单词记录
     * 这里增加预加载 Word，防止懒加载异常
     */
    @Transactional(readOnly = true)  // 加这一行
    public List<HardWord> findByUser(User user) {
        List<HardWord> list = hardWordRepository.findByUser(user);
        // 强制加载 word 字段
        for (HardWord hw : list) {
            if (hw.getWord() != null) {
                hw.getWord().getId();
                hw.getWord().getWord();
                hw.getWord().getMeaning();
            }
        }
        return list;
    }


    /**
     * 查询某用户是否已标记该单词为顽固
     */
    public Optional<HardWord> findByUserAndWord(User user, Word word) {
        return hardWordRepository.findByUserAndWord(user, word);
    }

    /**
     * 标记顽固单词（新增）
     */
    public HardWord addHardWord(User user, Word word) {
        // 防止重复添加
        Optional<HardWord> exist = hardWordRepository.findByUserAndWord(user, word);
        if (exist.isPresent()) {
            return exist.get();
        }
        HardWord hardWord = new HardWord();
        hardWord.setUser(user);
        hardWord.setWord(word);
        hardWord.setIsStar(0);
        hardWord.setCreatedAt(java.time.LocalDateTime.now());
        return hardWordRepository.save(hardWord);
    }

    /**
     * 取消标记某单词为顽固
     */
    @Transactional  // 添加这个注解
    public void removeHardWord(User user, Word word) {
        hardWordRepository.deleteByUserAndWord(user, word);
    }

    /**
     * 按用户 + 语言 + 天数查询顽固单词（返回 Word 实体列表）
     */
    @Transactional(readOnly = true)
    public List<HardWord> findHardWordsByUserLangDay(User user, String lang, Integer day) {
        if (user == null || lang == null || day == null) {
            return List.of();
        }
        List<HardWord> list = hardWordRepository.findHardWords(user.getId(), lang, day);
        for (HardWord hw : list) {
            if (hw.getWord() != null) {
                hw.getWord().getId();
                hw.getWord().getWord();
                hw.getWord().getMeaning();
            }
        }
        return list;
    }

    @Transactional
    public void updateStar(int userId, int wordId, boolean isStar) {
        hardWordRepository.updateStar(userId, wordId, isStar ? 1 : 0);
    }

}
