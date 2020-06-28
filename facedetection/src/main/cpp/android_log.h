//
// Created by yuanhao on 20-6-10.
//

#ifndef FACEENGINE_ANDROID_LOG_H
#define FACEENGINE_ANDROID_LOG_H

#include <jni.h>
#include <android/log.h>
#include <android/bitmap.h>
#include <android/asset_manager_jni.h>

#define OS_ANDROID
#define LOG_TAG     "FaceEngine"

#define LOG_INFO(...)       __android_log_print(ANDROID_LOG_INFO,   LOG_TAG, __VA_ARGS__)
#define LOG_DEBUG(...)      __android_log_print(ANDROID_LOG_DEBUG,  LOG_TAG, __VA_ARGS__)
#define LOG_WARN(...)       __android_log_print(ANDROID_LOG_WARN,   LOG_TAG, __VA_ARGS__)
#define LOG_ERR(...)        __android_log_print(ANDROID_LOG_ERROR,  LOG_TAG, __VA_ARGS__)


#define FACE_ENGINE_METHOD(METHOD_NAME) \
    Java_com_facecool_attendance_facedetector_FaceEngine_##METHOD_NAME

#endif //FACEENGINE_ANDROID_LOG_H
