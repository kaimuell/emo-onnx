package com.kaimueller_code.emo_onnx.model;


public class EmotionData {

    private static String[] emotions={"Anger", "Contempt", "Disgust", "Fear", "Happiness", "Neutral", "Sadness", "Surprise"};
    public static String getEmotion(float[] emotionScores){
        int bestInd=-1;
        if (emotionScores!=null){
            float maxScore=0;
            for(int i=0;i<emotionScores.length;++i){
                if(maxScore<emotionScores[i]){
                    maxScore=emotionScores[i];
                    bestInd=i;
                }
            }
        }
        return EmotionData.emotions[bestInd];
    }
}
