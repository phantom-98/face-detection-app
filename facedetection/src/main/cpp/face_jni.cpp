//
// Created by kevin on 22-7-20.
//

#include "android_log.h"
#include "live.h"
#include "img_process.h"
#include "feature/facefeature.h"

extern "C" {

JNIEXPORT jint JNICALL
FACE_ENGINE_METHOD(nativeLoadLiveModel)(JNIEnv *env, jobject instance, jobject asset_manager,
                             jobject configs);

JNIEXPORT jfloat JNICALL
FACE_ENGINE_METHOD(nativeDetectYuv)(JNIEnv *env, jobject instance, jbyteArray yuv, jint preview_width,
                             jint preview_height, jint orientation, jint left, jint top, jint right, jint bottom);

JNIEXPORT jfloat JNICALL
FACE_ENGINE_METHOD(nativeDetectBmp)(JNIEnv *env, jobject instance, jobject bmp, jint left, jint top, jint right, jint bottom);

JNIEXPORT void JNICALL
FACE_ENGINE_METHOD(nativeSetCheckLiveness)(JNIEnv *env, jobject instance, jboolean isCheckLiveness){
    Live *pLive = Live::getInstance();
    pLive->SetCheckLiveness((bool)isCheckLiveness);
}

JNIEXPORT jint JNICALL
FACE_ENGINE_METHOD(nativeLoadFeatureModelFromFile)(JNIEnv *env, jobject instance, jstring model, jstring weight);

JNIEXPORT jboolean JNICALL
FACE_ENGINE_METHOD(nativeLoadFeatureModel)(JNIEnv *env, jobject instance,
                                           jbyteArray pModel, int lenModel, jbyteArray pWeight, int lenWeight);

JNIEXPORT jfloat JNICALL
FACE_ENGINE_METHOD(nativeSimilarity)(JNIEnv *env, jobject,
                                       jfloatArray vFeat1, jfloatArray vFeat2, jint feature_len);

JNIEXPORT jfloat JNICALL
FACE_ENGINE_METHOD(nativeExtractLiveFeature)(JNIEnv *env, jobject instance,
        jobject bmp, jint left, jint top, jint right, jint bottom,
        jfloatArray landmarksX, jfloatArray landmarksY, jfloatArray features);

JNIEXPORT jfloat JNICALL
FACE_ENGINE_METHOD(nativeGetSharpness)(JNIEnv *env, jobject instance, jobject bmp);

JNIEXPORT jfloat JNICALL
FACE_ENGINE_METHOD(nativeGetSharpnessWithCustom)(JNIEnv *env, jobject instance, jobject bmp);

JNIEXPORT jfloat JNICALL
FACE_ENGINE_METHOD(nativeGetDarknessWithHistogram)(JNIEnv *env, jobject instance, jobject bmp);
}


void ConvertAndroidConfig2NativeConfig(JNIEnv *env,jobject model_configs, std::vector<ModelConfig>& modelConfigs) {
    modelConfigs.clear();

    jclass list_clz = env->GetObjectClass(model_configs);
    jmethodID list_size = env->GetMethodID(list_clz, "size", "()I");
    jmethodID list_get = env->GetMethodID(list_clz, "get", "(I)Ljava/lang/Object;");

    env->DeleteLocalRef(list_clz);

    int len = env->CallIntMethod(model_configs, list_size);
    for(int i = 0; i < len; i++) {
        jobject config = env->CallObjectMethod(model_configs, list_get, i);
        jclass config_clz = env->GetObjectClass(config);
        jfieldID config_name         = env->GetFieldID(config_clz, "name" ,"Ljava/lang/String;");
        jfieldID config_width        = env->GetFieldID(config_clz, "width", "I");
        jfieldID config_height       = env->GetFieldID(config_clz, "height", "I");
        jfieldID config_scale        = env->GetFieldID(config_clz, "scale", "F");
        jfieldID config_shift_x      = env->GetFieldID(config_clz, "shift_x", "F");
        jfieldID config_shift_y      = env->GetFieldID(config_clz, "shift_y", "F");
        jfieldID config_org_resize   = env->GetFieldID(config_clz, "org_resize", "Z");

        env->DeleteLocalRef(config_clz);

        ModelConfig modelConfig;
        modelConfig.width       = env->GetIntField(config, config_width);
        modelConfig.height      = env->GetIntField(config, config_height);
        modelConfig.scale       = env->GetFloatField(config, config_scale);
        modelConfig.shift_x     = env->GetFloatField(config, config_shift_x);
        modelConfig.shift_y     = env->GetFloatField(config, config_shift_y);
        modelConfig.org_resize  = env->GetBooleanField(config, config_org_resize);
        jstring model_name_jstr   = static_cast<jstring>(env->GetObjectField(config, config_name));
        const char *name = env->GetStringUTFChars(model_name_jstr, 0);

        std::string nameStr(name);
        modelConfig.name = nameStr;
        modelConfigs.push_back(modelConfig);

        env->ReleaseStringUTFChars(model_name_jstr, name);
    }
}


