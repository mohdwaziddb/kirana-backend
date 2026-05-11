package com.kiranastore.kirana.controller;

import com.kiranastore.kirana.dto.ItemResponse;
import com.kiranastore.kirana.service.ItemProcessingService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/process")
@CrossOrigin
public class ItemController {

    private final ItemProcessingService service;

    public ItemController(ItemProcessingService service) {
        this.service = service;
    }

    @PostMapping
    public List<ItemResponse> process(@RequestBody String text) {
        return service.processText(text);
    }
}