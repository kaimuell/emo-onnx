package com.kaimueller_code.emo_onnx.service;

import ai.onnxruntime.OrtException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.awt.image.BufferedImage;
import java.util.Optional;

@Service
public class InferenceService {
    @Autowired
    private final FaceDetectionService faceDetectionService;

    @Autowired
    private final EmotionService emotionService;

    public InferenceService(FaceDetectionService faceDetectionService, EmotionService emotionService) {
        this.faceDetectionService = faceDetectionService;
        this.emotionService = emotionService;
    }

    public String detectSingleEmotion(BufferedImage image) throws OrtException {
        Optional<BufferedImage> croppedImage = faceDetectionService.inferFace(image);
        if (croppedImage.isEmpty()){
            return "NA";
        }
        return emotionService.inferEmotion(croppedImage.get());
    }
}
