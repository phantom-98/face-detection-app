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
public class FaceEngine {

	static {
		System.loadLibrary("faceengine");
	}

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

//	public static float detect_live(Bitmap bmp_face){
//		return nativeDetect(bmp_face);
//	}

	public static float detect_bmp(Bitmap bmp, int lFace, int tFace, int rFace, int bFace){
		return nativeDetectBmp(bmp, lFace, tFace, rFace, bFace);
	}


	public static int loadFeatureModel(Context con){
//		String strDirApp = ctt.getFilesDir().toString();
//		File fileDirApp = new File(strDirApp);
//		if ( fileDirApp.exists() == false ) {
//			//Make "tessdata" directory if not existed.
//			if( fileDirApp.mkdirs() == false )
//				Constants.LogDebug("Faild to make the dir.");
//		}
//
//		String strDirModel = ctt.getFilesDir().toString() + File.separator + Constants.DIR_MODEL;
//		File fileDirModel = new File(strDirModel);
//		if ( fileDirModel.exists() == false ) {
//			//Make "tessdata" directory if not existed.
//			fileDirModel.mkdirs();
//		}
//
//		String pathModel = strDirApp + File.separator + "model.prototxt";
//		writeFileToPrivateStorage(R.raw.model, pathModel, ctt);
//		String pathWeight = strDirApp + File.separator + "weight.dat";
//		writeFileToPrivateStorage(R.raw.weight, pathWeight, ctt);
//


		String sPathModel = writeFileToPrivateStorage("model.prototxt", con);
		String sPathWeight = writeFileToPrivateStorage("weight.caffemodel", con);
		if( sPathModel.equals("") || sPathWeight.equals("") )
			return -1;

		return nativeLoadFeatureModelFromFile(sPathModel, sPathWeight);

//		String fModel = "model.prototxt";
//		int size = 0;
//		byte[] pModel = null;
//		int lenModel = 0;
//		try {
//			InputStream is = assetsManager.open(fModel);
//			size = is.available();
//			pModel = new byte[size];
//			lenModel = size;
//			is.read(pModel);
//			is.close();
//		} catch (IOException e) {
//			e.printStackTrace();
//			return -1;
//		}
//
//		String fWeight = "weight.caffemodel";
//		byte[] pWeight = null;
//		int lenWeight = 0;
//		try {
//			InputStream is = assetsManager.open(fWeight);
//			size = is.available();
//			pWeight = new byte[size];
//			lenWeight = size;
//			is.read(pModel);
//			is.close();
//		} catch (IOException e) {
//			e.printStackTrace();
//			return -1;
//		}
//
//		return nativeLoadFeatureModel(pModel, lenModel, pWeight, lenWeight);
	}

	public static void extractLiveFeature(Bitmap bmp, int lFace, int tFace, int rFace, int bFace, float[] landmarksX, float[] landmarksY, FaceData fd){
		float[] fvalues = new float[128];
		//synchronized of nativeExtractLiveFeature
		synchronized (FaceEngine.class)	{
			float live = nativeExtractLiveFeature(bmp, lFace, tFace, rFace, bFace, landmarksX, landmarksY, fvalues);
			fd.setLive(live);
			fd.setFeatures(floatsToBytes(fvalues));
		}
	}

	public static float getSimilarity(byte[] ft1, byte[] ft2){
		float[] feature1 = bytesToFloats(ft1);
		float[] feature2 = bytesToFloats(ft2);
		float sim = nativeSimilarity(feature1, feature2, 128);
		return sim;
	}

