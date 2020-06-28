package com.facecool.utils

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.view.View
import androidx.core.animation.doOnEnd


fun View.startRecordAnimation(): AnimatorSet {
    val scaleDownX = ObjectAnimator.ofFloat(this, "scaleX", 0.6f)
    val scaleDownY = ObjectAnimator.ofFloat(this, "scaleY", 0.6f);
    scaleDownX.duration = 600
    scaleDownY.duration = 600
    scaleDownX.repeatMode = ValueAnimator.REVERSE
    scaleDownY.repeatMode = ValueAnimator.REVERSE
    scaleDownX.repeatCount = ValueAnimator.INFINITE
    scaleDownY.repeatCount = ValueAnimator.INFINITE
    scaleDownY.doOnEnd {
        val scaleDownXEnd = ObjectAnimator.ofFloat(this, "scaleX", 1f)
        val scaleDownYEnd = ObjectAnimator.ofFloat(this, "scaleY", 1f);
        val scaleDownEnd = AnimatorSet();
        scaleDownEnd.duration = 400
        scaleDownEnd.play(scaleDownXEnd).with(scaleDownYEnd);
        scaleDownEnd.start()
    }
    val scaleDown = AnimatorSet();
    scaleDown.play(scaleDownX).with(scaleDownY);
    scaleDownX.addUpdateListener {
        this.invalidate()
    }
    scaleDown.start()
    return scaleDown
}
