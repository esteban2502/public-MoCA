package com.ucp.moca.repository;

import com.ucp.moca.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface QuestionRepository extends JpaRepository<Question, Long> {

    @Query("SELECT q FROM Question q WHERE q.test.id = :id ORDER BY q.questionOrder ASC")
    List<Question> getAllByTestId(@Param("id") Long id);

    @Query("SELECT q FROM Question q WHERE q.test.id = :testId AND q.questionOrder = :questionOrder")
    List<Question> findByTestIdAndQuestionOrder(@Param("testId") Long testId, @Param("questionOrder") Integer questionOrder);

    Long countByTestId(Long testId);

}
