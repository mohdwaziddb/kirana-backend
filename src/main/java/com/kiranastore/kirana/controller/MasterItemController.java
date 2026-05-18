package com.kiranastore.kirana.controller;

import com.kiranastore.kirana.entity.MasterItem;
import com.kiranastore.kirana.service.MasterItemService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

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
        if (item.getUserId() == null) {
            throw new RuntimeException("Missing required field: userId");
        }
        return service.save(item);
    }

    @GetMapping("/seller/{userId}")
    public List<MasterItem> getSellerItems(
            @PathVariable Long userId,
            @RequestParam(required = false) Boolean live,
            @RequestParam(required = false, name = "q") String query
    ) {
        return service.getSellerItems(userId, live, query);
    }

    @PutMapping("/{itemId}")
    public MasterItem updateSellerItem(@PathVariable Long itemId, @RequestBody MasterItem item) {
        if (item.getUserId() == null) {
            throw new RuntimeException("Missing required field: userId");
        }
        return service.updateSellerItem(itemId, item.getUserId(), item);
    }

    @DeleteMapping("/{itemId}")
    public Map<String, Object> deleteSellerItem(
            @PathVariable Long itemId,
            @RequestParam Long userId
    ) {
        service.deleteSellerItem(itemId, userId);
        return Map.of("message", "Product deleted successfully", "id", itemId);
    }

    @PostMapping("/{itemId}/delete")
    public Map<String, Object> deleteSellerItemPost(
            @PathVariable Long itemId,
            @RequestParam Long userId
    ) {
        service.deleteSellerItem(itemId, userId);
        return Map.of("message", "Product deleted successfully", "id", itemId);
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
