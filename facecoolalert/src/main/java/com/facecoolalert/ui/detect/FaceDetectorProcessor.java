package com.facecoolalert.ui.detect;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.PointF;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;

import com.facecool.attendance.CallRateMeter;
import com.facecool.attendance.Constants;
import com.facecool.attendance.facedetector.FaceData;
import com.facecool.attendance.facedetector.FaceEngine;
import com.facecool.attendance.facedetector.FakeFaceEngine;
import com.facecool.cameramanager.camera.BitmapUtils;
import com.facecool.cameramanager.camera.FaceGraphic2;
import com.facecool.cameramanager.camera.FrameMetadata;
import com.facecool.cameramanager.camera.GraphicOverlay;
import com.facecool.cameramanager.camera.VisionProcessorBase;
import com.facecool.cameramanager.entity.CameraInfo;
import com.facecoolalert.App;
import com.facecoolalert.database.MyDatabase;
import com.facecoolalert.database.Repositories.AlertDao;
import com.facecoolalert.database.Repositories.AlertLogDao;
import com.facecoolalert.database.Repositories.RecognitionResultDao;
import com.facecoolalert.database.Repositories.SubscriberDistributionListDao;
import com.facecoolalert.database.Repositories.WatchlistDao;
import com.facecoolalert.database.entities.Alert;
import com.facecoolalert.database.entities.AlertLog;
import com.facecoolalert.database.entities.RecognitionResult;
import com.facecoolalert.database.entities.Subject;
import com.facecoolalert.database.entities.WatchList;
import com.facecoolalert.ui.camera.RecentRecognitionsAdapter;
import com.facecoolalert.utils.Alert.AlertUtils;
import com.facecoolalert.utils.RecognitionUtils;
import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;
import com.google.mlkit.vision.face.FaceLandmark;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by Kevin on 01-July-22
 */
public class FaceDetectorProcessor extends VisionProcessorBase<List<Face>> {//

	private static final String TAG = "FaceDetectorProcessor";
	private ConcurrentHashMap<Integer, RecognitionResult> recognitionResults;
	private ResultListener resultListener;
	private FaceDetector detector;
	private float _fThrMatch;
	private float _fThrLive;
	private boolean _isCheckLiveness;
	public boolean isReportSuccessNull = false;
	private Context context;
	private CameraInfo mycamera;
	public Boolean saveToDb;
	private int lastSubjectsChange=0;
	public ConcurrentSkipListSet<Integer> workingOn;
	private int landmarkMode;
	private int performanceMode;
	private boolean bMoreAccurate;
	private float minFaceSize;
	private static final long RECOGNITION_INTERVAL_MS = 200;
	private final Map<Integer, Integer> recognitionAttemptCount = new HashMap<>();
	private Map<Integer, Long> recognitionAttemptTime = new HashMap<>();
	private ConcurrentLinkedQueue<FaceTask> taskQueue = new ConcurrentLinkedQueue<>();
	private ConcurrentHashMap<Integer, Boolean> currentlyProcessing = new ConcurrentHashMap<>();
	private ExecutorService recognitionExecutor = Executors.newFixedThreadPool(2);
	private AtomicBoolean isProcessing = new AtomicBoolean(false);
	private Handler mainHandler = new Handler(Looper.getMainLooper());
	private List<Integer> trackingIds = new ArrayList<>();
	private final Set<Integer> relevantFaces = ConcurrentHashMap.newKeySet();

	public void setReportSuccessNull(boolean bFlag)
	{
		isReportSuccessNull = bFlag;
	}
	private int currentMode = 0;

	public static Boolean filterFaces=true;


	public void init(Context context)
	{
		this.landmarkMode = FaceDetectorOptions.LANDMARK_MODE_ALL;//.LANDMARK_MODE_NONE;
		this.performanceMode = FaceDetectorOptions.PERFORMANCE_MODE_FAST;//.PERFORMANCE_MODE_ACCURATE;
		this.bMoreAccurate = Constants.getFaceMoreAccurate(context);
		if( bMoreAccurate )
			this.performanceMode = FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE;
		this.minFaceSize = Constants.getMinFaceSize(context);
	}

	public final void createFaceDetector(Context context){
		if(!App.hasModelLoaded)
			App.USE_FAKE=true;

		// mlkit face detector options. has been chosen because of balance between quality and speed
		FaceDetectorOptions options_temp = new FaceDetectorOptions.Builder()
				.setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
				.setContourMode(FaceDetectorOptions.CONTOUR_MODE_NONE)
				.setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_NONE)
				.setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
				.setMinFaceSize(0.03f)
				.enableTracking()
				.build();

