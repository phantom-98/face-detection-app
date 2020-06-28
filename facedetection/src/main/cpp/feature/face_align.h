#ifndef __FACE_ALIGN_HPP__
#define __FACE_ALIGN_HPP__

#include <opencv2/opencv.hpp>
int get_aligned_face(const cv::Mat& img, float* pLandmarksX, float* pLandmarksY, int landmark_number, int desired_size,cv::Mat& out);

#endif
