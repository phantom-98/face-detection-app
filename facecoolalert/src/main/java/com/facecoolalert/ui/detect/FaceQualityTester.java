package com.facecoolalert.ui.detect;

import static com.facecoolalert.ui.detect.InternalStorageFacePhotoWriter.saveImageToInternalStorage;

import android.annotation.SuppressLint;
import android.content.Context;

import com.facecool.attendance.Constants;
import com.facecool.attendance.facedetector.FaceData;
import com.facecoolalert.ui.settings.Rule;
import com.facecoolalert.utils.PrefManager;

import java.util.Locale;

public class FaceQualityTester {

    private static FaceQualityTester instance;
    private Context context;
    private InternalStorageCSVWriter csvWriter;

    private boolean isCSVWritingEnabled = false;

    private FaceQualityTester(Context context) {
        this.context = context;
    }

    public static synchronized FaceQualityTester getInstance(Context context) {
        if (instance == null) {
            instance = new FaceQualityTester(context);
        }
        return instance;
    }

    public boolean isFaceQualityGood(FaceData faceData) {
        return testRules(context, (int) faceData.getQualityBrightness(), (int) faceData.getQualitySharpness(),
                faceData.getQualitySize(), (int) faceData.getFace().getHeadEulerAngleY(), faceData);
    }

    private boolean testRules(Context context, int br, int sharpness, int size, int absYaw, FaceData faceData) {
        String[] ruleNames = {
                Constants.RULE_1, Constants.RULE_2, Constants.RULE_3,
                Constants.RULE_4, Constants.RULE_5
        };

        for (String ruleName : ruleNames) {
            try {
                if (discardFaceByRule(context, ruleName, br, sharpness, size, absYaw, faceData)) {
                    return false; // Rule violated
                }
            } catch (Exception e) {
                // If an exception is thrown, assume the rule is not properly configured and let the face pass
                continue;
            }
        }

        return true; // All rules passed or not properly configured
    }

    private boolean discardFaceByRule(Context context, String ruleName, int br, int sharpness, int size, int absYaw, FaceData faceData) {
        Rule rule = PrefManager.getRule(context, ruleName);
        if (rule == null) {
            faceData.setFailedRule(ruleName);
            faceData.setFailureReason("Rule not found, default behavior assumed.");
            return false; // If the rule is not found, pass by default
        }

        boolean discardFaceImage;
        switch (ruleName) {
            case Constants.RULE_1:
                discardFaceImage = (br > rule.getBrHigh() || br < rule.getBrLow()) && sharpness < rule.getSharpness() && size < rule.getSize() && absYaw > rule.getAbsYaw();
                break;

            case Constants.RULE_2:
                discardFaceImage = (br > rule.getBrHigh() || br < rule.getBrLow()) && size < rule.getSize() && absYaw > rule.getAbsYaw();
                break;

            case Constants.RULE_3:
                discardFaceImage = size < rule.getSize() && absYaw > rule.getAbsYaw();
                break;

            case Constants.RULE_4:
                discardFaceImage = (br > rule.getBrHigh() || br < rule.getBrLow());
                break;

            case Constants.RULE_5:
                discardFaceImage = sharpness < rule.getSharpness();
                break;

            default:
                faceData.setFailedRule("Unknown");
                faceData.setFailureReason("Unknown rule, default behavior assumed.");
                return false; // If the rule is not recognized, pass by default
        }

        if (discardFaceImage) {
            faceData.setFailedRule(ruleName);
            faceData.setFailureReason("Failure at rule " + ruleName);
        } else {
            faceData.setFailedRule(null);
            faceData.setFailureReason(null);
        }

        return discardFaceImage;
    }

    public void enableCSVWriting(boolean enable) {
        if(enable == false) {
            if (csvWriter != null) {
                csvWriter.closeCSV();
                csvWriter = null;
            }
        }
        this.isCSVWritingEnabled = enable;
    }

    @SuppressLint("DefaultLocale")
    public void writeQualityDataToCSV(FaceData faceData){
        if (!isCSVWritingEnabled)
            return;
        if (csvWriter == null) {
            csvWriter = InternalStorageCSVWriter.getInstance(context);
            csvWriter.initializeCSV("face_quality_data.csv");
        }
        long currentTimeSeconds = System.currentTimeMillis() / 1000;
        String shortTimestamp = Long.toString(currentTimeSeconds);

        String formattedName = String.format(
                Locale.US,
                "si_%d_br_%.2f_sh_%.2f_ya_%.2f_pi_%.2f_ro_%.2f_sc_%.2f_iq_%.2f",
                faceData.getQualitySize(),
                faceData.getAdjustedBrightness(),
                faceData.getQualitySharpness(),
                faceData.getAbsYaw(),
                faceData.getAbsPitch(),
                faceData.getAbsRoll(),
                faceData.getScoreMatch(),
                faceData.getImageQualityScore()
        );

        String imageName = "f_" + shortTimestamp + "_" + formattedName + ".png";
        saveImageToInternalStorage(context, faceData.getBestImage(), imageName);

        csvWriter.appendRowToCSV(new String[]{
                imageName,
                String.valueOf(faceData.getQualitySize()),
                String.valueOf(faceData.getAdjustedBrightness()),
                String.valueOf(faceData.getQualitySharpness()),
                String.valueOf(faceData.getAbsYaw()),
                String.valueOf(faceData.getAbsPitch()),
                String.valueOf(faceData.getAbsRoll()),
                String.valueOf(faceData.getScoreMatch()),
                String.valueOf(faceData.getImageQualityScore())
        });
    }

    public boolean isCSVWritingEnabled() {
        return isCSVWritingEnabled;
    }

}
