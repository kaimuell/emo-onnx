package com.kaimueller_code.emo_onnx.model;

public class ModelData {

    private final Float[] std;
    private final Float[] mean;
    private final int imageSize;

    public ModelData(Float[] std, Float[] mean, int imageSize) {
        this.std = std;
        this.mean = mean;
        this.imageSize = imageSize;
    }

    public Float[] getStd() {
        return std;
    }

    public Float[] getMean() {
        return mean;
    }

    public int getImageSize() {
        return imageSize;
    }
}
