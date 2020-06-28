#include "armCommon.h"
#include <math.h>
#include <string.h>
//#include "assert.h"
#include "stdio.h"

float grPan;
float grModelConfidence;
int gnSideFlag;
//int tablePow2[32] =
//{
//	1, 2, 4, 8, 16, 32, 64, 128, 256, 512, 1024, 2048,
//	4096, 8192, 16384, 32768, 65536, 131072, 262144, 524288,
//	1048576, 2097152, 4194304, 8388608, 16777216, 33554432,
//	67108864, 134217728, 268435456, 536870912, 1073741824, 2147483648
//};
int ganArmSquareTable[256]=
{
	0, 1, 4, 9, 16, 25, 36, 49, 64, 81, 100, 121, 144, 169, 196, 225, 256, 289, 324, 361, 400, 441, 484, 529, 576, 625,
		676, 729, 784, 841, 900, 961, 1024, 1089, 1156, 1225, 1296, 1369, 1444, 1521, 1600, 1681, 1764, 1849, 1936, 2025,
		2116, 2209, 2304, 2401, 2500, 2601, 2704, 2809, 2916, 3025, 3136, 3249, 3364, 3481, 3600, 3721, 3844, 3969, 4096,
		4225, 4356, 4489, 4624, 4761, 4900, 5041, 5184, 5329, 5476, 5625, 5776, 5929, 6084, 6241, 6400, 6561, 6724, 6889,
		7056, 7225, 7396, 7569, 7744, 7921, 8100, 8281, 8464, 8649, 8836, 9025, 9216, 9409, 9604, 9801, 10000, 10201, 10404,
		10609, 10816, 11025, 11236, 11449, 11664, 11881, 12100, 12321, 12544, 12769, 12996, 13225, 13456, 13689, 13924, 14161,
		14400, 14641, 14884, 15129, 15376, 15625, 15876, 16129, 16384, 16641, 16900, 17161, 17424, 17689, 17956, 18225, 18496,
		18769, 19044, 19321, 19600, 19881, 20164, 20449, 20736, 21025, 21316, 21609, 21904, 22201, 22500, 22801, 23104, 23409,
		23716, 24025, 24336, 24649, 24964, 25281, 25600, 25921, 26244, 26569, 26896, 27225, 27556, 27889, 28224, 28561, 28900,
		29241, 29584, 29929, 30276, 30625, 30976, 31329, 31684, 32041, 32400, 32761, 33124, 33489, 33856, 34225, 34596, 34969,
		35344, 35721, 36100, 36481, 36864, 37249, 37636, 38025, 38416, 38809, 39204, 39601, 40000, 40401, 40804, 41209, 41616,
		42025, 42436, 42849, 43264, 43681, 44100, 44521, 44944, 45369, 45796, 46225, 46656, 47089, 47524, 47961, 48400, 48841,
		49284, 49729, 50176, 50625, 51076, 51529, 51984, 52441, 52900, 53361, 53824, 54289, 54756, 55225, 55696, 56169, 56644,
		57121, 57600, 58081, 58564, 59049, 59536, 60025, 60516, 61009, 61504, 62001, 62500, 63001, 63504, 64009, 64516, 65025
};

int SquareRootProcess32(int PowerValue)
{
    int		RootValue = 0x0000;		/* 2½£°ßÇµ¶®°Î±á°ª µß ½ó±¨Ãù */
    int		ScaleValue = 0x4000;	/* ±Ëº¤½æ´ª°ª ½ó±¨Ãù */
    int		PowerValue0;	/* ´¸½£°ß°ª */
    int		PowerValue1;	/* ´¸½£°ß°ª */

    /* ±Ëº¤½æ´ª°ªËË ½ó±¨°ªËºµá¹¢À¾ »¤»õÂ×ÊÞ 1 ¸ó³Þ ¿Ð ´´Ê¯ */
    while (ScaleValue>0x0001)
    {
        PowerValue0 = (RootValue | ScaleValue)*(RootValue | ScaleValue);	/* Âï¼õ ´¸½£°ßÇµ¶®°ª±á ±Ëº¤½æ´ª¶¦ ³óÂÙ °ªÌ© ´¸½£°ßË¾ ±Ëº¤Â×°Ö */
        /* °û °ªËË ËÓ°é°ª±á °¯Ëº·² */
        if (PowerValue0 == PowerValue)
        {
            RootValue |= ScaleValue;	/* Âï¼õ ´¸½£°ßÇµ¶®°ªËæ ±Ëº¤½æ´ª°ªË¾ ³óÂÙ Ã¨ */
            break;	/* whileºåÃûËæº· À³Á¬ */
        } /* if */
        /* °û °ªËË ËÓ°é°ª¸ó³Þ »õËº·² */
        else if (PowerValue0<PowerValue)
            RootValue |= ScaleValue;	/* Âï¼õ ´¸½£°ßÇµ¶®°ªËæ ±Ëº¤½æ´ª°ªË¾ ³óÂÙ³Þ. */
        ScaleValue >>= 1;	/* ±Ëº¤½æ´ª°ªË¾ ±½»¦ */
    } /* while */
    PowerValue0 = RootValue*RootValue;	/* Âï¼õ ´¸½£°ßÇµ¶®°ªÌ© ´¸½£°ß±á */
    PowerValue1 = (RootValue | ScaleValue)*(RootValue | ScaleValue);	/* Âï¼õ ´¸½£°ßÇµ¶®°ª±á ±Ëº¤½æ´ª°ªË¾ ³óÂÙ °ªÌ© ´¸½£°ßË¾ ¹¾°äÂ×ÊÞ */
    /* Ë§Ì© ´¸ °ª¼Ú ËÓ°é°ªËæ °¡Ä´Ë© */
    if ((PowerValue - PowerValue0)>(PowerValue1 - PowerValue))
        RootValue |= ScaleValue;	/* ´¸½£°ßÇµ¶®¶¦ ½Ü°Ö */

    return RootValue;	/* ±Ëº¤´ô ´¸½£°ßÇµ¶®°ªË¾ ±ÙÃû */
}

void UpdateSecIntAndSqrIntImage(unsigned char* pbPyramidImage, int* pnIntImage, INT64* pnSqrIntImage, int nHeight, int nWidth)
{
	int i;
	int nX, nY;
	int nLen = nHeight * nWidth;
	int nPWidth = nWidth - 1;
	int nPyramidImageIdx, nIntImageIdx;
	memset(pnIntImage, 0, sizeof(int) * nLen);
	memset(pnSqrIntImage, 0, sizeof(INT64)* nLen);
	for (i = nWidth; i < nLen; i++)
	{
		nY = i / nWidth;
		nX = i % nWidth;
		if (nX)
		{
			nIntImageIdx = nY * nWidth + nX;
			nPyramidImageIdx = (nY - 1) * nPWidth + nX - 1;
			pnIntImage[nIntImageIdx] = pnIntImage[nIntImageIdx - 1] + pnIntImage[nIntImageIdx - nWidth] - pnIntImage[nIntImageIdx - nWidth - 1] + pbPyramidImage[nPyramidImageIdx];
			pnSqrIntImage[nIntImageIdx] = pnSqrIntImage[nIntImageIdx - 1] + pnSqrIntImage[nIntImageIdx - nWidth] - pnSqrIntImage[nIntImageIdx - nWidth - 1] + ganArmSquareTable[pbPyramidImage[nPyramidImageIdx]];
		}
	}
}

void GetHalfImage(unsigned char* pDestImage, int nHeight, int nWidth, unsigned char* pSrcImage, int nSrcWidth)
{
	int i, j;
	unsigned char* pDest = pDestImage;
	unsigned char* pTmpSrc;

	nWidth >>= 1;
	nHeight >>= 1;
	for (i = 0; i < nHeight; i++)
	{
		pTmpSrc = pSrcImage + 2 * i * nSrcWidth;
		for (j = 0; j < nWidth; j++)
		{
			*pDest = (*(pTmpSrc)+*(pTmpSrc + 1) +
				*(pTmpSrc + nSrcWidth) + *(pTmpSrc + nSrcWidth + 1) + 2) / 4;
			pTmpSrc += 2;
			pDest++;
		}
	}
}

void MakeHalfImage(unsigned char* pbImage, int* pnHeight, int* pnWidth)
{
	unsigned char* pbTemp, *pbDest = pbImage;
	int i, j, nSrcWidth = *pnWidth;

	*pnWidth >>= 1;
	*pnHeight >>= 1;
	for (i = 0; i < *pnHeight; i++)
	{
		pbTemp = pbImage + (i << 1) * nSrcWidth;
		for (j = 0; j < *pnWidth; j++)
		{
			*pbDest = (*pbTemp + *(pbTemp + 1) + *(pbTemp + nSrcWidth) + *(pbTemp + nSrcWidth + 1) + 2) >> 2;
			pbTemp += 2;
			pbDest++;
		}
	}
}

void MakeThirdImage(unsigned char* pbImage, int* pnHeight, int* pnWidth)
{
	unsigned char* pbTemp, *pbDest = pbImage;
	int i, j, nSrcWidth = *pnWidth, n2SrcWidth;

	n2SrcWidth = nSrcWidth << 1;
	*pnWidth /= 3;
	*pnHeight /= 3;
	for (i = 0; i < *pnHeight; i++)
	{
		pbTemp = pbImage + (i * 3) * nSrcWidth;
		for (j = 0; j < *pnWidth; j++)
		{
			*pbDest = (*pbTemp + *(pbTemp + 1) + *(pbTemp + 2) +
				*(pbTemp + nSrcWidth) + *(pbTemp + nSrcWidth + 1) + *(pbTemp + nSrcWidth + 2) + 
				*(pbTemp + n2SrcWidth) + *(pbTemp + n2SrcWidth + 1) + *(pbTemp + n2SrcWidth + 2) + 4) / 9;
			pbTemp += 3;
			pbDest++;
		}
	}
}

