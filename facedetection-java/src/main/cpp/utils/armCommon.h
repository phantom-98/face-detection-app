#ifndef _ARMCOMMON_H_
#define _ARMCOMMON_H_

#include <stdint.h>
#include <math.h>

// Macros
typedef int64_t INT64;
typedef int BOOL;

#ifndef TRUE
#define TRUE 1
#endif  

#ifndef FALSE
#define FALSE 0
#endif  

#ifndef MIN
#define MIN(a,b)  ((a) > (b) ? (b) : (a))
#endif  //	MIN

#ifndef MAX
#define MAX(a,b)  ((a) < (b) ? (b) : (a))
#endif  //	MAX

#ifndef Swap
#define Swap(a,b,t) ((t) = (a), (a) = (b), (b) = (t))
#endif

#ifndef ROUNDFIXED
#define ROUNDFIXED(x) (int)((float)x * 0x10000 + 0.5)
#endif

#ifndef ABS
#define ABS(a)     (a) < 0 ? (-a) : (a)
#endif

#ifndef __sinf
#define __sinf(a)    ((float)sin((double) a))
#endif

#ifndef __atan2f
#define __atan2f(a,b)    (float)atan2((double) (a), (double) (b))
#endif

#ifndef __sqrtf
#define __sqrtf(a)    (float)sqrt((double)(a))
#endif

#ifndef __cosf
#define __cosf(a)    (float)cos((double)(a))
#endif

#define	Q_4			16			//	pow ( 2, 4 )
#define	Q_5			32			//	pow ( 2, 5 )
#define	Q_6			64			//	pow ( 2, 6 )
#define	Q_7			128			//	pow ( 2, 6 )
#define	Q_10		1024			//	pow ( 2, 10 )
#define	Q_13		8192			//	pow ( 2, 13 )
#define	Q_14		16384			//	pow ( 2, 15 )
#define	Q_15		32768			//	pow ( 2, 15 )
#define	Q_17		131072			//	pow ( 2, 17 )
#define Q_19		524288
#define Q_16		65536
#define	Q_20		1048576			//	pow ( 2, 20 )
#define Q_23        8388608
#define Q_24        16777216
#define Q_25        33554432
#define	Q_27		134217728		//	pow ( 2, 27 )
#define	Q_28		268435456		//	pow ( 2, 27 )
#define Q_30		1073741824		//  pow ( 2, 30 )
#define Q_31		2147483648		//  pow ( 2, 31 )
#define Q_32		4294967296		//  pow ( 2, 32 )
#define Q_36		68719476736		//  pow ( 2, 36 )
#define Q_40		1099511627776		//  pow ( 2, 40 )
#define Q_46		70368744177664
#define Q_44		17592186044416		//  pow ( 2, 44 )
#define	Q_50		1125899906842624			//	pow ( 2, 50)
#define	Q_51		2251799813685248			//	pow ( 2, 51)
#define Q_60		1152921504606846976		//  pow ( 2, 60 )

//#define LANDSCAPE
//// Detector Macro////

#define D_IMAGE_SIZE 76800
#define D_MAX_INT_IMAGE_SIZE 77361

#define MODEL_FACE_LEN 1000
#define MODEL_FACE_SIZE 1000000

#define IMAGE_SIZE 307200

#ifndef BYTE
typedef unsigned char BYTE;
#endif

//extern int tablePow2[32];
extern int ganArmSquareTable[256];
static int gnCos[91] = 
{
	1048576,	1048416,	1047937,	1047139,	1046022,	1044586,	1042832,	1040760,	1038371,	1035666,	
	1032646,	1029311,	1025662,	1021701,	1017429,	1012847,	1007956,	1002758,	997255,		991448,	
	985339,		978930,		972223,		965219,		957922,		950333,		942454,		934288,		925838,		917105,	
	908093,     898805,		889243,		879410,		869309,		858943,		848316,		837430,		826289,		814897,	
	803256,		791370,		779244,		766880,		754282,		741455,		728402,		715127,		701634,		687928,	
	674012,		659890,		645568,		631049,		616338,		601438,		586356,		571095,		555661,		540057,	
	524288,		508360,		492277,		476044,		459665,		443147,		426494,		409711,		392803,		375776,	
	358634,		341383,		324028,		306574,		289027,		271391,		253673,		235878,		218011,		200078,	
	182083,		164033,		145934,		127789,		109606,		91389,		73145,		54878,		36595,		18300,
	0
};

