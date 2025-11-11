package com.ucp.moca.controller;

import com.ucp.moca.entity.Test;
import com.ucp.moca.service.TestService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/test/v1")
public class TestController {

    private final TestService testService;

    public TestController(TestService testService){
        this.testService = testService;
    }

    @GetMapping
    public ResponseEntity<List<Test>> getAll(){
        return ResponseEntity.ok(testService.getAll());
    }

    @PostMapping
    public ResponseEntity<?> save(@RequestBody Test test){
        testService.save(test);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PutMapping("{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody Test testUpdate){
        testService.update(id, testUpdate);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("{id}")
    public ResponseEntity<?> deleteById(@PathVariable Long id){
        testService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Void> changeStatus(@PathVariable Long id) {
        testService.changeStatus(id);
        return ResponseEntity.noContent().build();
    }


}