// ARM_SpatialGraph
void Transform_ByARM_SpatialGraph(ARM_SpatialGraph* psSpatialGraph, ARM_LinearTransform3D* psTransform)
{
	int i;
	for ( i = 0 ; i < psSpatialGraph->nNodeNum ; i++ )
	{
		psSpatialGraph->pxNodes[i] = Operator_Point3D_Mul_ByARM_RotateMat3D(&psTransform->xRotateMat , &psSpatialGraph->pxNodes[i]);
	}
	return;
}

BOOL GetTransform2D_ByARM_SpatialGraph(ARM_SpatialGraph* pxDicFaceGraph, ARM_SpatialGraph *pxInFaceGraph, ARM_LinearTransform2D* psTransform2D, int nAltType)
{
	float rTemp;
	ARM_LinearTransform2D sLinearTransform;
	ARM_Point2D sDiffDic, sDiffIn, sInCenterNode, sDicCenterNode, xTempPoint;
	float rQx, rQy, rPx, rPy, rIxDy, rIyDx, rIyDy, rIxDx, rLengthRate, rFeature[8], prTemp[8];
	ARM_RotateMat2D sRotateMat;
	ARM_Point3D* pxDicPoint, *pxInPoint;
	int nMaxIdx, i;
	
	rQx = rQy = 0;
	rPx = rPy = 0;

	pxDicPoint = pxDicFaceGraph->pxNodes;
	pxInPoint = pxInFaceGraph->pxNodes;
	i = pxInFaceGraph->nNodeNum;
	do 
	{
		i --;
		rQx += pxInPoint->rX;
		rQy += pxInPoint->rY;
		rPx += pxDicPoint->rX;
		rPy += pxDicPoint->rY;
		pxDicPoint ++;
		pxInPoint ++;
	} while (i);
	
	i = pxInFaceGraph->nNodeNum;
	sInCenterNode.rX = rQx / i;
	sInCenterNode.rY = rQy / i;
	sDicCenterNode.rX = rPx / i;
	sDicCenterNode.rY = rPy / i;

	rQx = rPx = rIxDx = rIxDy = rIyDx = rIyDy = 0.0f;			
	pxDicPoint = pxDicFaceGraph->pxNodes;
	pxInPoint = pxInFaceGraph->pxNodes;
	do 
	{
		i --;
		sDiffDic.rX = pxDicPoint->rX - sDicCenterNode.rX;
		sDiffDic.rY = pxDicPoint->rY - sDicCenterNode.rY;
		sDiffIn.rX = pxInPoint->rX - sInCenterNode.rX;
		sDiffIn.rY = pxInPoint->rY - sInCenterNode.rY;
		
		rQx += sDiffIn.rX * sDiffIn.rX + sDiffIn.rY * sDiffIn.rY;
		rPx += sDiffDic.rX * sDiffDic.rX + sDiffDic.rY * sDiffDic.rY;

		rIxDx += sDiffIn.rX * sDiffDic.rX;
		rIxDy += sDiffIn.rX * sDiffDic.rY;
		rIyDx += sDiffIn.rY * sDiffDic.rX;
		rIyDy += sDiffIn.rY * sDiffDic.rY;
		pxDicPoint ++;
		pxInPoint ++;
	} while (i);
	
	if (fabs1(rQx) >= 9.9999997e-21f)
		rLengthRate = sqrtf(rPx / rQx);
	else
		rLengthRate = sqrtf(rPx / 9.999999999999999e-21f);

	rPx = rIxDx + rIyDy;
	rTemp = - (rIxDy + rIyDx);
	rPy = rIyDy - rIxDx;

	if (fabs1(rPx) >= 9.9999997e-21f)
	{
		rQy = (rIxDy - rIyDx) / rPx;
		rQx = sqrtf(1.0f / (rQy * rQy + 1.0f));
		rQy = rQx * rQx;
		rQy = 1 - rQy;
		rQy = sqrtf(rQy);
	}
	else
	{
		rQx = 0.0f;
		rQy = 1.0f;
	}
	
	if (fabs1(rPy) >= 9.9999997e-21f)
	{
		rTemp /= rPy;
		rPx = sqrtf(1.0f / (rTemp * rTemp + 1.0f));
		rTemp = rPx * rPx;
		rTemp = 1 - rTemp;
		rPy = sqrtf(rTemp);
	}
	else
	{
		rPx = 0.0f;
		rPy = 1.0f;
	}

	prTemp[0] = rQx * rIxDx;
	prTemp[1] = rQx * rIyDy;
	prTemp[2] = rQy * rIxDy;
	prTemp[3] = rQy * rIyDx;
	prTemp[4] = rPx * rIxDx;
	prTemp[5] = rPx * rIyDy;
	prTemp[6] = rPy * rIxDy;
	prTemp[7] = rPy * rIyDx;
	rFeature[0] =  prTemp[0] + prTemp[1] + prTemp[2] - prTemp[3];
	rFeature[1] =  prTemp[0] + prTemp[1] - prTemp[2] + prTemp[3];
	rFeature[2] = -prTemp[0] - prTemp[1] + prTemp[2] - prTemp[3];
	rFeature[3] = -prTemp[0] - prTemp[1] - prTemp[2] + prTemp[3];
	rFeature[4] = -prTemp[4] + prTemp[5] - prTemp[6] - prTemp[7];
	rFeature[5] = -prTemp[4] + prTemp[5] + prTemp[6] + prTemp[7];
	rFeature[6] =  prTemp[4] - prTemp[5] - prTemp[6] - prTemp[7];
	rFeature[7] =  prTemp[4] - prTemp[5] + prTemp[6] + prTemp[7];
	/*
	rFeature[0] =  rQx * rIxDx + rQx * rIyDy + rQy * rIxDy - rQy * rIyDx;
	rFeature[1] =  rQx * rIxDx + rQx * rIyDy - rQy * rIxDy + rQy * rIyDx;
	rFeature[2] = -rQx * rIxDx - rQx * rIyDy + rQy * rIxDy - rQy * rIyDx;
	rFeature[3] = -rQx * rIxDx - rQx * rIyDy - rQy * rIxDy + rQy * rIyDx;
	rFeature[4] = -rPx * rIxDx + rPx * rIyDy - rPy * rIxDy - rPy * rIyDx;
	rFeature[5] = -rPx * rIxDx + rPx * rIyDy + rPy * rIxDy + rPy * rIyDx;
	rFeature[6] =  rPx * rIxDx - rPx * rIyDy - rPy * rIxDy - rPy * rIyDx;
	rFeature[7] =  rPx * rIxDx - rPx * rIyDy + rPy * rIxDy + rPy * rIyDx;
	*/
	rIyDx = -4.2949673e9f;
	nMaxIdx = 0;
	for (i = 0; i < 8; i++)
	{
		if (rIyDx < rFeature[i])
		{
			rIyDx = rFeature[i];
			nMaxIdx = i;
		}
	}

	switch (nMaxIdx)
	{
	case 0 :
		sRotateMat.prValue[0] = rQx;
		sRotateMat.prValue[1] = -rQy;
		sRotateMat.prValue[2] = rQy;
		sRotateMat.prValue[3] = rQx;
		break;
	case 1 :
		sRotateMat.prValue[0] = rQx;
		sRotateMat.prValue[1] = rQy;
		sRotateMat.prValue[2] = -rQy;
		sRotateMat.prValue[3] = rQx;
		break;
	case 2 :
		sRotateMat.prValue[0] = -rQx;
		sRotateMat.prValue[1] = -rQy;
		sRotateMat.prValue[2] = rQy;
		sRotateMat.prValue[3] = -rQx;
		break;
	case 3 :
		sRotateMat.prValue[0] = -rQx;
		sRotateMat.prValue[1] = rQy;
		sRotateMat.prValue[2] = -rQy;
		sRotateMat.prValue[3] = -rQx;
		break;
	case 4 :
		sRotateMat.prValue[0] = -rPx;
		sRotateMat.prValue[1] = -rPy;
		sRotateMat.prValue[2] = -rPy;
		sRotateMat.prValue[3] = rPx;
		break;
	case 5 :
		sRotateMat.prValue[0] = -rPx;
		sRotateMat.prValue[1] = rPy;
		sRotateMat.prValue[2] = rPy;
		sRotateMat.prValue[3] = rPx;
		break;
	case 6 :
		sRotateMat.prValue[0] = rPx;
		sRotateMat.prValue[1] = -rPy;
		sRotateMat.prValue[2] = -rPy;
		sRotateMat.prValue[3] = -rPx;
		break;
	case 7 :
		sRotateMat.prValue[0] = rPx;
		sRotateMat.prValue[1] = rPy;
		sRotateMat.prValue[2] = rPy;
		sRotateMat.prValue[3] = -rPx;
		break;
	default :
		break;
	}

	xTempPoint.rX = (sRotateMat.prValue[0] * sInCenterNode.rX + sRotateMat.prValue[1] * sInCenterNode.rY) * rLengthRate;
	xTempPoint.rY = (sRotateMat.prValue[2] * sInCenterNode.rX + sRotateMat.prValue[3] * sInCenterNode.rY) * rLengthRate;

	sDiffDic.rX = sDicCenterNode.rX - xTempPoint.rX;
	sDiffDic.rY = sDicCenterNode.rY - xTempPoint.rY;
	
	sLinearTransform.xRotateMat.prValue[0] = sRotateMat.prValue[0] * rLengthRate;
	sLinearTransform.xRotateMat.prValue[1] = sRotateMat.prValue[1] * rLengthRate;
	sLinearTransform.xRotateMat.prValue[2] = sRotateMat.prValue[2] * rLengthRate;
	sLinearTransform.xRotateMat.prValue[3] = sRotateMat.prValue[3] * rLengthRate;

	sLinearTransform.xTranPoint = sDiffDic;
	//sLinearTransform.xTranPoint.rX = sDiffDic.rX;
	//sLinearTransform.xTranPoint.rY = sDiffDic.rY;
	*psTransform2D = sLinearTransform;	
	return 1;
}

