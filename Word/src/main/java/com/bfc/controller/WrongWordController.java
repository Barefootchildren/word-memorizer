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

    @GetMapping
    public List<WordDto> getWrongWords(@RequestParam(required = false) String username) {
        User user = resolveUser(username);
        if (user == null) {
            return List.of();
        }
        List<WrongWord> wrongWords = wrongWordService.findByUser(user);
        List<Word> words = wrongWords.stream()
                .map(WrongWord::getWord)
                .filter(Objects::nonNull)
                .toList();
        return wordService.buildWordDtos(words);
    }

    @PostMapping("/add")
    public String addWrongWords(@RequestParam(required = false) String username,
                                @RequestBody List<Integer> wordIds) {
        User user = resolveUser(username);
        if (user == null || wordIds == null || wordIds.isEmpty()) {
            return "用户或单词不存在";
        }
        List<Word> words = wordIds.stream()
                .map(wordService::findById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
        wrongWordService.addWrongWords(user, words);
        return "OK";
    }

    @DeleteMapping("/remove/{wordId}")
    public String removeWrongWord(@PathVariable Integer wordId,
                                  @RequestParam(required = false) String username) {
        User user = resolveUser(username);
        if (user == null) {
            return "用户不存在";
        }
        Optional<Word> wordOpt = wordService.findById(wordId);
        if (wordOpt.isEmpty()) {
            return "单词不存在";
        }
        wrongWordService.removeWrongWord(user, wordOpt.get());
        return "OK";
    }

    private User resolveUser(String username) {
        if (username != null && !username.isEmpty()) {
            User user = userService.findByUsername(username);
            if (user != null) {
                return user;
            }
        }
        return userService.findById(1).orElse(null);
    }
}
