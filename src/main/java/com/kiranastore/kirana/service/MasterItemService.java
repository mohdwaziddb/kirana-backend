package com.kiranastore.kirana.service;

import com.kiranastore.kirana.entity.MasterItem;
import com.kiranastore.kirana.repository.MasterItemRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@Service
public class MasterItemService {

    private final MasterItemRepository repo;

    public MasterItemService(MasterItemRepository repo) {
        this.repo = repo;
    }

    // Save item
    public MasterItem save(MasterItem item) {
        if (item.getIsProductLive() == null) {
            item.setIsProductLive(true);
        }
        return repo.save(item);
    }

    // Get all items
    public List<MasterItem> getAll() {
        return repo.findAll();
    }

    // Search (Hindi + English)
    public List<MasterItem> search(String keyword) {
        List<MasterItem> eng = repo.findByNameEnglishContainingIgnoreCase(keyword);
        List<MasterItem> hin = repo.findByNameHindiContainingIgnoreCase(keyword);

        eng.addAll(hin);
        return eng;
    }

    public List<MasterItem> getSellerItems(Long userId, Boolean live, String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return live == null
                    ? repo.findByUserId(userId)
                    : repo.findByUserIdAndIsProductLive(userId, live);
        }

        String q = keyword.trim();
        List<MasterItem> eng = live == null
                ? repo.findByUserIdAndNameEnglishContainingIgnoreCase(userId, q)
                : repo.findByUserIdAndIsProductLiveAndNameEnglishContainingIgnoreCase(userId, live, q);
        List<MasterItem> hin = live == null
                ? repo.findByUserIdAndNameHindiContainingIgnoreCase(userId, q)
                : repo.findByUserIdAndIsProductLiveAndNameHindiContainingIgnoreCase(userId, live, q);

        return Stream.concat(eng.stream(), hin.stream())
                .distinct()
                .toList();
    }

    public Optional<MasterItem> getSellerItem(Long itemId, Long userId) {
        return repo.findById(itemId)
                .filter(item -> item.getUserId() != null && item.getUserId().equals(userId));
    }

    public MasterItem updateSellerItem(Long itemId, Long userId, MasterItem nextItem) {
        MasterItem existing = getSellerItem(itemId, userId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        existing.setNameEnglish(nextItem.getNameEnglish());
        existing.setNameHindi(nextItem.getNameHindi());
        existing.setPricePerUnit(nextItem.getPricePerUnit());
        existing.setUnit(nextItem.getUnit());
        existing.setProductImage(nextItem.getProductImage());
        existing.setIsProductLive(nextItem.getIsProductLive() == null ? true : nextItem.getIsProductLive());
        return repo.save(existing);
    }

    public void deleteSellerItem(Long itemId, Long userId) {
        MasterItem item = getSellerItem(itemId, userId)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        repo.delete(item);
    }
}
