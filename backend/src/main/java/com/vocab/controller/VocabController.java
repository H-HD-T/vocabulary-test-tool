package com.vocab.controller;

import com.vocab.model.FullResult;
import com.vocab.model.QuizWord;
import com.vocab.model.WordEntry;
import com.vocab.service.EstimationService;
import com.vocab.service.QuizService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class VocabController {
    private final QuizService quizService;
    private final EstimationService estimationService;

    public VocabController(QuizService quizService, EstimationService estimationService) {
        this.quizService = quizService;
        this.estimationService = estimationService;
    }

    @PostMapping("/quiz/words")
    public List<List<QuizWord>> getQuizWords() {
        return quizService.sampleAllBatches();
    }

    @PostMapping("/estimate")
    public FullResult estimate(@RequestBody List<WordEntry> entries) {
        return estimationService.estimateAll(entries);
    }
}
