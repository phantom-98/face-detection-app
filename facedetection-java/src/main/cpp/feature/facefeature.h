#ifndef __FACE_EXTRACT_HPP__
#define __FACE_EXTRACT_HPP__

//#include <opencv2/opencv.hpp>
#include "opencv2/highgui.hpp"
#include "opencv2/imgproc.hpp"
#include "opencv2/imgcodecs.hpp"
#include <opencv2/dnn.hpp>

//void GetFeatures(unsigned char* pSRCImgBuff, int width, int height, float* plandmarks, float* ppFeatureBuf, int imgType);
void GetFeatures(cv::Mat ImgMat, float* plandmarks, float* ppFeatureBuf, int imgType);
double Match_templates(float *verification_template, const int verification_template_size,
	float *enrollment_template, const int enrollment_template_size);

class FaceFeature {
public:
	FaceFeature();

	~FaceFeature();

	static FaceFeature *getInstance() {
		static FaceFeature *instance = nullptr;
		if (instance == nullptr) {
			instance = new FaceFeature();
		}
		return instance;
	}

	int LoadModelFromFile(const char* modelTxt, const char* modelBin);
	int LoadModel(const char* pModel, int lenModel, const char* pWeight, int lenWeight);
	void GetFeatures(cv::Mat matFrame, float* pLandmarksX, float* pLandmarksY, float* ppFeatureBuf);//, int imgType);
	double GetSimilarity(float *verification_template, const int verification_template_size,
						   float *enrollment_template, const int enrollment_template_size);
    std::vector<float> getHighestSimilarity(const std::vector<double>& baseVec, const std::vector<std::vector<double>>& compVecs);


private:
	cv::dnn::Net netCaffe;
	bool bLoad;
	bool mCheckPossible;

	void checkPossible();

};

#endif //__FACE_EXTRACT_HPP__