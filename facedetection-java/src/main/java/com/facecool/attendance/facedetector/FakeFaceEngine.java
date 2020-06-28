package com.facecool.attendance.facedetector;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;

import androidx.annotation.Keep;

import com.facecool.attendance.Constants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Kevin on 01-July-22
 */
public  class FakeFaceEngine {

	public static int loadLiveModel(AssetManager assetsManager){
		List<ModelConfig> listConfigs = parseConfig(assetsManager);
		return nativeLoadLiveModel(assetsManager, listConfigs);
	}

	public static void SetCheckLiveness(boolean isCheckLiveness){
		nativeSetCheckLiveness(isCheckLiveness);
	}

	public static float detectLive(ByteBuffer data, int w, int h, int orientation, int lFace, int tFace, int rFace, int bFace){
		return nativeDetectYuv(data.array(), w, h, orientation, lFace, tFace, rFace, bFace);
	}


	public static float detect_bmp(Bitmap bmp, int lFace, int tFace, int rFace, int bFace){
		return nativeDetectBmp(bmp, lFace, tFace, rFace, bFace);
	}


	public static int loadFeatureModel(Context con){

		String sPathModel = writeFileToPrivateStorage("model.prototxt", con);
		String sPathWeight = writeFileToPrivateStorage("weight.caffemodel", con);
		if( sPathModel.equals("") || sPathWeight.equals("") )
			return -1;

		return nativeLoadFeatureModelFromFile(sPathModel, sPathWeight);
	}

	public static void extractLiveFeature(Bitmap bmp, int lFace, int tFace, int rFace, int bFace, float[] landmarksX, float[] landmarksY, FaceData fd){
		float[] fvalues = new float[128];
		float live = nativeExtractLiveFeature(bmp, lFace, tFace, rFace, bFace, landmarksX, landmarksY, fvalues);
		fd.setLive(live);
		fd.setFeatures(floatsToBytes(fvalues));
	}

	public static float getSimilarity(byte[] ft1, byte[] ft2){
		float[] feature1 = bytesToFloats(ft1);
		float[] feature2 = bytesToFloats(ft2);
		float sim = nativeSimilarity(feature1, feature2, 128);
		return sim;
	}

	private static List<ModelConfig> parseConfig(AssetManager assetManager)  {
		List<ModelConfig> listConfig = new ArrayList<>();
		String line = "";
		try{
			InputStream is = assetManager.open("live/config.json");
			BufferedReader br = new BufferedReader( new InputStreamReader(is));
			line = br.readLine();
		} catch (IOException e) {
			return listConfig;
		}

		if( line.equals("") )
			return listConfig;

		try {
			JSONArray jsonArray = new JSONArray(line);

			for (int i = 0; i < jsonArray.length(); i++) {
				JSONObject config = jsonArray.getJSONObject(i);
				ModelConfig modelConfig = new ModelConfig();
				modelConfig.name = config.optString("name");
				modelConfig.width = config.optInt("width");
				modelConfig.height = config.optInt("height");
				modelConfig.scale = (float) config.optDouble("scale");
				modelConfig.shift_x = (float) config.optDouble("shift_x");
				modelConfig.shift_y = (float) config.optDouble("shift_y");
				modelConfig.org_resize = config.optBoolean("org_resize");

				listConfig.add(modelConfig);
			}
		}catch (JSONException jsonEx){

		}
		return listConfig;
	}

	// Upload file to storage and return a path.
	private static String writeFileToPrivateStorage(String file, Context context) {
		AssetManager assetManager = context.getAssets();
		BufferedInputStream inputStream = null;
		try {
			// Read data from assets.
			inputStream = new BufferedInputStream(assetManager.open(file));
			byte[] data = new byte[inputStream.available()];
			inputStream.read(data);
			inputStream.close();
			// Create copy file in storage.
			File outFile = new File(context.getFilesDir(), file);
			FileOutputStream os = new FileOutputStream(outFile);
			os.write(data);
			os.close();
			// Return a path to file which may be read in common way.
			return outFile.getAbsolutePath();
		} catch (IOException ex) {
			Constants.LogDebug("Failed to upload a file");
		}
		return "";
	}

	public static byte[] floatsToBytes(float[] floats) {
		byte bytes[] = new byte[Float.BYTES * floats.length];
		ByteBuffer.wrap(bytes).asFloatBuffer().put(floats);
		return bytes;
	}

	public static float[] bytesToFloats(byte[] bytes) {
		if (bytes.length % Float.BYTES != 0)
			throw new RuntimeException("Illegal length");
		float floats[] = new float[bytes.length / Float.BYTES];
		ByteBuffer.wrap(bytes).asFloatBuffer().get(floats);
		return floats;
	}


	@Keep
	private static int nativeLoadLiveModel(AssetManager assetsManager, List<ModelConfig> modelConfig) {
		return 0;
	}

	@Keep
	private static void nativeSetCheckLiveness(boolean isCheckLiveness) {

	}

	@Keep
	private static float nativeDetectYuv(byte[] yuv, int previewWidth, int previewHeight,
										 int orientation, int leftFace, int topFace, int rightFace, int bottomFace) {
		return 0.0f;
	}

	@Keep
	private static float nativeDetectBmp(Bitmap bmp, int leftFace, int topFace, int rightFace, int bottomFace) {
		return 0.0f;
	}

	@Keep
	private static int nativeLoadFeatureModelFromFile(String modelpath, String weightpath) {
		return 0;
	}

	@Keep
	private static int nativeLoadFeatureModel(byte[] pModel, int lenModel, byte[] pWeight, int lenWeight) {
		return 0;
	}

	@Keep
	private static float nativeSimilarity(float[] vFeat1, float[] vFeat2, int feature_len) {
		return 0;
	}

	@Keep
	private static float nativeExtractLiveFeature(Bitmap bmp, int leftFace, int topFace, int rightFace, int bottomFace,
												  float[] landmarksX, float[] landmarksY, float[] features){
		return 0;
	}

}
