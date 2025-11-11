package com.ucp.moca.repository;

import com.ucp.moca.entity.Result;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ResultRepository extends JpaRepository<Result, Long> {
    
    // Buscar resultados por test
    List<Result> findByTestId(Long testId);
    
    // Buscar resultados por test ordenados por fecha
    List<Result> findByTestIdOrderByEvaluationDateDesc(Long testId);
    
    // Buscar resultados por usuario ordenados por fecha
    List<Result> findByUserIdOrderByEvaluationDateDesc(Integer userId);
}
