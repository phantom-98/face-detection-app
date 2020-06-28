//
// Created by yuanhao on 20-6-12.
//

#ifndef LIVEBODYEXAMPLE_LIVE_H
#define LIVEBODYEXAMPLE_LIVE_H

#include <string>
#include "android_log.h"
//#include <opencv2/core/mat.hpp>
#include "include/ncnn/net.h"
#include "opencv2/highgui.hpp"
#include "opencv2/imgproc.hpp"
#include "opencv2/imgcodecs.hpp"

struct ModelConfig {
    float scale;
    float shift_x;
    float shift_y;
    int height;
    int width;
    std::string name;
    bool org_resize;
};


class Live {
public:
    Live();

    ~Live();

    static Live* getInstance() {
        static Live* instance = nullptr;
        if (instance == nullptr) {
            instance = new Live();
        }
        return instance;
    }

    int LoadModel(AAssetManager *assetManager, std::vector<ModelConfig> &configs);
    void SetCheckLiveness(bool isCheckLiveness);

    float Detect(cv::Mat &src, cv::Rect rtFace);

private:
    cv::Rect CalculateBox(cv::Rect rtFace, int w, int h, ModelConfig &config);

    std::vector<ncnn::Net *> nets_;
    std::vector<ModelConfig> configs_;
    const std::string net_input_name_ = "data";
    const std::string net_output_name_ = "softmax";
    int model_num_;
    int thread_num_;

    bool m_isCheckLiveness;

    ncnn::Option option_;
};

#endif //LIVEBODYEXAMPLE_LIVE_H
