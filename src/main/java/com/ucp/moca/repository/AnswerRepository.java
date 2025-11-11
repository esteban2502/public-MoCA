package com.ucp.moca.repository;

import com.ucp.moca.entity.Answer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AnswerRepository extends JpaRepository<Answer, Long> {
    List<Answer> findByQuestionId(Long questionId);
    List<Answer> findByQuestionTestId(Long testId);
    List<Answer> findByResultId(Long resultId);
}
