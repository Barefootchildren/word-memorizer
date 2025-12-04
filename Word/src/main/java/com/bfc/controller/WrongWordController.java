package com.bfc.controller;

import com.bfc.dto.WordDto;
import com.bfc.entity.User;
import com.bfc.entity.Word;
import com.bfc.entity.WrongWord;
import com.bfc.service.UserServiceImpl;
import com.bfc.service.WordServiceImpl;
import com.bfc.service.WrongWordServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/wrongWords")
public class WrongWordController {

    @Autowired
    private WrongWordServiceImpl wrongWordService;

    @Autowired
    private UserServiceImpl userService;

    @Autowired
    private WordServiceImpl wordService;

    /**
     * 查询某用户某一天某语言的错词本
     * GET /api/wrongWords?username=xx&day=1&lang=EN
     */
    @GetMapping
    public List<WordDto> getWrongWords(@RequestParam String username,
                                       @RequestParam Integer day,
                                       @RequestParam String lang) {
        User user = userService.findByUsername(username);
        if (user == null || day == null || lang == null || lang.isBlank()) {
            return List.of();
        }

        List<WrongWord> wrongWords =
                wrongWordService.findByUserAndDayAndLang(user, day, lang);

        List<Word> words = wrongWords.stream()
                .map(WrongWord::getWord)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        return wordService.buildWordDtos(words);
    }

    /**
     * 添加错词（按天 + 语言）
     * POST /api/wrongWords/add?username=xx&day=1&lang=EN
     * body = [wordId1, wordId2, ...]
     */
    @PostMapping("/add")
    public String addWrongWords(@RequestParam String username,
                                @RequestParam Integer day,
                                @RequestParam String lang,
                                @RequestBody List<Integer> wordIds) {
        User user = userService.findByUsername(username);
        if (user == null || day == null || lang == null || lang.isBlank()
                || wordIds == null || wordIds.isEmpty()) {
            return "用户或参数不存在";
        }

        List<Word> words = wordIds.stream()
                .map(wordService::findById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());

        wrongWordService.addWrongWords(user, day, lang, words);
        return "添加成功";
    }

    /**
     * 删除某一天某语言的某个错词
     * DELETE /api/wrongWords/remove/{wordId}?username=xx&day=1&lang=EN
     */
    @DeleteMapping("/remove/{wordId}")
    public String removeWrongWord(@PathVariable Integer wordId,
                                  @RequestParam String username,
                                  @RequestParam Integer day,
                                  @RequestParam String lang) {
        User user = userService.findByUsername(username);
        if (user == null || wordId == null || day == null || lang == null || lang.isBlank()) {
            return "用户或参数不存在";
        }

        Optional<Word> wordOpt = wordService.findById(wordId);
        if (wordOpt.isEmpty()) {
            return "单词不存在";
        }

        wrongWordService.removeWrongWord(user, wordOpt.get(), day, lang);
        return "移除成功";
    }
}