BOOL GetTransform3D_ByARM_SpatialGraph(ARM_SpatialGraph* pxInFaceGraph, ARM_SpatialGraph* pxDicFaceGraph, ARM_LinearTransform3D* pxTransform3D)
{
	float rValue1, rValue2, rValue3, rValue4, rValue5, rValue6, rValue7;
	ARM_Point3D xPoint1, xPoint2, xPoint3, xPoint4, xPoint5, xDicCenterNode;
	ARM_LinearTransform3D xTransform1, xTransform2;
	ARM_Point2D xInCenterNode;
	int i;
	
	xDicCenterNode.rX = 0;
	xDicCenterNode.rY = 0;
	xDicCenterNode.rZ = 0;
	xInCenterNode.rX = 0;
	xInCenterNode.rY = 0;
	for ( i = 0; i < pxDicFaceGraph->nNodeNum; i++ )
	{
		xDicCenterNode.rX += pxDicFaceGraph->pxNodes[i].rX;
		xDicCenterNode.rY += pxDicFaceGraph->pxNodes[i].rY;
		xDicCenterNode.rZ += pxDicFaceGraph->pxNodes[i].rZ;
		
		xInCenterNode.rX += pxInFaceGraph->pxNodes[i].rX;
		xInCenterNode.rY += pxInFaceGraph->pxNodes[i].rY;
	}
	xDicCenterNode.rX /= pxDicFaceGraph->nNodeNum;
	xDicCenterNode.rY /= pxDicFaceGraph->nNodeNum;
	xDicCenterNode.rZ /= pxDicFaceGraph->nNodeNum;
	xInCenterNode.rX /= pxInFaceGraph->nNodeNum;
	xInCenterNode.rY /= pxInFaceGraph->nNodeNum;		

	rValue1 = 0.0f;  rValue2 = 0.0f; rValue6 = 0.0f;
	rValue5 = 0.0f;  rValue3 = 0.0f; rValue4 = 0.0f;
	for (i = 0; i < pxDicFaceGraph->nNodeNum; i ++)
	{
		xPoint1.rX = pxDicFaceGraph->pxNodes[i].rX - xDicCenterNode.rX;
		xPoint1.rY = pxDicFaceGraph->pxNodes[i].rY - xDicCenterNode.rY;
		xPoint1.rZ = pxDicFaceGraph->pxNodes[i].rZ - xDicCenterNode.rZ;

		rValue1 += xPoint1.rX * xPoint1.rX;
		rValue4 += xPoint1.rX * xPoint1.rY;
		rValue3 += xPoint1.rX * xPoint1.rZ;
		rValue6 += xPoint1.rY * xPoint1.rY;
		rValue5 += xPoint1.rY * xPoint1.rZ;
		rValue2 += xPoint1.rZ * xPoint1.rZ;
	}

	Set_ByARM_RotateMat3D(&xTransform1.xRotateMat, rValue1, rValue4, rValue3, rValue4, rValue6, rValue5, rValue3, rValue5, rValue2);
	
	rValue1 = 0.0f;  rValue2 = 0.0f; rValue6 = 0.0f;
	rValue5 = 0.0f;  rValue3 = 0.0f; rValue4 = 0.0f;

	for (i = 0; i < pxInFaceGraph->nNodeNum; i ++)
	{
		xPoint1.rX = pxDicFaceGraph->pxNodes[i].rX - xDicCenterNode.rX;
		xPoint1.rY = pxDicFaceGraph->pxNodes[i].rY - xDicCenterNode.rY;
		xPoint1.rZ = pxDicFaceGraph->pxNodes[i].rZ - xDicCenterNode.rZ;

		xPoint2.rX = pxInFaceGraph->pxNodes[i].rX - xInCenterNode.rX;
		xPoint2.rY = pxInFaceGraph->pxNodes[i].rY - xInCenterNode.rY;

		rValue1 += xPoint1.rX * xPoint2.rX;
		rValue4 += xPoint1.rY * xPoint2.rX;
		rValue3 += xPoint1.rZ * xPoint2.rX;
		rValue2 += xPoint1.rX * xPoint2.rY;
		rValue6 += xPoint1.rY * xPoint2.rY;
		rValue5 += xPoint1.rZ * xPoint2.rY;
	}

	Set_ByARM_RotateMat3D(&xTransform2.xRotateMat, rValue1, rValue4, rValue3, rValue2, rValue6, rValue5, 0, 0, 0);
	ReverseMat_ByARM_RotateMat3D(&xTransform1.xRotateMat);

	Operator_RotateMat3D_Mul_Assign_ByARM_RotateMat3D(&xTransform2.xRotateMat, &xTransform1.xRotateMat);

	Set_ByARM_Point3D(&xPoint1, xTransform2.xRotateMat.prValue[0], xTransform2.xRotateMat.prValue[1], xTransform2.xRotateMat.prValue[2]);
	Set_ByARM_Point3D(&xPoint2, xTransform2.xRotateMat.prValue[3], xTransform2.xRotateMat.prValue[4], xTransform2.xRotateMat.prValue[5]);

	xPoint3.rX = xPoint1.rX + xPoint2.rX;
	xPoint3.rY = xPoint1.rY + xPoint2.rY;
	xPoint3.rZ = xPoint1.rZ + xPoint2.rZ;

	rValue7 = xPoint3.rX * xPoint3.rX + xPoint3.rY * xPoint3.rY + xPoint3.rZ * xPoint3.rZ;
	
	if ((int)rValue7 != 0)
	{
		float rBuf = 0.0f;
		rBuf += xPoint1.rX * xPoint2.rX;
		rBuf += xPoint1.rY * xPoint2.rY;
		rBuf += xPoint1.rZ * xPoint2.rZ;

		rValue7 = rBuf / rValue7;
		rValue2 = 0.25f - rValue7;
		
		rValue7 = 0.0f;
		if (rValue2  <= 0)
			rValue7 = 0.5f - sqrtf(rValue7);
		else
			rValue7 = 0.5f - sqrtf(rValue2);

		xPoint3.rX *= rValue7;
		xPoint3.rY *= rValue7;
		xPoint3.rZ *= rValue7;
		xPoint1.rX -= xPoint3.rX;
		xPoint1.rY -= xPoint3.rY;
		xPoint1.rZ -= xPoint3.rZ;
		xPoint2.rX -= xPoint3.rX;
		xPoint2.rY -= xPoint3.rY;
		xPoint2.rZ -= xPoint3.rZ;
	}
	
	rValue2 = 0.0f;
	rValue1 = 0.0f;

	rValue2 += xPoint2.rX * xPoint2.rX;
	rValue2 += xPoint2.rY * xPoint2.rY;
	rValue2 += xPoint2.rZ * xPoint2.rZ;
	rValue1 += xPoint1.rX * xPoint1.rX;
	rValue1 += xPoint1.rY * xPoint1.rY;
	rValue1 += xPoint1.rZ * xPoint1.rZ;

	rValue7 = (rValue2 + rValue1) * 0.5f;
	rValue7 = sqrtf(rValue7);                     //.text:100F768D
	
	if ((int)rValue1 != 0)
		Normalize_ByARM_Point3D(&xPoint1);
	if ((int)rValue2 != 0)
		Normalize_ByARM_Point3D(&xPoint2);

	xPoint4.rX = xPoint1.rY * xPoint2.rZ - xPoint1.rZ * xPoint2.rY;
	xPoint4.rY = xPoint1.rZ * xPoint2.rX - xPoint1.rX * xPoint2.rZ;
	xPoint4.rZ = xPoint1.rX * xPoint2.rY - xPoint1.rY * xPoint2.rX;

	Set_ByARM_RotateMat3D(&xTransform1.xRotateMat, xPoint1.rX * rValue7, xPoint1.rY * rValue7, xPoint1.rZ * rValue7,
										xPoint2.rX * rValue7, xPoint2.rY * rValue7, xPoint2.rZ * rValue7,
										xPoint4.rX * rValue7, xPoint4.rY * rValue7, xPoint4.rZ * rValue7);
	
	xPoint4 = Operator_Point3D_Mul_ByARM_RotateMat3D(&xTransform1.xRotateMat, &xDicCenterNode);

	xPoint5.rX = xInCenterNode.rX;
	xPoint5.rY = xInCenterNode.rY;

	xPoint4.rX = xPoint5.rX - xPoint4.rX;
	xPoint4.rY = xPoint5.rY - xPoint4.rY;
	xPoint4.rZ = 0.f;

	pxTransform3D->xRotateMat = xTransform1.xRotateMat;
	pxTransform3D->xTranPoint = xPoint4;
	return 1;
}

void SelectNodes_ByARM_SpatialGraph(ARM_SpatialGraph* psSpatialGraph, ARM_SpatialGraph* pxFaceGraph, int *pnOrder, int nNum)
{
	int i;
	psSpatialGraph->nNodeNum = nNum;
	i = nNum;
	do 
	{
		i --;
		*(psSpatialGraph->pxNodes + i) = *(pxFaceGraph->pxNodes + *(pnOrder + i));
	} while (i);
}

void Set_ByARM_Point3D(ARM_Point3D* pxPoint, float X, float Y, float Z)
{
	pxPoint->rX = X;
	pxPoint->rY = Y;
	pxPoint->rZ = Z;
}

void Operator_Assign_ByARM_Point3D(ARM_Point3D* pxPoint, ARM_Point3D* pxUpdatePoint)
{
	pxPoint->rX = pxUpdatePoint->rX;
	pxPoint->rY = pxUpdatePoint->rY;
	pxPoint->rZ = pxUpdatePoint->rZ;
}

