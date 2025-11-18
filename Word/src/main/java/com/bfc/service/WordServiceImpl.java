package com.bfc.service;

import com.bfc.entity.Word;
import com.bfc.repository.WordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class WordServiceImpl {

    @Autowired
    private WordRepository wordRepository;

    /**
     * 查询所有单词
     */
    public List<Word> findAll() {
        return wordRepository.findAll();
    }

    /**
     * 按语言查询全部单词
     */
    public List<Word> findByLang(String lang) {
        return wordRepository.findByLang(lang);
    }

    /**
     * 按 day + lang 查询单词
     */
    public List<Word> findByDayAndLang(Integer day, String lang) {
        return wordRepository.findByDayAndLang(day, lang);
    }

    /**
     * 查询某语言有哪些天数
     */
    public List<Integer> findDistinctDayByLang(String lang) {
        return wordRepository.findDistinctDayByLang(lang);
    }

    /**
     * 根据 id 查询单词
     */
    public Optional<Word> findById(Integer id) {
        return wordRepository.findById(id);
    }

    /**
     * 新增或修改单词
     */
    public Word save(Word word) {
        return wordRepository.save(word);
    }

    /**
     * 删除单词
     */
    public void deleteById(Integer id) {
        wordRepository.deleteById(id);
    }

    /**
     * 按单词内容精确查找
     */
    public Word findByWord(String word) {
        return wordRepository.findByWord(word);
    }
}
