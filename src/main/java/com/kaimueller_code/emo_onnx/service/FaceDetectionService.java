package com.kaimueller_code.emo_onnx.service;

import ai.onnxruntime.OnnxTensor;
import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtException;
import ai.onnxruntime.OrtSession;
import com.kaimueller_code.emo_onnx.util.BoundingBoxUtils;
import com.kaimueller_code.emo_onnx.util.ImageUtils;
import com.kaimueller_code.emo_onnx.model.BoundingBox;
import com.kaimueller_code.emo_onnx.model.ModelData;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Optional;


@Service
public class FaceDetectionService {

    private OrtSession session;
    private final ModelData modelData;
    private final OrtEnvironment env;


    public FaceDetectionService() throws IOException, OrtException {
        // HSEmotion
        File file = new ClassPathResource("static/version-RFB-640.onnx").getFile();

        this.env = OrtEnvironment.getEnvironment();
        // nutze CUDA wenn möglich
        try {
            OrtSession.SessionOptions options = new OrtSession.SessionOptions();
            options.addCUDA();
            this.session = env.createSession(file.getPath(), options);
        } catch (OrtException oe){
            this.session = env.createSession(file.getPath());
        }
        this.modelData = new ModelData(
                new Float[]{128.0f,128.0f,128.0f},
                new Float[]{127.0f,127.0f,127.0f},
                640,
                480

        );
    }

    public Optional<BufferedImage> inferFace(BufferedImage image) throws OrtException {
        BufferedImage scaledImage = ImageUtils.resizeImage(image, modelData.width(), modelData.height());
        // Normalize the image
        float[][][][] normalizedImg = normalizeImageAndTranspose(scaledImage);
        // Transpose and reshape the image
        OnnxTensor tensor = OnnxTensor.createTensor(env, normalizedImg);
        OrtSession.Result result = session.run(Map.of("input", tensor));
        float[][][] scores = (float[][][]) result.get("scores").orElseThrow().getValue();
        float[][][] boxes = (float[][][]) result.get("boxes").orElseThrow().getValue();
        List<BoundingBox> bboxes = BoundingBoxUtils.predict(image.getWidth(), image.getHeight(), scores[0], boxes[0], 0.5f, 0.5f, 1);
        if (!bboxes.isEmpty()){
            BoundingBox box = bboxes.getFirst(); //topk = 1 deshalb nur wahrscheinlichste BB
           return Optional.of(image.getSubimage((int) box.left(), (int) box.top(), (int) (box.right()-box.left()), (int) (box.bottom()-box.top())));
        }
        return Optional.empty();
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
