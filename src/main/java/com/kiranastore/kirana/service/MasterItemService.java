package com.kiranastore.kirana.service;

import com.kiranastore.kirana.entity.MasterItem;
import com.kiranastore.kirana.repository.MasterItemRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MasterItemService {

    private final MasterItemRepository repo;

    public MasterItemService(MasterItemRepository repo) {
        this.repo = repo;
    }

    // Save item
    public MasterItem save(MasterItem item) {
        if (item.getProductLive() == null) {
            item.setProductLive(true);
        }
        return repo.save(item);
    }

    public MasterItem update(Long id, MasterItem nextItem) {
        if (nextItem.getUserId() == null) {
            throw new RuntimeException("Seller userId is required");
        }

        MasterItem item = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        if (item.getUserId() == null || !item.getUserId().equals(nextItem.getUserId())) {
            throw new RuntimeException("Product does not belong to this seller");
        }

        item.setNameEnglish(nextItem.getNameEnglish());
        item.setNameHindi(nextItem.getNameHindi());
        item.setPricePerUnit(nextItem.getPricePerUnit());
        item.setUnit(nextItem.getUnit());
        item.setProductImage(nextItem.getProductImage());

        if (nextItem.getUserId() != null) {
            item.setUserId(nextItem.getUserId());
        }

        if (nextItem.getProductLive() != null) {
            item.setProductLive(nextItem.getProductLive());
        }

        return repo.save(item);
    }

    public void delete(Long id, Long userId) {
        if (userId == null) {
            throw new RuntimeException("Seller userId is required");
        }

        MasterItem item = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        if (item.getUserId() == null || !item.getUserId().equals(userId)) {
            throw new RuntimeException("Product does not belong to this seller");
        }

        repo.delete(item);
    }

    // Get all items
    public List<MasterItem> getAll() {
        return repo.findAll();
    }

    public List<MasterItem> getSellerItems(Long userId, Boolean productLive, String keyword) {
        String trimmedKeyword = keyword == null ? "" : keyword.trim();

        if (!trimmedKeyword.isEmpty() && productLive != null) {
            return repo.searchSellerItemsByLiveStatus(userId, productLive, trimmedKeyword);
        }

        if (!trimmedKeyword.isEmpty()) {
            return repo.searchSellerItems(userId, trimmedKeyword);
        }

        if (productLive != null) {
            return repo.findByUserIdAndProductLiveOrderByCreatedAtDesc(userId, productLive);
        }

        return repo.findByUserIdOrderByCreatedAtDesc(userId);
    }

    // Search (Hindi + English)
    public List<MasterItem> search(String keyword) {
        List<MasterItem> eng = repo.findByNameEnglishContainingIgnoreCase(keyword);
        List<MasterItem> hin = repo.findByNameHindiContainingIgnoreCase(keyword);

        eng.addAll(hin);
        return eng;
    }
}
