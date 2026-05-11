package com.kiranastore.kirana.service;

import com.kiranastore.kirana.dto.ItemRequest;
import com.kiranastore.kirana.dto.ItemResponse;
import com.kiranastore.kirana.entity.MasterItem;
import com.kiranastore.kirana.repository.MasterItemRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class MatchingService {

    private final MasterItemRepository repo;

    public MatchingService(MasterItemRepository repo) {
        this.repo = repo;
    }

    public List<ItemResponse> matchItems(List<ItemRequest> items) {

        List<MasterItem> masterList = repo.findAll();
        List<ItemResponse> result = new ArrayList<>();

        for (ItemRequest input : items) {

            String inputName = normalize(input.getName());
            MasterItem matchedItem = null;

            // 🔥 BEST MATCH (exact / contains both side)
            for (MasterItem master : masterList) {

                String eng = normalize(master.getNameEnglish());
                String hin = normalize(master.getNameHindi());

                if (
                        inputName.equals(eng) ||
                                inputName.equals(hin) ||
                                eng.contains(inputName) ||
                                hin.contains(inputName) ||
                                inputName.contains(eng) ||
                                inputName.contains(hin)
                ) {
                    matchedItem = master;
                    break;
                }
            }

            ItemResponse res = new ItemResponse();
            res.setName(input.getName());
            res.setQuantity(input.getQuantity());

            if (matchedItem != null) {

                res.setPrice(matchedItem.getPricePerUnit());

                double qty = extractNumber(input.getQuantity());
                res.setTotal(qty * matchedItem.getPricePerUnit());

                res.setMatched(true);

                // 🔥 Hindi name bhi bhejo
                res.setHindiName(matchedItem.getNameHindi());

            } else {
                // 🔥 IMPORTANT: show unknown item
                res.setPrice(null);
                res.setTotal(null);
                res.setMatched(false);
                res.setHindiName(null);
            }

            result.add(res); // 🔥 ALWAYS ADD
        }

        return result;
    }

    private String normalize(String text) {
        if (text == null) return "";
        return text.toLowerCase().replaceAll("\\s+", "");
    }

    private double extractNumber(String text) {
        if (text == null) return 0;
        return Double.parseDouble(text.replaceAll("[^0-9.]", ""));
    }
}