package com.bfc.service;

import com.bfc.entity.User;
import com.bfc.entity.Word;
import com.bfc.entity.WrongWord;
import com.bfc.repository.WrongWordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
public class WrongWordServiceImpl {

    @Autowired
    private WrongWordRepository wrongWordRepository;

    /**
     * 查询某用户某一天某语言的错词列表
     */
    @Transactional(readOnly = true)
    public List<WrongWord> findByUserAndDayAndLang(User user, Integer day, String lang) {
        List<WrongWord> list = wrongWordRepository.findByUserAndDayAndLang(user, day, lang);
        for (WrongWord ww : list) {
            if (ww.getWord() != null) {
                ww.getWord().getId();
                ww.getWord().getWord();
                ww.getWord().getMeaning();
            }
        }
        return list;
    }

    /**
     * 添加错词（按天与语言分类）
     */
    @Transactional
    public void addWrongWords(User user, Integer day, String lang, List<Word> words) {
        if (user == null || words == null || words.isEmpty() || day == null || lang == null) {
            return;
        }

        for (Word word : words) {
            if (word == null) {
                continue;
            }

            // 已存在：当天 + 用户 + 语言 + 单词，则跳过
            if (wrongWordRepository.existsByUserAndWordAndDayAndLang(user, word, day, lang)) {
                continue;
            }

            WrongWord wrongWord = new WrongWord();
            wrongWord.setUser(user);
            wrongWord.setWord(word);
            wrongWord.setDay(day);
            wrongWord.setLang(lang);
            wrongWord.setCreatedAt(LocalDateTime.now());
            wrongWordRepository.save(wrongWord);
        }
    }

    /**
     * 删除某一天某语言的某个错词
     */
    @Transactional
    public void removeWrongWord(User user, Word word, Integer day, String lang) {
        if (user == null || Objects.isNull(word) || day == null || lang == null) {
            return;
        }
        wrongWordRepository.deleteByUserAndWordAndDayAndLang(user, word, day, lang);
    }
}
