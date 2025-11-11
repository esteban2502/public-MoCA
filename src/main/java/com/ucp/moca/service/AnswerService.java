package com.ucp.moca.service;

import com.ucp.moca.entity.Answer;

import java.util.List;

public interface AnswerService {
    List<Answer> getAll();
    List<Answer> getAllByQuestionId(Long questionId);
    List<Answer> getAllByTestId(Long testId);
    List<Answer> getAllByResultId(Long resultId);
    Answer getById(Long id);
    void save(Answer answer);
    void saveAll(List<Answer> answers);
    void update(Long id, Answer answerUpdated);
    void delete(Long id);
}
