package com.bfc.service;

import com.bfc.dto.WordDto;
import com.bfc.dto.WordExtensionDto;
import com.bfc.entity.Word;
import com.bfc.entity.WordExtension;
import com.bfc.repository.WordExtensionRepository;
import com.bfc.repository.WordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class WordServiceImpl {

    @Autowired
    private WordRepository wordRepository;

    @Autowired
    private WordExtensionRepository wordExtensionRepository;

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
     * 按 day + lang 查询单词列表，并填充拓展词
     */
    public List<WordDto> findDtosByDayAndLang(Integer day, String lang) {
        List<Word> words = wordRepository.findByDayAndLang(day, lang);
        if (words.isEmpty()) {
            return List.of();
        }

        // 1. 取出所有单词 id
        List<Integer> wordIds = words.stream()
                .map(Word::getId)
                .toList();

        // 2. 一次性把这些单词的拓展词查出来
        List<WordExtension> extensions =
                wordExtensionRepository.findByWordIdIn(wordIds);

        // 3. 按 wordId 分组：wordId -> 当前单词的所有扩展词
        Map<Integer, List<WordExtension>> extensionMap = extensions.stream()
                .collect(Collectors.groupingBy(ext -> ext.getWord().getId()));

        // 4. 组装 WordDto + WordExtensionDto
        return words.stream().map(word -> {
            WordDto dto = new WordDto();
            dto.setId(word.getId());
            dto.setWord(word.getWord());
            dto.setMeaning(word.getMeaning());
            dto.setDay(word.getDay());
            dto.setLang(word.getLang());
            dto.setCreatedAt(word.getCreatedAt());

            List<WordExtensionDto> extDtos = extensionMap
                    .getOrDefault(word.getId(), List.of())
                    .stream()
                    .sorted(Comparator.comparing(
                            ext -> ext.getSortOrder() == null ? 0 : ext.getSortOrder()
                    ))
                    .map(ext -> {
                        WordExtensionDto e = new WordExtensionDto();
                        e.setId(ext.getId());
                        e.setType(ext.getType());
                        e.setTextKor(ext.getTextKor());
                        e.setTextCn(ext.getTextCn());
                        e.setSortOrder(ext.getSortOrder() == null ? 0 : ext.getSortOrder());
                        return e;
                    })
                    .toList();

            dto.setExtensions(extDtos);
            return dto;
        }).toList();
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
