package com.ucp.moca.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AnswerRequest {
    private Long questionId;
    private String userAnswer;
    private Integer score;
    private String notes;
}
