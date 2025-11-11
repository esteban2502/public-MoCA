package com.ucp.moca.service;



import com.ucp.moca.entity.Question;

import java.util.List;

public interface QuestionService {
    List<Question> getAllByTestId(Long id);
    void save(Question question);
    void update(Long id, Question questionUpdated);
    void delete(Long id);
}
