
#include <android/asset_manager_jni.h>

typedef struct _tagSMSize
{
    int iH;
    int iW;
}SMSize;

int init_landmark_vanila(AAssetManager *assetManager);
int deinit_landmark_vanila();
void get_face_68Landmark(unsigned char* pbGrayImg, int iW, int iH, int iFaceX, int iFaceY, int iFaceW, int iFaceH, float* landmark_ptr);
void get_face_68Landmark2(unsigned char* pbFace, int iExFaceW, int iExFaceH, float* landmark_ptr);