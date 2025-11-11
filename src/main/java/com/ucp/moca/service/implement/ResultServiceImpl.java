package com.ucp.moca.service.implement;

import com.ucp.moca.dto.ResultRequest;
import com.ucp.moca.dto.AnswerRequest;
import com.ucp.moca.entity.Result;
import com.ucp.moca.entity.Answer;
import com.ucp.moca.entity.Test;
import com.ucp.moca.entity.Question;
import com.ucp.moca.entity.UserEntity;
import com.ucp.moca.entity.Patient;
import com.ucp.moca.repository.ResultRepository;
import com.ucp.moca.repository.TestRepository;
import com.ucp.moca.repository.QuestionRepository;
import com.ucp.moca.repository.UserEntityRepository;
import com.ucp.moca.repository.PatientRepository;
import com.ucp.moca.service.ResultService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ResultServiceImpl implements ResultService {

    @Autowired
    private ResultRepository resultRepository;
    
    @Autowired
    private TestRepository testRepository;
    
    @Autowired
    private QuestionRepository questionRepository;
    
    @Autowired
    private UserEntityRepository userEntityRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Override
    public Result createResult(Test test, List<Answer> answers) {
        Result result = new Result();
        result.setTest(test);
        result.setAnswers(answers);
        result.setEvaluationDate(java.time.LocalDateTime.now());
        
        // Establecer la relación bidireccional
        for (Answer answer : answers) {
            answer.setResult(result);
        }
        
        result.calculateTotalScore();
        return resultRepository.save(result);
    }

    @Override
    public Result createFromRequest(ResultRequest resultRequest) {
        System.out.println("=== DEBUG: createFromRequest ===");
        System.out.println("ResultRequest recibido: " + resultRequest);
        System.out.println("TestId: " + resultRequest.getTestId());
        System.out.println("Answers: " + resultRequest.getAnswers());
        
        Result result = new Result();
        result.setEvaluationDate(java.time.LocalDateTime.now());
        
        // Buscar el Test por ID
        if (resultRequest.getTestId() != null) {
            System.out.println("Buscando Test con ID: " + resultRequest.getTestId());
            Optional<Test> testOpt = testRepository.findById(resultRequest.getTestId());
            if (testOpt.isPresent()) {
                result.setTest(testOpt.get());
                System.out.println("Test encontrado: " + testOpt.get());
            } else {
                throw new RuntimeException("Test no encontrado con ID: " + resultRequest.getTestId());
            }
        } else {
            throw new RuntimeException("Test ID no puede ser null");
        }
        
        // Asignar el usuario autenticado como owner del resultado
        UserEntity currentUser = null;
        try {
            currentUser = getCurrentUser();
            result.setUser(currentUser);
            System.out.println("Usuario asignado al resultado: " + currentUser.getFullName());
        } catch (RuntimeException e) {
            System.err.println("No se pudo asignar usuario al resultado: " + e.getMessage());
        }
        
        // Asociar el paciente si llega patientId
        if (resultRequest.getPatientId() != null) {
            Optional<Patient> patientOpt = patientRepository.findById(resultRequest.getPatientId());
            if (patientOpt.isPresent()) {
                Patient patient = patientOpt.get();
                result.setPatient(patient);
                
                // Agregar el psicólogo actual a la lista de psicólogos del paciente
                if (currentUser != null) {
                    final Integer currentUserId = currentUser.getId();
                    final String currentUserName = currentUser.getFullName();
                    
                    // Verificar si el psicólogo ya está en la lista
                    boolean wasAlreadyPsychologist = patient.getPsychologists().stream()
                        .anyMatch(psychologist -> psychologist.getId().equals(currentUserId));
                    
                    if (!wasAlreadyPsychologist) {
                        // Buscar el UserEntity desde la base de datos para evitar problemas de contexto
                        Optional<UserEntity> userFromDb = userEntityRepository.findById(currentUserId);
                        if (userFromDb.isPresent()) {
                            patient.getPsychologists().add(userFromDb.get());
                            Patient savedPatient = patientRepository.save(patient);
                            System.out.println("✅ Psicólogo agregado al paciente: " + savedPatient.getFullName() + 
                                             " - Psicólogo: " + currentUserName + 
                                             " (Total psicólogos: " + savedPatient.getPsychologists().size() + ")");
                        } else {
                            System.err.println("❌ No se pudo encontrar el usuario en la base de datos: " + currentUserId);
                        }
                    } else {
                        System.out.println("ℹ️ Psicólogo ya estaba asignado al paciente: " + patient.getFullName());
                    }
                }
            } else {
                throw new RuntimeException("Paciente no encontrado con ID: " + resultRequest.getPatientId());
            }
        }
        
        // Procesar las respuestas
        if (resultRequest.getAnswers() != null && !resultRequest.getAnswers().isEmpty()) {
            System.out.println("Procesando " + resultRequest.getAnswers().size() + " respuestas");
            List<Answer> processedAnswers = new ArrayList<>();
            
            for (int i = 0; i < resultRequest.getAnswers().size(); i++) {
                AnswerRequest answerRequest = resultRequest.getAnswers().get(i);
                System.out.println("Procesando AnswerRequest " + i + ": " + answerRequest);
                System.out.println("AnswerRequest.questionId: " + answerRequest.getQuestionId());
                
                // Crear una nueva Answer con los datos correctos
                Answer newAnswer = new Answer();
                newAnswer.setUserAnswer(answerRequest.getUserAnswer());
                newAnswer.setScore(answerRequest.getScore());
                newAnswer.setNotes(answerRequest.getNotes());
                newAnswer.setResult(result);
                
                // Buscar la Question por ID
                if (answerRequest.getQuestionId() != null) {
                    System.out.println("Buscando Question con ID: " + answerRequest.getQuestionId());
                    Optional<Question> questionOpt = questionRepository.findById(answerRequest.getQuestionId());
                    if (questionOpt.isPresent()) {
                        newAnswer.setQuestion(questionOpt.get());
                        System.out.println("Question encontrada: " + questionOpt.get());
                    } else {
                        throw new RuntimeException("Question no encontrada con ID: " + answerRequest.getQuestionId());
                    }
                } else {
                    System.out.println("ERROR: Question ID es null");
                    throw new RuntimeException("Question ID no puede ser null en AnswerRequest");
                }
                
                processedAnswers.add(newAnswer);
            }
            result.setAnswers(processedAnswers);
        }
        
        result.calculateTotalScore();
        return resultRepository.save(result);
    }

    @Override
    public Result getById(Long id) {
        Optional<Result> result = resultRepository.findById(id);
        if (result.isPresent()) {
            return result.get();
        }
        throw new RuntimeException("Resultado no encontrado con ID: " + id);
    }

    @Override
    public List<Result> getAllByTestId(Long testId) {
        return resultRepository.findByTestIdOrderByEvaluationDateDesc(testId);
    }

    @Override
    public List<Result> getAll() {
        return resultRepository.findAll();
    }

    @Override
    public List<Result> getByUserId(Integer userId) {
        return resultRepository.findByUserIdOrderByEvaluationDateDesc(userId);
    }

    @Override
    public void update(Long id, Result result) {
        if (resultRepository.existsById(id)) {
            result.setId(id);
            result.calculateTotalScore();
            resultRepository.save(result);
        } else {
            throw new RuntimeException("Resultado no encontrado con ID: " + id);
        }
    }

    @Override
    public void delete(Long id) {
        if (resultRepository.existsById(id)) {
            resultRepository.deleteById(id);
        } else {
            throw new RuntimeException("Resultado no encontrado con ID: " + id);
        }
    }

    @Override
    public void save(Result result) {
        result.calculateTotalScore();
        resultRepository.save(result);
    }

    private UserEntity getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserEntity) {
            return (UserEntity) authentication.getPrincipal();
        }
        throw new RuntimeException("Usuario no autenticado");
    }
}
