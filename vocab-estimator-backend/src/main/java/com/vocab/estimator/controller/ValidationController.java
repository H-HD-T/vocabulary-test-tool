package com.vocab.estimator.controller;

import com.vocab.estimator.common.Result;
import com.vocab.estimator.dto.ValidationDTO;
import com.vocab.estimator.service.ValidationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/validation")
public class ValidationController {

    @Autowired
    private ValidationService validationService;

    @PostMapping("/import")
    public Result<ValidationDTO> importValidation(@RequestBody String jsonData) {
        try {
            return Result.success(validationService.importAndValidate(jsonData));
        } catch (Exception e) {
            return Result.error("Validation failed: " + e.getMessage());
        }
    }

    @PostMapping("/upload")
    public Result<ValidationDTO> uploadValidation(@RequestParam("file") MultipartFile file) {
        try {
            StringBuilder sb = new StringBuilder();
            BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8));
            String line;
            while ((line = reader.readLine()) != null) sb.append(line).append("\n");
            reader.close();
            return Result.success(validationService.importAndValidate(sb.toString()));
        } catch (Exception e) {
            return Result.error("Failed: " + e.getMessage());
        }
    }
}
