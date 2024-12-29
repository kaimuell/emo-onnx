package com.kaimueller_code.emo_onnx.service;

import ai.onnxruntime.*;
import com.kaimueller_code.emo_onnx.model.EmotionData;
import com.kaimueller_code.emo_onnx.model.ModelData;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Map;


@Service
public class EmotionService {


    private final OrtSession session;
    private final ModelData modelData;
    private final OrtEnvironment env;

    EmotionService() throws OrtException, IOException {
        ClassPathResource file = new ClassPathResource("static/enet_b2_8_best.onnx");
        this.env = OrtEnvironment.getEnvironment();
        this.session = env.createSession(file.getURI().getPath());
        this.modelData = new ModelData(
                new Float[]{0.229f, 0.224f, 0.225f},
                new Float[]{0.485f, 0.456f, 0.406f},
                260);
    }


    public String inferEmotion(BufferedImage image) throws OrtException {
        BufferedImage scaledImage = resizeImage(image, modelData.getImageSize(), modelData.getImageSize());
        // Normalize the image
        float[][][][] normalizedImg = normalizeImage(scaledImage);
        // Transpose and reshape the image
        OnnxTensor tensor = OnnxTensor.createTensor(env, normalizedImg);
        OrtSession.Result result = session.run(Map.of("input", tensor));

        float[][] output = (float[][]) result.get("output").orElseThrow().getValue();
        return EmotionData.getEmotion(output[0]);
    }


    private BufferedImage resizeImage(BufferedImage img, int width, int height) {
        Image temp = img.getScaledInstance(width, height, Image.SCALE_SMOOTH);
        BufferedImage resizedImg = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = resizedImg.createGraphics();
        g2d.drawImage(temp, 0, 0, null);
        g2d.dispose();
        return resizedImg;
    }

    private float[][][][] normalizeImage(BufferedImage img) {
        int width = img.getWidth();
        int height = img.getHeight();

        float[][][][] normalizedImg = new float[1][3][height][width];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                // Get the RGB values
                int rgb = img.getRGB(x, y);
                float r = ((rgb >> 16) & 0xFF) / 255.0f;
                float g = ((rgb >> 8) & 0xFF) / 255.0f;
                float b = (rgb & 0xFF) / 255.0f;

                // Normalize using mean and std and transpose
                normalizedImg[0][0][y][x] = (r - modelData.getMean()[0]) / modelData.getStd()[0];  // Red channel
                normalizedImg[0][1][y][x] = (g - modelData.getMean()[1]) / modelData.getStd()[1];  // Green channel
                normalizedImg[0][2][y][x] = (b - modelData.getMean()[2]) / modelData.getStd()[2];  // Blue channel
            }
        }
        return normalizedImg;
    }
}
