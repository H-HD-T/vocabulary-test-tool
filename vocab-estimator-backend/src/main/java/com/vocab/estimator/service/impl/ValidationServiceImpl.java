package com.vocab.estimator.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vocab.estimator.algorithm.AlgorithmFactory;
import com.vocab.estimator.algorithm.AlgorithmValidator;
import com.vocab.estimator.dto.ValidationDTO;
import com.vocab.estimator.service.ValidationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class ValidationServiceImpl implements ValidationService {

    @Autowired private AlgorithmFactory algorithmFactory;
    @Autowired private AlgorithmValidator algorithmValidator;
    private final ObjectMapper om = new ObjectMapper();

    @Override
    public ValidationDTO validateAlgorithm(List<Map<String, Object>> validationData) {
        var validator = algorithmFactory.getAllAlgorithms().get(0);
        var report = algorithmValidator.validate(validationData, validator);
        List<ValidationDTO.ValidationItem> items = new ArrayList<>();
        for (var item : report.getItems()) {
            items.add(new ValidationDTO.ValidationItem(item.getKnownWords(), item.getUnknownWords(),
                item.getStandardEstimate(), item.getAlgorithmEstimate(), item.getDiff()));
        }
        ValidationDTO dto = new ValidationDTO();
        dto.setItems(items);
        dto.setMeanError(report.getMeanError());
        dto.setMeanBias(report.getMeanBias());
        dto.setCorrelation(report.getCorrelation());
        dto.setSampleCount(report.getSampleCount());
        return dto;
    }

    @Override
    public ValidationDTO importAndValidate(String jsonData) {
        try {
            List<Map<String, Object>> data = om.readValue(jsonData, new TypeReference<List<Map<String, Object>>>(){});
            return validateAlgorithm(data);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse: " + e.getMessage());
        }
    }
}
