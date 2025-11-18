package com.bfc.repository;

import com.bfc.entity.WordExtension;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WordExtensionRepository extends JpaRepository<WordExtension, Integer> {
    List<WordExtension> findByWordIdIn(List<Integer> wordIds);
}
