package com.kaimueller_code.emo_onnx.controller;


import com.kaimueller_code.emo_onnx.model.EmotionEntity;
import com.kaimueller_code.emo_onnx.model.EmotionRepository;
import com.kaimueller_code.emo_onnx.service.InferenceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.time.LocalDate;

@RestController
public class BitmapController {

    Logger logger = LoggerFactory.getLogger(BitmapController.class);

    @Autowired
    InferenceService service;

    @Autowired
    EmotionRepository repository;

    @PostMapping("emotion")
    public ResponseEntity<String> postImageForEmotion(@RequestParam("file") MultipartFile multipartFile) throws IOException {

        try {
            logger.info("recieved file");
            if (multipartFile.isEmpty()){
                return ResponseEntity.badRequest().body("empty file");
            }
            BufferedImage image = ImageIO.read(multipartFile.getInputStream());
            if (image == null) {
                return ResponseEntity.badRequest().body("could not open image");
            }
            // Process the image (e.g., save to disk, process content, etc.)
            String emotion = service.detectSingleEmotion(image);
            logger.info("infered emoton: " + emotion);

            EmotionEntity entity = new EmotionEntity();
            entity.setEmotion(emotion);
            entity.setDate(LocalDate.now());
            repository.save(entity);

            return ResponseEntity.ok(emotion);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Failed to upload the file.");
        }
    }
}
