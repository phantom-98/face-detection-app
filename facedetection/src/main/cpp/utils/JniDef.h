#ifndef JNI_DEF_H
#define JNI_DEF_H


#include <android/log.h>
#include "base.h"

#define  LOG_TAG    "TestEngine"
#if (DEBUG_EN == 1)
#define  LOGI(...)  __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)
#define  LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)
#else
#define  LOGI(...)  printf("")
#define  LOGE(...)  printf("")
#endif

#define JNI_EXPORT extern "C"


#endif // ARMDEF_H
