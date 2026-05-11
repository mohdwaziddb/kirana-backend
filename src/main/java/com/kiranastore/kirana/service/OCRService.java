package com.kiranastore.kirana.service;

import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.vision.v1.*;
import net.sourceforge.tess4j.Tesseract;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class OCRService {

    // Static OCR post-processing prompt for grocery extraction
    private static final String OCR_POST_PROCESSING_PROMPT = "You are an OCR post-processing assistant.\n" +
            "\n" +
            "Convert handwritten grocery/shop list OCR text into structured items and quantities.\n" +
            "\n" +
            "Strict Rules:\n" +
            "\n" +
            "1. Extract only product/item lines.\n" +
            "Ignore:\n" +
            "- Dates (examples: 28/04/2026, 05-04-2026)\n" +
            "- Headings like Dal, Grocery, Total, Amount\n" +
            "- Prices, totals, rupee values\n" +
            "- Tick marks/check marks\n" +
            "- Arrows -> or →\n" +
            "- Random notes\n" +
            "\n" +
            "2. Ignore serial numbers in any format at the beginning:\n" +
            "1\n" +
            "1.\n" +
            "(1)\n" +
            "01\n" +
            "①\n" +
            "2)\n" +
            "etc.\n" +
            "\n" +
            "3. For every line return:\n" +
            "item_name | quantity\n" +
            "\n" +
            "4. Quantity Rules:\n" +
            "A) If pattern exists:\n" +
            "(10x20)\n" +
            "(10*20)\n" +
            "(10X20)\n" +
            "\n" +
            "Use SECOND number as quantity.\n" +
            "\n" +
            "Examples:\n" +
            "Oreo (10x20) = 20\n" +
            "Sauce (5*12) = 12\n" +
            "\n" +
            "5. If only one number in bracket:\n" +
            "Donut (2)\n" +
            "use quantity = 2\n" +
            "\n" +
            "6. If quantity appears without brackets:\n" +
            "Sugar 5kg -> qty 5\n" +
            "Pulse 10 -> qty 10\n" +
            "\n" +
            "7. If no quantity found:\n" +
            "default quantity = 1\n" +
            "\n" +
            "8. Remove units from quantity:\n" +
            "kg, gm, g, litre, ltr, ml, pcs, pc\n" +
            "\n" +
            "9. Keep item names exactly as written.\n" +
            "\n" +
            "10. Merge duplicate items by summing quantities.\n" +
            "\n" +
            "11. If text like:\n" +
            "item (10*20)\n" +
            "ignore first number (pack size)\n" +
            "take second number only.\n" +
            "\n" +
            "12. Output ONLY valid JSON:\n" +
            "[\n" +
            "{\n" +
            "\"item_name\":\"Chana Dal\",\n" +
            "\"qty\":5\n" +
            "},\n" +
            "{\n" +
            "\"item_name\":\"Oreo\",\n" +
            "\"qty\":20\n" +
            "}\n" +
            "]\n" +
            "\n" +
            "No explanation.\n" +
            "No markdown.\n" +
            "Only JSON output.";

    @Value("${google.cloud.vision.type}")
    private String googleType;

    @Value("${google.cloud.vision.project-id}")
    private String googleProjectId;

    @Value("${google.cloud.vision.private-key-id}")
    private String googlePrivateKeyId;

    @Value("${google.cloud.vision.private-key}")
    private String googlePrivateKey;

    @Value("${google.cloud.vision.client-email}")
    private String googleClientEmail;

    @Value("${google.cloud.vision.client-id}")
    private String googleClientId;


    public String extractText(MultipartFile file) {

        List<String> allResults = new ArrayList<>();

        // 1 Google Vision
        String text = extractWithGoogleVision(file);

        if (text != null && !text.trim().isEmpty()) {
            allResults.add("GoogleVision: " + text);
            System.out.println("✅ Google Cloud Vision success");
            System.out.println("📝 OCR Text for post-processing: " + text);
            // Post-process with the static prompt
            String postProcessedText = postProcessOCRText(text);
            if (postProcessedText != null && !postProcessedText.trim().isEmpty()) {
                System.out.println("✅ Post-processed text: " + postProcessedText);
                return postProcessedText;
            }
            return text;
        }

        // 2 Tesseract
        text = extractWithTesseract(file);

        if (text != null && !text.trim().isEmpty()) {
            allResults.add("Tesseract: " + text);
            System.out.println("✅ Tesseract OCR success");
            // Post-process with the static prompt
            String postProcessedText = postProcessOCRText(text);
            if (postProcessedText != null && !postProcessedText.trim().isEmpty()) {
                System.out.println("✅ Post-processed text: " + postProcessedText);
                return postProcessedText;
            }
            return text;
        }

        System.out.println("❌ All OCR failed: " + allResults);

        return "";
    }

    // Post-process OCR text using the static prompt
    private String postProcessOCRText(String ocrText) {
        System.out.println("🔧 Post-processing OCR text with prompt...");
        System.out.println("📋 Prompt length: " + OCR_POST_PROCESSING_PROMPT.length() + " characters");
        System.out.println("📥 OCR text length: " + ocrText.length() + " characters");
        
        // For now, return the original text
        // TODO: Integrate with LLM API for actual post-processing
        // This would involve calling an AI service with the prompt and OCR text
        return ocrText;
    }



    // ================= GOOGLE VISION =================

    private String extractWithGoogleVision(MultipartFile file) {

        try {

            System.out.println("🔑 Using Google credentials from properties");
            System.out.println("📧 " + googleClientEmail);
            System.out.println("🆔 " + googleProjectId);

            ImageAnnotatorClient visionClient = null;

            try {

                String formattedPrivateKey = googlePrivateKey
                        .replace("\\n","\n")
                        .trim();

                if(!formattedPrivateKey.contains("BEGIN PRIVATE KEY")){
                    formattedPrivateKey =
                            "-----BEGIN PRIVATE KEY-----\n"
                                    + formattedPrivateKey
                                    + "\n-----END PRIVATE KEY-----";
                }

                String credentialJson =
                        "{"
                                + "\"type\":\"service_account\","
                                + "\"project_id\":\""+googleProjectId+"\","
                                + "\"private_key_id\":\""+googlePrivateKeyId+"\","
                                + "\"private_key\":\""
                                + formattedPrivateKey.replace("\n","\\n")
                                + "\","
                                + "\"client_email\":\""+googleClientEmail+"\","
                                + "\"client_id\":\""+googleClientId+"\","
                                + "\"token_uri\":\"https://oauth2.googleapis.com/token\""
                                + "}";


                GoogleCredentials credentials =
                        ServiceAccountCredentials.fromStream(
                                new ByteArrayInputStream(
                                        credentialJson.getBytes()
                                )
                        );


                ImageAnnotatorSettings settings =
                        ImageAnnotatorSettings.newBuilder()
                                .setCredentialsProvider(
                                        FixedCredentialsProvider.create(
                                                credentials
                                        )
                                )
                                .build();

                visionClient =
                        ImageAnnotatorClient.create(settings);

                System.out.println(
                        "✅ Google Vision Client Created"
                );

            }
            catch (Exception e) {

                System.out.println(
                        "❌ Vision setup failed "
                                + e.getMessage()
                );

                throw e;
            }


            byte[] imageBytes = file.getBytes();

            com.google.cloud.vision.v1.Image img =
                    com.google.cloud.vision.v1.Image.newBuilder()
                            .setContent(
                                    com.google.protobuf.ByteString
                                            .copyFrom(imageBytes)
                            )
                            .build();


            List<AnnotateImageRequest> requests =
                    new ArrayList<>();

            AnnotateImageRequest request =
                    AnnotateImageRequest.newBuilder()

                            .addFeatures(
                                    Feature.newBuilder()
                                            .setType(Feature.Type.DOCUMENT_TEXT_DETECTION)
                            )

                            .setImage(img)

                            .setImageContext(
                                    ImageContext.newBuilder()
                                            .addLanguageHints("en")
                                            .addLanguageHints("hi")
                                            .addLanguageHints("en-t-i0-handwrit")
                                            .build()
                            )

                            .build();


            requests.add(request);


            BatchAnnotateImagesResponse response =
                    visionClient.batchAnnotateImages(requests);


            List<EntityAnnotation> annotations =
                    response.getResponsesList()
                            .get(0)
                            .getTextAnnotationsList();


            if (annotations.isEmpty()) {
                visionClient.close();
                return null;
            }


            String fullText =
                    annotations.get(0).getDescription();

            visionClient.close();

            return cleanGoogleVisionText(
                    fullText
                            .replaceAll("^\\(?\\d+\\)?\\.?\\s*", "") // serial remove
            );

        }

        catch (Exception e) {

            System.out.println(
                    "⚠️ Google Vision Error: "
                            + e.getMessage()
            );

            return null;
        }
    }



    private String cleanGoogleVisionText(String text) {

        if(text==null){
            return "";
        }

        // preserve lines
        text = text.replaceAll("[ \t]+"," ").trim();
        text = text.replaceAll("\\n{2,}","\n");

        // split merged items like "5kg चावल"
        text = text.replaceAll("(\\d+(?:\\.\\d+)?\\s*kg)\\s+(?=[\\p{L}अ-ह])", "$1\n");

        text = text.replace("०","0")
                .replace("१","1")
                .replace("२","2")
                .replace("३","3")
                .replace("४","4")
                .replace("५","5")
                .replace("६","6")
                .replace("७","7")
                .replace("८","8")
                .replace("९","9");

        return text;
    }




    // ================= TESSERACT =================

    private String extractWithTesseract(MultipartFile file) {

        try {

            File temp=
                    File.createTempFile(
                            "upload",
                            ".jpg"
                    );

            file.transferTo(temp);

            Tesseract tesseract =
                    new Tesseract();

            tesseract.setDatapath(
                    "C:/Program Files/Tesseract-OCR/tessdata"
            );

            tesseract.setLanguage("eng+hin");

            String result=
                    tesseract.doOCR(temp);

            temp.delete();

            return result;

        }
        catch (Exception e){

            System.out.println(
                    "⚠️ Tesseract Error "
                            + e.getMessage()
            );

            return null;
        }

    }




    
}