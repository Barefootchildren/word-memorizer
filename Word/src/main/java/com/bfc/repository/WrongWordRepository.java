package com.bfc.repository;

import com.bfc.entity.User;
import com.bfc.entity.Word;
import com.bfc.entity.WrongWord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WrongWordRepository extends JpaRepository<WrongWord, Integer> {

    /** 查询某用户某一天某语言的全部错词 */
    List<WrongWord> findByUserAndDayAndLang(User user, Integer day, String lang);

    /** 查询：是否已存在（避免重复加入） */
    boolean existsByUserAndWordAndDayAndLang(User user, Word word, Integer day, String lang);

    /** 查询单条 */
    Optional<WrongWord> findByUserAndWordAndDayAndLang(User user, Word word, Integer day, String lang);

    /** 删除某一天某语言的某个错词 */
    void deleteByUserAndWordAndDayAndLang(User user, Word word, Integer day, String lang);
}
