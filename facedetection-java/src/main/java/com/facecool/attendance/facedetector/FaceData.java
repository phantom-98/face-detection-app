package com.facecool.attendance.facedetector;

import android.graphics.Bitmap;

import com.facecool.attendance.Constants;
import com.google.mlkit.vision.face.Face;

import java.nio.ByteBuffer;

/**
 * Created by Kevin on 01-July-22
 */
public class FaceData {

	private Face face;
	private float fLive;
	private byte[] features = null;
	private Bitmap bmp = null;
	private String name;
	private float scoreMatch;
	public Object subject;
	private Bitmap qualityImage;
	private Integer qualitySize = null;
	private Float qualityBrightness = null;
	private Float qualitySharpness = null;
	private Boolean isQualityGood = null;
	private String failedRule = null;
	private String failureReason = null;
	private double imageQualityScore = 0.0;

	private float adjustedBrightness;
	private double absYaw, absPitch, absRoll;
	private int adjustedWidth;
	public long created;//unix epoch

	public FaceData(Face face, float fLive, Bitmap bmpFace) {
		this.created = System.currentTimeMillis();
		this.face = face;
		this.fLive = fLive;
		this.bmp = bmpFace;
		//this.features = new byte[512];//4*128
		this.name = Constants.Name_NOTIDENTIFIED;
		this.scoreMatch = 0.0f;
	}

	public float getAdjustedBrightness() {
		return adjustedBrightness;
	}

	public void setAdjustedBrightness(float value) {
		this.adjustedBrightness = value;
	}

	public double getAbsYaw() {
		return absYaw;
	}

	public void setAbsYaw(double value) {
		this.absYaw = value;
	}

	public double getAbsPitch() {
		return absPitch;
	}

	public void setAbsPitch(double value) {
		this.absPitch = value;
	}

	public double getAbsRoll() {
		return absRoll;
	}

	public void setAbsRoll(double value) {
		this.absRoll = value;
	}

	public int getAdjustedWidth() {
		return adjustedWidth;
	}

	public void setAdjustedWidth(int value) {
		this.adjustedWidth = value;
	}

	public void calculateQualityValues(){
		qualityBrightness = FaceEngine.getBrightness(getBestImage());
		qualitySharpness = FaceEngine.getSharpness(getBestImage());
		qualitySize = Math.max(getFace().getBoundingBox().width(), getFace().getBoundingBox().height());
	}

	public float getLive(){ return this.fLive; };
	public void setLive(float fLive){ this.fLive = fLive; };

	public Face getFace() { return this.face; };
	public Bitmap getBmp() { return this.bmp; };

	public byte[] getFeatures(){ return this.features; };
	public void setFeatures(byte[] features) { this.features = features; };

	public String getName(){ return this.name; };
	public void setName(String name){ this.name = name; };
	public float getScoreMatch(){ return this.scoreMatch; };
	public void setScoreMatch(float score){ this.scoreMatch = score; };

	public Bitmap getBestImage() {
		if(qualityImage!=null)
			return qualityImage;
		else
			return getBmp();
	}

	public void setQualityImage(Bitmap qualityImage) {
		this.qualityImage = qualityImage;
	}

	public int getQualitySize() {
		return qualitySize;
	}

	public float getQualityBrightness() {
		return qualityBrightness;
	}

	public float getQualitySharpness() {
		return qualitySharpness;
	}

	public Boolean isQualityGood() {
		return isQualityGood;
	}

	public void setIsQualityGood(boolean isQualityGood){
		this.isQualityGood = isQualityGood;
	}

	public String getFailedRule() {
		return failedRule;
	}

	public void setFailedRule(String failedRule) {
		this.failedRule = failedRule;
	}

	public String getFailureReason() {
		return failureReason;
	}

	public void setFailureReason(String failureReason) {
		this.failureReason = failureReason;
	}

	public static double[] getFeaturesAsDoubleArray(byte[] features) {
		if (features.length % Double.BYTES != 0) {
			throw new IllegalArgumentException("Byte array length is not divisible by 8.");
		}

		double[] doubles = new double[features.length / Double.BYTES];
		ByteBuffer.wrap(features).asDoubleBuffer().get(doubles);
		return doubles;
	}

	public double getImageQualityScore() {
		return imageQualityScore;
	}

	public void setImageQualityScore(double imageQualityScore) {
		this.imageQualityScore = imageQualityScore;
	}

}
