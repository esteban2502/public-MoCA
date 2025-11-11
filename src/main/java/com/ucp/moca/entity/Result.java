package com.ucp.moca.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "results")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Result {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "test_id", nullable = false)
    @JsonIgnoreProperties({"questions"})
    private Test test;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = true)
    @JsonIgnoreProperties({"roles", "password", "tests"})
    private UserEntity user;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "patient_id")
    @JsonIgnoreProperties({})
    private Patient patient;

    @OneToMany(mappedBy = "result", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JsonIgnoreProperties({"result"})
    private List<Answer> answers;

    // Puntaje total del test
    @Column(name = "total_score")
    private Integer totalScore;

    // Fecha y hora de la evaluación
    @Column(name = "evaluation_date")
    private LocalDateTime evaluationDate;

    // Constructor para crear un resultado
    public Result(Test test, List<Answer> answers) {
        this.test = test;
        this.answers = answers;
        this.evaluationDate = LocalDateTime.now();
        this.calculateTotalScore();
    }

    // Constructor para crear un resultado con usuario
    public Result(Test test, UserEntity user, List<Answer> answers) {
        this.test = test;
        this.user = user;
        this.answers = answers;
        this.evaluationDate = LocalDateTime.now();
        this.calculateTotalScore();
    }

    // Método para calcular el puntaje total
    public void calculateTotalScore() {
        this.totalScore = answers != null ? 
            answers.stream()
                .mapToInt(answer -> answer.getScore() != null ? answer.getScore() : 0)
                .sum() : 0;
    }
}
