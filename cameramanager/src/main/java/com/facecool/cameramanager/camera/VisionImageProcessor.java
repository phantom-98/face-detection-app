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

import android.content.Context;
import android.graphics.Bitmap;
import android.view.SurfaceView;

import com.google.mlkit.common.MlKitException;

import java.nio.ByteBuffer;

/** An interface to process the images with different vision detectors and custom image models. */
public interface VisionImageProcessor {

  void processBitmapOfStream(Bitmap bitmap, GraphicOverlay graphicOverlay);

  /** Processes a bitmap image. */
  void processBitmap(Bitmap bitmap, GraphicOverlay graphicOverlay);

  /** Processes a bitmap image. */
  void detectFace(Bitmap bitmap);

  /** Processes ByteBuffer image data, e.g. used for Camera1 live preview case. */
  void processByteBuffer(
          ByteBuffer data, FrameMetadata frameMetadata, GraphicOverlay graphicOverlay)
      throws MlKitException;

//  /** Processes ImageProxy image data, e.g. used for CameraX live preview case. */
//  @RequiresApi(VERSION_CODES.KITKAT)
//  void processImageProxy(ImageProxy image, GraphicOverlay graphicOverlay) throws MlKitException;

  void UpdateSetting(Context con);

  /** Stops the underlying machine learning model and release resources. */
  void stop();

  public void createFaceDetector(Context context);

  public float getScale();

  public void setScale(float scale);

  public int getViewHeight();

  public int getViewWidth();

  public void setViewHeight(int height);

  public void setViewWidth(int width);
  public void setExternalScreen(boolean isExternalScreen);
  public boolean getIsExternalScreen();
  public void setCurrentOrientation(int orientation);
  public int getCurrentOrientation();

  public void setCurrentMode(int mode);
  public int getCurrentMode();
}
