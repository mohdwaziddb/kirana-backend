package com.kiranastore.kirana.controller;

import com.kiranastore.kirana.entity.MasterItem;
import com.kiranastore.kirana.service.MasterItemService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/master")
@CrossOrigin
public class MasterItemController {

    private final MasterItemService service;

    public MasterItemController(MasterItemService service) {
        this.service = service;
    }

    // ➕ Add item
    @PostMapping("/add")
    public MasterItem add(@RequestBody MasterItem item) {
        return service.save(item);
    }

    // 📋 Get all items
    @GetMapping("/all")
    public List<MasterItem> getAll() {
        return service.getAll();
    }

    // 🔍 Search
    @GetMapping("/search")
    public List<MasterItem> search(@RequestParam String q) {
        return service.search(q);
    }
}