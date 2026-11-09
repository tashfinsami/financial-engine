package com.engine.financialengine.controller;

import java.math.BigDecimal;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.engine.financialengine.service.RetryService;

@RestController
@RequestMapping("/api/retry")
public class RetryController {

    private final RetryService retryService;

    public RetryController(RetryService retryService) {

        this.retryService = retryService;
    }
    
    @PostMapping("/{id}")
    public String retry(
            @PathVariable Long id,
            @RequestBody BigDecimal amount) {

        retryService.updateWithRetry(id, amount);

        return "Balance updated successfully";
    }
}