		if( detector != null )
			detector = null;
		detector = FaceDetection.getClient(options_temp);

		this.saveToDb=true;
		RecognitionUtils.refreshHistory();

	}

	public FaceDetectorProcessor(Context context, ResultListener resultListener) {//, FaceDetectorOptions options
		super(context);
		//Log.v(MANUAL_TESTING_LOG, "Face detector options: " + options);
		this.resultListener = resultListener;

		_fThrLive = Constants.getThresholdLive(context);
		_fThrMatch = Constants.getThresholdSimilar(context);
		_isCheckLiveness = Constants.getLiveFlag(context);
		if (App.USE_FAKE||!App.hasModelLoaded ) {
			FakeFaceEngine.SetCheckLiveness(_isCheckLiveness);
		} else {
			FaceEngine.SetCheckLiveness(_isCheckLiveness);
		}

		this.context=context;
		this.saveToDb=true;

		this.recognitionResults=new ConcurrentHashMap<>();
		workingOn=new ConcurrentSkipListSet<Integer>();
	}

	@Override
	public void stop() {
		super.stop();
		try {
			if(detector!=null)
				detector.close();
		}catch (Exception ed)
		{
			ed.printStackTrace();
		}
	}

	@Override
	protected Task<List<Face>> detectInImage(InputImage image) {
		if(detector==null)
			createFaceDetector(context);

		return detector.process(image);
	}

	@Override
	protected void onSuccessReg(@NonNull Bitmap bmpFrame, @NonNull List<Face> faces) {
		List<FaceData> listFaceData = new ArrayList<>();
		for (Face face : faces) {
			try {
				FaceData fd = GetFaceLiveFeature(bmpFrame, RecognitionUtils.createFaceData(bmpFrame, face));

				fd.calculateQualityValues();
				double qualityScore = ImageQualityScorer.calculateScoreUsingFaceData(fd);
				fd.setImageQualityScore(qualityScore);
				//FaceData fd = GetFaceData(bmpFrame, face);
				listFaceData.add(fd);
			}catch (Exception es)
			{
				es.printStackTrace();
			}
		}

		if(!App.hasModelLoaded)
		{
			new Handler(Looper.getMainLooper()).postDelayed(() -> resultListener.onError(new Exception("Ai Model encountered some error, please restart App")), 100);
			return;
		}

		if( listFaceData.size() > 0 )
		{
			finishScanning(listFaceData.get(0), true);
		}
		else
		{
			if(isReportSuccessNull)
			{
				new Handler(Looper.getMainLooper()).postDelayed(() -> resultListener.onSuccessNull(), 1000);
			}
		}
	}

	// is called after mlkit has an answer
	protected void onSuccess(@NonNull Bitmap bmpFrame, @NonNull List<Face> faces, @NonNull GraphicOverlay graphicOverlay, ByteBuffer buffer, FrameMetadata frameMetadata) {
		CallRateMeter.measureCallRate();
		graphicOverlay.clear();
		relevantFaces.clear();
		for (Face face : faces) {
			if(!isValidFace(face)) continue;
			relevantFaces.add(face.getTrackingId()); // preventing multiple processings of the same face/trackingId
			processSingleFace(face, graphicOverlay, bmpFrame, buffer, frameMetadata); // processing faces sequential because I had issues with face recognition otherwise
		}
		graphicOverlay.postInvalidate();
	}

	private void processSingleFace(Face face, GraphicOverlay graphicOverlay, Bitmap bmpFrame, ByteBuffer buffer, FrameMetadata frameMetadata) {
		RecognitionResult existingResult = recognitionResults.get(face.getTrackingId());
		FaceData fd = new FaceData(face, 1, null);
		if (isRecognised(existingResult)) {
			fd.setName(existingResult.getSubject().getName()); // set name for face rectangular if face has been recognised
		}
		if (existingResult != null) {
			fd.setScoreMatch(existingResult.getScoreMatch()); // set recognition score if one has been calculated
			fd.setImageQualityScore(existingResult.getFaceData().getImageQualityScore()); // set image quality score
		}

		if (FaceQualityTester.getInstance(context).isCSVWritingEnabled()) {
			processSingleFaceWithCsv(fd, graphicOverlay, bmpFrame, existingResult, buffer, frameMetadata); // flow is different. here always calculate recognition value
		} else {
			processSingleFaceRegularly(fd, graphicOverlay, bmpFrame, existingResult, buffer, frameMetadata); // normal flow. calculate recognition value if not isRecognised
		}
	}

	private void processSingleFaceWithCsv(FaceData fd, GraphicOverlay graphicOverlay, Bitmap bmpFrame, RecognitionResult existingResult, ByteBuffer buffer, FrameMetadata frameMetadata) {
		startRecognition(bmpFrame, fd.getFace(), buffer, frameMetadata);
		FaceGraphic2 fg = new FaceGraphic2(graphicOverlay, fd, getScale(), getViewWidth(), getViewHeight(), isRecognised(existingResult), getCurrentOrientation(), getCurrentMode());
		graphicOverlay.add(fg);
	}

	private void processSingleFaceRegularly(FaceData fd, GraphicOverlay graphicOverlay, Bitmap bmpFrame, RecognitionResult existingResult, ByteBuffer buffer, FrameMetadata frameMetadata) {
		if (!isRecognised(existingResult)) {
			startRecognition(bmpFrame, fd.getFace(),  buffer, frameMetadata);
		}
		GraphicOverlay.Graphic fg;
		fg = new FaceGraphic2(graphicOverlay, fd, getScale(), getViewWidth(), getViewHeight(), isRecognised(existingResult), getCurrentOrientation(), getCurrentMode());

		graphicOverlay.add(fg);
	}

	private boolean isRecognised(RecognitionResult existingResult){
		return existingResult != null && existingResult.getSubject() != null && existingResult.getScoreMatch() >= RecognitionUtils.similarityThreshold;
	}

	private boolean isValidFace(Face face) {
		return face != null
				&& face.getTrackingId() != null
				&& isFaceLargeEnough(face);
	}

    private boolean isFaceLargeEnough(Face face) {
		Rect bb = face.getBoundingBox();
		int faceWidth = bb.right - bb.left;
		int faceHeight = bb.bottom - bb.top;
		return (faceWidth > 30 && faceHeight > 30);
	}

	private void startRecognition(Bitmap bmpFrame, Face face, ByteBuffer buffer, FrameMetadata frameMetadata) {
		Long lastAttemptTime = recognitionAttemptTime.getOrDefault(face.getTrackingId(), 0L);
		int attempts = recognitionAttemptCount.getOrDefault(face.getTrackingId(), 0);
		if (System.currentTimeMillis() - lastAttemptTime < RECOGNITION_INTERVAL_MS ) { //|| attempts >= MAX_RECOGNITION_ATTEMPTS
			// If the interval has not yet elapsed or the maximum number of attempts has been reached, do nothing
			return;
		}
		//continue with recognition. recognition calculation is not up to date.
		recognitionAttemptTime.put(face.getTrackingId(), System.currentTimeMillis());
		recognitionAttemptCount.put(face.getTrackingId(), attempts + 1);
		if (!currentlyProcessing.containsKey(face.getTrackingId())) {
			currentlyProcessing.put(face.getTrackingId(), true);
			taskQueue.add(new FaceTask(face, bmpFrame, buffer, frameMetadata));
			recognizeNextFace();
		}
	}

	private void recognizeNextFace() {
		FaceTask task = taskQueue.poll();
		if (task != null) {
			recognitionExecutor.execute(() -> {
				try {
					recognitionProcess(task.bmpFrame, task.face, task.buffer, task.frameMetadata);
				} finally {
					currentlyProcessing.remove(task.face.getTrackingId());
					if (hasRecognized(task.face.getTrackingId())) {
						finishScanning(getRecognition(task.face.getTrackingId()).getFaceData(), false);
					}
				}
			});
		}
	}

	private static class FaceTask {
		Face face;
		Bitmap bmpFrame;
		ByteBuffer buffer;
		FrameMetadata frameMetadata;

		public FaceTask(Face face, Bitmap bmpFrame, ByteBuffer buffer, FrameMetadata frameMetadata) {
			this.face = face;
			this.bmpFrame = bmpFrame;
			this.buffer = buffer;
			this.frameMetadata = frameMetadata;
		}
	}

	private void recognitionProcess(Bitmap bmpFrame, Face face, ByteBuffer buffer, FrameMetadata frameMetadata) {
		Integer trackingId = face.getTrackingId();
		if (!relevantFaces.contains(trackingId)) {
			return; // second mechanism besides queue to prevent parallel recognition calculation
		}

		try {
			if(bmpFrame == null){
				bmpFrame = BitmapUtils.getBitmap(buffer, frameMetadata); // bmp important for creating face bitmap. in case of android camera until no bmp created
			}
			FaceData faceData = RecognitionUtils.createFaceData(bmpFrame, face);

			faceData.calculateQualityValues(); // image quality values in order to decide to use face image for recognition and so on or not
			double qualityScore = ImageQualityScorer.calculateScoreUsingFaceData(faceData);
			faceData.setImageQualityScore(qualityScore);

//			FaceQualityTester.getInstance(context).isFaceQualityGood(faceData);

			RecognitionResult result = null;
			if(!filterFaces||qualityScore >= RecognitionUtils.recognitionQualityThreshold) {
				FaceData fd = GetFaceLiveFeature(bmpFrame, faceData); // face recognition method

				result = new RecognitionResult((Subject) fd.subject, fd);
				result.setLocation(mycamera.location);
				result.setCamera(mycamera.index);
				recognitionResults.put(face.getTrackingId(), result);

				saveRecognitionToDatabase(fd.getFace(), fd, true);
			}
			else {
				result = new RecognitionResult(null, faceData);
				result.setLocation(mycamera.location);
				result.setCamera(mycamera.index);
				recognitionResults.put(face.getTrackingId(), result);
			}

			FaceQualityTester.getInstance(context).writeQualityDataToCSV(faceData); // for creating the csv file on your phone
			RecognitionUtils.putRecognitionResult(mycamera.index, face.getTrackingId(), result); // for e2e testing

		} catch (Exception e) {
			Log.e("FaceDetectorProcessor", "Error in recognition process", e);
		}
	}

	private void saveRecognitionToDatabase(Face face, FaceData fd, Boolean willInsertToDb) {
		if(willInsertToDb) {
			Executor executor = Executors.newSingleThreadExecutor();
			executor.execute(new Runnable() {
				@Override
				public void run() {
					try {
						RecognitionResultDao recognitionResultDao = MyDatabase.getInstance(context).recognitionResultDao();
						RecognitionResult currentRecognition=getRecognition(face.getTrackingId());
//						currentRecognition.setBmp(MyUtils.getQualityImage(face,bmpFrame));
//						if(qualityfaceImage!=null)
//							currentRecognition.setBmp(qualityfaceImage);
						if(currentRecognition.getScoreMatch()<RecognitionUtils.similarityThreshold) {
							currentRecognition.setSubjectId(null);
							currentRecognition.setSubject(null);
						}
						recognitionResultDao.insertRecognitionResult(currentRecognition);
						RecentRecognitionsAdapter.queue.offer(currentRecognition);

						//sending the alerts
						AlertDao alertDao=MyDatabase.getInstance(context).alertDao();
						AlertLogDao alertLogDao=MyDatabase.getInstance(context).alertLogDao();
						SubscriberDistributionListDao subscriberDistributionListDao=MyDatabase.getInstance(context).subscriberDistributionListDao();
						WatchlistDao watchlistDao=MyDatabase.getInstance(context).watchlistDao();


						Subject subject=null;
						if(fd.subject!=null)
							subject=(Subject) fd.subject;

						if(fd.getScoreMatch()>=RecognitionUtils.similarityThreshold &&subject!=null&&currentRecognition.getSubjectId() == subject.getUid()) {
							//know if to Alert
							Long now = new Date().getTime();
							Long allowedLess = now - RecognitionUtils.alertIntervals*1000L;
							Long latestAlert = recognitionResultDao.getLatestAlertforSubject(subject.getUid());
							if (latestAlert == null || latestAlert <= allowedLess) {//check if its within set interval

								WatchList watchList = watchlistDao.getByName(subject.getWatchlist());

								if (watchList != null) {
									for (Alert alert : alertDao.getAllAlertsByWatchList(watchList.getUid(), mycamera.location)) {
										AlertLog alertLog = new AlertLog();
										alertLog.setAlertName(alert.getName());
										alertLog.setRecognitionResult_id(currentRecognition.getUid());
										alertLogDao.insertAlertLog(alertLog);


										{
											AlertUtils.sendAlerts(alert.getName(),subscriberDistributionListDao.getSubscribersForList(alert.getDistributionList_id()),alertLog,currentRecognition,subject,context);
										}

									}

								}
							}
						}

						System.out.println("Saved Successfully");
					} catch (Exception erd) {
						erd.printStackTrace();
					}
				}
			});
		}
	}

	@Override
	protected void onFailure(@NonNull Exception ex){
		Constants.LogDebug("Face detection failed " + ex.getMessage());
		new Handler(Looper.getMainLooper()).post(()->resultListener.onError(ex));
	}

	@Override
	public void updateThreshould(Context con){
		_fThrMatch = Constants.getThresholdSimilar(con);
		_fThrLive = Constants.getThresholdLive(con);
		_isCheckLiveness = Constants.getLiveFlag(con);
		createFaceDetector(con);
	}

	private void finishScanning(final FaceData faceData, boolean bOnlyReg) {
		try {
			if(faceData != null) {
				// Delay returning result 1 sec. in order to make mrz text become visible on graphicOverlay by user
				// You want to call 'resultListener.onSuccess(mrzInfo)' without no delay
				if(isReportSuccessNull)
				{
					new Handler(Looper.getMainLooper()).post(() -> resultListener.onSuccess(faceData, bOnlyReg));
				}
				else
				{
					new Handler(Looper.getMainLooper()).postDelayed(() -> resultListener.onSuccess(faceData, bOnlyReg), 1000);
				}

			}
		} catch(Exception exp) {
			Constants.LogDebug("Face DATA is not valid");
		}
	}

	private FaceData GetFaceLiveFeature(Bitmap bmpFrame, FaceData fd){

		long liveStartMs = System.currentTimeMillis();
		FaceData faceData = RecognitionUtils.extractFeatures(bmpFrame, fd);

		RecognitionUtils.recognizeFace(faceData);

		long liveEndMs = System.currentTimeMillis();
		int proc_time = (int)(liveEndMs - liveStartMs);

		Constants.LogDebug(String.format("Live Result (Processing Time, Live Value) = (%d, %s)", proc_time, String.valueOf(faceData.getLive())));

		return faceData;
	}

	private static int[] landMarkTypes = new int[] {
			FaceLandmark.LEFT_EYE, FaceLandmark.RIGHT_EYE,
			FaceLandmark.NOSE_BASE,
			FaceLandmark.MOUTH_LEFT, FaceLandmark.MOUTH_RIGHT,
	};

	private static void logExtrasForTesting(Face face) {
		if (face != null) {
			Log.v(MANUAL_TESTING_LOG, "face bounding box: " + face.getBoundingBox().flattenToString());
			Log.v(MANUAL_TESTING_LOG, "face Euler Angle X: " + face.getHeadEulerAngleX());
			Log.v(MANUAL_TESTING_LOG, "face Euler Angle Y: " + face.getHeadEulerAngleY());
			Log.v(MANUAL_TESTING_LOG, "face Euler Angle Z: " + face.getHeadEulerAngleZ());

			String[] landMarkTypesStrings =
					new String[] {
							"MOUTH_BOTTOM",
							"MOUTH_RIGHT",
							"MOUTH_LEFT",
							"RIGHT_EYE",
							"LEFT_EYE",
							"RIGHT_EAR",
							"LEFT_EAR",
							"RIGHT_CHEEK",
							"LEFT_CHEEK",
							"NOSE_BASE"
					};
			for (int i = 0; i < landMarkTypes.length; i++) {
				FaceLandmark landmark = face.getLandmark(landMarkTypes[i]);
				if (landmark == null) {
					Log.v(
							MANUAL_TESTING_LOG,
							"No landmark of type: " + landMarkTypesStrings[i] + " has been detected");
				} else {
					PointF landmarkPosition = landmark.getPosition();
					String landmarkPositionStr =
							String.format(Locale.US, "x: %f , y: %f", landmarkPosition.x, landmarkPosition.y);
					Log.v(
							MANUAL_TESTING_LOG,
							"Position for face landmark: "
									+ landMarkTypesStrings[i]
									+ " is :"
									+ landmarkPositionStr);
				}
			}
			Log.v(
					MANUAL_TESTING_LOG,
					"face left eye open probability: " + face.getLeftEyeOpenProbability());
			Log.v(
					MANUAL_TESTING_LOG,
					"face right eye open probability: " + face.getRightEyeOpenProbability());
			Log.v(MANUAL_TESTING_LOG, "face smiling probability: " + face.getSmilingProbability());
			Log.v(MANUAL_TESTING_LOG, "face tracking id: " + face.getTrackingId());
		}
	}


	public CameraInfo getMycamera() {
		return mycamera;
	}

	public void setMycamera(CameraInfo mycamera) {
		this.mycamera = mycamera;
	}

	public boolean hasRecognized(Integer trackingId) {
		return recognitionResults.containsKey(trackingId);
	}

	public  RecognitionResult getRecognition(Integer trackingId) {
		return recognitionResults.get(trackingId);
	}

	public interface ResultListener {
		void onSuccess(FaceData faceData, boolean bOnlyReg);
		void onSuccessNull();
		void onError(Exception exp);
	}
}