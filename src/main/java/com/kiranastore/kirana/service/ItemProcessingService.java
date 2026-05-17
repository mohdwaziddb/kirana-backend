package com.kiranastore.kirana.service;

import com.kiranastore.kirana.dto.ItemRequest;
import com.kiranastore.kirana.dto.ItemResponse;
import com.kiranastore.kirana.entity.MasterItem;
import com.kiranastore.kirana.repository.MasterItemRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ItemProcessingService {

    private final MatchingService matchingService;
    private final MasterItemRepository masterItemRepository;

    public ItemProcessingService(MatchingService matchingService, MasterItemRepository masterItemRepository) {
        this.matchingService = matchingService;
        this.masterItemRepository = masterItemRepository;
    }

    // ================= MAIN ENTRY =================
    public List<ItemResponse> processText(String inputText) {
        return processText(inputText, null);
    }

    public List<ItemResponse> processText(String inputText, Long userId) {
        List<ItemRequest> items = userId == null
                ? manualParse(inputText)
                : parseWithSellerProducts(inputText, userId);

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

    private List<ItemRequest> parseWithSellerProducts(String inputText, Long userId) {
        List<MasterItem> products = masterItemRepository.findByUserIdAndProductLiveOrderByCreatedAtDesc(userId, true);
        products.sort(Comparator.comparingInt((MasterItem item) -> normalizeForMatch(item.getNameEnglish()).length()).reversed());

        List<ItemRequest> items = new ArrayList<>();
        String[] parts = inputText.split("[,;\\n\\r]+");

        for (String rawPart : parts) {
            String part = rawPart == null ? "" : rawPart.trim();
            if (part.isEmpty()) continue;

            MasterItem matchedProduct = findProductInText(part, products);
            if (matchedProduct == null) {
                items.addAll(manualParse(part));
                continue;
            }

            ItemRequest item = new ItemRequest();
            item.setName(matchedProduct.getNameEnglish());
            item.setQuantity(extractQuantityAfterProduct(part, matchedProduct.getNameEnglish()));
            items.add(item);
        }

        return items;
    }

    private MasterItem findProductInText(String text, List<MasterItem> products) {
        String normalizedText = normalizeForMatch(text);
        MasterItem bestProduct = null;
        int bestScore = -1;

        for (MasterItem product : products) {
            String english = normalizeForMatch(product.getNameEnglish());
            String hindi = normalizeForMatch(product.getNameHindi());

            int score = -1;
            if (!english.isEmpty() && normalizedText.contains(english)) {
                score = scoreProductMatch(text, product, english, normalizedText);
            }

            if (!hindi.isEmpty() && normalizedText.contains(hindi)) {
                score = Math.max(score, scoreProductMatch(text, product, hindi, normalizedText));
            }

            if (score > bestScore) {
                bestScore = score;
                bestProduct = product;
            }
        }

        return bestProduct;
    }

    private int scoreProductMatch(String sourceText, MasterItem product, String normalizedProduct, String normalizedText) {
        int score = normalizedProduct.length();

        if (normalizedText.equals(normalizedProduct)) {
            score += 1000;
        }

        if (product.getNameEnglish() != null && product.getNameEnglish().contains("(")) {
            score += 100;
        }

        String productName = product.getNameEnglish() == null ? "" : product.getNameEnglish().toLowerCase();
        if (productName.contains(" wali") || productName.contains(" wala") || productName.contains(" wale")) {
            score -= 200;
        }

        if (sourceText.toLowerCase().contains("wali") && product.getNameEnglish() != null && product.getNameEnglish().contains("(")) {
            score += 200;
        }

        return score;
    }

    private String extractQuantityAfterProduct(String text, String productName) {
        String remaining = text.substring(findProductMatchEnd(text, productName)).trim();
        String[] words = remaining.split("\\s+");

        for (int i = words.length - 1; i >= 0; i--) {
            String word = words[i].replaceAll("[^0-9.]", "");
            if (word.matches("\\d+(\\.\\d+)?")) {
                String quantity = word;
                if (i + 1 < words.length && UNITS.contains(words[i + 1].toLowerCase())) {
                    quantity += words[i + 1].toLowerCase();
                }
                return quantity;
            }
        }

        return "1";
    }

    private int findProductMatchEnd(String text, String productName) {
        Pattern pattern = buildProductPattern(productName);
        Matcher matcher = pattern.matcher(text);

        if (matcher.find()) {
            return matcher.end();
        }

        return 0;
    }

    private Pattern buildProductPattern(String productName) {
        String[] productWords = productName
                .replaceAll("[()]", " ")
                .replaceAll("[^a-zA-Z0-9\\u0900-\\u097F\\s]", " ")
                .trim()
                .split("\\s+");

        StringBuilder pattern = new StringBuilder();

        for (String word : productWords) {
            if (word.isBlank()) continue;

            if (pattern.length() > 0) {
                pattern.append("[^a-zA-Z0-9\\u0900-\\u097F]*");
            }

            pattern.append(Pattern.quote(word));
        }

        pattern.append("(?:\\s+(?:wali|wala|wale|waali|waala|waale))?");

        return Pattern.compile(pattern.toString(), Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
    }

    private String removeProductWords(String text, String productName) {
        String result = text;
        String[] productWords = productName
                .replaceAll("[()]", " ")
                .replaceAll("[^a-zA-Z0-9\\u0900-\\u097F\\s]", " ")
                .split("\\s+");

        for (String word : productWords) {
            if (!word.isBlank()) {
                result = result.replaceAll("(?i)\\b" + java.util.regex.Pattern.quote(word) + "\\b", " ");
            }
        }

        return result.replaceAll("\\s+", " ");
    }

    private String normalizeForMatch(String text) {
        if (text == null) return "";
        return text.toLowerCase()
                .replaceAll("\\b(wali|wala|wale|waali|waala|waale)\\b", "")
                .replaceAll("(वाली|वाला|वाले)", "")
                .replaceAll("[^a-z0-9\\u0900-\\u097F]", "");
    }

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

                if (i + 1 < words.length && isVariantWord(words[i + 1])) {
                    nameBuilder.append(word).append(" ").append(words[i + 1]).append(" ");
                    i++;
                    continue;
                }

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
            item.setQuantity(pendingQuantity == null ? "1" : pendingQuantity);

            items.add(item);
        }

        return items;
    }

    private boolean isVariantWord(String word) {
        return word != null && word.matches("(?i)wali|wala|wale|waali|waala|waale");
    }
}