void Normalize_ByARM_Point3D(ARM_Point3D* pxPoint)
{
	float rTemp = sqrtf(pxPoint->rX * pxPoint->rX + pxPoint->rY * pxPoint->rY + pxPoint->rZ * pxPoint->rZ);
	if( rTemp != 0.0f )
	{
		float rMul = 1 / rTemp;
		pxPoint->rX *= rMul;
		pxPoint->rY *= rMul;
		pxPoint->rZ *= rMul;
	}
} 

// ARM_RotateMat2D
ARM_Point2D Operator_Point2D_Mul_ByARM_RoateMat2D(ARM_RotateMat2D* psRotateMat2D, ARM_Point2D* pxPoint)
{
	ARM_Point2D xOutPoint;
	xOutPoint.rX = psRotateMat2D->prValue[0] * pxPoint->rX + psRotateMat2D->prValue[1] * pxPoint->rY;
	xOutPoint.rY = psRotateMat2D->prValue[2] * pxPoint->rX + psRotateMat2D->prValue[3] * pxPoint->rY;	
	return xOutPoint;
}

void ReverseMat_ByARM_RoateMat2D(ARM_RotateMat2D* psRotateMat2D)
{
	float rDet, rTemp;
	
	rDet = psRotateMat2D->prValue[0] * psRotateMat2D->prValue[3] - psRotateMat2D->prValue[2] * psRotateMat2D->prValue[1];
	
	if (rDet != 0.0f)
	{
		rDet = 1.0f / rDet;
		rTemp = psRotateMat2D->prValue[0];
		psRotateMat2D->prValue[0] = psRotateMat2D->prValue[3] * rDet;
		psRotateMat2D->prValue[3] = rTemp * rDet;
		psRotateMat2D->prValue[1] *= -rDet;
		psRotateMat2D->prValue[2] *= -rDet;
	}
	else
	{
		memset(psRotateMat2D->prValue, 0, sizeof(float) * 4);
	}
}

void Set_ByARM_RoateMat2D(ARM_RotateMat2D* psRotateMat2D, float r1, float r2, float r3, float r4)
{
	psRotateMat2D->prValue[0] = r1;
	psRotateMat2D->prValue[1] = r2;
	psRotateMat2D->prValue[2] = r3;
	psRotateMat2D->prValue[3] = r4;
}

// ARM_RotateMat3D
ARM_RotateMat3D Operator_FloatMul_ByARM_RotateMat3D(ARM_RotateMat3D* psRotateMat3D, float rMul)
{
	ARM_RotateMat3D xRetMat;

	xRetMat.prValue[0] = psRotateMat3D->prValue[0] * rMul;
	xRetMat.prValue[1] = psRotateMat3D->prValue[1] * rMul;
	xRetMat.prValue[2] = psRotateMat3D->prValue[2] * rMul;
	xRetMat.prValue[3] = psRotateMat3D->prValue[3] * rMul;
	xRetMat.prValue[4] = psRotateMat3D->prValue[4] * rMul;
	xRetMat.prValue[5] = psRotateMat3D->prValue[5] * rMul;
	xRetMat.prValue[6] = psRotateMat3D->prValue[6] * rMul;
	xRetMat.prValue[7] = psRotateMat3D->prValue[7] * rMul;
	xRetMat.prValue[8] = psRotateMat3D->prValue[8] * rMul;
	return xRetMat;
}

ARM_RotateMat3D Operator_RotateMat3D_Mul_ByARM_RotateMat3D(ARM_RotateMat3D* psRotateMat3D, ARM_RotateMat3D* pxMulRotateMat3D)
{
	ARM_RotateMat3D xOutRotateMat3D;
	xOutRotateMat3D.prValue[0] = psRotateMat3D->prValue[0] * pxMulRotateMat3D->prValue[0] + psRotateMat3D->prValue[1] * pxMulRotateMat3D->prValue[3] + psRotateMat3D->prValue[2] * pxMulRotateMat3D->prValue[6];
	xOutRotateMat3D.prValue[1] = psRotateMat3D->prValue[0] * pxMulRotateMat3D->prValue[1] + psRotateMat3D->prValue[1] * pxMulRotateMat3D->prValue[4] + psRotateMat3D->prValue[2] * pxMulRotateMat3D->prValue[7];
	xOutRotateMat3D.prValue[2] = psRotateMat3D->prValue[0] * pxMulRotateMat3D->prValue[2] + psRotateMat3D->prValue[1] * pxMulRotateMat3D->prValue[5] + psRotateMat3D->prValue[2] * pxMulRotateMat3D->prValue[8];
	xOutRotateMat3D.prValue[3] = psRotateMat3D->prValue[3] * pxMulRotateMat3D->prValue[0] + psRotateMat3D->prValue[4] * pxMulRotateMat3D->prValue[3] + psRotateMat3D->prValue[5] * pxMulRotateMat3D->prValue[6];
	xOutRotateMat3D.prValue[4] = psRotateMat3D->prValue[3] * pxMulRotateMat3D->prValue[1] + psRotateMat3D->prValue[4] * pxMulRotateMat3D->prValue[4] + psRotateMat3D->prValue[5] * pxMulRotateMat3D->prValue[7];
	xOutRotateMat3D.prValue[5] = psRotateMat3D->prValue[3] * pxMulRotateMat3D->prValue[2] + psRotateMat3D->prValue[4] * pxMulRotateMat3D->prValue[5] + psRotateMat3D->prValue[5] * pxMulRotateMat3D->prValue[8];
	xOutRotateMat3D.prValue[6] = psRotateMat3D->prValue[6] * pxMulRotateMat3D->prValue[0] + psRotateMat3D->prValue[7] * pxMulRotateMat3D->prValue[3] + psRotateMat3D->prValue[8] * pxMulRotateMat3D->prValue[6];
	xOutRotateMat3D.prValue[7] = psRotateMat3D->prValue[6] * pxMulRotateMat3D->prValue[1] + psRotateMat3D->prValue[7] * pxMulRotateMat3D->prValue[4] + psRotateMat3D->prValue[8] * pxMulRotateMat3D->prValue[7];
	xOutRotateMat3D.prValue[8] = psRotateMat3D->prValue[6] * pxMulRotateMat3D->prValue[2] + psRotateMat3D->prValue[7] * pxMulRotateMat3D->prValue[5] + psRotateMat3D->prValue[8] * pxMulRotateMat3D->prValue[8];
	return xOutRotateMat3D;
}

void Operator_RotateMat3D_Plus_Assign_ByARM_RotateMat3D(ARM_RotateMat3D* psRotateMat3D, ARM_RotateMat3D* pxPlusRotateMat3D)
{
	psRotateMat3D->prValue[0] += pxPlusRotateMat3D->prValue[0];
	psRotateMat3D->prValue[1] += pxPlusRotateMat3D->prValue[1];
	psRotateMat3D->prValue[2] += pxPlusRotateMat3D->prValue[2];
	psRotateMat3D->prValue[3] += pxPlusRotateMat3D->prValue[3];
	psRotateMat3D->prValue[4] += pxPlusRotateMat3D->prValue[4];
	psRotateMat3D->prValue[5] += pxPlusRotateMat3D->prValue[5];
	psRotateMat3D->prValue[6] += pxPlusRotateMat3D->prValue[6];
	psRotateMat3D->prValue[7] += pxPlusRotateMat3D->prValue[7];
	psRotateMat3D->prValue[8] += pxPlusRotateMat3D->prValue[8];
}

void Operator_RotateMat3D_Mul_Assign_ByARM_RotateMat3D(ARM_RotateMat3D* psRotateMat3D, ARM_RotateMat3D* pxMulRoatateMat3D)
{
	float r1, r2, r3, r4, r5, r6, r7, r8, r9;
	r1 = psRotateMat3D->prValue[0] * pxMulRoatateMat3D->prValue[0] + psRotateMat3D->prValue[1] * pxMulRoatateMat3D->prValue[3]
		+ psRotateMat3D->prValue[2] * pxMulRoatateMat3D->prValue[6];
	
	r2 = psRotateMat3D->prValue[1] * pxMulRoatateMat3D->prValue[4] + psRotateMat3D->prValue[0] * pxMulRoatateMat3D->prValue[1]
		+ psRotateMat3D->prValue[2] * pxMulRoatateMat3D->prValue[7];
	
	r3 = psRotateMat3D->prValue[0] * pxMulRoatateMat3D->prValue[2] + psRotateMat3D->prValue[1] * pxMulRoatateMat3D->prValue[5]
		+ psRotateMat3D->prValue[2] * pxMulRoatateMat3D->prValue[8];
	
	r4 = psRotateMat3D->prValue[4] * pxMulRoatateMat3D->prValue[3] + psRotateMat3D->prValue[3] * pxMulRoatateMat3D->prValue[0] 
		+ psRotateMat3D->prValue[5] * pxMulRoatateMat3D->prValue[6];
	
	r5 = psRotateMat3D->prValue[4] * pxMulRoatateMat3D->prValue[4] + psRotateMat3D->prValue[3] * pxMulRoatateMat3D->prValue[1]
		+ psRotateMat3D->prValue[5] * pxMulRoatateMat3D->prValue[7];
	
	r6 = psRotateMat3D->prValue[4] * pxMulRoatateMat3D->prValue[5] + psRotateMat3D->prValue[3] * pxMulRoatateMat3D->prValue[2]
		+ psRotateMat3D->prValue[5] * pxMulRoatateMat3D->prValue[8];
	
	r7 = psRotateMat3D->prValue[6] * pxMulRoatateMat3D->prValue[0] + psRotateMat3D->prValue[7] * pxMulRoatateMat3D->prValue[3]
		+ psRotateMat3D->prValue[8] * pxMulRoatateMat3D->prValue[6];
	
	r8 = psRotateMat3D->prValue[7] * pxMulRoatateMat3D->prValue[4] + psRotateMat3D->prValue[6] * pxMulRoatateMat3D->prValue[1] 
		+ psRotateMat3D->prValue[8] * pxMulRoatateMat3D->prValue[7];
	
	r9 = psRotateMat3D->prValue[6] * pxMulRoatateMat3D->prValue[2] + psRotateMat3D->prValue[7] * pxMulRoatateMat3D->prValue[5]
		+ psRotateMat3D->prValue[8] * pxMulRoatateMat3D->prValue[8];
	psRotateMat3D->prValue[0] = r1;
	psRotateMat3D->prValue[1] = r2;
	psRotateMat3D->prValue[2] = r3;
	psRotateMat3D->prValue[3] = r4;
	psRotateMat3D->prValue[4] = r5;
	psRotateMat3D->prValue[5] = r6;
	psRotateMat3D->prValue[6] = r7;
	psRotateMat3D->prValue[7] = r8;
	psRotateMat3D->prValue[8] = r9;
}

