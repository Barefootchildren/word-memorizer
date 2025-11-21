package com.bfc.service;

import com.bfc.entity.WordBook;
import com.bfc.repository.WordBookRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WordBookServiceImpl {

    @Autowired
    private WordBookRepository wordBookRepository;

    public List<WordBook> findByLang(String lang) {
        return wordBookRepository.findByLangOrderBySortOrderAscIdAsc(lang);
    }
}
