package com.kaimueller_code.emo_onnx.service;

import ai.onnxruntime.OnnxTensor;
import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtException;
import ai.onnxruntime.OrtSession;
import com.kaimueller_code.emo_onnx.ImageUtils;
import com.kaimueller_code.emo_onnx.model.ModelData;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Map;


@Service
public class FaceDetectionService {

    private final OrtSession session;
    private final ModelData modelData;
    private final OrtEnvironment env;


    public FaceDetectionService() throws IOException, OrtException {
        // HSEmotion
        ClassPathResource file = new ClassPathResource("static/version-RFB-640.onnx");

        // nutze CUDA wenn möglich
        OrtSession.SessionOptions options = new OrtSession.SessionOptions();
        options.addCUDA();


        this.env = OrtEnvironment.getEnvironment();
        this.session = env.createSession(file.getURI().getPath(), options);
        this.modelData = new ModelData(
                new Float[]{128.0f,128.0f,128.0f},
                new Float[]{127.0f,127.0f,127.0f},
                320,
                240

        );
    }

    public void inferFaceBoundingBox(BufferedImage image) throws OrtException {
        BufferedImage scaledImage = ImageUtils.resizeImage(image, modelData.width(), modelData.height());
        // Normalize the image
        float[][][][] normalizedImg = normalizeImageAndTranspose(scaledImage);
        // Transpose and reshape the image
        OnnxTensor tensor = OnnxTensor.createTensor(env, normalizedImg);
        OrtSession.Result result = session.run(Map.of("input", tensor));
        float[][] scores = (float[][]) result.get("scores").orElseThrow().getValue();
        float[][] boxes = (float[][]) result.get("boxes").orElseThrow().getValue();


    }






    private float[][][][] normalizeImageAndTranspose(BufferedImage img) {
        int width = img.getWidth();
        int height = img.getHeight();

        float[][][][] normalizedImg = new float[1][3][height][width];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                // Get the RGB values
                int rgb = img.getRGB(x, y);
                float r = ((rgb >> 16) & 0xFF);
                float g = ((rgb >> 8) & 0xFF);
                float b = (rgb & 0xFF);

                // Normalize using mean and std and transpose
                normalizedImg[0][0][y][x] = (r - modelData.mean()[0]) / modelData.std()[0];  // Red channel
                normalizedImg[0][1][y][x] = (g - modelData.mean()[1]) / modelData.std()[1];  // Green channel
                normalizedImg[0][2][y][x] = (b - modelData.mean()[2]) / modelData.std()[2];  // Blue channel
            }
        }
        return normalizedImg;
    }
}