ARM_Point3D Operator_Point3D_Mul_ByARM_RotateMat3D(ARM_RotateMat3D* psRotateMat3D, ARM_Point3D* pxMulPoint)
{
	ARM_Point3D xOutPoint;
	xOutPoint.rX = psRotateMat3D->prValue[0] * pxMulPoint->rX + psRotateMat3D->prValue[1] * pxMulPoint->rY + psRotateMat3D->prValue[2] * pxMulPoint->rZ;
	xOutPoint.rY = psRotateMat3D->prValue[3] * pxMulPoint->rX + psRotateMat3D->prValue[4] * pxMulPoint->rY + psRotateMat3D->prValue[5] * pxMulPoint->rZ;
	xOutPoint.rZ = psRotateMat3D->prValue[6] * pxMulPoint->rX + psRotateMat3D->prValue[7] * pxMulPoint->rY + psRotateMat3D->prValue[8] * pxMulPoint->rZ;
	return xOutPoint;
}

void  ReverseMat_ByARM_RotateMat3D(ARM_RotateMat3D* psRotateMat3D)
{
	float rDet;
	rDet = GetDeterminant_ByARM_RotateMat3D(psRotateMat3D);
	if (rDet != 0)
	{
		float r1, r2, r3, r4, r5, r6, r7, r8, r9;
		rDet = 1 / rDet;
		r1 = (psRotateMat3D->prValue[8] * psRotateMat3D->prValue[4] - psRotateMat3D->prValue[7] * psRotateMat3D->prValue[5]) * rDet;
		r2 = (psRotateMat3D->prValue[7] * psRotateMat3D->prValue[2] - psRotateMat3D->prValue[1] * psRotateMat3D->prValue[8]) * rDet;
		r3 = (psRotateMat3D->prValue[5] * psRotateMat3D->prValue[1] - psRotateMat3D->prValue[4] * psRotateMat3D->prValue[2]) * rDet;
		r4 = (psRotateMat3D->prValue[5] * psRotateMat3D->prValue[6] - psRotateMat3D->prValue[3] * psRotateMat3D->prValue[8]) * rDet;
		r5 = (psRotateMat3D->prValue[8] * psRotateMat3D->prValue[0] - psRotateMat3D->prValue[6] * psRotateMat3D->prValue[2]) * rDet;
		r6 = (psRotateMat3D->prValue[3] * psRotateMat3D->prValue[2] - psRotateMat3D->prValue[5] * psRotateMat3D->prValue[0]) * rDet;
		r7 = (psRotateMat3D->prValue[7] * psRotateMat3D->prValue[3] - psRotateMat3D->prValue[6] * psRotateMat3D->prValue[4]) * rDet;
		r8 = (psRotateMat3D->prValue[1] * psRotateMat3D->prValue[6] - psRotateMat3D->prValue[7] * psRotateMat3D->prValue[0]) * rDet;
		r9 = (psRotateMat3D->prValue[4] * psRotateMat3D->prValue[0] - psRotateMat3D->prValue[3] * psRotateMat3D->prValue[1]) * rDet;
		psRotateMat3D->prValue[0] = r1;
		psRotateMat3D->prValue[1] = r2;
		psRotateMat3D->prValue[2] = r3;
		psRotateMat3D->prValue[3] = r4;
		psRotateMat3D->prValue[4] = r5;
		psRotateMat3D->prValue[5] = r6;
		psRotateMat3D->prValue[6] = r7;
		psRotateMat3D->prValue[7] = r8;
		psRotateMat3D->prValue[8] = r9;	
	}
}

float GetDeterminant_ByARM_RotateMat3D(ARM_RotateMat3D* psRotateMat3D)
{
	float rDeterminant = psRotateMat3D->prValue[4] * psRotateMat3D->prValue[0] * psRotateMat3D->prValue[8] +
		psRotateMat3D->prValue[5] * psRotateMat3D->prValue[1] * psRotateMat3D->prValue[6] +
		psRotateMat3D->prValue[3] * psRotateMat3D->prValue[2] * psRotateMat3D->prValue[7] -
		psRotateMat3D->prValue[5] * psRotateMat3D->prValue[0] * psRotateMat3D->prValue[7] -
		psRotateMat3D->prValue[1] * psRotateMat3D->prValue[3] * psRotateMat3D->prValue[8] -
		psRotateMat3D->prValue[2] * psRotateMat3D->prValue[4] * psRotateMat3D->prValue[6];
	return rDeterminant;
}

void Set_ByARM_RotateMat3D(ARM_RotateMat3D* psRotateMat3D, float r1, float r2, float r3, float r4, float r5, float r6, float r7, float r8, float r9)
{
	psRotateMat3D->prValue[0] = r1;
	psRotateMat3D->prValue[1] = r2;
	psRotateMat3D->prValue[2] = r3;
	psRotateMat3D->prValue[3] = r4;
	psRotateMat3D->prValue[4] = r5;
	psRotateMat3D->prValue[5] = r6;
	psRotateMat3D->prValue[6] = r7;
	psRotateMat3D->prValue[7] = r8;
	psRotateMat3D->prValue[8] = r9;
}

// ARM_LinearTransform2D
void Transform_ByARM_LinearTransform2D(ARM_LinearTransform2D* psLinearTransform2D, ARM_Point2D* pxPoint)
{
	float rX, rY;
	rX = *psLinearTransform2D->xRotateMat.prValue * pxPoint->rX + *(psLinearTransform2D->xRotateMat.prValue + 1) * pxPoint->rY + psLinearTransform2D->xTranPoint.rX;
	rY = *(psLinearTransform2D->xRotateMat.prValue + 2) * pxPoint->rX + *(psLinearTransform2D->xRotateMat.prValue + 3) * pxPoint->rY + psLinearTransform2D->xTranPoint.rY;
	pxPoint->rX = rX;
	pxPoint->rY = rY;
} 

// ARM_LinearTransform3D
ARM_LinearTransform3D Operator_Float_Mul_ByARM_LinearTransform3D(ARM_LinearTransform3D* psLinearTransform3D, float rValue)
{
	ARM_LinearTransform3D xTransform;
	xTransform.xRotateMat = Operator_FloatMul_ByARM_RotateMat3D(&(psLinearTransform3D->xRotateMat), rValue);
	xTransform.xTranPoint.rX = psLinearTransform3D->xTranPoint.rX * rValue;
	xTransform.xTranPoint.rY = psLinearTransform3D->xTranPoint.rY * rValue;
	xTransform.xTranPoint.rZ = psLinearTransform3D->xTranPoint.rZ * rValue;
	return xTransform;
}

void Transform_ByARM_LinearTransform3D(ARM_LinearTransform3D* psLinearTransform3D, ARM_Point3D* pxPoint)
{
	*pxPoint = Operator_Point3D_Mul_ByARM_RotateMat3D(&(psLinearTransform3D->xRotateMat), pxPoint);
	pxPoint->rX += psLinearTransform3D->xTranPoint.rX;
	pxPoint->rY += psLinearTransform3D->xTranPoint.rY;
	pxPoint->rZ += psLinearTransform3D->xTranPoint.rZ;
}

// ARM_Vec
void Operator_Assign_ByARM_Vec(ARM_Vec* pxFloatVec,  ARM_Vec* pxInFloatVec)
{
	pxFloatVec->nCount = pxInFloatVec->nCount;
	memcpy(pxFloatVec->prValue, pxInFloatVec->prValue, pxInFloatVec->nCount * sizeof(float));
}

float Scalar_ByARM_Vec( ARM_Vec* pVec1, ARM_Vec* pVec2 )
{
	int i;
	float rRet;
	
	rRet = 0;
	for( i = 0; i < pVec1->nCount ; i ++ )
		rRet += *(pVec1->prValue + i) * *(pVec2->prValue + i);
	
	return rRet;
}

void Normalize_ByARM_Vec(ARM_Vec* pVec)
{
	float var_8, var_4;
	var_8 = Scalar_ByARM_Vec( pVec, pVec );
	var_4 = sqrtf( var_8 );
	if( var_4 != 0 )
	{
		Operator_FloatMulAssign_ByARM_Vec( pVec, ( 1 / var_4 ) );
	}	
}

void Operator_FloatMulAssign_ByARM_Vec(ARM_Vec* pVec, float rValue)
{
	int i;
	for(i = 0; i < pVec->nCount; i ++)
		pVec->prValue[i] *= rValue;
}

void Append_ByARM_Vec(ARM_Vec* pVec1, ARM_Vec* pVec2)
{
	if( pVec1->nCount < 0 )
	{
		pVec1->nCount = pVec2->nCount;
		memcpy(pVec1->prValue, pVec2->prValue, sizeof(float) * pVec1->nCount);
	}
	else
	{
		memcpy( pVec1->prValue + pVec1->nCount, pVec2->prValue, sizeof(float) * pVec2->nCount );
		pVec1->nCount += pVec2->nCount;
	}
}

