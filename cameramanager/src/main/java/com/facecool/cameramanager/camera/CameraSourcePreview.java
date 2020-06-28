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
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.facecool.attendance.Constants;
import com.google.android.gms.common.images.Size;

import java.io.IOException;

/** Preview the camera image in the screen. */
public class CameraSourcePreview extends FrameLayout {
  private static final String TAG = "MIDemoApp:Preview";

  public static final boolean USE_COPY = false;

  private final Context context;
  private final SurfaceView surfaceView;
  public final ImageView mView;
  private boolean startRequested;
  private boolean surfaceAvailable;
  private CameraSource cameraSource;

  private GraphicOverlay overlay;

  private static final int MODE_FILL = 0;
  private static final int MODE_FIT = 1;
  private int finalWidth, finalHeight;
  private float scale;
  private int currentMode = MODE_FIT;
  private boolean layoutSetAfterCameraReady = false;

  public CameraSourcePreview(Context context, AttributeSet attrs) {
    super(context, attrs);
    this.context = context;
    startRequested = false;
    surfaceAvailable = false;

    mView  = new ImageView(context);
    mView.setScaleType(ImageView.ScaleType.FIT_XY);

    surfaceView = new SurfaceView(context);
    surfaceView.setZOrderMediaOverlay(true);
    surfaceView.setZOrderOnTop(false);
//    surfaceView.setWillNotDraw(false);
    surfaceView.getHolder().setFormat(PixelFormat.TRANSPARENT);

//    setBackgroundColor(Color.BLUE);
    surfaceView.getHolder().addCallback(new SurfaceCallback());

    ViewGroup.LayoutParams layoutParams=new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
    surfaceView.setLayoutParams(layoutParams);
    mView.setLayoutParams(layoutParams);
    addView(surfaceView);
    addView(mView);
    setClipChildren(true);
  }

  private void start(CameraSource cameraSource) throws IOException {
    this.cameraSource = cameraSource;
    this.cameraSource.mCameraSourcePreview = this;

    if (this.cameraSource != null) {
      startRequested = true;
      startIfReady();
    }
  }

  public void start(CameraSource cameraSource, GraphicOverlay overlay) throws IOException {
    this.overlay = overlay;
    if ( cameraSource instanceof  AndroidCameraSource)
      surfaceView.setZOrderMediaOverlay(false);
    start(cameraSource);

  }

  public void stop() {
    if (cameraSource != null) {
      cameraSource.stop();
    }
  }

  public void release() {
    if (cameraSource != null) {
      cameraSource.release();
      cameraSource = null;
    }
    surfaceView.getHolder().getSurface().release();
  }

  private void startIfReady() throws IOException, SecurityException {
    if (startRequested && surfaceAvailable) {
//      if (PreferenceUtils.isCameraLiveViewportEnabled(context)) {
        cameraSource.start(surfaceView.getHolder(), surfaceView);
//      } else {
//        cameraSource.start();
//      }
      requestLayout();

      if (overlay != null) {
        Size size = cameraSource.getPreviewSize();
        if ( size == null ) {
          return;
        }
        int min = Math.min(size.getWidth(), size.getHeight());
        int max = Math.max(size.getWidth(), size.getHeight());
        boolean isImageFlipped = cameraSource.isImageFlipped();
        int portraitMode = isPortraitMode();
        if (portraitMode == 1) {
          // Swap width and height sizes when in portrait, since it will be rotated by 90 degrees.
          // The camera preview and the image being processed have the same size.
          overlay.setImageSourceInfo(min, max, isImageFlipped);
        } else if ( portraitMode == 0 )  {
          overlay.setImageSourceInfo(max, min, isImageFlipped);
        } else {
          overlay.setImageSourceInfo(size.getWidth(), size.getHeight(), isImageFlipped);
        }
        overlay.clear();
      }
      startRequested = false;
    }
  }

  private class SurfaceCallback implements SurfaceHolder.Callback {
    @Override
    public void surfaceCreated(SurfaceHolder surface) {
      surface.setFormat(PixelFormat.OPAQUE);
      surface.setFixedSize(getWidth(), getHeight());
      surfaceAvailable = true;
      try {
        startIfReady();
      } catch (IOException e) {
        Log.e(TAG, "Could not start camera source.", e);
      }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surface) {
      surfaceAvailable = false;
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {}

  }

  @Override
  protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
    super.onLayout(changed, left, top, right, bottom);

    if ((cameraSource != null && cameraSource.getPreviewSize() != null) && !layoutSetAfterCameraReady) {
      // Layout the children views based on the current mode
      layoutChildren(left, top, right, bottom);

      // Set the flag to true to indicate that the layout logic has been applied
      layoutSetAfterCameraReady = true;
    }
  }

