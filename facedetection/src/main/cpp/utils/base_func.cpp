//#include "JniDef.h"
//#include "base_func.h"
//#include <memory>
//#include <math.h>
//#include <algorithm>
//
//float cubic(float rX)
//{
//	float rAbsX = fabs(rX);
//
//	if (rAbsX >= 2)
//	{
//		return 0;
//	}
//
//	float rAbsX2 = rAbsX * rAbsX;
//	float rAbsX3 = rAbsX2 * rAbsX;
//
//	if (rAbsX <= 1)
//	{
//		return (float)(rAbsX3 * 1.5f - rAbsX2 * 2.5 + 1);
//	}
//
//	return (float)(rAbsX3 * (-0.5f) + rAbsX2 * 2.5f - rAbsX * 4 + 2);
//}
//
//template<typename _T>_T* wExpandArray_(_T* pSrc, int nHeight, int nWidth, int nPadWidth, bool fSetZero)
//{
//	int nResultWidth, nResultHeight;
//	nResultWidth = nWidth + nPadWidth * 2;
//	nResultHeight = nHeight + nPadWidth * 2;
//
//	int nXIndex, nYIndex;
//	_T* pResultBuffer = (_T*)malloc(sizeof(_T) * nResultWidth * nResultHeight);
//	memset(pResultBuffer, 0, sizeof(_T) * nResultWidth * nResultHeight);
//
//	for (nYIndex = 0; nYIndex < nResultHeight; nYIndex ++)
//	{
//		if (nYIndex < nPadWidth)
//		{
//			if(!fSetZero)
//			{
//				memcpy(pResultBuffer + (nResultWidth) * nYIndex + nPadWidth, pSrc + nWidth * (nPadWidth - nYIndex), sizeof(_T) * nWidth);
//			}
//		}
//		else if (nYIndex >= nHeight + nPadWidth)
//		{
//			if (!fSetZero)
//			{
//				memcpy(pResultBuffer + (nResultWidth) * nYIndex + nPadWidth, pSrc + nWidth * (nHeight - 2 - (nYIndex - (nHeight + nPadWidth))), sizeof(_T) * nWidth);
//			}
//		}
//		else
//		{
//			memcpy(pResultBuffer + (nResultWidth) * nYIndex + nPadWidth, pSrc + nWidth * (nYIndex - nPadWidth), sizeof(_T) * nWidth);
//		}
//
//		if (!fSetZero)
//		{
//			for (nXIndex = 0; nXIndex < nPadWidth; nXIndex ++)
//			{
//				memcpy(pResultBuffer + (nResultWidth) * nYIndex + nPadWidth - 1 - nXIndex, pResultBuffer + (nResultWidth) * nYIndex + nPadWidth + nXIndex + 1, sizeof(_T));
//				memcpy(pResultBuffer + (nResultWidth) * nYIndex + nPadWidth + nWidth + nXIndex, pResultBuffer + (nResultWidth) * nYIndex + nPadWidth + nWidth - 2 - nXIndex, sizeof(_T));
//			}
//		}
//	}
//	return pResultBuffer;
//}
//
//float* wExpandArray(float* pSrc, int nHeight, int nWidth, int nPadWidth, bool fSetZero)
//{
//	float* pReturnValue = 0;
//	pReturnValue = wExpandArray_(pSrc, nHeight, nWidth, nPadWidth, fSetZero);
//	return pReturnValue;
//}
//
//
//template<typename _T>_T* imgResize_(_T* pSrc, int nHeight, int nWidth, float rScale, float rMin, float rMax)
//{
//	int nScaledHeight, nScaledWidth;
//	int nKernelSize = 4;
//	if (rScale < 1)
//	{
//		nKernelSize = nKernelSize / rScale;
//	}
//	float rDeltaScale = std::min(1.0f, rScale);
//
//	int nP = nKernelSize + 2;
//	nScaledHeight = (int)((float)nHeight * rScale);
//	nScaledWidth = (int)((float)nWidth * rScale);
//
//	_T* pTempScaled = (_T*)malloc(sizeof(_T) * nScaledHeight * nWidth);
//	_T* pReturnScaled = (_T*)malloc(sizeof(_T) * nScaledHeight * nScaledWidth);
//
//// 	int nIndeces[6];
//// 	float rDeltaX[6];
//// 	float rW[6];
//
//	int* nIndeces = (int*)malloc(sizeof(int) * nP);
//	float* rDeltaX = (float*)malloc(sizeof(float) * nP);
//	float* rW = (float*)malloc(sizeof(float) * nP);
//
//	int nX, nY;
//	for (nY = 0; nY < nScaledHeight; nY ++)
//	{
//		float rU = (float)(nY + 1) / rScale +  0.5 * (1.0f - 1.0f/rScale);
//
//		if (rU == (int)rU)
//		{
//			memcpy(pTempScaled + nY * nWidth, pSrc + (int)rU * nWidth, sizeof(_T) * nWidth);
//			continue;
//		}
//
//		int nLeft = floor(rU - nKernelSize / 2);
//// 		int nIndeces[6];
//// 		float rDeltaX[6];
//// 		float rW[6];
//		int nIndiceIndex;
//
//		float rSum = 0;
//
//		for (nIndiceIndex = 0; nIndiceIndex < nP; nIndiceIndex++)
//		{
//			nIndeces[nIndiceIndex] = nLeft + nIndiceIndex;
//			rDeltaX[nIndiceIndex] = (rU - nIndeces[nIndiceIndex]) * rDeltaScale;
//			rW[nIndiceIndex] = cubic(rDeltaX[nIndiceIndex]) * rDeltaScale;
//			rSum += rW[nIndiceIndex];
//		}
//
//		for (nIndiceIndex = 0; nIndiceIndex < nP; nIndiceIndex++)
//		{
//			rW[nIndiceIndex] /= rSum;
//			nIndeces[nIndiceIndex] = std::min(std::max(0, nIndeces[nIndiceIndex] - 1), nHeight - 1);
//		}
//
//		for (nX = 0; nX < nWidth; nX++)
//		{
//			rSum = 0;
//			for (nIndiceIndex = 0; nIndiceIndex < nP; nIndiceIndex++)
//			{
//				rSum += rW[nIndiceIndex] * pSrc[nIndeces[nIndiceIndex] * nWidth + nX];
//			}
//
//
//			//rSum = std::min(std::max(rMin, rSum), rMax);
//			pTempScaled[nY * nWidth + nX] = rSum;
//		}
//	}
//
//	for (nX = 0; nX < nScaledWidth; nX ++)
//	{
//		float rU = (float)(nX + 1) / rScale +  0.5 * (1.0f - 1.0f/rScale);
//
//		if (rU == (int)rU)
//		{
//			for (nY = 0; nY < nScaledHeight; nY++)
//			{
//				pReturnScaled[nY * nScaledWidth + nX] = pTempScaled[nY * nWidth + (int)rU];
//			}
//			continue;
//		}
//
//		int nTop = floor(rU - nKernelSize / 2);
//		int nIndiceIndex;
//
//		float rSum = 0;
//
//		for (nIndiceIndex = 0; nIndiceIndex < nP; nIndiceIndex++)
//		{
//			nIndeces[nIndiceIndex] = nTop + nIndiceIndex;
//			rDeltaX[nIndiceIndex] = (rU - nIndeces[nIndiceIndex]) * rDeltaScale;
//			rW[nIndiceIndex] = cubic(rDeltaX[nIndiceIndex]) * rDeltaScale;
//			rSum += rW[nIndiceIndex];
//		}
//
//		for (nIndiceIndex = 0; nIndiceIndex < nP; nIndiceIndex++)
//		{
//			rW[nIndiceIndex] /= rSum;
//			nIndeces[nIndiceIndex] = std::min(std::max(0, nIndeces[nIndiceIndex] - 1), nWidth - 1);
//		}
//
//		for (nY = 0; nY < nScaledHeight; nY++)
//		{
//			rSum = 0;
//			for (nIndiceIndex = 0; nIndiceIndex < nP; nIndiceIndex++)
//			{
//				rSum += rW[nIndiceIndex] * pTempScaled[nY * nWidth + nIndeces[nIndiceIndex]];
//			}
//
//			rSum = std::min(std::max(rMin, rSum), rMax);
//			pReturnScaled[nY * nScaledWidth + nX] = rSum;
//		}
//	}
//
//	free(pTempScaled);
//	free(nIndeces);
//	free(rW);
//	free(rDeltaX);
//
//	return pReturnScaled;
//}
//
//
//float*  imgResize(float* pSrc, int nHeight, int nWidth, float rScale, float rMin, float rMax)
//{
//	return imgResize_(pSrc, nHeight, nWidth, rScale, rMin, rMax);
//}
//
//int align_vertical(unsigned char* pSrcBuf, int nSrcWidth, int nSrcHeight, unsigned char* pDstBuf, int nDstWidth, int nDstHeight, int nChannelCount, float* landmark_ptr,
//				   float rDistanceEye_Mouth, float rFaceCenterX, float rFaceCenterY)
//{
//	float rConstPoints[5][3] =
//			{
//					{ -25.0167198, -24.2606792, -4.95942402 },
//					{ 24.9832897, -24.2606792, -4.95942402 },
//					{ -19.3741798, 30.3567104, -5.03859901 },
//					{ 19.3408508, 30.3567104, -5.03862381 },
//					{ -0.0166723691, 12.5998402, -18.8323593 },
//			};
//
//	float rEyeCenterX, rEyeCenterY, rMouthCenterX, rMouthCenterY;
//	rMouthCenterX = (rConstPoints[2][0] + rConstPoints[3][0]) / 2;
//	rMouthCenterY = (rConstPoints[2][1] + rConstPoints[3][1]) / 2;
//	rEyeCenterX = (rConstPoints[0][0] + rConstPoints[1][0]) / 2;
//	rEyeCenterY = (rConstPoints[0][1] + rConstPoints[1][1]) / 2;
//
//	float rScale = (float)nDstHeight / 229.39;
//	if (rDistanceEye_Mouth != 0.0f)
//	{
//		if (rMouthCenterY - rEyeCenterY)
//		{
//			rScale = rDistanceEye_Mouth / (rMouthCenterY - rEyeCenterY);
//		}
//		else
//		{
//			rScale = rDistanceEye_Mouth;
//		}
//	}
//
//	ARM_Point3D offset;
//	ARM_Point3D eyeOffset;
//
//	Set_ByARM_Point3D(&eyeOffset, -rEyeCenterX, -rEyeCenterY, 0);
//
//	if (rFaceCenterX == -1.0f || rFaceCenterY == -1.0f)
//	{
//		Set_ByARM_Point3D(&offset, nDstWidth * 0.5f, nDstHeight * 0.5f, 0);
//	}
//	else
//	{
//		Set_ByARM_Point3D(&offset, rFaceCenterX, rFaceCenterY, 0);
//	}
//
//	ARM_SpatialGraph cFaceGraphDest, cFaceGraphSource;
//	cFaceGraphDest.nNodeNum = 5;
//	cFaceGraphSource.nNodeNum = 5;
//
//	int nSelectedPointIndex[2][5] =
//			{
//					{ 0, 1, 10, 11, 15 },
//					{ 1, 0, 11, 10, 15 },
//			};
//
//	int landmark68_id[2][5] = {
//			{36, 42, 48, 54, 33},
//			{39, 45, 60, 64, 33},
//	};
//
//	int i;
//	for (i = 0; i < 5; i++)
//	{
//		float rDestPointX, rDestPointY, rDestPointZ;
//		rDestPointX = rConstPoints[i][0];
//		rDestPointY = rConstPoints[i][1];
//		rDestPointZ = rConstPoints[i][2];
//
//		rDestPointX += eyeOffset.rX;
//		rDestPointY += eyeOffset.rY;
//		rDestPointZ += eyeOffset.rZ;
//
//		rDestPointX *= rScale;
//		rDestPointY *= rScale;
//		rDestPointZ *= rScale;
//
//		rDestPointX += offset.rX;
//		rDestPointY += offset.rY;
//		rDestPointZ += offset.rZ;
//
//		cFaceGraphDest.pxNodes[i].rX = rDestPointX;
//		cFaceGraphDest.pxNodes[i].rY = rDestPointY;
//		cFaceGraphDest.pxNodes[i].rZ = rDestPointZ;
//
//		cFaceGraphSource.pxNodes[i].rX = (landmark_ptr[landmark68_id[0][i] * 2] + landmark_ptr[landmark68_id[1][i] * 2]) / 2;
//		cFaceGraphSource.pxNodes[i].rY = (landmark_ptr[landmark68_id[0][i] * 2 + 1] + landmark_ptr[landmark68_id[1][i] * 2 + 1]) / 2;
//
//		cFaceGraphSource.pxNodes[i].rZ = 0;
//	}
//
//	ARM_LinearTransform2D cTransform2D;
//
//	GetTransform2D_ByARM_SpatialGraph(&cFaceGraphDest, &cFaceGraphSource, &cTransform2D, 7);
//
//	//cFaceGraphDest.GetTransform2D(&cFaceGraphSource, &cTransform2D, 7);
//	ARM_Point3D pTempEyePoint[2];
//	pTempEyePoint[0] = cFaceGraphSource.pxNodes[0];
//	pTempEyePoint[1] = cFaceGraphSource.pxNodes[1];
//
//	Transform_ByARM_LinearTransform2D(&cTransform2D, (ARM_Point2D*)&pTempEyePoint[0]);
//	Transform_ByARM_LinearTransform2D(&cTransform2D, (ARM_Point2D*)&pTempEyePoint[1]);
//
//	float rTempCenterX, rTempCenterY;
//	rTempCenterX = (pTempEyePoint[0].rX + pTempEyePoint[1].rX) / 2;
//	rTempCenterY = (pTempEyePoint[0].rY + pTempEyePoint[1].rY) / 2;
//
//	cTransform2D.xTranPoint.rX += (rFaceCenterX - rTempCenterX);
//	cTransform2D.xTranPoint.rY += (rFaceCenterY - rTempCenterY);
//
//	//GetReverseTransform(&cTransform2D);
//	ReverseMat_ByARM_RoateMat2D(&cTransform2D.xRotateMat);
//	ARM_Point2D pxPoint = Operator_Point2D_Mul_ByARM_RoateMat2D(&cTransform2D.xRotateMat, &cTransform2D.xTranPoint);
//	cTransform2D.xTranPoint.rX = -pxPoint.rX;
//	cTransform2D.xTranPoint.rY = -pxPoint.rY;
//
//	memset(pDstBuf, 0, sizeof(BYTE)* nDstWidth * nDstHeight * nChannelCount);
//
//	BYTE* pProcessDestBuffer;
//	pProcessDestBuffer = pDstBuf;
//
//	int nX, nY;
//	for (nY = 0; nY < nDstHeight; nY++)
//	{
//		for (nX = 0; nX < nDstWidth; nX++)
//		{
//			float rSourceX, rSourceY;
//			ARM_Point2D pointDest, pointSource;
//
//			pointDest.rX = nX;
//			pointDest.rY = nY;
//
//			Transform_ByARM_LinearTransform2D(&cTransform2D, &pointDest);
//
//			pointSource = pointDest;
//			rSourceX = pointSource.rX;
//			rSourceY = pointSource.rY;
//
//			if (rSourceX < 0 || rSourceX > nSrcWidth - 1 || rSourceY < 0 || rSourceY > nSrcHeight - 1)
//			{
//				pProcessDestBuffer += nChannelCount;
//				continue;
//			}
//			else
//			{
//				int nFllorR, nFloorC, nFllorR1, nFloorC1;
//				nFllorR = floor(rSourceY);
//				nFloorC = floor(rSourceX);
//
//				if (nFllorR == rSourceY && nFloorC == rSourceX)
//				{
//					BYTE* pSourceProcessBuffer;
//					pSourceProcessBuffer = pSrcBuf + (nFllorR * nSrcWidth + nFloorC);
//					int nChannelIndex;
//					for (nChannelIndex = 0; nChannelIndex < nChannelCount; nChannelIndex++)
//					{
//						*pProcessDestBuffer = *pSourceProcessBuffer;
//						pProcessDestBuffer++;
//					}
//				}
//				else
//				{
//					float rXRate, rYRate;
//					rYRate = rSourceY - nFllorR;
//					rXRate = rSourceX - nFloorC;
//
//					nFloorC1 = nFloorC + 1;
//					nFllorR1 = nFllorR + 1;
//
//					if (nFloorC == nSrcWidth - 1)
//					{
//						nFloorC1 = nFloorC;
//						rXRate = 0;
//					}
//
//					if (nFllorR == nSrcHeight - 1)
//					{
//						nFllorR1 = nFllorR;
//						rYRate = 0;
//					}
//
//					int nChanelIndex;
//					BYTE *pSourceProcessBuffer1, *pSourceProcessBuffer2, *pSourceProcessBuffer3, *pSourceProcessBuffer4;
//					pSourceProcessBuffer1 = pSrcBuf + (nFllorR1 * nSrcWidth + nFloorC1) * nChannelCount;
//					pSourceProcessBuffer2 = pSrcBuf + (nFllorR * nSrcWidth + nFloorC1) * nChannelCount;
//					pSourceProcessBuffer3 = pSrcBuf + (nFllorR * nSrcWidth + nFloorC) * nChannelCount;
//					pSourceProcessBuffer4 = pSrcBuf + (nFllorR1 * nSrcWidth + nFloorC) * nChannelCount;
//					for (nChanelIndex = 0; nChanelIndex < nChannelCount; nChanelIndex++)
//					{
//						*pProcessDestBuffer = (char)((float)*pSourceProcessBuffer1 * rXRate * rYRate +
//													 (float)*pSourceProcessBuffer2 * rXRate * (1 - rYRate) +
//													 (float)*pSourceProcessBuffer3 * (1 - rXRate) * (1 - rYRate) +
//													 (float)*pSourceProcessBuffer4 * (1 - rXRate) * rYRate);
//
//						pProcessDestBuffer++;
//						pSourceProcessBuffer1++;
//						pSourceProcessBuffer2++;
//						pSourceProcessBuffer3++;
//						pSourceProcessBuffer4++;
//					}
//
//				}
//			}
//		}
//	}
//	return 0;
//}
//
//float cosine_sim(const float* pFeat1, const float* pFeat2, int nLength) {
//
//	float rSum = 0.0f;
//	for (int i = 0; i < nLength; i++)
//		rSum += pFeat1[i] * pFeat2[i];
//	float rDistance = 1 - rSum;
//	if (rDistance < 0)
//		rDistance = 0;
//	else if (rDistance > 2)
//		rDistance = 2;
//	return rDistance;
//}
//
//float get_distance(const float* imgData1, const float* imgData2, int size) {
//	if ((imgData1 == NULL) || (imgData2 == NULL))
//		return -1;
//
//	float rDistance = 0;
//	rDistance = cosine_sim(imgData1, imgData2, size);
//	return rDistance;
//}
//
//void shrink_RGB(unsigned char *src, int src_height, int src_width, unsigned char *dst, int dst_height, int dst_width)
//{
//	int* E05BFF1C_2 = (int*)malloc(0x4000);
//	int* E05C83D4_2 = (int*)malloc(0x4000);//E05C83D4;
//	int nRateXDesToSrc = ((src_width - 1) << 10) / (dst_width - 1);
//	int nRateYDesToSrc = ((src_height - 1) << 10) / (dst_height - 1);
//
//	//LOGE("In shrink 0 = %d, %d", dst_height, dst_width);
//	int nX, nY;
//	if (dst_width > 0)
//	{
//
//		int nSrcX_10 = 0;
//		for (nX = 0; nX < dst_width; nX++)
//		{
//			int nSrcX = nSrcX_10 >> 10;
//			int nDiffX = nSrcX_10 - (nSrcX << 10);
//			int nSrcX_1 = nSrcX + 1;
//			int n1_DiffX = 0x400 - nDiffX;
//
//			if (src_width - 1 <= nSrcX)
//			{
//				E05C83D4_2[(dst_height + nX) * 2] = 0x400;
//				E05BFF1C_2[(dst_height + nX) * 2] = src_width - 1;
//				E05BFF1C_2[(dst_height + nX) * 2 + 1] = src_width - 1;
//				E05C83D4_2[(dst_height + nX) * 2 + 1] = 0;
//			}
//			else
//			{
//				E05BFF1C_2[(dst_height + nX) * 2] = nSrcX;
//				E05C83D4_2[(dst_height + nX) * 2] = n1_DiffX;
//				E05BFF1C_2[(dst_height + nX) * 2 + 1] = nSrcX_1;
//				E05C83D4_2[(dst_height + nX) * 2 + 1] = nDiffX;
//			}
//			nSrcX_10 += nRateXDesToSrc;
//		}
//	}
//
//
//	if (dst_height > 0)
//	{
//		int nSrcY_10 = 0;
//
//		for (nY = 0; nY < dst_height; nY++)
//		{
//			int nSrcY = nSrcY_10 >> 10;
//			int nDiffY = nSrcY_10 - (nSrcY << 10);
//			int n1_DiffY = 0x400 - nDiffY;
//			int nSrcY_1 = nSrcY + 1;
//
//			if (src_height - 1 <= nSrcY)
//			{
//				E05BFF1C_2[nY * 2] = src_height - 1;
//				E05C83D4_2[nY * 2] = 0x400;
//				E05BFF1C_2[nY * 2 + 1] = src_height - 1;
//				E05C83D4_2[nY * 2 + 1] = 0;
//			}
//			else
//			{
//				E05BFF1C_2[nY * 2] = nSrcY;
//				E05C83D4_2[nY * 2] = n1_DiffY;
//				E05BFF1C_2[nY * 2 + 1] = nSrcY_1;
//				E05C83D4_2[nY * 2 + 1] = nDiffY;
//			}
//			nSrcY_10 += nRateYDesToSrc;
//		}
//
//		unsigned char* pDst = dst;
//		for (nY = 0; nY < dst_height; nY++)
//		{
//			int nSrcIndexY1, nSrcIndexY2;
//			nSrcIndexY1 = src_width * E05BFF1C_2[nY * 2];
//			nSrcIndexY2 = src_width * E05BFF1C_2[nY * 2 + 1];
//			int nYAlpha1, nYAlpha2;
//			nYAlpha1 = E05C83D4_2[nY * 2];
//			nYAlpha2 = E05C83D4_2[nY * 2 + 1];
//
//			if (dst_width > 0)
//			{
//				for (nX = 0; nX < dst_width; nX++)
//				{
//					int nSrcIndexX1 = E05BFF1C_2[(dst_height + nX) * 2 + 1];
//					int nSrcIndexX2 = E05BFF1C_2[(dst_height + nX) * 2];
//					int nAlpha1, nAlpha2;
//					nAlpha1 = E05C83D4_2[(dst_height + nX) * 2 + 1];
//					nAlpha2 = E05C83D4_2[(dst_height + nX) * 2];
//
//					int nColorIndex;
//					for (nColorIndex = 0; nColorIndex < 1; nColorIndex++)
//					{
//						*pDst = (nYAlpha2 * src[(nSrcIndexY2 + nSrcIndexX2) * 1 + nColorIndex] * nAlpha2 + nYAlpha2 * src[(nSrcIndexY2 + nSrcIndexX1) * 1 + nColorIndex] * nAlpha1 + nAlpha2 * nYAlpha1 * src[(nSrcIndexY1 + nSrcIndexX2) * 1 + nColorIndex] + nAlpha1 * nYAlpha1 * src[(nSrcIndexY1 + nSrcIndexX1) * 1 + nColorIndex] + 0x80000) >> 20;//STRB		R1, [R11],#1
//						pDst++;
//					}
//				}
//			}
//		}
//
//	}
//
//	free(E05BFF1C_2);
//	free(E05C83D4_2);
//}
//
//int EvalNightImage(unsigned char* pbSrcImage, int nWidth, int nHeight, int* pnImageMean)
//{
//	int nScore = 0, nBrightness = 0, nCount = 0;
//	int i, j, nCurrVal, k = 20;
//	int nHalfW = nWidth >> 1;
//	int nHalfH = nHeight >> 1;
//	int nHalfSize = nHalfW * nHalfH;
//
//	unsigned char* pbDataPtr1, *pbDataPtr2, *pbDataPtr3;
//	unsigned char* pbNextDataPtr = 0;
//
//	unsigned char* pbHalfData = (unsigned char*)malloc((nWidth / 2) * (nHeight / 2));
//	unsigned char* pbDstPtr = pbHalfData;
//	unsigned char* pbSrcPtr1 = pbSrcImage;
//	unsigned char* pbSrcPtr2 = pbSrcImage + nWidth;
//
//	for (j = 0 ; j < nHalfH ; j ++)
//	{
//		for (i = 0 ; i < nHalfW ; i ++)
//		{
//			nCurrVal = (*pbSrcPtr1 + *(pbSrcPtr1 + 1) + *pbSrcPtr2 + *(pbSrcPtr2 + 1) + 2) >> 2;
//			nBrightness += nCurrVal;
//			*pbDstPtr ++ = nCurrVal;
//			pbSrcPtr1 += 2;
//			pbSrcPtr2 += 2;
//		}
//		pbSrcPtr1 += nWidth;
//		pbSrcPtr2 += nWidth;
//	}
//	nBrightness /= nHalfSize;
//	if (pnImageMean)
//		*pnImageMean = nBrightness;
//
//	free(pbHalfData);
//
//	if (nBrightness < 50 && nScore < 5)		// Night Photo
//		return 1;
//
//	return 0;
//}
//
//void ShrinkImage(BYTE *pbInputImg, int nImgWid, int nImgHei, BYTE *pbOutImg, int nOutWid, int nOutHei)
//{
//	BYTE *pbStartAddr, *pbEndAddr;
//	short nHeight, nWidth, nX, nY;
//	{
//		nHeight = nImgHei;
//		nWidth = nImgWid;
//		nX = 0;
//		nY = 0;
//	}
//
//	pbStartAddr = pbInputImg;
//	pbEndAddr = pbInputImg + nImgWid * nImgHei;
//
//	if (nWidth > nOutWid || nHeight > nOutHei)
//	{
//		int i, j, nSum;
//
//		short nShrink;
//		short nRest;
//		short nR, nNR, nSkip, nShift, nC;
//
//		BYTE bNext;
//
//		short nTemp1, nTemp2, nTemp3, nTemp4;
//		BYTE *pbTemp, *pbBuffer1, *pbBuffer2;
//
//		nShrink = nWidth / nOutWid;
//
//		nRest = nWidth - (nOutWid * nShrink);
//
//		for (i = 0; i < nOutHei; i++) {
//			nR = ((short)i) * nHeight / nOutHei;
//			nNR = ((short)(i + 1)) * nHeight / nOutHei;
//
//			bNext = ((nNR - nR) > 1) ? 1 : 0;
//
//			nSkip = nShift = 0;
//
//			pbTemp = (BYTE *)(pbOutImg + nOutWid * i);
//
//			nTemp1 = nY + nR;
//
//			nTemp3 = nTemp1 + bNext;
//
//			pbBuffer1 = pbInputImg + nImgWid * nTemp1;
//			pbBuffer2 = pbInputImg + nImgWid * nTemp3;
//
//			for (j = 0; j < nOutWid; j++, pbTemp++) {
//				nSkip += nRest;
//				nC = ((short)j) * nShrink + nShift;
//
//				nTemp2 = nX + nC;
//				nTemp4 = nTemp2 + 1;
//
//				if ((pbBuffer1 + nTemp2) < pbStartAddr || (pbBuffer1 + nTemp2) >= pbEndAddr ||
//					(pbBuffer2 + nTemp2) < pbStartAddr || (pbBuffer2 + nTemp2) >= pbEndAddr)
//					nSum = 0;
//				else
//					nSum = *((BYTE *)(pbBuffer1 + nTemp2)) + *((BYTE *)(pbBuffer2 + nTemp2));
//				if (nSkip >= nOutWid) {
//					nShift++;
//					nSkip -= nOutWid;
//					if ((pbBuffer1 + nTemp4) < pbStartAddr || (pbBuffer1 + nTemp4) >= pbEndAddr ||
//						(pbBuffer2 + nTemp4) < pbStartAddr || (pbBuffer2 + nTemp4) >= pbEndAddr)
//						nSum = 0;
//					else
//						nSum += *((BYTE *)(pbBuffer1 + nTemp4)) + *((BYTE *)(pbBuffer2 + nTemp4));
//					nSum >>= 2;
//				}
//				else
//					nSum >>= 1;
//				(*pbTemp) = nSum;
//			}
//		}
//	}
//	else {
//		int i, j;
//		int nRestWid = ((nOutWid - nWidth) >> 1);
//		int nRestHei = ((nOutHei - nHeight) >> 1);
//		BYTE *pbInIdx, *pbOutIdx, *pbTemp;
//
//		pbOutIdx = pbOutImg;
//		for (i = 0; i < nOutHei; i++)
//		{
//			for (j = 0; j < nOutWid; j++)
//			{
//				*pbOutIdx++ = 0x20;
//			}
//		}
//
//		pbOutIdx = pbOutImg + nRestHei * nOutWid + nRestWid;
//		pbInputImg += (nX + nY * nImgWid);
//		for (i = 0; i < nHeight; i++) {
//			pbTemp = pbOutIdx;
//			pbInIdx = pbInputImg + i * nImgWid;
//			for (j = 0; j < nWidth; j++) {
//				*pbTemp++ = *pbInIdx++;
//			}
//
//			pbOutIdx += nOutWid;
//		}
//	}
//}
//
//int resize_bilinear(unsigned char* src_img, int w, int h, unsigned char* dst_img, int w2, int h2)
//{
//	int32_t r_a, r_b, r_c, r_d, g_a, g_b, g_c, g_d, b_a, b_b, b_c, b_d;
//	int x, y, index;
//	float x_ratio = ((float)(w - 1)) / w2;
//	float y_ratio = ((float)(h - 1)) / h2;
//	float x_diff, y_diff, blue, red, green;
//	int offset = 0;
//	for (int i = 0; i < h2; i++) {
//		for (int j = 0; j < w2; j++) {
//			x = (int)(x_ratio * j);
//			y = (int)(y_ratio * i);
//			x_diff = (x_ratio * j) - x;
//			y_diff = (y_ratio * i) - y;
//			index = (y*w + x);
//			r_a = src_img[3 * index]; g_a = src_img[3 * index + 1]; b_a = src_img[3 * index + 2];
//			r_b = src_img[3 * (index + 1)]; g_b = src_img[3 * (index + 1) + 1]; b_b = src_img[3 * (index + 1) + 2];
//			r_c = src_img[3 * (index + w)]; g_c = src_img[3 * (index + w) + 1]; b_c = src_img[3 * (index + w) + 2];
//			r_d = src_img[3 * (index + w + 1)]; g_d = src_img[3 * (index + w + 1) + 1]; b_d = src_img[3 * (index + w + 1) + 2];
//
//			// red element
//			red = (r_a)*(1 - x_diff)*(1 - y_diff) + (r_b)*(x_diff)*(1 - y_diff) + (r_c)*(y_diff)*(1 - x_diff) + (r_d)*(x_diff*y_diff);
//
//			// blue element
//			blue = (b_a)*(1 - x_diff)*(1 - y_diff) + (b_b)*(x_diff)*(1 - y_diff) + (b_c)*(y_diff)*(1 - x_diff) + (b_d)*(x_diff*y_diff);
//
//			// green element
//			green = (g_a)*(1 - x_diff)*(1 - y_diff) + (g_b)*(x_diff)*(1 - y_diff) + (g_c)*(y_diff)*(1 - x_diff) + (g_d)*(x_diff*y_diff);
//
//			dst_img[3 * (i * w2 + j)] = (unsigned char)(red);
//			dst_img[3 * (i * w2 + j) + 1] = (unsigned char)(green);
//			dst_img[3 * (i * w2 + j) + 2] = (unsigned char)(blue);
//		}
//	}
//	return 0;
//}
//
//double now_ms(void)
//{
//	struct timeval tv;
//	gettimeofday(&tv, NULL);
//	return tv.tv_sec * 1000. + tv.tv_usec / 1000.;
//}
//
//int CreateDicFiles(const char* szLinkFileName, const char* szDicFolder, AlignParam& param)
//{
//	static char *szDicName[] = { MODEL_WEIGHT_1, MODEL_WEIGHT_2, MODEL_WEIGHT_3 };
//	FILE *fp, *fp_dic;
//	char szDicFileName[256];
//	int anSize[3];
//	fp = fopen(szLinkFileName, "rb");
//	if (!fp) return 0;
//	int nLabel;
//	fread(&nLabel, 4, 1, fp);
//	/* read parameters*/
//	fread(&param.nFeatureSize, 4, 1, fp);
//	fread(&param.nAlignWid, 4, 1, fp);
//	fread(&param.nAlignHei, 4, 1, fp);
//	fread(&param.nEyeMouthDis, 4, 1, fp);
//	fread(&param.nEyeCenterXPos, 4, 1, fp);
//	fread(&param.nEyeCenterYPos, 4, 1, fp);
//
//	fread(&anSize[0], 4, 1, fp);
//	fread(&anSize[1], 4, 1, fp);
//	fread(&anSize[2], 4, 1, fp);
//
//	for (int i = 0; i < 3; i++) {
//		char *buffer = (char *)malloc(anSize[i]);
//		fread(buffer, 1, anSize[i], fp);
//		strcpy(szDicFileName, szDicFolder);
//		strcat(szDicFileName, szDicName[i]);
//		fp_dic = fopen(szDicFileName, "wb");
//		if (!fp) {
//			free(buffer);
//			fclose(fp);
//			return 0;
//		}
//		fwrite(buffer, 1, anSize[i], fp_dic);
//		fclose(fp_dic);
//		free(buffer);
//	}
//	fclose(fp);
//	return 1;
//}
//
//void RemoveDicFiles(const char* szDicPath)
//{
//	static char *szDicName[] = { MODEL_WEIGHT_1, MODEL_WEIGHT_2, MODEL_WEIGHT_3 };
//	char filename[256];
//	for (int i = 0; i < 3; i++)
//	{
//		strcpy(filename, szDicPath);
//		strcat(filename, szDicName[i]);
//		remove(filename);
//	}
//	return;
//}
//
//unsigned char* ReadDicPatch(int &size1, int &size2, const char *szDicPath)
//{
//	char weight_file[256];
//	strcpy(weight_file, szDicPath);
//	strcat(weight_file, MODEL_WEIGHT_1);
//
//	char modelFileName[256];
//	strcpy(modelFileName, szDicPath);
//	strcat(modelFileName, MODEL_WEIGHT_2);
//
//	FILE *fp_weight, *fp_model;
//	fp_weight = fopen(weight_file, "rb");
//	if (!fp_weight)
//		return NULL;
//
//	fp_model = fopen(modelFileName, "rb");
//	if (!fp_model) {
//		fclose(fp_weight);
//		return NULL;
//	}
//
//	fseek(fp_weight, 0L, SEEK_END);
//	size1 = ftell(fp_weight);
//	fseek(fp_weight, 0L, SEEK_SET);
//
//	fseek(fp_model, 0L, SEEK_END);
//	size2 = ftell(fp_model);
//	fseek(fp_model, 0L, SEEK_SET);
//
//	unsigned char *pDic = (unsigned char *)malloc(size1 + size2);
//	fread(pDic, 1, size1, fp_weight);
//	fclose(fp_weight);
//
//	fread(pDic + size1, 1, size2, fp_model);
//	fclose(fp_model);
//
//	int nTotalSize = size1 + size2;
//	for (int i = 0; i < nTotalSize; i++)
//		pDic[i] = ~pDic[i];
//
//	return pDic;
//}
//
//int PrepareDataFile(const char *szDicPath)
//{
//	char trainedFileName[256];
//	strcpy(trainedFileName, szDicPath);
//	strcat(trainedFileName, "/kcc1.tmp");
//
//	char modelFileName[256];
//	strcpy(modelFileName, szDicPath);
//	strcat(modelFileName, "/kcc2.tmp");
//
//	char TFileName[256];
//	strcpy(TFileName, szDicPath);
//	strcat(TFileName, MODEL_WEIGHT_3);
//
//	int size1, size2;
//	unsigned char *pDic = NULL;
//	pDic = ReadDicPatch(size1, size2, szDicPath);
//
//	if (!pDic)
//		return 0;
//
//	/* Read kd3.bin and decode contents*/
//	FILE *fp;
//	fp = fopen(TFileName, "rb"); // kd3.bin
//	if (!fp) {
//		free(pDic);
//		return 0;
//	}
//
//	fseek(fp, 0L, SEEK_END);
//	unsigned long dwSizeOfDic2 = ftell(fp);
//	unsigned char *pDic2 = (unsigned char *)malloc(dwSizeOfDic2);
//	fseek(fp, 0L, SEEK_SET);
//	fread(pDic2, 1, dwSizeOfDic2, fp);
//	fclose(fp);
//
//	/* decode contents*/
//	for (int i = 0; i < dwSizeOfDic2; i++)
//		pDic2[i] = ~pDic2[i];
//	/*Create network model weight file kcc1.tmp */
//	fp = fopen(trainedFileName, "wb"); // kcc1.tmp ; model weight file
//	if (!fp) {
//		free(pDic);
//		free(pDic2);
//		return 0;
//	}
//
//	fwrite(pDic, 1, size1, fp);
//	fwrite(pDic2, 1, dwSizeOfDic2, fp);
//	fclose(fp);
//	free(pDic2);
//	/* Create network model definition file kcc2.tmp */
//	fp = fopen(modelFileName, "wb"); // kcc2.tmp ; model definition file
//	if (!fp) {
//		free(pDic);
//		return 0;
//	}
//
//	fwrite(pDic + size1, 1, size2, fp);
//	fclose(fp);
//	free(pDic);
//	return 1;
//}
//
//void KdnnSetupInputofNet(float* pInput_data, int nSrcChannel, int nDstChannel, unsigned char* psCropedimg, int nWidth, int nHeight)
//{
//	if ((nSrcChannel != 1 && nSrcChannel != 3) || (nDstChannel != 1 && nDstChannel != 3))
//		return;
//
//	float* input_data = pInput_data;
//	int num_channels_ = nDstChannel;
//
//	int nInput_width = nWidth;
//	int nInput_height = nHeight;
//
//	int nIdx = 0;
//	int nChsize = nInput_height * nInput_width;
//	float rVal;
//
//	if (nSrcChannel == 3) {
//		if (num_channels_ == 1) {
//			for (int i = 0; i < nHeight; i++) {
//				for (int j = 0; j < nWidth; j++) {
//					rVal = (0.2989 * psCropedimg[3 * nIdx] +		//r
//							0.5870 * psCropedimg[3 * nIdx + 1] +   //g
//							0.1140* psCropedimg[3 * nIdx + 2]) * KDNN_SCALE; //b
//					input_data[i * nInput_width + j] = rVal;
//					nIdx++;
//				}
//			}
//		}
//		else {
//			for (int i = 0; i < nHeight; i++) {
//				for (int j = 0; j < nWidth; j++) {
//					input_data[i * nInput_width + j] = (float)(psCropedimg[3 * nIdx]) * KDNN_SCALE;//b
//					input_data[nChsize + i * nInput_width + j] = (float)(psCropedimg[3 * nIdx + 1]) * KDNN_SCALE;//g
//					input_data[2 * nChsize + i * nInput_width + j] = (float)(psCropedimg[3 * nIdx + 2]) * KDNN_SCALE;//r
//					nIdx++;
//				}
//			}
//		}
//	}
//	else { //nSrcChannel == 1
//		if (num_channels_ == 1) { //Dst channel == 1
//			int i, j;
//			for (i = 0; i < nHeight; i++) {
//				for (j = 0; j < nWidth; j++) {
//					rVal = psCropedimg[nIdx] * KDNN_SCALE; //gray
//					input_data[nIdx] = rVal;
//					nIdx++;
//				}
//			}
//		}
//		else { //Dst channel == 3
//			for (int i = 0; i < nHeight; i++) {
//				for (int j = 0; j < nWidth; j++) {
//					input_data[i * nInput_width + j] = (float)(psCropedimg[nIdx]) * KDNN_SCALE;//b
//					input_data[nChsize + i * nInput_width + j] = (float)(psCropedimg[nIdx]) * KDNN_SCALE;//g
//					input_data[2 * nChsize + i * nInput_width + j] = (float)(psCropedimg[nIdx]) * KDNN_SCALE;//r
//					nIdx++;
//				}
//			}
//		}
//	}
//}
