package com.kiranastore.kirana.controller;

import com.kiranastore.kirana.dto.ItemRequest;
import com.kiranastore.kirana.dto.ItemResponse;
import com.kiranastore.kirana.service.MatchingService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/match")
@CrossOrigin
public class MatchingController {

    private final MatchingService service;

    public MatchingController(MatchingService service) {
        this.service = service;
    }

    @PostMapping
    public List<ItemResponse> match(@RequestBody List<ItemRequest> items) {
        return service.matchItems(items);
    }
}