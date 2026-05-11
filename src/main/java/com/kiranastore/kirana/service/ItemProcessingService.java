package com.kiranastore.kirana.service;

import com.kiranastore.kirana.dto.ItemRequest;
import com.kiranastore.kirana.dto.ItemResponse;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ItemProcessingService {

    private final MatchingService matchingService;

    public ItemProcessingService(MatchingService matchingService) {
        this.matchingService = matchingService;
    }

    // ================= MAIN ENTRY =================
    public List<ItemResponse> processText(String inputText) {

        List<ItemRequest> items = manualParse(inputText);
        return matchingService.matchItems(items);
    }

    // ================= OCR ENTRY =================
    public List<ItemRequest> processOCRText(String ocrText) {

        ocrText = cleanOCRText(ocrText);

        List<String> lines = extractLines(ocrText);

        List<ItemRequest> items = new ArrayList<>();

        for (String line : lines) {

            line = line.replaceAll("[()]", ""); // remove brackets

            items.addAll(manualParse(line));
        }

        return items;
    }

    // ================= CLEAN OCR =================
    private String cleanOCRText(String text) {

        return text
                .replaceAll("[^a-zA-Z0-9\\u0900-\\u097F\\s()]", " ")
                .replaceAll("\\d+\\)", "") // remove 1)
                .replaceAll("->", " ")
                .replaceAll("\\s+", " ")
                .toLowerCase();
    }

    // ================= EXTRACT LINES =================
    private List<String> extractLines(String text) {

        String[] rawLines = text.split("\\n");
        List<String> lines = new ArrayList<>();

        for (String line : rawLines) {

            line = line.trim();

            if (line.length() < 2) continue;

            // remove starting number
            line = line.replaceAll("^\\d+\\s*", "");

            lines.add(line);
        }

        return lines;
    }

    // ================= SMART PARSER =================
    private static final List<String> UNITS =
            List.of("kg", "g", "gm", "ltr", "l", "ml", "pcs");

    private List<ItemRequest> manualParse(String inputText) {

        inputText = inputText.toLowerCase().trim();

        inputText = inputText
                .replace(",", " ")
                .replace(";", " ")
                .replace("-", " ")
                .replaceAll("\\s+", " ");

        String[] words = inputText.split(" ");

        List<ItemRequest> items = new ArrayList<>();

        StringBuilder nameBuilder = new StringBuilder();
        String pendingQuantity = null;

        for (int i = 0; i < words.length; i++) {

            String word = words[i];

            // 🔥 detect number
            if (word.matches("\\d+(\\.\\d+)?[a-zA-Z]*")) {

                String quantity = word;

                // ✅ attach only valid units
                if (i + 1 < words.length && UNITS.contains(words[i + 1])) {
                    quantity += words[i + 1];
                    i++;
                }

                if (nameBuilder.length() == 0) {
                    pendingQuantity = quantity;
                } else {

                    ItemRequest item = new ItemRequest();
                    item.setName(nameBuilder.toString().trim());
                    item.setQuantity(quantity);

                    items.add(item);

                    nameBuilder.setLength(0);
                }

            } else {

                if (pendingQuantity != null) {

                    nameBuilder.append(word).append(" ");

                    if (i == words.length - 1 ||
                            words[i + 1].matches("\\d+(\\.\\d+)?[a-zA-Z]*")) {

                        ItemRequest item = new ItemRequest();
                        item.setName(nameBuilder.toString().trim());
                        item.setQuantity(pendingQuantity);

                        items.add(item);

                        nameBuilder.setLength(0);
                        pendingQuantity = null;
                    }

                } else {
                    nameBuilder.append(word).append(" ");
                }
            }
        }

        // fallback
        if (nameBuilder.length() > 0) {

            ItemRequest item = new ItemRequest();
            item.setName(nameBuilder.toString().trim());
            item.setQuantity("1");

            items.add(item);
        }

        return items;
    }
}