JNIEXPORT jint JNICALL
FACE_ENGINE_METHOD(nativeLoadLiveModel)(JNIEnv *env, jobject instance, jobject asset_manager,
        jobject configs) {
    std::vector<ModelConfig> model_configs;
    ConvertAndroidConfig2NativeConfig(env, configs, model_configs);

    AAssetManager* mgr = AAssetManager_fromJava(env, asset_manager);
    Live *pLive = Live::getInstance();
    return pLive->LoadModel(mgr, model_configs);
}


JNIEXPORT jfloat JNICALL
FACE_ENGINE_METHOD(nativeDetectYuv)(JNIEnv *env, jobject instance, jbyteArray yuv, jint preview_width,
        jint preview_height, jint orientation, jint left, jint top, jint right, jint bottom) {
    jbyte *yuv_ = env->GetByteArrayElements(yuv, nullptr);

    cv::Mat bgr;
    Yuv420sp2bgr(reinterpret_cast<unsigned char *>(yuv_), preview_width, preview_height, orientation, bgr);

    if( bgr.empty() || bgr.cols == 0 || bgr.rows == 0)
        return 0.0f;

    LOG_DEBUG("Frame Size = (%d, %d)", bgr.cols, bgr.rows);
    LOG_DEBUG("Face Rect = (%d, %d, %d, %d)", left, top, right, bottom);

    cv::Rect rtFace;
    rtFace.x = max(0, left);
    rtFace.y = max(0, top);
    rtFace.width = min(bgr.cols, right) - rtFace.x;
    rtFace.height = min(bgr.rows, bottom) - rtFace.y;

    Live *pLive = Live::getInstance();
    float confidence = pLive->Detect(bgr, rtFace);
    env->ReleaseByteArrayElements(yuv, yuv_, 0);

    return confidence;
}


JNIEXPORT jfloat JNICALL
FACE_ENGINE_METHOD(nativeDetectBmp)(JNIEnv *env, jobject instance, jobject bmp, jint left, jint top, jint right, jint bottom) {
    cv::Mat bgr;
    ConvertBitmap2Mat(env, bmp, bgr);
    if( bgr.empty() || bgr.cols == 0 || bgr.rows == 0)
        return 0.0f;

    LOG_DEBUG("Frame Size = (%d, %d)", bgr.cols, bgr.rows);
    LOG_DEBUG("Face Rect = (%d, %d, %d, %d)", left, top, right, bottom);

    cv::Rect rtFace;
    rtFace.x = max(0, left);
    rtFace.y = max(0, top);
    rtFace.width = min(bgr.cols, right) - rtFace.x;
    rtFace.height = min(bgr.rows, bottom) - rtFace.y;

    Live *pLive = Live::getInstance();
    float confidence = pLive->Detect(bgr, rtFace);

    return confidence;
}

JNIEXPORT jint JNICALL
FACE_ENGINE_METHOD(nativeLoadFeatureModelFromFile)(JNIEnv *env, jobject instance, jstring model,jstring weight){
    jint nRet = -1;

    char* model_path = (char*)env->GetStringUTFChars(model, 0);
    char* weight_path = (char*)env->GetStringUTFChars(weight, 0);

    FaceFeature *pFF = FaceFeature::getInstance();

    nRet = pFF->LoadModelFromFile(model_path, weight_path);
    if( nRet != 0 )
        LOG_DEBUG("It was failed to load the featrue models");

    env->ReleaseStringUTFChars(model, model_path);
    env->ReleaseStringUTFChars(weight, weight_path);

    return nRet;
}

JNIEXPORT jboolean JNICALL
FACE_ENGINE_METHOD(nativeLoadFeatureModel)(JNIEnv *env, jobject instance,
        jbyteArray pModel, int lenModel, jbyteArray pWeight, int lenWeight){
    jboolean tRet = false;

    const char* pbyModel = (const char*) env->GetByteArrayElements(pModel, NULL);
    const char* pbyWeight = (const char* ) env->GetByteArrayElements(pWeight, NULL);

    FaceFeature *pFF = FaceFeature::getInstance();

    if(pFF->LoadModel(pbyModel, lenModel, pbyWeight, lenWeight))
        tRet = true;
    else
        LOG_DEBUG("It was failed to load the featrue models");

    env->ReleaseByteArrayElements(pModel, (jbyte*)pbyModel, 0);
    env->ReleaseByteArrayElements(pWeight, (jbyte*)pbyWeight, 0);

    return tRet;
}

