/*
 * Copyright 2020 Google LLC. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.facecool.cameramanager.camera;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import com.facecool.attendance.facedetector.FaceData;
import com.google.mlkit.vision.face.Face;

/**
 * Graphic instance for rendering face position, contour, and landmarks within the associated
 * graphic overlay view.
 */
public class FaceGraphic extends GraphicOverlay.Graphic {
  private static final float FACE_POSITION_RADIUS = 8.0f;
  private static final float ID_TEXT_SIZE = 30.0f;
  private static final float ID_Y_OFFSET = 40.0f;
  private static final float BOX_STROKE_WIDTH = 5.0f;
  private static final int NUM_COLORS = 10;
  private static final int[][] COLORS =
      new int[][] {
        // {Text color, background color}
        {Color.BLACK, Color.WHITE},
        {Color.WHITE, Color.MAGENTA},
        {Color.BLACK, Color.LTGRAY},
        {Color.WHITE, Color.RED},
        {Color.WHITE, Color.BLUE},
        {Color.WHITE, Color.DKGRAY},
        {Color.BLACK, Color.CYAN},
        {Color.BLACK, Color.YELLOW},
        {Color.WHITE, Color.BLACK},
        {Color.BLACK, Color.GREEN}
      };

  private final Paint facePositionPaint;
  private final Paint[] idPaints;
  private final Paint[] boxPaints;
  private final Paint[] labelPaints;

  private volatile Face face;
  private String name_face = "unknown";
  private float fLive = 0.0f;
  private float fScoreMatch = 0.0f;
  private boolean _bReal = true;
  private boolean _isCheckLiveness = false;

  public FaceGraphic(GraphicOverlay overlay, FaceData faceData, boolean bReal, boolean isCheckLiveness) {
    super(overlay);

    this.face = faceData.getFace();
    this.fLive = faceData.getLive();
    this.name_face = faceData.getName();
    this._bReal = bReal;
    this._isCheckLiveness = isCheckLiveness;
    this.fScoreMatch = faceData.getScoreMatch();

    final int selectedColor = Color.WHITE;

    facePositionPaint = new Paint();
    facePositionPaint.setColor(selectedColor);

    int numColors = COLORS.length;
    idPaints = new Paint[numColors];
    boxPaints = new Paint[numColors];
    labelPaints = new Paint[numColors];
    for (int i = 0; i < numColors; i++) {
      idPaints[i] = new Paint();
      idPaints[i].setColor(COLORS[i][0] /* text color */);
      idPaints[i].setTextSize(ID_TEXT_SIZE);

      boxPaints[i] = new Paint();
      boxPaints[i].setColor(COLORS[i][1] /* background color */);
      boxPaints[i].setStyle(Paint.Style.STROKE);
      boxPaints[i].setStrokeWidth(BOX_STROKE_WIDTH);

      labelPaints[i] = new Paint();
      labelPaints[i].setColor(COLORS[i][1] /* background color */);
      labelPaints[i].setStyle(Paint.Style.FILL);
    }
  }

