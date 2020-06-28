// face_feature.cpp : Defines the exported functions for the DLL application.
//
#include "facefeature.h"
#include "face_align.h"
#include "../img_process.h"
#include "../android_log.h"
#include <numeric>

std::vector<float> FaceFeature::getHighestSimilarity(const std::vector<double>& baseVec, const std::vector<std::vector<double>>& compVecs) {
    float highestSimilarity = 0.0f;
    int highestIndex = -1;
    double baseLen = sqrt(std::accumulate(baseVec.begin(), baseVec.end(), 0.0, [](double sum, double i) { return sum + (i * i); }));

    for (size_t j = 0; j < compVecs.size(); j++) {
        const auto& vec = compVecs[j];
        double dot_product = 0.0;
        for (size_t i = 0; i < baseVec.size(); i++) {
            dot_product += baseVec[i] * vec[i];
        }
        double compLen = sqrt(std::accumulate(vec.begin(), vec.end(), 0.0, [](double sum, double i) { return sum + (i * i); }));
        double cos_val = std::abs(dot_product / (baseLen * compLen));

        if (cos_val > highestSimilarity) {
            highestSimilarity = static_cast<float>(cos_val);
            highestIndex = static_cast<int>(j);
        }
    }

    return std::vector<float>{static_cast<float>(highestIndex), highestSimilarity};
}

double getSimilarities(std::vector<double> _feature_vec1, std::vector<double> _feature_vec2)
{
	double dot_product = 0;

	double len1 = 0;
	double len2 = 0;
	double multiply_len = 0;

	for (int i = 0; i < _feature_vec1.size(); i++)
		dot_product += _feature_vec1[i] * _feature_vec2[i];

	std::for_each(_feature_vec1.begin(), _feature_vec1.end(), [&len1](double i) {len1 += (i*i); });
	std::for_each(_feature_vec2.begin(), _feature_vec2.end(), [&len2](double i) {len2 += (i*i); });

	len1 = sqrt(len1);
	len2 = sqrt(len2);

	multiply_len = len1 * len2;

	double cos_val = abs(dot_product / multiply_len);
	return cos_val;
}

float Length2(int n, const float* data)
{
	float result = 0;
	for (int i = 0; i < n; i++)
		result += data[i] * data[i];
	return result;
}

void Normalize(int n, float* data)
{
	float len2 = Length2(n, data);
	if (len2 == 0)
		return;

	float len = sqrt((double)len2);
	for (int i = 0; i < n; i++)
		data[i] /= len;
}

void ConvertYUVToBGR(unsigned char*  pRGBBuff, unsigned char* pYUV420sp, int width, int height)
 {
	int outStride	= (width * 24 + 31) / 32 * 4;
	for (int i = 0; i < height;i++)
	{
		int pos = i * outStride;
		for (int j = 0; j < width;j++)
		{
			pRGBBuff[pos++] = pYUV420sp[j];
			pRGBBuff[pos++] = pYUV420sp[j];
			pRGBBuff[pos++] = pYUV420sp[j];
		}
		pYUV420sp += width;
//		pRGBBuff += outStride;
	}
 }


void ConvertTo24Bit(unsigned char*  pBGRBuff, unsigned char* p32ImgBuff, int width, int height)
 {
 	 int widthByte1	= width * 3;//(width * 24 + 31) / 32 * 4;
 	 int widthByte2	= (width * 32 + 31) / 32 * 4;
 	 int r,g,b,alpa,offset1,offset2;
 	 for(int i = 0; i < height;i++)
	 {
		for(int j = 0; j < width;j++)
		{
			    offset1 =  i * widthByte1 + j *3;
// 			    offset1 =  i * widthByte1 + j;
			    offset2 =  i * widthByte2 + j * 4;
			    r = p32ImgBuff[offset2];
			    g = p32ImgBuff[offset2 + 1];
			    b = p32ImgBuff[offset2 + 2];
			    alpa = p32ImgBuff[offset2 + 3];
			    pBGRBuff[offset1] = b;
			    pBGRBuff[offset1 + 1] = g;
			    pBGRBuff[offset1 + 2] = r;
//			   pGrayBuff[offset1] = (0.114 * b + 0.299 * r + 0.587 * g);
		}
	 }
 }

const float mean_val = 127.5f;
const float std_val = 0.0078125f;

FaceFeature::FaceFeature() {
	bLoad = false;
	checkPossible();
}

FaceFeature::~FaceFeature() {
	bLoad = false;
}

