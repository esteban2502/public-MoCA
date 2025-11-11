package com.ucp.moca.service.implement;

import com.ucp.moca.entity.Question;
import com.ucp.moca.entity.Test;
import com.ucp.moca.exception.DuplicateQuestionOrderException;
import com.ucp.moca.repository.QuestionRepository;
import com.ucp.moca.service.QuestionService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class QuestionServiceImpl implements QuestionService {

    private final QuestionRepository questionRepository;

    public QuestionServiceImpl(QuestionRepository questionRepository){
        this.questionRepository = questionRepository;
    }

    @Override
    public List<Question> getAllByTestId(Long id) {
        return questionRepository.getAllByTestId(id);
    }

    @Override
    public void save(Question question) {
        // Validar que no exista otra pregunta con el mismo orden en el mismo examen
        validateQuestionOrder(question.getTest().getId(), question.getQuestionOrder(), null);
        questionRepository.save(question);
    }

    @Override
    public void update(Long id, Question questionUpdated) {
        Question existingQuestion = questionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Question con id " + id + " no encontrado"));

        // Validar que no exista otra pregunta con el mismo orden en el mismo examen (excluyendo la pregunta actual)
        validateQuestionOrder(existingQuestion.getTest().getId(), questionUpdated.getQuestionOrder(), id);

        existingQuestion.setQuestion(questionUpdated.getQuestion());
        existingQuestion.setDescription(questionUpdated.getDescription());
        existingQuestion.setQuestionOrder(questionUpdated.getQuestionOrder());
        existingQuestion.setMaxScore(questionUpdated.getMaxScore());

        questionRepository.save(existingQuestion);
    }

    @Override
    public void delete(Long id) {
        questionRepository.deleteById(id);
    }

    private void validateQuestionOrder(Long testId, Integer questionOrder, Long excludeQuestionId) {
        List<Question> existingQuestions = questionRepository.findByTestIdAndQuestionOrder(testId, questionOrder);
        
        // Si estamos actualizando, excluir la pregunta actual de la validación
        if (excludeQuestionId != null) {
            existingQuestions = existingQuestions.stream()
                    .filter(q -> !q.getId().equals(excludeQuestionId))
                    .toList();
        }
        
        if (!existingQuestions.isEmpty()) {
            throw new DuplicateQuestionOrderException(
                "Ya existe una pregunta con el orden " + questionOrder + " en este examen"
            );
        }
    }
}