int CosAlpha(int rTheta);
int SinAlpha(int rTheta);

typedef struct _tagSSize
{
	int nH;
	int nW;
}SSize;

typedef struct _tagARM_Image
{
	int nImageHeight;
	int nImageWidth;
	unsigned char *ptr;
} ARM_Image;

typedef struct _tagARM_MatchInfo{
	int		nPatchWidth;
	int		nPatchHeight;
	int		nIntImageWidth;// ���� �� �����߼�������̩ width
	int		nX;// x����
	int		nY;// y����
	int* pnIntImage;// ���������溷̩ �Ѻ�
	INT64* pnPowIntImage;// �����߼��������溷̩ �Ѻ�
}ARM_MatchInfo;

typedef struct _tagARM_MatchResult{
	char szUnk[8];
	float rActivity;
	float rConfidence;
	int nPassStageNum;
	int nTotalStageNum;
	int nStatus;// 0 ���� -1
}ARM_MatchResult;

typedef struct _tagARM_WAVEPATTERN_PROPERTY{
	float rTilt;
	float rPan;
	float rRoll;
	float rTiltDev;
	float rPanDev;
	float rRollDev;
	float rTransDev;
	float rScaleDev;
}ARM_WAVEPATTERN_PROPERTY;

typedef struct _tagARM_COMPACT_RECT_PATTERN 
{
	unsigned char bOffX;
	unsigned char bOffY;
	unsigned char bRectWidth;
	unsigned char bRectHeight;
	unsigned char bPatternType;
	unsigned char bShift;
	unsigned short wWeight;
	signed char pchActivityArray[16];
	int nOffset;
}ARM_COMPACT_RECT_PATTERN;

typedef union _tagUCommon{
	int nValue;
	float rValue;
}UCommon;

typedef struct _tagARM_COMPACT_QUAD_PATTERN
{
	unsigned char bOffX;
	unsigned char bOffY;
	unsigned char bRectWidth;
	unsigned char bRectHeight;
	unsigned short w8Sig;
	char pAchWeight[25];
	UCommon xWeight;
	UCommon xOffset;
	unsigned char b2CShift;
	unsigned char pb2DThresholdArray[15];
	signed char pch3CActivityArray[16];
}ARM_COMPACT_QUAD_PATTERN;

typedef struct _tagARM_COMPACT_WAVE_PATTERN
{
	unsigned char	bOffX;
	unsigned char	bOffY;
	unsigned char	bRectWidth;
	unsigned char	bRectHeight;
	char	m_8_bKX;
	char	m_9_bKY;
	unsigned char	m_A_bGaussStart;
	unsigned char	m_B_bGaussStep;
	UCommon xWeight;
	UCommon xOffset;
	unsigned char	m_14_bActShift;
	unsigned char	pm_15_baThrArray[0xF];
	char	pm_24_baActArray[0x10];
}ARM_COMPACT_WAVE_PATTERN;

typedef struct _tagARM_FeaturePattern
{
	void* pxPattern;
	int		nStageNum;
	unsigned short*	pwStagePatternNum;
	float* prUnk1;
	int		nFilterNum;
	float* prRejectActivityThresholdArray;
	int		nStructSize;
	char*	pxCompactPatternArray;
	float	rMul;
	int		nTotalPatternNum;
}ARM_FeaturePattern;

typedef struct _tagARM_MATCHER
{
	ARM_FeaturePattern*	ppFeaturePattern[3]; /**<������ ��˦��� ²�� Rect²��, Quad²��, Wave²��.*/
	int					nUnk;
	int					nFeaturePatternNum;  /**<���²������.*/
	ARM_MatchResult		xMatchResult;    /**<���ݰαἳ��.*/
	ARM_WAVEPATTERN_PROPERTY*	pxWaveProperty;
}ARM_Matcher;

