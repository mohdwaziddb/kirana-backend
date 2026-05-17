package com.kiranastore.kirana.controller;

import com.kiranastore.kirana.entity.MasterItem;
import com.kiranastore.kirana.service.MasterItemService;
import org.springframework.http.ResponseEntity;
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

    @PostMapping("/add")
    public ResponseEntity<?> add(@RequestBody MasterItem item) {
        if (item.getUserId() == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Seller userId is required"));
        }

        if (item.getNameEnglish() == null || item.getNameEnglish().trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Product English name is required"));
        }

        if (item.getNameHindi() == null || item.getNameHindi().trim().isEmpty()) {
            item.setNameHindi(item.getNameEnglish());
        }

        return ResponseEntity.ok(service.save(item));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody MasterItem item) {
        try {
            if (item.getNameEnglish() == null || item.getNameEnglish().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("message", "Product English name is required"));
            }

            if (item.getNameHindi() == null || item.getNameHindi().trim().isEmpty()) {
                item.setNameHindi(item.getNameEnglish());
            }

            return ResponseEntity.ok(service.update(id, item));
        } catch (Exception error) {
            return ResponseEntity.badRequest().body(Map.of("message", error.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(
            @PathVariable Long id,
            @RequestParam Long userId
    ) {
        return deleteProduct(id, userId);
    }

    @PostMapping("/{id}/delete")
    public ResponseEntity<?> deleteViaPost(
            @PathVariable Long id,
            @RequestParam Long userId
    ) {
        return deleteProduct(id, userId);
    }

    @PostMapping("/delete/{id}")
    public ResponseEntity<?> deleteViaPostAlias(
            @PathVariable Long id,
            @RequestParam Long userId
    ) {
        return deleteProduct(id, userId);
    }

    private ResponseEntity<?> deleteProduct(Long id, Long userId) {
        try {
            service.delete(id, userId);
            return ResponseEntity.ok(Map.of("message", "Product deleted successfully"));
        } catch (Exception error) {
            return ResponseEntity.badRequest().body(Map.of("message", error.getMessage()));
        }
    }

    @GetMapping("/all")
    public List<MasterItem> getAll(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Boolean live,
            @RequestParam(required = false) String q
    ) {
        if (userId != null) {
            return service.getSellerItems(userId, live, q);
        }

        return service.getAll();
    }

    @GetMapping("/seller/{userId}")
    public List<MasterItem> getSellerItems(
            @PathVariable Long userId,
            @RequestParam(required = false) Boolean live,
            @RequestParam(required = false) String q
    ) {
        return service.getSellerItems(userId, live, q);
    }

    @GetMapping("/search")
    public List<MasterItem> search(@RequestParam String q) {
        return service.search(q);
    }
}
