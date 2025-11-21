package com.bfc.controller;

import com.bfc.entity.WordBook;
import com.bfc.service.WordBookServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/books")
public class WordBookController {

    @Autowired
    private WordBookServiceImpl wordBookService;

    @GetMapping
    public List<WordBook> getBooks(@RequestParam String lang) {
        return wordBookService.findByLang(lang);
    }
}