typedef struct _tagARM_MATCHARRAY  
{
	ARM_Matcher* ppxMatch[3];
	int			nMatcherCount;
	int			pnSuccessMatcherIndex[10];
	int			nSucessMatcherNum;
	ARM_MatchResult	xMatchResult;
}ARM_MatchArray;

typedef struct _tagARM_FACE_RECT
{
	float rX;
	float rY;
	float rRate;
	int nPyramidIdx;
}ARM_FaceRect;

typedef struct _tagARM_Point2D
{
	float rX;
	float rY;
}ARM_Point2D;

typedef struct _tagARM_Point3D
{
	float rX;
	float rY;
	float rZ;
}ARM_Point3D;

typedef struct _tagARM_RotateMat2D
{
	float prValue[4];
}ARM_RotateMat2D;

typedef struct _tagARM_RotateMat3D
{
	float prValue[9];
}ARM_RotateMat3D;

typedef struct _tagARM_LinearTransform2D
{
	ARM_RotateMat2D		xRotateMat;
	ARM_Point2D			xTranPoint;
}ARM_LinearTransform2D;

typedef struct _tagARM_LinearTransform3D
{
	ARM_RotateMat3D		xRotateMat;
	ARM_Point3D			xTranPoint;
}ARM_LinearTransform3D;

typedef struct _tagARM_SpatialGraph
{
	int nNodeNum;
	ARM_Point3D pxNodes[360];
}ARM_SpatialGraph;

typedef struct _tagARM_Vec{
	int		nCount;
	float* prValue;
}ARM_Vec;

typedef struct _tagARM_RBFMap2D{
	int					nBasisFuncType;	// 2
	int					nAltType;		// 7
	int				nFormatType;
	ARM_SpatialGraph	xSrcCluster;
	ARM_SpatialGraph	xDstCluster;
	float rSigma;
	ARM_Vec		xObj;
	ARM_Vec		xFloatVecX;
	ARM_Vec		xFloatVecY;
	ARM_LinearTransform2D xTransform;
	int				fInit;
}ARM_RBFMap2D;

typedef struct _tagARM_FEATURE{
	float	rInitSim;
	int		m_pnFeature[0xFA];
}ARM_Feature;

typedef struct _tagARM_Face
{
	ARM_FaceRect		xFaceRect;
	ARM_SpatialGraph	xModelFaceGraph;
	ARM_RBFMap2D		xRBFMap2D[2];
	ARM_LinearTransform3D xTransform3D[2];
	ARM_Image			xFaceImage[2];
	float				rPan;
	int					nSideFlag;
	float				rActivity;
}ARM_Face;

typedef struct _tagSUB_ARM_Face
{
	ARM_FaceRect		xFaceRect;
	float				rPan;
	float				rRoll;
	float				rTilt;
	int					nSideFlag;
	float				rActivity;
	int					nGdx;
	int					nHit;
}SUB_ARM_Face;

typedef struct _tagARM_Mat{
	int nRows;
	int	nCols;
	float* prValue;
}ARM_Mat;

typedef struct _tagARM_Boundary{
	ARM_Point2D xLeftTop;
	ARM_Point2D xRightBottom;
}ARM_Boundary;

typedef struct _tagARM_MatcherArray
{
	ARM_Matcher* ppsMatch[3];
	int			nMatcherCount;
	int			pnSuccessMatcherIndex[10];
	int			nSucessMatcherNum;
	ARM_MatchResult	sMatchResult;
}ARM_MatcherArray;

typedef struct _tagARM_PoseDic{
	int				fBig;
	ARM_Matcher*	pxMinutematcher;	
	ARM_SpatialGraph*	ppxOrgFaceGraph[4];
	float*				prImage;
	int					nImageHeight;
	int					nImageWidth;
}ARM_PoseDic;

