package com.ucp.moca.service.implement;

import com.ucp.moca.entity.Answer;
import com.ucp.moca.repository.AnswerRepository;
import com.ucp.moca.service.AnswerService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AnswerServiceImpl implements AnswerService {

    private final AnswerRepository answerRepository;

    public AnswerServiceImpl(AnswerRepository answerRepository) {
        this.answerRepository = answerRepository;
    }

    @Override
    public List<Answer> getAll() {
        return answerRepository.findAll();
    }

    @Override
    public List<Answer> getAllByQuestionId(Long questionId) {
        return answerRepository.findByQuestionId(questionId);
    }

    @Override
    public List<Answer> getAllByTestId(Long testId) {
        return answerRepository.findByQuestionTestId(testId);
    }

    @Override
    public List<Answer> getAllByResultId(Long resultId) {
        return answerRepository.findByResultId(resultId);
    }

    @Override
    public Answer getById(Long id) {
        return answerRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Answer con id " + id + " no encontrado"));
    }

    @Override
    public void save(Answer answer) {
        answerRepository.save(answer);
    }

    @Override
    public void saveAll(List<Answer> answers) {
        answerRepository.saveAll(answers);
    }

    @Override
    public void update(Long id, Answer answerUpdated) {
        Answer existingAnswer = answerRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Answer con id " + id + " no encontrado"));

        existingAnswer.setQuestion(answerUpdated.getQuestion());
        existingAnswer.setUserAnswer(answerUpdated.getUserAnswer());
        existingAnswer.setTextResponse(answerUpdated.getTextResponse());
        existingAnswer.setScore(answerUpdated.getScore());
        existingAnswer.setNotes(answerUpdated.getNotes());

        answerRepository.save(existingAnswer);
    }

    @Override
    public void delete(Long id) {
        answerRepository.deleteById(id);
    }
}
