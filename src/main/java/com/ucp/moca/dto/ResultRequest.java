package com.ucp.moca.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ResultRequest {
    private Long testId;
    private Integer userId; // legacy, deprecado
    private Long patientId;
    private List<AnswerRequest> answers;
    private Integer totalScore;
    private String evaluationDate;
}
