package com.kaimueller_code.emo_onnx.service;

import ai.onnxruntime.OrtException;
import com.kaimueller_code.emo_onnx.model.EmotionEntity;
import com.kaimueller_code.emo_onnx.model.EmotionRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;

import javax.imageio.ImageIO;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class EmotionServiceTest {

    @Autowired
    InferenceService service;

    @Autowired
    EmotionRepository emotionRepository;

    @Test
    public void testEmotionService() throws IOException, OrtException {

        ClassPathResource resource = new ClassPathResource("test_b.jpg");
        InputStream inputStream = resource.getInputStream();
        BufferedImage image = ImageIO.read(inputStream);
        assertEquals("Fröhlich", service.detectSingleEmotion(image));
    }


    @Test
    public void testRepo(){
        String em = "Fröhlich";
        EmotionEntity e = new EmotionEntity();
        e.setEmotion(em);
        e.setDate(LocalDate.now());
        emotionRepository.save(e);
        assertNotNull(e.getId());

        Optional<EmotionEntity> e2 = emotionRepository.findById(e.getId());
        assertTrue(e2.isPresent());
        assertNotNull(e2.get().getDate());
    }

}