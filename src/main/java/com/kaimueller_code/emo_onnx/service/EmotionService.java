package com.kaimueller_code.emo_onnx.service;

import ai.onnxruntime.*;
import com.kaimueller_code.emo_onnx.util.ImageUtils;
import com.kaimueller_code.emo_onnx.model.EmotionData;
import com.kaimueller_code.emo_onnx.model.ModelData;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Map;


@Service
public class EmotionService {


    private OrtSession session;
    private final ModelData modelData;
    private final OrtEnvironment env;

    EmotionService() throws OrtException, IOException {
        // HSEmotion
        File file = new ClassPathResource("static/enet_b2_8_best.onnx").getFile();

        this.env = OrtEnvironment.getEnvironment();

        // nutze CUDA wenn möglich
        try {
            OrtSession.SessionOptions options = new OrtSession.SessionOptions();
            options.addCUDA();
            this.session = env.createSession(file.getPath(), options);
        } catch (OrtException oe) {
            this.session = env.createSession(file.getPath());

        }
        //normalisation Parameter definiert durch Model
        this.modelData = new ModelData(
                new Float[]{0.229f, 0.224f, 0.225f},
                new Float[]{0.485f, 0.456f, 0.406f},
                260,
                260
        );
    }


    public String inferEmotion(BufferedImage image) throws OrtException {
        BufferedImage scaledImage = ImageUtils.resizeImage(image, modelData.width(), modelData.height());
        // normalisiere das Image
        float[][][][] normalizedImg = normalizeImageAndTranspose(scaledImage);
        OnnxTensor tensor = OnnxTensor.createTensor(env, normalizedImg);
        OrtSession.Result result = session.run(Map.of("input", tensor));
        try (
                var outputMem = result.get("output").orElseThrow();
        ) {
            float[][] output = (float[][]) outputMem.getValue();
            return EmotionData.getEmotion(output[0]);
        }
    }

    /**
     * normalisiert ein Image entsprechend der Modell-Parameter und transponiert (Python äquivalent np.transpose(image, [2, 0, 1])
     *
     * @param image das Image
     */
    private float[][][][] normalizeImageAndTranspose(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();

        float[][][][] normalizedImg = new float[1][3][height][width];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                // Hole RGB-Werte
                int rgb = image.getRGB(x, y);
                float r = ((rgb >> 16) & 0xFF) / 255.0f;
                float g = ((rgb >> 8) & 0xFF) / 255.0f;
                float b = (rgb & 0xFF) / 255.0f;

                // normalisiere mit mean, std und transpose
                normalizedImg[0][0][y][x] = (r - modelData.mean()[0]) / modelData.std()[0];  // Red channel
                normalizedImg[0][1][y][x] = (g - modelData.mean()[1]) / modelData.std()[1];  // Green channel
                normalizedImg[0][2][y][x] = (b - modelData.mean()[2]) / modelData.std()[2];  // Blue channel
            }
        }
        return normalizedImg;
    }


}