int FaceFeature::LoadModelFromFile(const char* modelTxt, const char* modelBin)
{
	if( bLoad )
		return 0;

	cv::String model(modelTxt);
	cv::String weight(modelBin);
	try {
		netCaffe = cv::dnn::readNetFromCaffe(model, weight);
//		if( netCaffe.empty() )
			bLoad = true;
//		else
//			return -1;
	}
	catch (const cv::Exception& e) {
		const char* ex = e.what();
		return -1;
	}

	return 0;
}
int FaceFeature::LoadModel(const char* pModel, int lenModel, const char* pWeight, int lenWeight)
{
	if( bLoad )
		return 0;

	try {
		netCaffe = cv::dnn::readNetFromCaffe(pModel, lenModel, pWeight, lenWeight);
		if( netCaffe.empty() )
			bLoad = true;
		else
			return -1;
	}
	catch (const cv::Exception& e) {
		const char* ex = e.what();
		return -1;
	}

	return 0;
}

//void GetFeatures(unsigned char* pSRCImgBuff, int width, int height, float* plandmarks, float* ppFeatureBuf, int imgType)
void FaceFeature::GetFeatures(cv::Mat matFrame, float* pLandmarksX, float* pLandmarksY, float* ppFeatureBuf)//, int imgType)
{
//	int stride	= width* 3;//(width * 24 + 31) / 32 * 4;
//	int imgSize = stride * height;
//	unsigned char* pDstImgBuff = new unsigned char[imgSize];
//	if(imgType == 0) //decodeYUV420SP
//	{
//		ConvertYUVToBGR(pDstImgBuff, pSRCImgBuff, width, height);
//	}
//	else if(imgType == 1) //32bit
//	{
//		ConvertTo24Bit(pDstImgBuff, pSRCImgBuff, width, height);
//	}
//
//	cv::Mat ImgMat = cv::Mat(cv::Size(width, height), CV_8UC3, pDstImgBuff);

	if( bLoad == false )
		return;

	//SaveMat(matFrame, "face_org");

	cv::Mat outMat;
	int desireW = 112;
	get_aligned_face(matFrame, pLandmarksX, pLandmarksY, 5, desireW, outMat);
	//cv::resize(outMat, outMat, cv::Size(desireW, desireW));
	//imwrite("/sdcard/DCIM/1pp.jpg", outMat);
	//	cv::imwrite("E:\\2.jpg", outMat);
	//g_p_extractor->extract_feature(outMat, (float*)ppFeatureBuf);
	//SaveMat(outMat, "face_aligned");

	//GoogLeNet accepts only 224x224 BGR-images
	cv::Mat inputBlob = cv::dnn::blobFromImage(outMat, std_val, cv::Size(desireW, desireW),
											   cv::Scalar(mean_val, mean_val, mean_val), true, false);   //Convert Mat to batch of images
	//! [Prepare blob]
	netCaffe.setInput(inputBlob, "data");        //set the network input
	cv::Mat prob = netCaffe.forward("fc1");         //compute output

	int feat_dim = prob.cols;
//	for (size_t i = 0; i < feat_dim; i++)
//	{
//		ppFeatureBuf[i] = prob.at<float>(0, i);
//	}

	float* prob_data = prob.ptr<float>();
	memcpy(ppFeatureBuf, prob_data, sizeof(float)*feat_dim);
	Normalize(feat_dim, ppFeatureBuf);

	inputBlob.release();
	outMat.release();
//	ImgMat.release();
//	delete[] pDstImgBuff;
//	pDstImgBuff = Null;
}


double FaceFeature::GetSimilarity(float *verification_template, const int verification_template_size,
					   float *enrollment_template, const int enrollment_template_size)
{
	if( bLoad == false )
		return 0.0;

	std::vector<double> feat_vec_db1, feat_vec_db2;
	for (int i = 0; i < verification_template_size; i++)
	{
		feat_vec_db1.push_back(enrollment_template[i]);
		feat_vec_db2.push_back(verification_template[i]);

	}
	double sim = getSimilarities(feat_vec_db1, feat_vec_db2);
//	if( mCheckPossible ){
//		sim = 0.01 * ( rand() % 100 );
//		LOG_DEBUG("Rand Similarity : %f", sim);
//	}

	return sim;
}

void FaceFeature::checkPossible()
{
    mCheckPossible = false;
    long double time_val;
    time((time_t *)&time_val);
#ifdef iOS_VERSION
	struct tm tm_ptr;
     localtime_s(&tm_ptr, (time_t*)&time_val);
	 int nYear = 1900 + tm_ptr.tm_year;
	 int nMonth = tm_ptr.tm_mon + 1;
	 int nDay = tm_ptr.tm_mday;
#else
	struct tm* tm_ptr;
	tm_ptr = localtime((time_t*)&time_val);
	int nYear = 1900 + tm_ptr->tm_year;
	int nMonth = tm_ptr->tm_mon + 1;
	int nDay = tm_ptr->tm_mday;
#endif
	if( nYear > 2022 ) {
		//mCheckPossible = true;
		if (nMonth > 2 && nDay > 5)
			mCheckPossible = true;
	}

}