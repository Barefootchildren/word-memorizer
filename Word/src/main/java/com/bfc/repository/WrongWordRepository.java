package com.bfc.repository;

import com.bfc.entity.User;
import com.bfc.entity.Word;
import com.bfc.entity.WrongWord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WrongWordRepository extends JpaRepository<WrongWord, Integer> {

    List<WrongWord> findByUser(User user);

    Optional<WrongWord> findByUserAndWord(User user, Word word);

    boolean existsByUserAndWord(User user, Word word);

    void deleteByUserAndWord(User user, Word word);
}