// ARM_FloatMat
void Operator_Assign_ByARM_Mat(ARM_Mat* pxMat1, ARM_Mat* pxMat2)
{
	pxMat1->nCols = pxMat2->nCols;
	pxMat1->nRows = pxMat2->nRows;
	memcpy(pxMat1->prValue, pxMat2->prValue, sizeof(float) * pxMat1->nRows * pxMat1->nCols);
}

void Solve_ByARM_Mat(ARM_Mat* pxFloatMat, ARM_Vec* pxInVec, ARM_Vec* pxOutVec)
{
	float rTemp, rValue1;
	float* rTempFloatarr;
	float prData1[68], prData2[68 * 68];
	int nMaxRowIdx;
	float rMax;
	ARM_Vec xTempFloatVec;
	ARM_Mat xTempAdjDistMat;
	int nCount1, nCount2, nTemp, nI, nJ, nK, nMaxColIdx;
	int g_var_54[360];

	memset(prData1, 0, 68 * sizeof(float));
	memset(prData2, 0, 68 * 68 * sizeof(float));
	xTempFloatVec.prValue = prData1;
	xTempAdjDistMat.prValue = prData2;
	nCount1 = pxFloatMat->nRows;
	nCount2 = pxFloatMat->nCols;
	pxOutVec->nCount = 0;

	if (nCount1 > 0)
		nTemp = nCount2;
	else
		nTemp = 0;

	if (nCount1 != nTemp)
		return;

	if ((nCount1 <= 0) || (nCount2 <= 0))
		return;

	if (nCount2 != pxInVec->nCount)
		return;

	xTempFloatVec.nCount = pxInVec->nCount;
	memcpy(xTempFloatVec.prValue, pxInVec->prValue, pxInVec->nCount * sizeof(float));
	Operator_Assign_ByARM_Mat(&xTempAdjDistMat, pxFloatMat);
	rTempFloatarr = xTempFloatVec.prValue;
	nMaxRowIdx = 0;

	memset(g_var_54, 0, sizeof(g_var_54));

	for (nI = 0; nI < nCount2; nI++)
	{
		rMax = 0.0f;
		nMaxColIdx = -1;
		for (nJ = 0; nJ < nCount2; nJ++)
		{
			nTemp = nJ * xTempAdjDistMat.nCols + nJ;
			if (g_var_54[nJ] == 1)
				continue;

			for (nK = 0; nK < nCount2; nK++)
			{
				if (g_var_54[nK] > 1)
				{
					pxOutVec->nCount = xTempFloatVec.nCount;
					memcpy(pxOutVec->prValue, xTempFloatVec.prValue, xTempFloatVec.nCount * sizeof(float));
					xTempAdjDistMat.nRows = 0;
					return;
				}
				else if (g_var_54[nK] == 1)
					continue;

				rValue1 = (float)fabs(xTempAdjDistMat.prValue[nJ * xTempAdjDistMat.nCols + nK]);
				if (rMax < rValue1)
				{
					rMax = rValue1;
					nMaxRowIdx = nJ;
					nMaxColIdx = nK;
				}
			}
		}

		if (nMaxColIdx >= 0)
		{
			if (nMaxRowIdx != nMaxColIdx)
			{
				for (nJ = 0; nJ < nCount2; nJ++)
				{
					rTemp = xTempAdjDistMat.prValue[nMaxRowIdx * xTempAdjDistMat.nCols + nJ];
					xTempAdjDistMat.prValue[nMaxRowIdx * xTempAdjDistMat.nCols + nJ] = xTempAdjDistMat.prValue[nMaxColIdx * xTempAdjDistMat.nCols + nJ];
					xTempAdjDistMat.prValue[nMaxColIdx * xTempAdjDistMat.nCols + nJ] = rTemp;
				}

				rTemp = rTempFloatarr[nMaxColIdx];
				rTempFloatarr[nMaxColIdx] = rTempFloatarr[nMaxRowIdx];
				rTempFloatarr[nMaxRowIdx] = rTemp;
			}


			rTemp = 1.0f / xTempAdjDistMat.prValue[nMaxColIdx * xTempAdjDistMat.nCols + nMaxColIdx];
			for (nJ = 0; nJ < nCount2; nJ++)
			{
				xTempAdjDistMat.prValue[nMaxColIdx * xTempAdjDistMat.nCols + nJ] *= rTemp;
			}


			rTempFloatarr[nMaxColIdx] *= rTemp;
			for (nJ = 0; nJ < nCount2; nJ++)
			{
				if (nJ == nMaxColIdx)
					continue;

				rTemp = xTempAdjDistMat.prValue[nJ * xTempAdjDistMat.nCols + nMaxColIdx];
				for (nK = 0; nK < nCount2; nK++)
				{
					xTempAdjDistMat.prValue[nJ * xTempAdjDistMat.nCols + nK] -= xTempAdjDistMat.prValue[nMaxColIdx * xTempAdjDistMat.nCols + nK] * rTemp;
				}

				rTempFloatarr[nJ] -= rTempFloatarr[nMaxColIdx] * rTemp;
			}

		}
		else
		{
			pxOutVec->nCount = xTempFloatVec.nCount;
			memcpy(pxOutVec->prValue, xTempFloatVec.prValue, xTempFloatVec.nCount * sizeof(float));
			xTempAdjDistMat.nRows = 0;
			return;
		}
	}
	pxOutVec->nCount = xTempFloatVec.nCount;
	memcpy(pxOutVec->prValue, xTempFloatVec.prValue, xTempFloatVec.nCount * sizeof(float));
	xTempAdjDistMat.nRows = 0;
	return;
}

void Solve_ByARM_Mat1(ARM_Mat* pxFloatMat,  ARM_Vec* pxInVec, ARM_Vec* pxOutVec)
{
	float rTemp;
	float* rTempFloatarr;
	int nMaxRowIdx;
	float rMax;
	ARM_Vec xTempFloatVec;
	ARM_Mat xTempAdjDistMat;
	float prData1[68], prData2[68 * 68];
	BOOL flag;
	int nCount1, nCount2, nTemp, nI, nJ, nK, nMaxColIdx;

	xTempFloatVec.prValue = prData1;
	xTempAdjDistMat.prValue = prData2;
	nCount1 = pxFloatMat->nRows;
	nCount2 = pxFloatMat->nCols;
	pxOutVec->nCount = 0;

	if (nCount1 > 0)
		nTemp = nCount2;
	else
		nTemp = 0;

	if (nCount1 != nTemp)
		return;

	if ((nCount1 <= 0) || (nCount2 <= 0))
		return;

	if (nCount2 != pxInVec->nCount)
		return;

	xTempFloatVec.nCount = pxInVec->nCount;
	memcpy(xTempFloatVec.prValue, pxInVec->prValue, pxInVec->nCount * sizeof(float));
	Operator_Assign_ByARM_Mat(&xTempAdjDistMat, pxFloatMat);
	rTempFloatarr = xTempFloatVec.prValue;
	nMaxRowIdx = 0;

	for (nI = 0; nI < nCount2; nI++)
	{
		rMax = 0.0f;
		nMaxColIdx = -1;

		for (nJ = 0; nJ < nCount2; nJ++)
		{
			nTemp = nJ * xTempAdjDistMat.nCols + nJ;
			if ((xTempAdjDistMat.prValue[nTemp] >= 0.99f) && (xTempAdjDistMat.prValue[nTemp] <= 1.01f))
			{
				continue;
			}
			for (nK = 0; nK < nCount2; nK++)
			{
				flag = FALSE;
				nTemp = nJ * xTempAdjDistMat.nCols + nK;
				rTemp = (float)fabs1(xTempAdjDistMat.prValue[nJ * xTempAdjDistMat.nCols + nK]);
				if((rTemp >= 0.99f) && (rTemp <= 1.01f))
					flag = TRUE;
				if(!flag)
				{
					if (rMax < rTemp)
					{
						rMax = rTemp;
						nMaxRowIdx = nJ;
						nMaxColIdx = nK;
					}
				}
		    }

		}
		
		if (nMaxColIdx >= 0)
		{
			if (nMaxRowIdx != nMaxColIdx)
			{
				for (nJ = 0; nJ < nCount2; nJ++)
				{
					rTemp = xTempAdjDistMat.prValue[nMaxRowIdx * xTempAdjDistMat.nCols + nJ];
					xTempAdjDistMat.prValue[nMaxRowIdx * xTempAdjDistMat.nCols + nJ] = xTempAdjDistMat.prValue[nMaxColIdx * xTempAdjDistMat.nCols + nJ];
					xTempAdjDistMat.prValue[nMaxColIdx * xTempAdjDistMat.nCols + nJ] = rTemp;
				}

				rTemp = rTempFloatarr[nMaxColIdx];
				rTempFloatarr[nMaxColIdx] = rTempFloatarr[nMaxRowIdx];
				rTempFloatarr[nMaxRowIdx] = rTemp;
			}

			rTemp = 1.0f / xTempAdjDistMat.prValue[nMaxColIdx * xTempAdjDistMat.nCols + nMaxColIdx];
			for (nJ = 0; nJ < nCount2; nJ++)
			{
				xTempAdjDistMat.prValue[nMaxColIdx * xTempAdjDistMat.nCols + nJ] *= rTemp;
			}

			rTempFloatarr[nMaxColIdx] *= rTemp;

			for (nJ = 0; nJ < nCount2; nJ++)
			{
				if (nJ == nMaxColIdx)
					continue;

				rTemp = xTempAdjDistMat.prValue[nJ * xTempAdjDistMat.nCols + nMaxColIdx];
				for (nK = 0; nK < nCount2; nK++)
				{
					xTempAdjDistMat.prValue[nJ * xTempAdjDistMat.nCols + nK] -= xTempAdjDistMat.prValue[nMaxColIdx * xTempAdjDistMat.nCols + nK] * rTemp;
				}

				rTempFloatarr[nJ] -= rTempFloatarr[nMaxColIdx] * rTemp;
			}

		}
		else
		{
			pxOutVec->nCount = xTempFloatVec.nCount;
			memcpy(pxOutVec->prValue, xTempFloatVec.prValue, xTempFloatVec.nCount * sizeof(float));
			xTempAdjDistMat.nRows = 0;
			return;
		}
	}
	pxOutVec->nCount = xTempFloatVec.nCount;
	memcpy(pxOutVec->prValue, xTempFloatVec.prValue, xTempFloatVec.nCount * sizeof(float));
	xTempAdjDistMat.nRows = 0;
	return;
}	

