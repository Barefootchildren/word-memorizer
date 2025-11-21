package com.bfc.repository;

import com.bfc.entity.WordBook;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WordBookRepository extends JpaRepository<WordBook, Integer> {
    List<WordBook> findByLangOrderBySortOrderAscIdAsc(String lang);
}
