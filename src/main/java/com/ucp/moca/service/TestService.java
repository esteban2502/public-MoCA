package com.ucp.moca.service;

import com.ucp.moca.entity.Test;

import java.util.List;

public interface TestService {

    List<Test> getAll();
    void save(Test test);
    void update(Long id, Test testUpdated);
    void delete(Long id);
    void changeStatus(Long id);

}