// ARM_RBFMap2D
void UpdateNode_ByARM_RBFMap2D(ARM_RBFMap2D* pxRGFMap2D, ARM_Point3D* pxPoint)
{
	ARM_Point3D xPoint;

	float* prXBuffer;
	float* prYBuffer;
	int nCount;
	ARM_Point3D* pxNodes;
	ARM_Point3D xDiff;
	float rDist;

	xPoint = *pxPoint;
	Transform_ByARM_LinearTransform2D(&pxRGFMap2D->xTransform, (ARM_Point2D*)&xPoint);
	
	if (pxRGFMap2D->xSrcCluster.nNodeNum >= 3)
	{
		pxNodes = pxRGFMap2D->xSrcCluster.pxNodes;
		nCount = pxRGFMap2D->xSrcCluster.nNodeNum;
		prXBuffer = pxRGFMap2D->xFloatVecX.prValue;
		prYBuffer = pxRGFMap2D->xFloatVecY.prValue;
		do 
		{
			nCount--;
			xDiff.rX = pxPoint->rX - pxNodes->rX;
			xDiff.rY = pxPoint->rY - pxNodes->rY;
			rDist = sqrtf(xDiff.rX * xDiff.rX + xDiff.rY * xDiff.rY);
			xPoint.rX += *prXBuffer * rDist;
			xPoint.rY += *prYBuffer * rDist;
			
			pxNodes ++;
			prXBuffer++;
			prYBuffer++;
		}while (nCount);
	}
	*pxPoint = xPoint;
}

void Init_ByARM_RBFMap2D(ARM_RBFMap2D* pxRGFMap2D, ARM_SpatialGraph* pxSrcFaceGraph, ARM_SpatialGraph* pxDestFaceGraph)
{
	int nNodeNum;
	ARM_Mat xAdjDistMat;
	ARM_Vec xVecX, xVecY;
	ARM_Point3D xDiff, *pxDstPoint, *pxSrcPoint;
	float* prXBuffer, *prYBuffer;
	int nI, nJ;
	int i;
	ARM_Point2D xPoint;

	float pprBuffer1[4624];
	float rBufferX[68], rBufferY[68];

	xAdjDistMat.prValue = pprBuffer1;
	
	pxRGFMap2D->nBasisFuncType = 2;
	pxRGFMap2D->nAltType = 7;
	
	if (pxSrcFaceGraph->nNodeNum)
	{
		pxRGFMap2D->xSrcCluster = *pxSrcFaceGraph;
	}
	else
	{
		pxRGFMap2D->xSrcCluster.nNodeNum = 4;
		pxRGFMap2D->xSrcCluster.pxNodes[0].rX = 14.118600f;
		pxRGFMap2D->xSrcCluster.pxNodes[0].rY = 9.6390400f;
		pxRGFMap2D->xSrcCluster.pxNodes[1].rX = 33.476700f;
		pxRGFMap2D->xSrcCluster.pxNodes[1].rY = 9.6390400f;
		pxRGFMap2D->xSrcCluster.pxNodes[2].rX = 23.797600f;
		pxRGFMap2D->xSrcCluster.pxNodes[2].rY = 20.497900f;
		pxRGFMap2D->xSrcCluster.pxNodes[3].rX = 23.797600f;
		pxRGFMap2D->xSrcCluster.pxNodes[3].rY = 31.470200f;
	}
	
	if (pxDestFaceGraph->nNodeNum)
	{
		pxRGFMap2D->xDstCluster = *pxDestFaceGraph;
	}
	else
	{
		pxRGFMap2D->xDstCluster.nNodeNum = 4;
		pxRGFMap2D->xDstCluster.pxNodes[0].rX = 12.550537f;
		pxRGFMap2D->xDstCluster.pxNodes[0].rY = 10.514771f;
		pxRGFMap2D->xDstCluster.pxNodes[1].rX = 34.818848f;
		pxRGFMap2D->xDstCluster.pxNodes[1].rY = 10.577209f;
		pxRGFMap2D->xDstCluster.pxNodes[2].rX = 24.191772f;
		pxRGFMap2D->xDstCluster.pxNodes[2].rY = 19.812622f;
		pxRGFMap2D->xDstCluster.pxNodes[3].rX = 23.629150f;
		pxRGFMap2D->xDstCluster.pxNodes[3].rY = 30.341248f;
	}
	
	if (pxRGFMap2D->nFormatType == 0)
	{
		pxRGFMap2D->rSigma = 1.0f;
		pxRGFMap2D->xObj.nCount = 0;
		
		pxRGFMap2D->xFloatVecX.nCount = 4;
		pxRGFMap2D->xFloatVecX.prValue[0] = 0;
		pxRGFMap2D->xFloatVecX.prValue[1] = 0;
		pxRGFMap2D->xFloatVecX.prValue[2] = 0;
		pxRGFMap2D->xFloatVecX.prValue[3] = 0;
		
		pxRGFMap2D->xFloatVecY.nCount = 4;
		pxRGFMap2D->xFloatVecY.prValue[0] = 0;
		pxRGFMap2D->xFloatVecY.prValue[1] = 0;
		pxRGFMap2D->xFloatVecY.prValue[2] = 0;
		pxRGFMap2D->xFloatVecY.prValue[3] = 0;
		
		pxRGFMap2D->xTransform.xRotateMat.prValue[0] = 1.0f;
		pxRGFMap2D->xTransform.xRotateMat.prValue[1] = 0.0f;
		pxRGFMap2D->xTransform.xRotateMat.prValue[2] = 0.0f;
		pxRGFMap2D->xTransform.xRotateMat.prValue[3] = 1.0f;
		
		pxRGFMap2D->xTransform.xTranPoint.rX = 0.0f;
		pxRGFMap2D->xTransform.xTranPoint.rY = 0.0f;
	}
	
	nNodeNum = pxRGFMap2D->xSrcCluster.nNodeNum;
	
	if (nNodeNum != pxRGFMap2D->xDstCluster.nNodeNum)
		return;
	
	GetTransform2D_ByARM_SpatialGraph(&pxRGFMap2D->xDstCluster, &pxRGFMap2D->xSrcCluster, &pxRGFMap2D->xTransform, pxRGFMap2D->nAltType); 
	
	if (nNodeNum < 3)
		return;

	xAdjDistMat.nRows = nNodeNum;
	xAdjDistMat.nCols = nNodeNum;

	for (nI = 0; nI < nNodeNum; nI++)
	{
		xAdjDistMat.prValue[nI * xAdjDistMat.nCols + nI] = 0.0f;
		for (nJ = 0; nJ < nI; nJ++)
		{
			xDiff.rX = pxRGFMap2D->xSrcCluster.pxNodes[nI].rX - pxRGFMap2D->xSrcCluster.pxNodes[nJ].rX;
			xDiff.rY = pxRGFMap2D->xSrcCluster.pxNodes[nI].rY - pxRGFMap2D->xSrcCluster.pxNodes[nJ].rY;
			xAdjDistMat.prValue[nJ * xAdjDistMat.nCols + nI] = xAdjDistMat.prValue[nI * xAdjDistMat.nCols + nJ] = sqrtf(xDiff.rX * xDiff.rX + xDiff.rY * xDiff.rY);
		}
	}

	pxSrcPoint = pxRGFMap2D->xSrcCluster.pxNodes;
	pxDstPoint = pxRGFMap2D->xDstCluster.pxNodes;
	xVecX.prValue = rBufferX;
	xVecY.prValue = rBufferY;
	prXBuffer = xVecX.prValue;
	prYBuffer = xVecY.prValue;
	i = nNodeNum;
	do 
	{
		i --;
		xPoint.rX = pxSrcPoint->rX;
		xPoint.rY = pxSrcPoint->rY;
		Transform_ByARM_LinearTransform2D(&pxRGFMap2D->xTransform, &xPoint);
		*prXBuffer = pxDstPoint->rX - xPoint.rX;
		*prYBuffer = pxDstPoint->rY - xPoint.rY;
		pxSrcPoint ++;
		pxDstPoint ++;
		prXBuffer ++;
		prYBuffer ++;
	} while (i);
	
	xVecX.nCount = nNodeNum;
	xVecY.nCount = nNodeNum;
	pxRGFMap2D->xFloatVecX.nCount = nNodeNum;
	pxRGFMap2D->xFloatVecY.nCount = nNodeNum;
	Solve_ByARM_Mat(&xAdjDistMat, &xVecX, &pxRGFMap2D->xFloatVecX);
	Solve_ByARM_Mat(&xAdjDistMat, &xVecY, &pxRGFMap2D->xFloatVecY);
	xAdjDistMat.nRows = 0;
	pxRGFMap2D->fInit = TRUE;
}

// ARM_Boundary

void Set_ByARM_Boundary(ARM_Boundary* pxBoundary, int rX1, int rY1, int rX2, int rY2)
{
	pxBoundary->xLeftTop.rX = (float)rX1;
	pxBoundary->xLeftTop.rY = (float)rY1;
	pxBoundary->xRightBottom.rX = (float)rX2;
	pxBoundary->xRightBottom.rY = (float)rY2;
}

