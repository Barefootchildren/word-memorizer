package com.bfc.service;

import com.bfc.entity.Word;
import com.bfc.entity.WrongWord;
import com.bfc.entity.User;
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

    @Transactional(readOnly = true)
    public List<WrongWord> findByUser(User user) {
        List<WrongWord> list = wrongWordRepository.findByUser(user);
        for (WrongWord ww : list) {
            if (ww.getWord() != null) {
                ww.getWord().getId();
                ww.getWord().getWord();
                ww.getWord().getMeaning();
            }
        }
        return list;
    }

    @Transactional
    public void addWrongWords(User user, List<Word> words) {
        if (user == null || words == null || words.isEmpty()) {
            return;
        }
        for (Word word : words) {
            if (word == null) {
                continue;
            }
            if (wrongWordRepository.existsByUserAndWord(user, word)) {
                continue;
            }
            WrongWord wrongWord = new WrongWord();
            wrongWord.setUser(user);
            wrongWord.setWord(word);
            wrongWord.setCreatedAt(LocalDateTime.now());
            wrongWordRepository.save(wrongWord);
        }
    }

    @Transactional
    public void removeWrongWord(User user, Word word) {
        if (user == null || Objects.isNull(word)) {
            return;
        }
        wrongWordRepository.deleteByUserAndWord(user, word);
    }
}