JNIEXPORT jfloat JNICALL
FACE_ENGINE_METHOD(nativeGetSharpness)(JNIEnv *env, jobject instance, jobject bmp){
    cv::Mat sourceMatImage;
    ConvertBitmap2Mat(env, bmp, sourceMatImage);
    if( sourceMatImage.empty() || sourceMatImage.cols == 0 || sourceMatImage.rows == 0)
        return 0.0f;

    cv::Mat M = (cv::Mat_<double>(3, 1) << -1, 2, -1);
    cv::Mat G = cv::getGaussianKernel(3, -1, CV_64F);

    cv::Mat Lx;
    cv::sepFilter2D(sourceMatImage, Lx, CV_64F, M, G);

    cv::Mat Ly;
    cv::sepFilter2D(sourceMatImage, Ly, CV_64F, G, M);

    cv::Mat FM = cv::abs(Lx) + cv::abs(Ly);

    double focusMeasure = cv::mean(FM).val[0];
    return focusMeasure;
}

JNIEXPORT jfloat JNICALL
FACE_ENGINE_METHOD(nativeGetSharpnessWithCustom)(JNIEnv *env, jobject instance, jobject bmp){
    cv::Mat sourceMatImage;
    ConvertBitmap2Mat(env, bmp, sourceMatImage);
    if( sourceMatImage.empty() || sourceMatImage.cols == 0 || sourceMatImage.rows == 0)
        return 0.0f;

    int gradientHorizontal[256];
    int *pGradientHorizontal = gradientHorizontal;
    int gradientVertical[256];
    int *pGradientVertical = gradientVertical;
    int luminanceHistogram[256];
    int *pLuminance = luminanceHistogram;
    int maxGradient = 0;

    for (int i = 0;i < 256;i++)
    {
        gradientHorizontal[i] = 0;
        gradientVertical[i] = 0;
        luminanceHistogram[i] = 0;
    }
    int height = sourceMatImage.rows;
    int width = sourceMatImage.cols;

    float pixels[height*width];
    for (int nRow = 0; nRow < height; nRow++)
    {
        for (int nCol = 0; nCol < width; nCol++)
        {
            cv::Vec3b vec = sourceMatImage.at<cv::Vec3b>(cv::Point2d(nRow, nCol));
            pixels[nRow*width+nCol] = (vec.val[0] * 0.299 + vec.val[1] * 0.587 + vec.val[2] * 0.114);
        }
    }
// pixel by pixel math...
    for (int nRow = 0; nRow < height-1; nRow++)
    {
        int nRowOffset = nRow * width;
        int nNextRowOffset = (nRow+1) * width;

        for (int nCol = 0; nCol < width-1; nCol++)
        {
            int gC = pixels[nRowOffset + nCol];
            int gH = abs(gC - pixels[nRowOffset + nCol + 1]);
            int gV = abs(gC - pixels[nNextRowOffset + nCol]);
//            LOG_DEBUG("Sharpness : %d, %d, %d", gC, gH, gV);
            pLuminance[gC]++;
            pGradientHorizontal[gH]++;
            pGradientVertical[gV]++;
        }
    }


// find max gradient
    for (int i = 255;i >= 0;i--)
    {
        // first one with a value
        if ((gradientHorizontal[i] > 0) || (gradientVertical[i] > 0))
        {
            maxGradient = i;
            break;
        }
    }

    int rangeLow = 0;
    int rangeHi = 0;
    int p;
    p = 0;
    int VFOCUS_N = 24;
    for (int i = 0; i < 256; i++)
    {
        if (luminanceHistogram[i] > 0)
        {
            if (p + luminanceHistogram[i] > VFOCUS_N)
            {
                rangeLow += (i * (VFOCUS_N - p));
                p = VFOCUS_N;
                break;
            }

            p += luminanceHistogram[i];
            rangeLow += (i * luminanceHistogram[i]);
        }
    }
    if (p)
        rangeLow /= p;

    p = 0;
    for (int i = 255;i >= 0;i--)
    {
        if (luminanceHistogram[i] > 0)
        {
            if (p + luminanceHistogram[i] > VFOCUS_N)
            {
                rangeHi += (i * (VFOCUS_N - p));
                p = VFOCUS_N;
                break;
            }

            p += luminanceHistogram[i];
            rangeHi += (i * luminanceHistogram[i]);
        }
    }
    if (p)
        rangeHi /= p;

//    float mFocusScore = (float)fmin((float)maxGradient / (fabs((float)rangeHi - (float)rangeLow) * 0.005), 100.00);
    return (float)maxGradient / (abs(rangeHi-rangeLow) * 0.005f);
}

