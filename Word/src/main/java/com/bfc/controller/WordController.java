package com.bfc.controller;

import com.bfc.entity.Word;
import com.bfc.service.WordServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/words")
public class WordController {

    @Autowired
    private WordServiceImpl wordService;

    /**
     * 查询所有单词（可传 lang）
     */
    @GetMapping
    public List<Word> getAllWords(
            @RequestParam(required = false, defaultValue = "EN") String lang
    ) {
        return wordService.findByLang(lang);
    }

    /**
     * 按 day + lang 查询单词列表
     */
    @GetMapping("/day/{day}")
    public List<Word> getWordsByDay(
            @PathVariable Integer day,
            @RequestParam(required = false, defaultValue = "EN") String lang
    ) {
        if (day == null || day < 1) {
            return List.of();
        }
        return wordService.findByDayAndLang(day, lang);
    }

    /**
     * 查询某语言下所有有单词的天数
     */
    @GetMapping("/days")
    public List<Integer> getAllDays(
            @RequestParam(required = false, defaultValue = "EN") String lang
    ) {
        return wordService.findDistinctDayByLang(lang);
    }

    /**
     * 根据 id 查单词
     */
    @GetMapping("/{id}")
    public Word getWordById(@PathVariable Integer id) {
        Optional<Word> word = wordService.findById(id);
        return word.orElse(null);
    }

    /**
     * 新增单词：如果未传 lang，则默认 EN
     */
    @PostMapping
    public Word addWord(@RequestBody Word word) {
        if (word.getLang() == null || word.getLang().isEmpty()) {
            word.setLang("EN");
        }
        return wordService.save(word);
    }

    /**
     * 修改单词（可只修改 meaning / word / day / lang）
     */
    @PutMapping("/{id}")
    public Word updateWord(@PathVariable Integer id, @RequestBody Word updatedWord) {
        Optional<Word> wordOpt = wordService.findById(id);
        if (wordOpt.isPresent()) {
            Word word = wordOpt.get();

            if (updatedWord.getWord() != null) {
                word.setWord(updatedWord.getWord());
            }
            if (updatedWord.getMeaning() != null) {
                word.setMeaning(updatedWord.getMeaning());
            }
            if (updatedWord.getDay() != null) {
                word.setDay(updatedWord.getDay());
            }
            if (updatedWord.getLang() != null) {
                word.setLang(updatedWord.getLang());
            }

            return wordService.save(word);
        }
        return null;
    }

    /**
     * 删除单词
     */
    @DeleteMapping("/{id}")
    public String deleteWord(@PathVariable Integer id) {
        wordService.deleteById(id);
        return "删除成功";
    }
}
