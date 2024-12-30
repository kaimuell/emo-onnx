package com.kaimueller_code.emo_onnx.controller;


import ai.onnxruntime.OrtException;
import com.kaimueller_code.emo_onnx.service.EmotionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

@RestController
public class BitmapController {

    @Autowired
    EmotionService service;

    @PostMapping("emotion")
    public ResponseEntity<String> postImageForEmotion(@RequestParam("file") MultipartFile multipartFile) throws IOException {

        try {
            if (multipartFile.isEmpty()){
                return ResponseEntity.badRequest().body("empty file");
            }
            BufferedImage image = ImageIO.read(multipartFile.getInputStream());
            if (image == null) {
                return ResponseEntity.badRequest().body("could not open image");
            }
            // Process the image (e.g., save to disk, process content, etc.)
            String emotion = service.inferEmotion(image);
            return ResponseEntity.ok(emotion);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Failed to upload the file.");
        }
    }
}
