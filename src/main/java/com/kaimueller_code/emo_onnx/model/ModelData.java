package com.kaimueller_code.emo_onnx.model;

public record ModelData (Float[] std, Float[] mean, int width, int height) { }