  private void layoutChildren(int left, int top, int right, int bottom) {
    Constants.logInfo("index " + cameraSource.cameraInfo.index + " width " + (right - left) + " height " + (bottom - top), "LAYOUTLOGS");
    int layoutWidth = right - left;
    int layoutHeight = bottom - top;
    Size previewSize = cameraSource != null ? cameraSource.getPreviewSize() : null;

    if (previewSize != null) {
      int cameraPreviewWidth = previewSize.getWidth();
      int cameraPreviewHeight = previewSize.getHeight();

      // Swap dimensions for portrait mode
      if (isPortraitMode() == 1) {
        int temp = cameraPreviewWidth;
        cameraPreviewWidth = cameraPreviewHeight;
        cameraPreviewHeight = temp;
      }

      float widthScale, heightScale;
      FrameLayout.LayoutParams layoutParams;
      int childLeft, childTop;

      switch (currentMode) {
        case MODE_FILL:
          Constants.logInfo("Entering MODE_FILL", null);

          widthScale = (float) layoutWidth / cameraPreviewWidth;
          heightScale = (float) layoutHeight / cameraPreviewHeight;
          scale = Math.max(widthScale, heightScale);

          finalWidth = (int) (cameraPreviewWidth * scale);
          finalHeight = (int) (cameraPreviewHeight * scale);

          layoutParams = new FrameLayout.LayoutParams(finalWidth, finalHeight);
          layoutParams.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
          surfaceView.setLayoutParams(layoutParams);

          childLeft = (layoutWidth - finalWidth) / 2;
          childTop = top;

          surfaceView.layout(childLeft, childTop, childLeft + finalWidth, childTop + finalHeight);
          break;

        case MODE_FIT:
          Constants.logInfo("Entering MODE_FIT", null);

          // Calculate the scale to fit the SurfaceView within the parent
          widthScale = (float) layoutWidth / cameraPreviewWidth;
          heightScale = (float) layoutHeight / cameraPreviewHeight;

          // Choose the smaller scale factor to fit the view within the parent
          scale = Math.min(widthScale, heightScale);

          finalWidth = (int) (cameraPreviewWidth * scale);
          finalHeight = (int) (cameraPreviewHeight * scale);

          layoutParams = new FrameLayout.LayoutParams(finalWidth, finalHeight);
          layoutParams.gravity = Gravity.CENTER;

          surfaceView.setLayoutParams(layoutParams);

          childLeft = (layoutWidth - finalWidth) / 2;
          childTop = (layoutHeight - finalHeight) / 2;

          surfaceView.layout(childLeft, childTop, childLeft + finalWidth, childTop + finalHeight);

          cameraSource.frameProcessor.setScale(scale);
          cameraSource.frameProcessor.setViewWidth(surfaceView.getWidth());
          cameraSource.frameProcessor.setViewHeight(surfaceView.getHeight());
          break;

        default:
          break;
      }


    } else {
      // Default layout if there's no preview size
      surfaceView.layout(left, top, right, bottom);
      if (overlay != null) {
        overlay.layout(left, top, right, bottom);
      }
    }
  }

