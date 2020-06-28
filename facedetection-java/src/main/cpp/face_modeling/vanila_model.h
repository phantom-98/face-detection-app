//
// Created by Dev on 3/24/2020.
//

#ifndef FACE_SDK_ADSDEMO_VANILA_MODEL_H
#define FACE_SDK_ADSDEMO_VANILA_MODEL_H

#include "../include/ncnn/net.h"
#include <string>

class FaceModelExtractor {
public:
    FaceModelExtractor(AAssetManager *assetManager, int input_width, int input_height, int num_thread = 4);

    void extract(float* data_ptr, float* out_ptr);

public:
    int         m_input_width;
    int         m_input_height;
    int         m_input_channel;
private:
    ncnn::Net                   m_model_net;
    std::vector<unsigned char>  m_model_bin;
    std::vector<unsigned char>  m_model_param;
};
#endif //FACE_SDK_ADSDEMO_VANILA_MODEL_H
