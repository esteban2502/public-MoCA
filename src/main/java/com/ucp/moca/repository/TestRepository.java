package com.ucp.moca.repository;

import com.ucp.moca.entity.Test;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TestRepository extends JpaRepository<Test,Long> {
    
    @Query("SELECT t FROM Test t ORDER BY t.id DESC")
    List<Test> findAllOrderByIdDesc();
}
