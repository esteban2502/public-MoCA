package com.ucp.moca.controller;

import com.ucp.moca.entity.Question;
import com.ucp.moca.service.QuestionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/question/v1")
public class QuestionController {

    private final QuestionService questionService;

    public QuestionController(QuestionService questionService){
        this.questionService = questionService;
    }

    @GetMapping("/test/{testId}")
    public ResponseEntity<List<Question>> getAllByTestId(@PathVariable Long testId){
        return ResponseEntity.ok(questionService.getAllByTestId(testId));
    }

    @PostMapping
    public ResponseEntity<Void> create(@RequestBody Question question){
        questionService.save(question);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> update(@PathVariable Long id, @RequestBody Question question){
        questionService.update(id, question);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id){
        questionService.delete(id);
        return ResponseEntity.noContent().build();
    }

}
