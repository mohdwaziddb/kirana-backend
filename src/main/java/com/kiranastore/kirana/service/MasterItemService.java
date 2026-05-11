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
}