package com.ucp.moca.service;

import com.ucp.moca.dto.ResultRequest;
import com.ucp.moca.entity.Result;
import com.ucp.moca.entity.Answer;
import com.ucp.moca.entity.Test;

import java.util.List;

public interface ResultService {
    
    // Crear un nuevo resultado con sus respuestas
    Result createResult(Test test, List<Answer> answers);
    
    // Crear resultado desde request del frontend
    Result createFromRequest(ResultRequest resultRequest);
    
    // Obtener resultado por ID
    Result getById(Long id);
    
    // Obtener todos los resultados de un test
    List<Result> getAllByTestId(Long testId);
    
    // Obtener todos los resultados
    List<Result> getAll();
    
    // Obtener resultados por usuario
    List<Result> getByUserId(Integer userId);
    
    // Actualizar resultado
    void update(Long id, Result result);
    
    // Eliminar resultado
    void delete(Long id);
    
    // Guardar resultado
    void save(Result result);
}
