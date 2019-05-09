package com.yuyashuai.frameanimation.drawer

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix

/**
 * @author yuyashuai   2019-05-05.
 * the animation drawer
 */
interface BitmapDrawer {
    /**
     * draw bitmap
     * @param matrix the matrix
     * @return canvas if draw success otherwise return null
     */
    fun draw(bitmap: Bitmap,matrix: Matrix):Canvas?

    fun unlockAndPost(canvas: Canvas)

    /**
     * clear draw content
     */
    fun clear()
}