package com.facecool.cameramanager.camera;

import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.SurfaceView;

import com.facecool.attendance.facedetector.FaceData;
import com.google.mlkit.vision.face.Face;


public class FaceGraphic2 extends GraphicOverlay.Graphic {

    private static final float ID_TEXT_SIZE = 45.0f;
    private static final float BOX_STROKE_WIDTH = 5.0f;
    private static final int BORDER_ALIGNMENT = 3;
    public static Boolean detectionPropertiesVerboseMode=false;
    private float scale;
    private GraphicOverlay graphicOverlay;
    private Rect faceRectangle;
    private Rect labelRectangle;

    private float fScoreMatch;
    private String sScore, sID;
    private int TEXT_MARGIN = 5;
    private int viewWidth, viewHeight;
    private boolean isRecognized;
    private double imageQualityScore;
    private int landscapeYAdjust = -60;
    private int currentOrientation;
    private int currentMode;

    private FaceData faceData;


    public FaceGraphic2(GraphicOverlay overlay, FaceData faceData, float scale, int viewWidth, int viewHeight, boolean isRecognized, int currentOrientation, int currentMode) {
        super(overlay);
        this.faceData = faceData;
        this.scale = scale;
        this.graphicOverlay = overlay;
        this.viewWidth = viewWidth;
        this.viewHeight = viewHeight;
        this.currentOrientation = currentOrientation;
        this.currentMode = currentMode;
        this.faceRectangle = createFaceRectangle();
        this.labelRectangle = createLabelRectangle();
        this.isRecognized = isRecognized;
        this.imageQualityScore = faceData.getImageQualityScore();
    }

    private Paint getFacePaint() {
        Paint faceRectangularPaint = isRecognized ? getPaint(Color.GREEN) : getPaint(Color.WHITE);
        faceRectangularPaint.setStyle(Paint.Style.STROKE);
        faceRectangularPaint.setStrokeWidth(BOX_STROKE_WIDTH);
        return faceRectangularPaint;
    }

    private Paint getLabelTextPaint() {
        Paint text = isRecognized ? getPaint(Color.GREEN) : getPaint(Color.WHITE);
        text.setTextSize(ID_TEXT_SIZE);
        return text;
    }

    private Paint getLabelRectanglePaint() {
        return getPaint(Color.TRANSPARENT);
    }

    private Paint getPaint(int color) {
        Paint paint = new Paint();
        paint.setColor(color);
        return paint;
    }

    private String getLabelText() {
        String labelText = String.format("%s, %.0f%%",
                this.faceData.getName(),
                this.faceData.getScoreMatch());
        if (detectionPropertiesVerboseMode) {
                labelText += String.format(", tr:%d, fq: %.0f, t:%d",
                        this.faceData.getFace().getTrackingId(),
                        this.faceData.getImageQualityScore(),
                        System.currentTimeMillis() - this.faceData.created
                );
        }
        return labelText;
    }

    @Override
    public void draw(Canvas canvas) {
        canvas.drawRect(faceRectangle, getFacePaint());
        canvas.drawRect(labelRectangle, getLabelRectanglePaint());

        int LINE_HEIGHT = 36;
        canvas.drawText(this.getLabelText(), labelRectangle.left + TEXT_MARGIN, labelRectangle.top + LINE_HEIGHT * 1, getLabelTextPaint());

        //image quality score
//        String qualityScoreText = String.format(" %.0f", imageQualityScore);
//        Paint qualityTextPaint = getLabelTextPaint();
//        qualityTextPaint.setColor(Color.RED);
//        float xPosition = labelRectangle.left + TEXT_MARGIN;
//        float yPosition = faceRectangle.top - qualityTextPaint.getTextSize();
//        canvas.drawText(qualityScoreText, xPosition, yPosition, qualityTextPaint);

    }

    public Rect createLabelRectangle() {
        Rect labelRectangle;


        float labelRectangleWidth = getLabelTextPaint().measureText(this.getLabelText());

        Paint.FontMetrics fontMetrics = getLabelTextPaint().getFontMetrics();
        float textHeight = fontMetrics.bottom - fontMetrics.top;
        float labelRectangleHeight =  textHeight * 1; //face.getTrackingId() != null ? textHeight * 3 : textHeight * 2;

        String labelPosition = "bottom"; // or "top or bottom"

        switch (labelPosition) {
            case "top":
                if (graphicOverlay.isImageFlipped()) {
                    labelRectangle = new Rect((int) (faceRectangle.right - BORDER_ALIGNMENT), (int) (faceRectangle.top - labelRectangleHeight), (int) (faceRectangle.right + labelRectangleWidth + TEXT_MARGIN), faceRectangle.top);
                } else {
                    labelRectangle = new Rect((int) (faceRectangle.left - BORDER_ALIGNMENT), (int) (faceRectangle.top - labelRectangleHeight), (int) (faceRectangle.left + labelRectangleWidth + TEXT_MARGIN), faceRectangle.top);
                }
                break;
            case "bottom":
                if (graphicOverlay.isImageFlipped()) {
                    labelRectangle = new Rect((int) (faceRectangle.right - BORDER_ALIGNMENT), (int) (faceRectangle.bottom), (int) (faceRectangle.right + labelRectangleWidth + TEXT_MARGIN), (int) (faceRectangle.bottom + labelRectangleHeight));
                } else {
                    labelRectangle = new Rect((int) (faceRectangle.left - BORDER_ALIGNMENT), (int) (faceRectangle.bottom), (int) (faceRectangle.left + labelRectangleWidth + TEXT_MARGIN), (int) (faceRectangle.bottom + labelRectangleHeight));
                }
                break;
            default:
                throw new IllegalArgumentException("Invalid label position: " + labelPosition);
        }

        return labelRectangle;
    }

    public Rect createFaceRectangle() {
        Rect bounds = this.faceData.getFace().getBoundingBox();

        int left = (int) (bounds.left * scale);
        int right = (int) (bounds.right * scale);
        int top = (int) (bounds.top * scale);
        int bottom = (int) (bounds.bottom * scale);

        int offsetX = (graphicOverlay.getWidth() - viewWidth) / 2;
        int offsetY = (graphicOverlay.getHeight() - viewHeight) / 2;

        if (currentOrientation == Configuration.ORIENTATION_LANDSCAPE && currentMode == 2) {
            offsetY += landscapeYAdjust;
        }

        if (graphicOverlay.isImageFlipped()) {
            left = graphicOverlay.getWidth() - left - offsetX;
            right = graphicOverlay.getWidth() - right - offsetX;
        } else {
            left += offsetX;
            right += offsetX;
        }

        top += offsetY;
        bottom += offsetY;

        Rect faceRectangle = new Rect(left, top, right, bottom);
        return faceRectangle;
    }

    public Rect getFaceRectangle() {
        return faceRectangle;
    }

}