  protected void onLayout2(boolean changed, int left, int top, int right, int bottom) {
    if (cameraSource == null)
      return;

    int width = 320;
    int height = 240;
    if (cameraSource != null) {
      Size size = cameraSource.getPreviewSize();

      if (size != null) {
        width = size.getWidth();
        height = size.getHeight();
      } else {
        width = right - left;
        height = bottom - top;
      }
    }

//    setClipBounds(new Rect(0,0,getWidth(),getHeight()));


    // Swap width and height sizes when in portrait, since it will be rotated 90 degrees
    if (isPortraitMode() == 1) {
      int tmp = width;
      width = height;
      height = tmp;
    }

//    height = getHeight();
//    width = getWidth();
//
//    if (cameraSource.getPreviewSize() != null) {
//      int tmpWidth = cameraSource.getPreviewSize().getWidth() / cameraSource.getPreviewSize().getHeight() * height;
//      int tmpheight=cameraSource.getPreviewSize().getHeight()/cameraSource.getPreviewSize().getWidth()*tmpWidth;
//
//      if (tmpWidth > width)
//      {
//       width=tmpWidth;
//
//      }
//
////      if(tmpheight<height)
////      {
//////        height=tmpheight;
////        width=tmpWidth;
////      }
//
////      while(tmpheight>)
////      if(heightz)
//
//      while(width>tmpWidth||height>getHeight())
//      {
////        width=tmpWidth;
////        tmpheight=cameraSource.getPreviewSize().getHeight()/cameraSource.getPreviewSize().getWidth()*tmpWidth;
//        height--;
//        width=cameraSource.getPreviewSize().getWidth() / cameraSource.getPreviewSize().getHeight() * height;
//
//
//      }
//
//
//    }




//    if(cameraSource.getPreviewSize()!=null)
//    {
////      width=320/240*width;
//      int tmpWidth=cameraSource.getPreviewSize().getWidth()/cameraSource.getPreviewSize().getHeight()*height;
//      if(tmpWidth>width)
//      {
////        surfaceView.setScaleX(width/tmpWidth);
////        width=tmpWidth;
//
//
////        height=cameraSource.getPreviewSize().getHeight()/cameraSource.getPreviewSize().getWidth()*width;
////
//
//      }
//      else if(width>tmpWidth){
////        surfaceView.setScaleY(tmpWidth/width);
////        width=tmpWidth;
//
//
//      }
//
//
//
//    }
//    while()
//    surfaceView.setClipBounds();
//    Rect clipBounds = new Rect(0, 0, surfaceView.getWidth(), surfaceView.getHeight());
//    surfaceView.setClipBounds(clipBounds);



    float previewAspectRatio = (float) width / height;
    int layoutWidth = right - left;
    int layoutHeight = bottom - top;
    int offsetW = (/*cameraSource instanceof AndroidCameraSource &&*/ USE_COPY) ? (cameraSource.cameraInfo.index + 1) * layoutWidth * 2 : 0;
    float layoutAspectRatio = (float) layoutWidth / layoutHeight;
    if (previewAspectRatio > layoutAspectRatio) {
      // The preview input is wider than the layout area. Fit the layout height and crop
      // the preview input horizontally while keep the center.
      int horizontalOffset = (int) (previewAspectRatio * layoutHeight - layoutWidth) / 2;
//      surfaceView.layout(offsetW + -horizontalOffset, 0, offsetW + layoutWidth + horizontalOffset, layoutHeight);
      surfaceView.layout(-horizontalOffset+offsetW, 0, layoutWidth+offsetW +horizontalOffset, layoutHeight);

      setForegroundGravity(Gravity.CENTER);
      surfaceView.setForegroundGravity(Gravity.CENTER);
//      mView.layout(0, 0, layoutWidth+horizontalOffset+offsetW, layoutHeight);
      mView.layout(0, 0, layoutWidth , layoutHeight);
//
//      float scale=(layoutWidth*1f)/(layoutWidth*1f+horizontalOffset*1f);
////      surfaceView.setScaleX(1f/scale);
//      System.out.println("scale Y is "+scale);
////      surfaceView.setScaleX(scale);
//      mView.setScaleX(scale);
//      mView.setScaleY(scale);
//      surfaceView
//      mView.setScaleType(ImageView.ScaleType.CENTER_CROP);
//      overlay.setTranslationX(horizontalOffset/2);
      //mView.setTranslationX(horizontalOffset/2);
//      overlay.layout(-horizontalOffset , 0, offsetW + layoutWidth+horizontalOffset , layoutHeight);
//      setTranslationX(-horizontalOffset/2);

//      surfaceView.setClipBounds(new Rect(0,0,layoutWidth,layoutHeight));

//      overlay.getLayoutParams().width=layoutWidth*2;
//      overlay.setTranslationX(horizontalOffset/2);

      //overlay.layout(0,0,layoutWidth,layoutHeight);
//      surfaceView.setTranslationX(horizontalOffset);
//      overlay.setTranslationX(horizontalOffset);
//      mView.setTranslationX(horizontalOffset/2);
//      overlay.layout(offsetW+horizontalOffset,0,layoutWidth+horizontalOffset,layoutHeight);

//      overlay.setTranslationX(horizontalOffset);
//      overlay.setTranslationX(horizontalOffset/2);
      surfaceView.setClipBounds(new Rect(horizontalOffset, 0, layoutWidth+horizontalOffset, layoutHeight));
//      surfaceView.setClipBounds(new Rect(horizontalOffset+layoutWidth, 0, horizontalOffset+2*layoutWidth, layoutHeight));
//      surfaceView.setScaleY(previewAspectRatio);

    } else {
      // The preview input is taller than the layout area. Fit the layout width and crop the preview
      // input vertically while keep the center.
      int verticalOffset = (int) (layoutWidth / previewAspectRatio - layoutHeight) / 2;
//      surfaceView.layout(offsetW + 0, -verticalOffset, offsetW + layoutWidth, layoutHeight + verticalOffset);
//      surfaceView.layout(0, -verticalOffset, layoutWidth, layoutHeight+verticalOffset );
      surfaceView.layout(0,-verticalOffset,layoutWidth,layoutHeight+verticalOffset);
//      float scale=(layoutHeight*1f)/(layoutHeight * 1f+verticalOffset*1f);
//      System.out.println("scale X is "+scale);
////      surfaceView.setScaleY(1f/scale);
//      mView.setScaleY(scale);


//      mView.layout(0, -verticalOffset, layoutWidth, layoutHeight + verticalOffset);
      mView.layout(0,-verticalOffset, layoutWidth, layoutHeight);

      surfaceView.setClipBounds(new Rect(0,verticalOffset,layoutWidth,layoutHeight+verticalOffset));
      setClipBounds(new Rect(0,0,layoutWidth,layoutHeight));
//      mView.setClipBounds(new Rect(0,verticalOffset,layoutWidth,layoutHeight+verticalOffset));
//      System.out.println("vertical deal");
//      surfaceView.setClipBounds(new Rect(0, verticalOffset, layoutWidth, layoutHeight + verticalOffset));
      //mView.setTranslationY(verticalOffset/2);
//      overlay.setTranslationY(verticalOffset/2);
//      surfaceView.setTranslationY(0);
//      mView.setTranslationY(verticalOffset);
//      setTranslationY(verticalOffset);

    }
  }

  protected int isPortraitMode() {
    return cameraSource.isPortraitMode();

  }
}
