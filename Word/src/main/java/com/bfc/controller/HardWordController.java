package com.bfc.controller;

import com.bfc.dto.WordDto;
import com.bfc.entity.User;
import com.bfc.entity.HardWord;
import com.bfc.entity.Word;
import com.bfc.service.HardWordServiceImpl;
import com.bfc.service.UserServiceImpl;
import com.bfc.service.WordServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/hard-words")
public class HardWordController {

    @Autowired
    private HardWordServiceImpl hardWordService;

    @Autowired
    private UserServiceImpl userService;

    @Autowired
    private WordServiceImpl wordService;

    /**
     * 查询某用户所有顽固单词（可选按语言过滤）
     * /api/hard-words/{username}?lang=EN 或 KO
     */
    @GetMapping("/{username}")
    public List<WordDto> getUserHardWords(@PathVariable String username,
                                          @RequestParam(required = false) String lang) {
        User user = userService.findByUsername(username);
        if (user == null) {
            return List.of();
        }

        List<HardWord> hardWords = hardWordService.findByUser(user)
                .stream()
                .filter(hw -> {
                    if (lang == null || lang.isEmpty()) {
                        return true;
                    }
                    Word w = hw.getWord();
                    return w != null && lang.equalsIgnoreCase(w.getLang());
                })
                .collect(Collectors.toList());

        return wordService.buildWordDtosWithStars(hardWords);
    }

    /**
     * 按天 + 语言查询顽固单词
     */
    @GetMapping("/{username}/day/{day}")
    public List<WordDto> getUserHardWordsByDay(@PathVariable String username,
                                               @PathVariable Integer day,
                                               @RequestParam String lang) {
        User user = userService.findByUsername(username);
        if (user == null || day == null) {
            return List.of();
        }

        List<HardWord> words = hardWordService.findHardWordsByUserLangDay(user, lang, day);
        return wordService.buildWordDtosWithStars(words);
    }

    /**
     * 标记顽固单词
     */
    @PostMapping("/add")
    public String addHardWord(@RequestParam String username, @RequestParam Integer wordId) {
        User user = userService.findByUsername(username);
        Optional<Word> wordOpt = wordService.findById(wordId);
        if (user == null || wordOpt.isEmpty()) {
            return "用户或单词不存在";
        }
        hardWordService.addHardWord(user, wordOpt.get());
        return "标记成功";
    }

    /**
     * 取消顽固标记
     */
    @PostMapping("/remove")
    public String removeHardWord(@RequestParam String username, @RequestParam Integer wordId) {
        User user = userService.findByUsername(username);
        Optional<Word> wordOpt = wordService.findById(wordId);
        if (user == null || wordOpt.isEmpty()) {
            return "用户或单词不存在";
        }
        hardWordService.removeHardWord(user, wordOpt.get());
        return "移除成功";
    }

    @PostMapping("/star")
    @Transactional
    public String updateStar(@RequestParam String username, @RequestParam Integer wordId, @RequestParam Integer star) {
        User user = userService.findByUsername(username);
        if (user == null || wordId == null || star == null) {
            return "用户或单词不存在";
        }
        hardWordService.updateStar(user.getId(), wordId, star != 0);
        return "OK";
    }
}
