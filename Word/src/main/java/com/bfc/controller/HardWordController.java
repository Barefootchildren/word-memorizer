package com.bfc.controller;

import com.bfc.entity.User;
import com.bfc.entity.Word;
import com.bfc.service.HardWordServiceImpl;
import com.bfc.service.UserServiceImpl;
import com.bfc.service.WordServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/hard-words")
public class HardWordController {

    @Autowired
    private HardWordServiceImpl hardWordService;

    @Autowired
    private UserServiceImpl userService;

    @Autowired
    private WordServiceImpl wordService;

    // 用于前端的简化 DTO，只返回单词本身信息
    public static class WordDTO {
        public Integer id;
        public String word;
        public String meaning;

        public WordDTO(Integer id, String word, String meaning) {
            this.id = id;
            this.word = word;
            this.meaning = meaning;
        }
    }

    /**
     * 查询某用户所有顽固单词（可选按语言过滤）
     * /api/hard-words/{username}?lang=EN 或 KO
     */
    @GetMapping("/{username}")
    public List<WordDTO> getUserHardWords(@PathVariable String username,
                                          @RequestParam(required = false) String lang) {
        User user = userService.findByUsername(username);
        if (user == null) {
            return List.of();
        }

        return hardWordService.findByUser(user)
                .stream()
                .filter(hw -> {
                    if (lang == null || lang.isEmpty()) {
                        return true;
                    }
                    Word w = hw.getWord();
                    return w != null && lang.equalsIgnoreCase(w.getLang());
                })
                .map(hw -> new WordDTO(
                        hw.getWord().getId(),
                        hw.getWord().getWord(),
                        hw.getWord().getMeaning()
                ))
                .toList();
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
}