  /** Draws the face annotations for position on the supplied canvas. */
  @Override
  public void draw(Canvas canvas) {
    Face face = this.face;
    if (face == null) {
      return;
    }

    // Draws a circle at the position of the detected face, with the face's track id below.
    float x = translateX(face.getBoundingBox().centerX());
    float y = translateY(face.getBoundingBox().centerY());
    //canvas.drawCircle(x, y, FACE_POSITION_RADIUS, facePositionPaint);

    // Calculate positions.
    float w2 = scale(face.getBoundingBox().width() / 2.5f);
    float h2 = scale(face.getBoundingBox().height() / 2.0f);
    float left = x - w2;
    float top = y - h2;
    float right = x + w2;
    float bottom = y + h2;
    float lineHeight = ID_TEXT_SIZE + BOX_STROKE_WIDTH;
    float yLabelOffset = (face.getTrackingId() == null) ? 0 : -lineHeight;
    if(!_isCheckLiveness)
      yLabelOffset+=lineHeight;

    // Decide color based on face ID
    int colorID = (face.getTrackingId() == null) ? 0 : Math.abs(face.getTrackingId() % NUM_COLORS);

    String sID = "ID: ";
    if( face.getTrackingId() != null )
      sID = String.format("ID: %d", face.getTrackingId());
    String sLive = String.format("Live : %.3f, Fake", this.fLive);
    if( _bReal )
      sLive = String.format("Live : %.3f, Real", this.fLive);

    String sName = String.format("Name : %s", this.name_face);
    String sScore = String.format("Score : %.3f", this.fScoreMatch);
    // Calculate width and height of label box
    float textWidth = idPaints[colorID].measureText(sID);
//    if (face.getSmilingProbability() != null) {
//      yLabelOffset -= lineHeight;
//      textWidth =
//          Math.max(
//              textWidth,
//              idPaints[colorID].measureText(
//                  String.format(Locale.US, "Happiness: %.2f", face.getSmilingProbability())));
//    }
//    if (face.getLeftEyeOpenProbability() != null) {
//      yLabelOffset -= lineHeight;
//      textWidth =
//          Math.max(
//              textWidth,
//              idPaints[colorID].measureText(
//                  String.format(
//                      Locale.US, "Left eye open: %.2f", face.getLeftEyeOpenProbability())));
//    }
//    if (face.getRightEyeOpenProbability() != null) {
//      yLabelOffset -= lineHeight;
//      textWidth =
//          Math.max(
//              textWidth,
//              idPaints[colorID].measureText(
//                  String.format(
//                      Locale.US, "Right eye open: %.2f", face.getRightEyeOpenProbability())));
//    }

    yLabelOffset = yLabelOffset - 3 * lineHeight;
    if( _isCheckLiveness )
      textWidth = Math.max(textWidth, idPaints[colorID].measureText(sLive));
    textWidth = Math.max(textWidth, idPaints[colorID].measureText(sName));
    textWidth = Math.max(textWidth, idPaints[colorID].measureText(sScore));

    // Draw labels
    canvas.drawRect(left - BOX_STROKE_WIDTH,top + yLabelOffset,
        left + textWidth + (2 * BOX_STROKE_WIDTH), top, labelPaints[colorID]);
    yLabelOffset += ID_TEXT_SIZE;
    canvas.drawRect(left, top, right, bottom, boxPaints[colorID]);

    if (face.getTrackingId() != null) {
      canvas.drawText(sID, left, top + yLabelOffset, idPaints[colorID]);
      yLabelOffset += lineHeight;
    }

//    // Draws all face contours.
//    for (FaceContour contour : face.getAllContours()) {
//      for (PointF point : contour.getPoints()) {
//        canvas.drawCircle(
//            translateX(point.x), translateY(point.y), FACE_POSITION_RADIUS, facePositionPaint);
//      }
//    }

//    // Draws smiling and left/right eye open probabilities.
//    if (face.getSmilingProbability() != null) {
//      canvas.drawText(
//          "Smiling: " + String.format(Locale.US, "%.2f", face.getSmilingProbability()),
//          left,
//          top + yLabelOffset,
//          idPaints[colorID]);
//      yLabelOffset += lineHeight;
//    }
//
//    FaceLandmark leftEye = face.getLandmark(FaceLandmark.LEFT_EYE);
//    if (face.getLeftEyeOpenProbability() != null) {
//      canvas.drawText(
//          "Left eye open: " + String.format(Locale.US, "%.2f", face.getLeftEyeOpenProbability()),
//          left,
//          top + yLabelOffset,
//          idPaints[colorID]);
//      yLabelOffset += lineHeight;
//    }
//    if (leftEye != null) {
//      float leftEyeLeft =
//          translateX(leftEye.getPosition().x) - idPaints[colorID].measureText("Left Eye") / 2.0f;
//      canvas.drawRect(
//          leftEyeLeft - BOX_STROKE_WIDTH,
//          translateY(leftEye.getPosition().y) + ID_Y_OFFSET - ID_TEXT_SIZE,
//          leftEyeLeft + idPaints[colorID].measureText("Left Eye") + BOX_STROKE_WIDTH,
//          translateY(leftEye.getPosition().y) + ID_Y_OFFSET + BOX_STROKE_WIDTH,
//          labelPaints[colorID]);
//      canvas.drawText(
//          "Left Eye",
//          leftEyeLeft,
//          translateY(leftEye.getPosition().y) + ID_Y_OFFSET,
//          idPaints[colorID]);
//    }
//
//    FaceLandmark rightEye = face.getLandmark(FaceLandmark.RIGHT_EYE);
//    if (face.getRightEyeOpenProbability() != null) {
//      canvas.drawText(
//          "Right eye open: " + String.format(Locale.US, "%.2f", face.getRightEyeOpenProbability()),
//          left,
//          top + yLabelOffset,
//          idPaints[colorID]);
//    }
//    if (rightEye != null) {
//      float rightEyeLeft =
//          translateX(rightEye.getPosition().x) - idPaints[colorID].measureText("Right Eye") / 2.0f;
//      canvas.drawRect(
//          rightEyeLeft - BOX_STROKE_WIDTH,
//          translateY(rightEye.getPosition().y) + ID_Y_OFFSET - ID_TEXT_SIZE,
//          rightEyeLeft + idPaints[colorID].measureText("Right Eye") + BOX_STROKE_WIDTH,
//          translateY(rightEye.getPosition().y) + ID_Y_OFFSET + BOX_STROKE_WIDTH,
//          labelPaints[colorID]);
//      canvas.drawText(
//          "Right Eye",
//          rightEyeLeft,
//          translateY(rightEye.getPosition().y) + ID_Y_OFFSET,
//          idPaints[colorID]);
//      yLabelOffset += lineHeight;
//    }

//    canvas.drawText(
//        "EulerX: " + face.getHeadEulerAngleX(), left, top + yLabelOffset, idPaints[colorID]);
//    yLabelOffset += lineHeight;
//    canvas.drawText(
//        "EulerY: " + face.getHeadEulerAngleY(), left, top + yLabelOffset, idPaints[colorID]);
//    yLabelOffset += lineHeight;
//    canvas.drawText(
//        "EulerZ: " + face.getHeadEulerAngleZ(), left, top + yLabelOffset, idPaints[colorID]);

    if(_isCheckLiveness) {
      canvas.drawText(sLive, left, top + yLabelOffset, idPaints[colorID]);
      yLabelOffset += lineHeight;
    }

    canvas.drawText(sName, left, top + yLabelOffset, idPaints[colorID]);
    yLabelOffset += lineHeight;
    canvas.drawText(sScore, left, top + yLabelOffset, idPaints[colorID]);

//    // Draw facial landmarks
//    drawFaceLandmark(canvas, FaceLandmark.LEFT_EYE);
//    drawFaceLandmark(canvas, FaceLandmark.RIGHT_EYE);
//    drawFaceLandmark(canvas, FaceLandmark.LEFT_CHEEK);
//    drawFaceLandmark(canvas, FaceLandmark.RIGHT_CHEEK);
  }

//  private void drawFaceLandmark(Canvas canvas, @LandmarkType int landmarkType) {
//    FaceLandmark faceLandmark = face.getLandmark(landmarkType);
//    if (faceLandmark != null) {
//      canvas.drawCircle(
//          translateX(faceLandmark.getPosition().x),
//          translateY(faceLandmark.getPosition().y),
//          FACE_POSITION_RADIUS,
//          facePositionPaint);
//    }
//  }
}
