package com.kiranastore.kirana.service;

import org.springframework.stereotype.Service;
import java.util.regex.*;

@Service
public class AIService {

    public String parseTextToJson(String inputText) {

        // normalize separators
        inputText = inputText
                .replace(",", "\n")
                .replace(";", "\n")
                .replace("-", " ")
                .replace("=", " ")
                .replaceAll("\\s+", " ");

        String[] lines = inputText.split("\\n");

        StringBuilder json = new StringBuilder("[");
        int count = 0;

        for (String line : lines) {

            line = line.trim();
            if (line.isEmpty()) continue;

            // Extract all numbers from the line
            Pattern numberPattern = Pattern.compile("\\d+");
            Matcher numberMatcher = numberPattern.matcher(line);
            
            java.util.List<String> numbers = new java.util.ArrayList<>();
            while (numberMatcher.find()) {
                numbers.add(numberMatcher.group());
            }

            String name = "";
            String qty = "1";
            String price = "0";

            if (numbers.size() >= 1) {
                // Extract quantity (first number)
                qty = numbers.get(0);
                
                // Extract price (second number if exists)
                if (numbers.size() >= 2) {
                    price = numbers.get(1);
                }
                
                // Extract name (everything before the first number)
                int firstNumberIndex = line.indexOf(numbers.get(0));
                if (firstNumberIndex > 0) {
                    name = line.substring(0, firstNumberIndex).trim();
                } else {
                    name = line.replaceAll("\\d+", "").trim();
                }
            } else {
                // No numbers found, use entire line as name
                name = line;
            }

            if (count > 0) json.append(",");

            json.append("{\"name\":\"")
                    .append(name.toLowerCase())
                    .append("\",\"quantity\":\"")
                    .append(qty)
                    .append("\",\"price\":\"")
                    .append(price)
                    .append("\"}");

            count++;
        }

        json.append("]");
        return json.toString();
    }
}