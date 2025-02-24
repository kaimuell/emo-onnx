package com.kaimueller_code.emo_onnx.util;
import com.kaimueller_code.emo_onnx.model.BoundingBox;

import java.util.*;

public class BoundingBoxUtils {

    public static float[] areaOf(float[][] leftTop, float[][] rightBottom) {
        int n = leftTop.length;
        float[] areas = new float[n];
        for (int i = 0; i < n; i++) {
            float width = Math.max(0, rightBottom[i][0] - leftTop[i][0]);
            float height = Math.max(0, rightBottom[i][1] - leftTop[i][1]);
            areas[i] = width * height;
        }
        return areas;
    }

    public static float[] iouOf(float[][] boxes0, float[][] boxes1, float eps) {
        int n = boxes0.length;
        float[] ious = new float[n];
        for (int i = 0; i < n; i++) {
            float[] overlapLeftTop = {Math.max(boxes0[i][0], boxes1[i][0]), Math.max(boxes0[i][1], boxes1[i][1])};
            float[] overlapRightBottom = {Math.min(boxes0[i][2], boxes1[i][2]), Math.min(boxes0[i][3], boxes1[i][3])};
            float[] overlapArea = areaOf(new float[][]{overlapLeftTop}, new float[][]{overlapRightBottom});
            float[] area0 = areaOf(new float[][]{{boxes0[i][0], boxes0[i][1]}}, new float[][]{{boxes0[i][2], boxes0[i][3]}});
            float[] area1 = areaOf(new float[][]{{boxes1[i][0], boxes1[i][1]}}, new float[][]{{boxes1[i][2], boxes1[i][3]}});
            ious[i] = overlapArea[0] / (area0[0] + area1[0] - overlapArea[0] + eps);
        }
        return ious;
    }

    public static float[][] hardNMS(float[][] boxScores, float iouThreshold, int topK, int candidateSize) {
        Arrays.sort(boxScores, Comparator.comparingDouble(o -> -o[4]));
        List<float[]> picked = new ArrayList<>();
        List<float[]> candidates = new ArrayList<>(Arrays.asList(boxScores));

        while (!candidates.isEmpty()) {
            float[] current = candidates.remove(0);
            picked.add(current);
            if (topK > 0 && picked.size() >= topK) {
                break;
            }
            candidates.removeIf(box -> iouOf(new float[][]{box}, new float[][]{current}, 1e-5f)[0] > iouThreshold);
        }
        return picked.toArray(new float[0][]);
    }

    /**
     * Erstelt vorhersage für Bounding boxes und projeziert sie auf die Grösse des original Bildes
     * @param width
     * @param height
     * @param confidences
     * @param boxes
     * @param probThreshold
     * @param iouThreshold
     * @param topK wie viele?
     * @return
     */
    public static List<BoundingBox> predict(int width, int height, float[][] confidences, float[][] boxes, float probThreshold, float iouThreshold, int topK) {
        List<float[]> pickedBoxProbs = new ArrayList<>();
        List<Integer> pickedLabels = new ArrayList<>();

        for (int classIndex = 1; classIndex < confidences[0].length; classIndex++) {
            List<float[]> boxProbs = new ArrayList<>();
            for (int i = 0; i < confidences.length; i++) {
                if (confidences[i][classIndex] > probThreshold) {
                    float[] newBox = Arrays.copyOf(boxes[i], 5);
                    newBox[4] = confidences[i][classIndex];
                    boxProbs.add(newBox);
                }
            }
            if (!boxProbs.isEmpty()) {
                float[][] boxArray = boxProbs.toArray(new float[0][]);
                float[][] nmsBoxes = hardNMS(boxArray, iouThreshold, topK, 200);
                pickedBoxProbs.addAll(Arrays.asList(nmsBoxes));
                for (float[] box : nmsBoxes) {
                    pickedLabels.add(classIndex);
                }
            }
        }

        if (pickedBoxProbs.isEmpty()) {
            return List.of();
        }

        List<BoundingBox> results = new ArrayList<>(pickedBoxProbs.size());
        for (float[] box : pickedBoxProbs) {
            results.add(new BoundingBox(
                    box[0] * width,
                    box[2] * width,
                    box[1] * height,
                    box[3] * height,
                    box[4],
                    box
            ));
        }

        return results;
    }

}
