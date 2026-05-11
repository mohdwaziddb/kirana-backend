package com.kiranastore.kirana.controller;

import com.kiranastore.kirana.service.AIService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
@CrossOrigin
public class AIController {

    private final AIService aiService;

    public AIController(AIService aiService) {
        this.aiService = aiService;
    }

    @PostMapping("/parse")
    public String parseText(@RequestBody String text) {
        return aiService.parseTextToJson(text);
    }
}