package com.yuyashuai.frameanimation.drawer

import android.graphics.*
import android.view.TextureView

/**
 * @author yuyashuai   2019-05-05.
 */
class TextureBitmapDrawer(private val mTextureView: TextureView) : BitmapDrawer {

    init {
        mTextureView.isOpaque = false
        val canvas = mTextureView.lockCanvas()
        canvas?.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
        mTextureView.unlockCanvasAndPost(canvas)
    }

    override fun draw(bitmap: Bitmap, matrix: Matrix): Canvas? {
        if(Thread.currentThread().isInterrupted){
            return null
        }
        val canvas = mTextureView.lockCanvas() ?: return null
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
        canvas.drawBitmap(bitmap, matrix, null)
        return canvas
    }

    override fun clear() {
        val canvas = mTextureView.lockCanvas() ?: return
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
        mTextureView.unlockCanvasAndPost(canvas)
    }

    override fun unlockAndPost(canvas: Canvas) {
        mTextureView.unlockCanvasAndPost(canvas)
    }
}