#ifdef __cplusplus
extern "C"{
#endif
extern float grPan;
extern float grModelConfidence;
//ARM_Point3D
void Set_ByARM_Point3D(ARM_Point3D* pxPoint, float rx, float ry, float rz);
void Normalize_ByARM_Point3D(ARM_Point3D* pxPoint); 
void GetPoseMat_ByARM_Point3D(ARM_Point3D* pxPoint, ARM_LinearTransform3D* pcTransform);

void Set_ByARM_Point3D_Fix(ARM_Point3D* pxPoint, int nX, int nY, int nZ);
ARM_Point3D Normalize_ByARM_Point3D_Fix(ARM_Point3D* pxPoint, int nQType);
void GetPoseMat_ByARM_Point3D_Fix(ARM_Point3D* pxPoint, ARM_LinearTransform3D* pcTransform);
int EuclidDist_ByARM_Point3D_Fix(ARM_Point3D* pxPoint);

// ARM_SpatialGraph
void Transform_ByARM_SpatialGraph(ARM_SpatialGraph* pxSpatialGraph, ARM_LinearTransform3D* pxTrasform);
int GetTransform2D_ByARM_SpatialGraph(ARM_SpatialGraph* pxSpatialGraph, ARM_SpatialGraph *pxInFaceGraph, ARM_LinearTransform2D* pxTransform2D, int nAltType);
int GetTransform3D_ByARM_SpatialGraph(ARM_SpatialGraph* pxSpatialGraph, ARM_SpatialGraph* pxDicFaceGraph, ARM_LinearTransform3D* pxTransform3D);
void SelectNodes_ByARM_SpatialGraph(ARM_SpatialGraph* pxSpatialGraph, ARM_SpatialGraph* pxFaceGraph, int *pnOrder, int nNum);

void Transform_ByARM_SpatialGraph_Fix(ARM_SpatialGraph* pxSpatialGraph, ARM_LinearTransform3D* pxTrasform, ARM_Point3D* pxPoint);
int GetTransform2D_ByARM_SpatialGraph_Fix(ARM_SpatialGraph* pxSpatialGraph, ARM_SpatialGraph *pxInFaceGraph, ARM_LinearTransform2D* pxTransform2D, int nAltType);
int GetTransform3D_ByARM_SpatialGraph_Fix(ARM_SpatialGraph* pxSpatialGraph, ARM_SpatialGraph* pxDicFaceGraph, ARM_LinearTransform3D* pxTransform3D);
int	 SelectNodes_ByARM_SpatialGraph_Fix(ARM_SpatialGraph* pxSpatialGraph, ARM_SpatialGraph* pxFaceGraph, int *pnOrder, int nNum);

// ARM_RotateMat2D
ARM_Point2D Operator_Point2D_Mul_ByARM_RoateMat2D(ARM_RotateMat2D* pxRotateMat2D, ARM_Point2D* pxPoint);
void ReverseMat_ByARM_RoateMat2D(ARM_RotateMat2D* pxRotateMat2D);
void Set_ByARM_RoateMat2D(ARM_RotateMat2D* pxRotateMat2D, float r1, float r2, float r3, float r4);

ARM_Point2D Operator_Point2D_Mul_ByARM_RoateMat2D_Fix(ARM_RotateMat2D* pxRotateMat2D, ARM_Point2D* pxPoint, int nMatQType);
void ReverseMat_ByARM_RoateMat2D_Fix(ARM_RotateMat2D* pxRotateMat2D, int nQType);
void Set_ByARM_RoateMat2D_Fix(ARM_RotateMat2D* pxRotateMat2D, int n1, int n2, int n3, int n4);

// ARM_RotateMat3D
ARM_RotateMat3D Operator_FloatMul_ByARM_RotateMat3D(ARM_RotateMat3D* pxRotateMat3D, float rMul);
void Operator_RotateMat3D_Plus_Assign_ByARM_RotateMat3D(ARM_RotateMat3D* pxRotateMat3D, ARM_RotateMat3D* pxPlusRotateMat3D);
void Operator_RotateMat3D_Mul_Assign_ByARM_RotateMat3D(ARM_RotateMat3D* pxRotateMat3D, ARM_RotateMat3D* pxMulRoatateMat3D);
ARM_RotateMat3D Operator_RotateMat3D_Mul_ByARM_RotateMat3D(ARM_RotateMat3D* pxRotateMat3D, ARM_RotateMat3D* pxMulRotateMat3D);
ARM_Point3D Operator_Point3D_Mul_ByARM_RotateMat3D(ARM_RotateMat3D* pxRotateMat3D, ARM_Point3D* pxMulPoint);
void  ReverseMat_ByARM_RotateMat3D(ARM_RotateMat3D* pxRotateMat3D);
float GetDeterminant_ByARM_RotateMat3D(ARM_RotateMat3D* pxRotateMat3D);
void  Set_ByARM_RotateMat3D(ARM_RotateMat3D* pxRotateMat3D, float r1, float r2, float r3, float r4, float r5, float r6, float r7, float r8, float r9);

ARM_RotateMat3D Operator_IntMul_ByARM_RotateMat3D_Fix(ARM_RotateMat3D* pxRotateMat3D, int nMul, int nIntQType);
void Operator_RotateMat3D_Plus_Assign_ByARM_RotateMat3D_Fix(ARM_RotateMat3D* pxRotateMat3D, ARM_RotateMat3D* pxPlusRotateMat3D);
void Operator_RotateMat3D_Mul_Assign_ByARM_RotateMat3D_Fix(ARM_RotateMat3D* pxRotateMat3D, ARM_RotateMat3D* pxMulRoatateMat3D);
ARM_RotateMat3D Operator_RotateMat3D_Mul_ByARM_RotateMat3D_Fix(ARM_RotateMat3D* pxRotateMat3D, ARM_RotateMat3D* pxMulRotateMat3D, int nQType);
ARM_Point3D Operator_Point3D_Mul_ByARM_RotateMat3D_Fix(ARM_RotateMat3D* pxRotateMat3D, ARM_Point3D* pxMulPoint, int nMatQType);
void  ReverseMat_ByARM_RotateMat3D_Fix(ARM_RotateMat3D* pxRotateMat3D);
int GetDeterminant_ByARM_RotateMat3D_Fix(ARM_RotateMat3D* pxRotateMat3D);
void  Set_ByARM_RotateMat3D_Fix(ARM_RotateMat3D* pxRotateMat3D, int n1, int n2, int n3, int n4, int n5, int n6, int n7, int n8, int n9);

// ARM_LinearTransform2D
void Transform_ByARM_LinearTransform2D(ARM_LinearTransform2D* pxLinearTransform2D, ARM_Point2D* pxPoint); 
void Transform_ByARM_LinearTransform2D_Fix(ARM_LinearTransform2D* pxLinearTransform2D, ARM_Point2D* pxPoint); 

// ARM_LinearTransform3D
ARM_LinearTransform3D Operator_Float_Mul_ByARM_LinearTransform3D(ARM_LinearTransform3D* pxLinearTransform3D, float rValue);
void Transform_ByARM_LinearTransform3D(ARM_LinearTransform3D* pxLinearTransform3D, ARM_Point3D* pxPoint);
void GetPoseVec_ByARM_LinearTransform3D(ARM_LinearTransform3D* pcTransform, ARM_Point3D* pxPoint);

ARM_LinearTransform3D Operator_Int_Mul_ByARM_LinearTransform3D_Fix(ARM_LinearTransform3D* pxLinearTransform3D, int nValue);
void Transform_ByARM_LinearTransform3D_Fix(ARM_LinearTransform3D* pxLinearTransform3D, ARM_Point3D* pxPoint);
void GetPoseVec_ByARM_LinearTransform3D_Fix(ARM_LinearTransform3D* pcTransform, ARM_Point3D* pxPoint);

// ARM_FloatVec
void Operator_Assign_ByARM_Vec(ARM_Vec* pxFloatVec,  ARM_Vec* pxInFloatVec);
float Scalar_ByARM_Vec(ARM_Vec* pVec1, ARM_Vec* pVec2);
void Normalize_ByARM_Vec(ARM_Vec* pVec);
void Operator_FloatMulAssign_ByARM_Vec(ARM_Vec* pVec, float rVale);
void Append_ByARM_Vec(ARM_Vec* pVec1, ARM_Vec* pVec2);

void Operator_Assign_ByARM_Vec_Fix(ARM_Vec* pxIntVec,  ARM_Vec* pxInIntVec);
INT64 Scalar_ByARM_Vec_Fix(ARM_Vec* pVec1, ARM_Vec* pVec2);
void Normalize_ByARM_Vec_Fix(ARM_Vec* pVec);
void Operator_IntMulAssign_ByARM_Vec_Fix(ARM_Vec* pVec, int nVale);
void Append_ByARM_Vec_Fix(ARM_Vec* pVec1, ARM_Vec* pVec2);
void Set_ByARM_Vec_Fix( ARM_Vec* pVec, int nValue );

// ARM_FloatMat
void Solve_ByARM_Mat(ARM_Mat* pxFloatMat,  ARM_Vec* pxInVec, ARM_Vec* pxOutVec);
void Operator_Assign_ByARM_Mat(ARM_Mat* pxMat1, ARM_Mat* pxMat2);

void Solve_ByARM_Mat_Fix(ARM_Mat* pxFloatMat,  ARM_Vec* pxInVec, ARM_Vec* pxOutVec);
void Operator_Assign_ByARM_Mat_Fix(ARM_Mat* pxintMat, ARM_Mat* pxUpdateintMat);
void Set_ByArm_Mat_Fix(ARM_Mat* pxintMat, int nValue);

// ARM_RBFMap2D
void UpdateNode_ByARM_RBFMap2D(ARM_RBFMap2D* pxRGFMap2D, ARM_Point3D* xParam );
void Init_ByARM_RBFMap2D(ARM_RBFMap2D* pxRGFMap2D, ARM_SpatialGraph* pxSrcFaceGraph, ARM_SpatialGraph* pxDestFaceGraph);

void UpdateNode_ByARM_RBFMap2D_Fix(ARM_RBFMap2D* pxRGFMap2D, ARM_Point3D* xParam );
void Init_ByARM_RBFMap2D_Fix(ARM_RBFMap2D* pxRGFMap2D, ARM_SpatialGraph* pxSrcFaceGraph, ARM_SpatialGraph* pxDestFaceGraph);

// ARM_Boundary
void Set_ByARM_Boundary(ARM_Boundary* pxBoundary, int rX1, int rY1, int rX2, int rY2);
ARM_Boundary OverlappedRegion_ByARM_Boundary( ARM_Boundary* pxBoundary, ARM_Boundary* cInput);

void Set_ByARM_Boundary_Fix(ARM_Boundary* pxBoundary, int rX1, int rY1, int rX2, int rY2);
ARM_Boundary OverlappedRegion_ByARM_Boundary_Fix( ARM_Boundary* pxBoundary, ARM_Boundary* cInput);

//// ArmFaceRect
void Set_ByARM_FaceRect( ARM_FaceRect* psFaceRect, float rX, float rY, float rRate );

void Set_ByARM_FaceRect_Fix( ARM_FaceRect* psFaceRect, int rX, int rY, int nRate );

//// ArmFace /////
void EmptyConstructor_ByARM_Face(ARM_Face* psFace);
void Constructor_ByARM_Face(SUB_ARM_Face* psFace, ARM_FaceRect* cDetectValue, float rConfidence, float rTilt, float rPan, float rRoll, int nIndex, int nStageNo);
void Operator_Assign_ByARM_Face( ARM_Face* psFace, ARM_Face* cResult );
void ConstructModel_ByARM_Face(ARM_Face* psFace);

void EmptyConstructor_ByARM_Face_Fix(ARM_Face* psFace);
void Constructor_ByARM_Face_Fix(ARM_Face* pxFace, ARM_FaceRect* cDetectValue, int nConfidence, float rPan);
void Operator_Assign_ByARM_Face_Fix( ARM_Face* psFace, ARM_Face* cResult );
void ConstructModel_ByARM_Face_Fix(ARM_Face* psFace);

void GetHalfImage(BYTE* pDestImage, int nHeight, int nWidth, BYTE* pSrcImage, int nSrcWidth);
void MakeHalfImage(unsigned char* pbImage, int* pnHeight, int* pnWidth);
void MakeThirdImage(unsigned char* pbImage, int* pnHeight, int* pnWidth);
int SquareRootProcess32(int PowerValue);
void UpdateSecIntAndSqrIntImage(unsigned char* pbImage, int* pnIntImage, INT64* pnSqrIntImage, int nHeight, int nWidth);
float fabs1(float r);
#ifdef __cplusplus
};
#endif
#endif // _ARMCOMMON_H_
