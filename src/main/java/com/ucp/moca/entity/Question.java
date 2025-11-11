package com.ucp.moca.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Entity
@Table(name = "questions", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"test_id", "question_order"})
})
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Texto principal de la pregunta
    @Column(length = 500)
    private String question;

    // Instrucciones adicionales para el psicólogo
    @Column(length = 500)
    private String description;

    // Orden en el que aparece dentro de la prueba
    @Column(name = "question_order")
    private Integer questionOrder;

    // Puntaje máximo de la pregunta
    @Column(name = "max_score")
    private Integer maxScore;

    // Categoría cognitiva (Memoria, Atención, Lenguaje, etc.)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "test_id")
    @JsonIgnoreProperties({"questions"})
    private Test test;

}
