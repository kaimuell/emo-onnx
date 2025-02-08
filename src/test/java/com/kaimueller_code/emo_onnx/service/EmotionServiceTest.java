package com.kaimueller_code.emo_onnx.service;

import ai.onnxruntime.OrtException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;

import javax.imageio.ImageIO;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class EmotionServiceTest {

    @Autowired
    InferenceService service;

    @Test
    public void testEmotionService() throws IOException, OrtException {

        ClassPathResource resource = new ClassPathResource("test_a.jpg");
        InputStream inputStream = resource.getInputStream();
        BufferedImage image = ImageIO.read(inputStream);
        assertEquals("Fröhlich", service.detectSingleEmotion(image));
    }

}