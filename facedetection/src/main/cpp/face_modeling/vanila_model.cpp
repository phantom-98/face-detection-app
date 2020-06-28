//
// Created by Dev on 3/24/2020.
//

#include <JniDef.h>
#include "base_func.h"
#include "vanila_model.h"
#include "vanl.id.h"

static int g_face_width = 60;
static int g_face_height = 60;
static AlignParam param_model = { 0, 0, 0, 0, 0, 0 };

FaceModelExtractor::FaceModelExtractor(AAssetManager *assetManager, int input_width, int input_height, int num_thread) {

    m_input_channel = 3;
    m_input_width = input_width;
    m_input_height = input_height;

    std::string param = "face_modeling/landmark.param";
    std::string model = "face_modeling/landmark.bin";

    int ret = m_model_net.load_param(assetManager, param.c_str());
    if (ret != 0) {
        LOGE("Face Modeling load param failed.");
        return;
    }

    ret = m_model_net.load_model(assetManager, model.c_str());
    if (ret != 0) {
        LOGE("Face Modeling model failed.");
        return;
    }
    LOGE("Face Modeling init success.");
}

void FaceModelExtractor::extract(float* data_ptr, float* out_ptr) {

    ncnn::Extractor ex = m_model_net.create_extractor();
    ex.set_light_mode(true);
    ex.set_num_threads(4);

    ncnn::Mat in(g_face_width, g_face_height, 1, data_ptr);
    ex.input(vanl_param_id::BLOB_data, in);

    ncnn::Mat out;
    ex.extract(vanl_param_id::BLOB_Dense3, out);

    for (int i = 0; i < 136; i++)
        out_ptr[i] = out[i];
}