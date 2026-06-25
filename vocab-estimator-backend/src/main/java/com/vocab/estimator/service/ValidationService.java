package com.vocab.estimator.service;

import com.vocab.estimator.dto.ValidationDTO;
import java.util.List;
import java.util.Map;

/**
 * Algorithm validation service interface
 */
public interface ValidationService {
    ValidationDTO validateAlgorithm(List<Map<String, Object>> validationData);
    ValidationDTO importAndValidate(String jsonData);
}