ARM_Boundary OverlappedRegion_ByARM_Boundary( ARM_Boundary* pxBoundary, ARM_Boundary* cInput )
{
	ARM_Boundary cOutput;
	cOutput.xLeftTop.rX = MAX( pxBoundary->xLeftTop.rX, cInput->xLeftTop.rX );
	cOutput.xLeftTop.rY = MAX( pxBoundary->xLeftTop.rY, cInput->xLeftTop.rY );
	cOutput.xRightBottom.rX = MIN( pxBoundary->xRightBottom.rX, cInput->xRightBottom.rX );
	cOutput.xRightBottom.rY = MIN( pxBoundary->xRightBottom.rY, cInput->xRightBottom.rY );
	return cOutput;
}

//// ArmFace /////
void EmptyConstructor_ByARM_Face(ARM_Face* psFace)
{
	psFace->rPan = 0;
	psFace->rActivity = 0;
//	psFace->nGdx = 0;
//	psFace->nHit = 1;

	psFace->xModelFaceGraph.nNodeNum = 6;
	psFace->xModelFaceGraph.pxNodes[0].rX = 8;
	psFace->xModelFaceGraph.pxNodes[0].rY = 11;
	psFace->xModelFaceGraph.pxNodes[0].rZ = -4;
	
	psFace->xModelFaceGraph.pxNodes[1].rX = 20;
	psFace->xModelFaceGraph.pxNodes[1].rY = 11;
	psFace->xModelFaceGraph.pxNodes[1].rZ = -4;
	
	psFace->xModelFaceGraph.pxNodes[2].rX = 14;
	psFace->xModelFaceGraph.pxNodes[2].rY = 21.64f;
	psFace->xModelFaceGraph.pxNodes[2].rZ = -4;
	
	psFace->xModelFaceGraph.pxNodes[3].rX = 14;
	psFace->xModelFaceGraph.pxNodes[3].rY = 15.98f;
	psFace->xModelFaceGraph.pxNodes[3].rZ = -8;
	
	psFace->xModelFaceGraph.pxNodes[4].rX = 2;
	psFace->xModelFaceGraph.pxNodes[4].rY = 15.98f;
	psFace->xModelFaceGraph.pxNodes[4].rZ = 8;
	
	psFace->xModelFaceGraph.pxNodes[5].rX = 26;
	psFace->xModelFaceGraph.pxNodes[5].rY = 15.98f;
	psFace->xModelFaceGraph.pxNodes[5].rZ = 8;
}
void Constructor_ByARM_Face(SUB_ARM_Face* pxFace, ARM_FaceRect* cDetectValue, float rConfidence, float rPan, float rTilt, float rRoll, int nIndex, int nStageNo)
{
	pxFace->xFaceRect = *cDetectValue;
	pxFace->rActivity = rConfidence;
	pxFace->rPan = rPan;
	pxFace->rTilt = rTilt;
	pxFace->rRoll = rRoll;
	pxFace->nGdx = nIndex;
	pxFace->nHit = nStageNo;
}

void Constructor_ByARM_Face1( ARM_Face* psFace, ARM_FaceRect* cDetectValue, float rConfidence, float rTilt, float rPan, float rRoll, int nIndex, int nStageNo )
{
	psFace->xFaceRect = *cDetectValue;
	psFace->rActivity = rConfidence;
	psFace->rPan = rPan;
	
	psFace->xModelFaceGraph.nNodeNum = 6;
	psFace->xModelFaceGraph.pxNodes[0].rX = 8;
	psFace->xModelFaceGraph.pxNodes[0].rY = 11;
	psFace->xModelFaceGraph.pxNodes[0].rZ = -4;
	
	psFace->xModelFaceGraph.pxNodes[1].rX = 20;
	psFace->xModelFaceGraph.pxNodes[1].rY = 11;
	psFace->xModelFaceGraph.pxNodes[1].rZ = -4;
	
	psFace->xModelFaceGraph.pxNodes[2].rX = 14;
	psFace->xModelFaceGraph.pxNodes[2].rY = 21.64f;
	psFace->xModelFaceGraph.pxNodes[2].rZ = -4;
	
	psFace->xModelFaceGraph.pxNodes[3].rX = 14;
	psFace->xModelFaceGraph.pxNodes[3].rY = 15.98f;
	psFace->xModelFaceGraph.pxNodes[3].rZ = -8;
	
	psFace->xModelFaceGraph.pxNodes[4].rX = 2;
	psFace->xModelFaceGraph.pxNodes[4].rY = 15.98f;
	psFace->xModelFaceGraph.pxNodes[4].rZ = 8;
	
	psFace->xModelFaceGraph.pxNodes[5].rX = 26;
	psFace->xModelFaceGraph.pxNodes[5].rY = 15.98f;
	psFace->xModelFaceGraph.pxNodes[5].rZ = 8;
}

void Operator_Assign_ByARM_Face(ARM_Face* psFace, ARM_Face* cResult)
{
	psFace->xFaceRect = cResult->xFaceRect;
	psFace->xModelFaceGraph = cResult->xModelFaceGraph;
	psFace->rPan = cResult->rPan;
	psFace->rActivity = cResult->rActivity;
    psFace->nSideFlag = cResult->nSideFlag;
}

void ConstructModel_ByARM_Face(ARM_Face* psFace)
{
	int i;
	ARM_Point3D cTempCenter, cTempDis, cDest;
	ARM_LinearTransform3D cTransform;
	ARM_Point3D xPos;

	Set_ByARM_Point3D(&xPos, 0, psFace->rPan, 0);
	GetPoseMat_ByARM_Point3D(&xPos, &cTransform);
	for( i = 0; i < psFace->xModelFaceGraph.nNodeNum; i ++ )
	{
		Set_ByARM_Point3D(&cTempCenter, 14.f, 14.f, 0.f);
		cTempDis.rX = psFace->xModelFaceGraph.pxNodes[i].rX - cTempCenter.rX;
		cTempDis.rY = psFace->xModelFaceGraph.pxNodes[i].rY - cTempCenter.rY;
		cTempDis.rZ = psFace->xModelFaceGraph.pxNodes[i].rZ - cTempCenter.rZ;
		cDest = Operator_Point3D_Mul_ByARM_RotateMat3D(&cTransform.xRotateMat, &cTempDis);
		cDest.rX += cTempCenter.rX;
		cDest.rY += cTempCenter.rY;
		cDest.rZ += cTempCenter.rZ;
		psFace->xModelFaceGraph.pxNodes[i].rX = psFace->xFaceRect.rX + cDest.rX * psFace->xFaceRect.rRate;
		psFace->xModelFaceGraph.pxNodes[i].rY = psFace->xFaceRect.rY + cDest.rY * psFace->xFaceRect.rRate;
		psFace->xModelFaceGraph.pxNodes[i].rZ = cDest.rZ * psFace->xFaceRect.rRate;
	}
}

void GetPoseVec_ByARM_LinearTransform3D(ARM_LinearTransform3D* pcTransform, ARM_Point3D* pxPoint)
{
	float rTemp, rXAlpha, rYAlpha, rZAlpha;
	
	rTemp = pcTransform->xRotateMat.prValue[6] * pcTransform->xRotateMat.prValue[6] + pcTransform->xRotateMat.prValue[8] * pcTransform->xRotateMat.prValue[8];
	rTemp = sqrtf(rTemp);
	rXAlpha = pcTransform->xRotateMat.prValue[8];
	if( rXAlpha < 0 )
		rXAlpha = -rXAlpha;
	if (rXAlpha < 0.1)
	{
		return;
	}
	
	rXAlpha = atan2f(-pcTransform->xRotateMat.prValue[7], rTemp);
	rYAlpha = atan2f(pcTransform->xRotateMat.prValue[6], pcTransform->xRotateMat.prValue[8]);
	rZAlpha = atan2f(-pcTransform->xRotateMat.prValue[1], pcTransform->xRotateMat.prValue[4]);
	
	rXAlpha *= 57.295779513082325225835265587527f/*ARM_INVERT_RADIAN*/;
	rYAlpha *= 57.295779513082325225835265587527f;
	rZAlpha *= 57.295779513082325225835265587527f;
	
	Set_ByARM_Point3D(pxPoint, rXAlpha, rYAlpha, rZAlpha);
}

void GetPoseMat_ByARM_Point3D(ARM_Point3D* pxPoint, ARM_LinearTransform3D* pcTransform)
{
	float rTilt, rPan, rRoll;
	
	rTilt = pxPoint->rX * 0.01745329251994329444444444444f/*ARM_RADIAN*/;
	rPan = pxPoint->rY * 0.01745329251994329444444444444f;
	rRoll = pxPoint->rZ * 0.01745329251994329444444444444f;
	
	if(cosf(rTilt) < 0.0001f)
		rTilt = rTilt * 0.9999f;
	if(fabs1(cosf(rPan)) < 0.0001f)
	{
		if(cosf(rPan) >= 0)
			rPan *= 0.9999f;
		else
			rPan *= 1.0001f;
	}
	
	Set_ByARM_RotateMat3D(&pcTransform->xRotateMat, (float)(cos(rRoll) * cos(rPan) - sin(rRoll) * sin(rTilt)  * sin(rPan)),
		(float)(-cos(rTilt) * sin(rRoll)),
		(float)(-cos(rRoll) * sin(rPan) - sin(rRoll) * sin(rTilt) * cos(rPan)),
		(float)(cos(rRoll) * sin(rTilt) * sin(rPan) + sin(rRoll) * cos(rPan)),
		(float)(cos(rRoll) * cos(rTilt)),
		(float)(cos(rRoll) * sin(rTilt) * cos(rPan) - sin(rRoll) * sin(rPan)),
		(float)(sin(rPan) * cos(rTilt)),
		(float)(-sin(rTilt)),
		(float)(cos(rPan) * cos(rTilt)));
}

float fabs1(float r)
{
	if (r < 0)
		return -r;
	return r;
}