	public static float[] getSimilarity2(byte[] ft1, byte[][] allFt2) {
		float[] feature1 = bytesToFloats(ft1);
		float[][] features2 = new float[allFt2.length][];
		for (int i = 0; i < allFt2.length; i++) {
			features2[i] = bytesToFloats(allFt2[i]);
		}
		float[] sim = nativeSimilarity2(feature1, features2, features2.length, 128);
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

//	public static void writeFileToPrivateStorage(int rawFileId, String toFile, Context context)
//	{
//		File targetFile = new File(toFile);
//		// If already exists.
//		if (targetFile.exists() && targetFile.isFile())
//		{
//			Log.e("Log", toFile + " file was already existed.");
//			return ;
//		}
//
//		try {
//			int read = 0;
//			byte[] buff = new byte[10240];
//			FileOutputStream out = new FileOutputStream(toFile);
//			InputStream in = context.getResources().openRawResource(rawFileId);
//			while ((read = in.read(buff)) > 0)
//			{
//				out.write(buff, 0, read);
//			}
//			out.close();
//			in.close();
//		}catch(Exception ex){
//			Log.e("Log", "Error while copying file.", ex);
//		}
//	}

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

	public static float getSharpness(Bitmap bitmap){
		return nativeGetSharpness(bitmap);
	}

	public static float getBrightness(Bitmap bitmap){
		return  100 - nativeGetDarknessWithHistogram(bitmap);
	}

	@Keep
	private native static int nativeLoadLiveModel(AssetManager assetsManager, List<ModelConfig> modelConfig);

	@Keep
	private native static void nativeSetCheckLiveness(boolean isCheckLiveness);

	@Keep
	private native static float nativeDetectYuv(byte[] yuv, int previewWidth, int previewHeight,
												int orientation, int leftFace, int topFace, int rightFace, int bottomFace);

	@Keep
	private native static float nativeDetectBmp(Bitmap bmp, int leftFace, int topFace, int rightFace, int bottomFace);

	@Keep
	private static native int nativeLoadFeatureModelFromFile(String modelpath, String weightpath);

	@Keep
	private static native int nativeLoadFeatureModel(byte[] pModel, int lenModel, byte[] pWeight, int lenWeight);

	@Keep
	private static native float[] nativeSimilarity2(float[] vFeat1, float[][] allCompVecs, int vecCount, int featureLen);

	@Keep
	private static native float nativeSimilarity(float[] vFeat1, float[] vFeat2, int feature_len);

	@Keep
	private native static float nativeExtractLiveFeature(Bitmap bmp, int leftFace, int topFace, int rightFace, int bottomFace,
														 float[] landmarksX, float[] landmarksY, float[] features);
	@Keep
	private native static float nativeGetDarknessWithHistogram(Bitmap bmp);

	@Keep
	private native static float nativeGetSharpnessWithCustom(Bitmap bmp);

	@Keep
	private native static float nativeGetSharpness(Bitmap bmp);

//	@Keep
//	private native static float nativeDetect(Bitmap bmp_face);


/*

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

//	public static float detect_live(Bitmap bmp_face){
//		return nativeDetect(bmp_face);
//	}

	public static float detect_bmp(Bitmap bmp, int lFace, int tFace, int rFace, int bFace){
		return nativeDetectBmp(bmp, lFace, tFace, rFace, bFace);
	}


	public static int loadFeatureModel(Context con){
//		String strDirApp = ctt.getFilesDir().toString();
//		File fileDirApp = new File(strDirApp);
//		if ( fileDirApp.exists() == false ) {
//			//Make "tessdata" directory if not existed.
//			if( fileDirApp.mkdirs() == false )
//				Constants.LogDebug("Faild to make the dir.");
//		}
//
//		String strDirModel = ctt.getFilesDir().toString() + File.separator + Constants.DIR_MODEL;
//		File fileDirModel = new File(strDirModel);
//		if ( fileDirModel.exists() == false ) {
//			//Make "tessdata" directory if not existed.
//			fileDirModel.mkdirs();
//		}
//
//		String pathModel = strDirApp + File.separator + "model.prototxt";
//		writeFileToPrivateStorage(R.raw.model, pathModel, ctt);
//		String pathWeight = strDirApp + File.separator + "weight.dat";
//		writeFileToPrivateStorage(R.raw.weight, pathWeight, ctt);
//


		String sPathModel = writeFileToPrivateStorage("model.prototxt", con);
		String sPathWeight = writeFileToPrivateStorage("weight.caffemodel", con);
		if( sPathModel.equals("") || sPathWeight.equals("") )
			return -1;

		return nativeLoadFeatureModelFromFile(sPathModel, sPathWeight);

//		String fModel = "model.prototxt";
//		int size = 0;
//		byte[] pModel = null;
//		int lenModel = 0;
//		try {
//			InputStream is = assetsManager.open(fModel);
//			size = is.available();
//			pModel = new byte[size];
//			lenModel = size;
//			is.read(pModel);
//			is.close();
//		} catch (IOException e) {
//			e.printStackTrace();
//			return -1;
//		}
//
//		String fWeight = "weight.caffemodel";
//		byte[] pWeight = null;
//		int lenWeight = 0;
//		try {
//			InputStream is = assetsManager.open(fWeight);
//			size = is.available();
//			pWeight = new byte[size];
//			lenWeight = size;
//			is.read(pModel);
//			is.close();
//		} catch (IOException e) {
//			e.printStackTrace();
//			return -1;
//		}
//
//		return nativeLoadFeatureModel(pModel, lenModel, pWeight, lenWeight);
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

//	public static void writeFileToPrivateStorage(int rawFileId, String toFile, Context context)
//	{
//		File targetFile = new File(toFile);
//		// If already exists.
//		if (targetFile.exists() && targetFile.isFile())
//		{
//			Log.e("Log", toFile + " file was already existed.");
//			return ;
//		}
//
//		try {
//			int read = 0;
//			byte[] buff = new byte[10240];
//			FileOutputStream out = new FileOutputStream(toFile);
//			InputStream in = context.getResources().openRawResource(rawFileId);
//			while ((read = in.read(buff)) > 0)
//			{
//				out.write(buff, 0, read);
//			}
//			out.close();
//			in.close();
//		}catch(Exception ex){
//			Log.e("Log", "Error while copying file.", ex);
//		}
//	}

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

//	@Keep
//	private static float nativeDetect(Bitmap bmp_face);

 */
}
