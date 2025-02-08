package com.kaimueller_code.emo_onnx.model;

public record BoundingBox(float left, float right, float top, float bottom, float prob, float[] result){}