JNIEXPORT jfloat JNICALL
FACE_ENGINE_METHOD(nativeGetDarknessWithHistogram)(JNIEnv *env, jobject instance, jobject bmp){
    cv::Mat sourceMatImage;
    ConvertBitmap2Mat(env, bmp, sourceMatImage);
    if( sourceMatImage.empty() || sourceMatImage.cols == 0 || sourceMatImage.rows == 0)
        return 0.0f;

    int histogram[256];
    for (int i=0;i<256;i++) {
        histogram[i] = 0;
    }

    for (int x = 0; x < sourceMatImage.cols; x++) {
        for(int y = 0; y < sourceMatImage.rows; y++) {
             cv::Vec3b pixel = sourceMatImage.at<cv::Vec3b>(cv::Point2i(x,y));
            int brightness = (int) (0.229*pixel.val[0] + 0.587*pixel.val[1] + 0.114*pixel.val[2]);
            histogram[brightness]++;
        }
    }

    int allPixelsCount = sourceMatImage.rows * sourceMatImage.cols;
// Count pixels with brightness less then 10
    int darkPixelCount = 0;
    for (int i=0;i<60;i++) {
        darkPixelCount += histogram[i];
    }
    return darkPixelCount/(float)allPixelsCount*100;
}

JNIEXPORT jfloat JNICALL
FACE_ENGINE_METHOD(nativeExtractLiveFeature)(JNIEnv *env, jobject instance,
        jobject bmp, jint left, jint top, jint right, jint bottom,
        jfloatArray landmarksX, jfloatArray landmarksY, jfloatArray features)
{
    cv::Mat matBGR;
    ConvertBitmap2Mat(env, bmp, matBGR);
    if( matBGR.empty() || matBGR.cols == 0 || matBGR.rows == 0)
        return 0.0f;

    LOG_DEBUG("Frame Size = (%d, %d)", matBGR.cols, matBGR.rows);
    LOG_DEBUG("Face Rect = (%d, %d, %d, %d)", left, top, right, bottom);

    cv::Rect rtFace;
    rtFace.x = max(0, left);
    rtFace.y = max(0, top);
    rtFace.width = min(matBGR.cols, right) - rtFace.x;
    rtFace.height = min(matBGR.rows, bottom) - rtFace.y;

    Live *pLive = Live::getInstance();
    float confidence = pLive->Detect(matBGR, rtFace);

    LOG_DEBUG("Face Live Conf = %f", confidence);

    float posLMX[5];
    float posLMY[5];
    jfloat* tmpLandX = env->GetFloatArrayElements(landmarksX, NULL);
    jfloat* tmpLandY = env->GetFloatArrayElements(landmarksY, NULL);
    for(int i = 0; i < 5; i++){
        posLMX[i] = tmpLandX[i];
        posLMY[i] = tmpLandY[i];
        LOG_DEBUG("Landmark (x, y) = (%.2f, %.2f)", posLMX[i], posLMY[i]);
    }

    float feat[128];
    FaceFeature *pFF = FaceFeature::getInstance();

    pFF->GetFeatures(matBGR, posLMX, posLMY, feat);
    LOG_DEBUG("It was finished to extract the feature");

    matBGR.release();

    env->SetFloatArrayRegion(features, 0, 128, (jfloat*)feat);

    env->ReleaseFloatArrayElements(landmarksX, tmpLandX, 0);
    env->ReleaseFloatArrayElements(landmarksY, tmpLandY, 0);

    return confidence;
}

JNIEXPORT jfloat JNICALL
FACE_ENGINE_METHOD(nativeSimilarity)(JNIEnv *env, jobject obj, jfloatArray vFeat1, jfloatArray vFeat2, jint feature_len)
{
    if(vFeat1 ==  NULL || vFeat2 == NULL)
        return -1.0f;
    float tSim = -1.0f;
    float *ptFeat1, *ptFeat2;

    jint i;
    ptFeat1 = (float*)env->GetFloatArrayElements(vFeat1, 0);
    if (ptFeat1 == 0)
        return 0;

    ptFeat2 = (float*)env->GetFloatArrayElements(vFeat2, 0);
    if (ptFeat2 == 0)
        return 0;

    FaceFeature *pFF = FaceFeature::getInstance();
    tSim = pFF->GetSimilarity(ptFeat1, feature_len, ptFeat2, feature_len);

    env->ReleaseFloatArrayElements(vFeat1, ptFeat1, 0);
    env->ReleaseFloatArrayElements(vFeat2, ptFeat2, 0);

    //LOG_DEBUG("CalcSimilarity, tSim = %.4f", tSim);//, time=%ld, t);

    tSim += 0.1f;
    if (tSim < 0)
        tSim = 0;
    if (tSim > 1)
        tSim = 1;

    if (tSim > 1)
        tSim = 1.0f;

    return tSim;
}

