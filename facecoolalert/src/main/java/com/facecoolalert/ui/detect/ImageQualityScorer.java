package com.facecoolalert.ui.detect;

import com.facecool.attendance.facedetector.FaceData;

public class ImageQualityScorer {

    private static float adjustBrightness(float brightness, int width) {
        if (width <= 300) {
            if (brightness > 95) {
                brightness = 100 - brightness + 27;
            } else if (brightness > 82 && brightness < 95) {
                brightness = 100 - brightness + 40;
            }
        }
        return brightness;
    }

    // formular for calculating the face image quality. formula created by Moshe
    public static double calculateScoreUsingFaceData(FaceData faceData) {
        int adjustedWidth = faceData.getQualitySize() >= 300 ? 300 : faceData.getQualitySize();
        faceData.setAdjustedWidth(adjustedWidth);

        double absYaw = Math.abs(faceData.getFace().getHeadEulerAngleY());
        faceData.setAbsYaw(absYaw);

        double absPitch = Math.abs(faceData.getFace().getHeadEulerAngleX());
        faceData.setAbsPitch(absPitch);

        double absRoll = Math.abs(faceData.getFace().getHeadEulerAngleZ());
        faceData.setAbsRoll(absRoll);

        float adjustedBrightness = adjustBrightness(faceData.getQualityBrightness(), adjustedWidth);
        faceData.setAdjustedBrightness(adjustedBrightness);

        double sharpnessAdjustment = calculateSharpnessAdjustment(faceData.getQualitySharpness());

        double score = 51 + 0.1273 * adjustedWidth + 0.3030 * adjustedBrightness
                - 0.4643 * absYaw - 0.4674 * absPitch - 0.2190 * absRoll
                - sharpnessAdjustment;
        if (score > 100) {
            score = 100 - (absPitch + absYaw + absRoll) / 2;
        }
        return score;
    }

    private static double calculateSharpnessAdjustment(double sharpness) {
        if (sharpness < 4) {
            int subtractingValue;
            if (sharpness <= 2.9) {
                subtractingValue = 60;
            } else if (sharpness <= 3.5) {
                subtractingValue = 55;
            } else {
                subtractingValue = 50;
            }
            return subtractingValue - sharpness * 10;
        }
        return 0;
    }
}

