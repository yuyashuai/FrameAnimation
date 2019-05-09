package com.yuyashuai.frameanimation.drawer

import android.graphics.*
import android.view.SurfaceView

/**
 * @author yuyashuai   2019-05-05.
 */
class SurfaceViewBitmapDrawer(surfaceView: SurfaceView) : BitmapDrawer {
    private val mSurfaceView = surfaceView
    private val mSurfaceHolder = surfaceView.holder

    init {
        mSurfaceHolder.setFormat(PixelFormat.TRANSLUCENT)
        mSurfaceView.setZOrderOnTop(true)
    }

    override fun draw(bitmap: Bitmap, matrix: Matrix): Canvas? {
        val canvas = mSurfaceHolder.lockCanvas() ?: return null
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
        canvas.drawBitmap(bitmap, matrix, null)
        return canvas
    }

    override fun clear() {
        val canvas = mSurfaceHolder.lockCanvas() ?: return
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
        mSurfaceHolder.unlockCanvasAndPost(canvas)
    }

    override fun unlockAndPost(canvas: Canvas) {
        mSurfaceHolder.unlockCanvasAndPost(canvas)
    }
}