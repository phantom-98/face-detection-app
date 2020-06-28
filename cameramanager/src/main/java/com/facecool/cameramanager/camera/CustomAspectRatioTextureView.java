package com.facecool.cameramanager.camera;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Surface;
import android.view.TextureView;
import android.view.View.MeasureSpec;
import com.jiangdg.ausbc.utils.Logger;
import com.jiangdg.ausbc.widget.IAspectRatio;
import kotlin.Metadata;
import kotlin.jvm.functions.Function0;
import kotlin.jvm.internal.DefaultConstructorMarker;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class CustomAspectRatioTextureView extends TextureView implements IAspectRatio {
    private double mAspectRatio;
    private static final String TAG = "AspectRatioTextureView";

    public void setAspectRatio(final int width, final int height) {
        this.post((Runnable)(new Runnable() {
            public final void run() {
                CustomAspectRatioTextureView.this.setAspectRatio((double)width / (double)height);
            }
        }));
    }

    public int getSurfaceWidth() {
        return this.getWidth();
    }

    public int getSurfaceHeight() {
        return this.getHeight();
    }

    @Nullable
    public Surface getSurface() {
        Surface var1;
        try {
            var1 = new Surface(this.getSurfaceTexture());
        } catch (Exception var3) {
            var1 = null;
        }

        return var1;
    }

    public void postUITask(@NotNull final Function0 task) {
        Intrinsics.checkNotNullParameter(task, "task");
        this.post((Runnable)(new Runnable() {
            public final void run() {
                task.invoke();
            }
        }));
    }

    private final void setAspectRatio(double aspectRatio) {
        if (!(aspectRatio < (double)0) && this.mAspectRatio != aspectRatio) {
            this.mAspectRatio = aspectRatio;
            Logger.INSTANCE.i("AspectRatioTextureView", "AspectRatio = " + this.mAspectRatio);
            this.requestLayout();
        }
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        /*int initialWidth = MeasureSpec.getSize(widthMeasureSpec);
        int initialHeight = MeasureSpec.getSize(heightMeasureSpec);
        int horizontalPadding = this.getPaddingLeft() - this.getPaddingRight();
        int verticalPadding = this.getPaddingTop() - this.getPaddingBottom();
        initialWidth -= horizontalPadding;
        initialHeight -= verticalPadding;
        double viewAspectRatio = (double)initialWidth / (double)initialHeight;
        double diff = this.mAspectRatio / viewAspectRatio - (double)1;
        int wMeasureSpec = widthMeasureSpec;
        int hMeasureSpec = heightMeasureSpec;
        /*if (this.mAspectRatio > (double)0 && Math.abs(diff) > 0.01) {
            if (diff > (double)0) {
                initialHeight = (int)((double)initialWidth / this.mAspectRatio);
            } else {
                initialWidth = (int)((double)initialHeight * this.mAspectRatio);
            }

            initialWidth += horizontalPadding;
            initialHeight += verticalPadding;
            wMeasureSpec = MeasureSpec.makeMeasureSpec(initialWidth, MeasureSpec.EXACTLY);
            hMeasureSpec = MeasureSpec.makeMeasureSpec(initialHeight, MeasureSpec.EXACTLY);
        }*/

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    public CustomAspectRatioTextureView(@NotNull Context context) {
        this(context, (AttributeSet)null);
    }

    public CustomAspectRatioTextureView(@NotNull Context context, @Nullable AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public CustomAspectRatioTextureView(@NotNull Context context, @Nullable AttributeSet attributeSet, int defStyleAttr) {
        super(context, attributeSet, defStyleAttr);
        this.mAspectRatio = -1.0;
    }
}
