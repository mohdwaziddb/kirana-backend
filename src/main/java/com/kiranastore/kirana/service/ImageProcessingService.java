package com.kiranastore.kirana.service;

import com.kiranastore.kirana.dto.ItemResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ImageProcessingService {

    private final OCRService ocrService;
    private final ItemProcessingService itemProcessingService;

    public Map<String, Object> processImage(MultipartFile file) {

        try {
            String extractedText = ocrService.extractText(file);

            System.out.println("OCR TEXT: " + extractedText);

            List<ItemResponse> items = itemProcessingService.processText(extractedText);

            Map<String, Object> response = new HashMap<>();
            response.put("items", items);
            response.put("extractedText", extractedText);

            return response;

        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> response = new HashMap<>();
            response.put("items", new ArrayList<>());
            response.put("extractedText", "Error: " + e.getMessage());
            return response;
        }
    }
}