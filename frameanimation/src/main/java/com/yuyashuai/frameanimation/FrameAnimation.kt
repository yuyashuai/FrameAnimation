package com.yuyashuai.frameanimation

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.RectF
import android.os.Handler
import android.os.SystemClock
import android.util.Log
import android.view.SurfaceView
import android.view.TextureView
import android.view.View
import com.yuyashuai.frameanimation.drawer.BitmapDrawer
import com.yuyashuai.frameanimation.drawer.SurfaceViewBitmapDrawer
import com.yuyashuai.frameanimation.drawer.TextureBitmapDrawer
import com.yuyashuai.frameanimation.io.DefaultBitmapPool
import com.yuyashuai.frameanimation.repeatmode.*
import java.io.File
import kotlin.concurrent.thread

/**
 * @author yuyashuai   2019-04-25.
 */
open class FrameAnimation private constructor(
        private var mTextureView: TextureView?,
        private var mSurfaceView: SurfaceView?,
        private var isTextureViewMode: Boolean?,
        private val mContext: Context) : AnimationController {
    constructor(surfaceView: SurfaceView) : this(null, surfaceView, false, surfaceView.context)
    constructor(textureView: TextureView) : this(textureView, null, true, textureView.context)
    constructor(context: Context) : this(null, null, null, context)

    private lateinit var mBitmapDrawer: BitmapDrawer

    private val TAG = javaClass.simpleName

    private val mBitmapPool = DefaultBitmapPool(mContext)

    /**
     * Indicates whether the animation is playing
     */
    private var isPlaying = false

    /**
     * Milliseconds interval between two frames
     * 42ms≈24fps
     */
    private var frameInterval = 42

    /**
     * The thread responsible for drawing
     */
    private var drawThread: Thread? = null

    /**
     *
     */
    private var relayDraw = false

    private var mRepeatStrategy: RepeatStrategy = RepeatOnce()

    /**
     * Whether to clear the view when the animation finished
     * if false the view will display the last frame
     */
    private var clearViewAfterStop = true

    private val MSG_STOP = 0X01

    private val mHandler = Handler(Handler.Callback { msg ->
        if (msg.what == MSG_STOP) {
            stopAnimation()
        }
        return@Callback true
    })

    /**
     * support the reuse of bitmap
     * Turn off this option if the picture resolution is inconsistent
     */
    private var supportInBitmap = true

    /**
     * the animation drawing frame index
     */
    private var drawIndex = 0

    init {
        if (isTextureViewMode == true) {
            mBitmapDrawer = TextureBitmapDrawer(mTextureView!!)
        } else if (isTextureViewMode == false) {
            mBitmapDrawer = SurfaceViewBitmapDrawer(mSurfaceView!!)
        }
    }

    override fun isPlaying(): Boolean {
        return isPlaying
    }

    override fun getFrameInterval(): Int {
        return frameInterval
    }

    override fun setFrameInterval(frameInterval: Int) {
        this.frameInterval = if (frameInterval <= 0) {
            0
        } else {
            frameInterval
        }
    }

    /**
     *only use for delegation
     */
    fun bindView(textureView: TextureView) {
        isTextureViewMode = true
        mTextureView = textureView
        mBitmapDrawer = TextureBitmapDrawer(textureView)
    }

    /**
     *only use for delegation
     */
    fun bindView(surfaceView: SurfaceView) {
        isTextureViewMode = false
        mSurfaceView = surfaceView
        mBitmapDrawer = SurfaceViewBitmapDrawer(surfaceView)
    }


    override fun supportInBitmap(): Boolean {
        return supportInBitmap
    }

    override fun setSupportInBitmap(supportInBitmap: Boolean) {
        this.supportInBitmap = supportInBitmap
    }

    override fun clearViewAfterStop(clearViewAfterStop: Boolean) {
        this.clearViewAfterStop = clearViewAfterStop
    }

    override fun clearViewAfterStop(): Boolean {
        return clearViewAfterStop
    }

    /**
     * play animation from assets files
     * @param assetsPath must be a directory
     */
    override fun playAnimationFromAssets(assetsPath: String) {
        playAnimationFromAssets(assetsPath, 0)
    }

    /**
     * play animation from a file directory path
     * @param filePath must be a directory
     */
    override fun playAnimationFromFile(filePath: String) {
        playAnimationFromFile(filePath, 0)
    }

    /**
     * play animation from assets files
     * @param assetsPath must be a directory
     * @param index the start frame index
     */
    override fun playAnimationFromAssets(assetsPath: String, index: Int) {
        val paths = Util.getPathList(mContext, assetsPath)
        playAnimation(paths.map {
            PathData(it, PATH_ASSETS)
        } as MutableList<PathData>, index)
    }

    /**
     * play animation from a file directory path
     * @param filePath must be a directory
     * @param index the start frame index
     */
    override fun playAnimationFromFile(filePath: String, index: Int) {
        val paths = Util.getPathList(File(filePath))
        playAnimation(paths.map {
            PathData(it, PATH_FILE)
        } as MutableList<PathData>, index)
    }

    var mPaths: MutableList<PathData>? = null
        private set

    /**
     * start playing animations
     * @param paths the path data
     */
    override fun playAnimation(paths: MutableList<PathData>, index: Int) {
        if (paths.isNullOrEmpty()) {
            Log.e(TAG, "path is null or empty")
            return
        }
        if (mSurfaceView == null && mTextureView == null) {
            throw NullPointerException("TextureView and SurfaceView is null")
        }
        mPaths = paths
        drawIndex = index
        mRepeatStrategy.setPaths(paths)
        mBitmapPool.start(mRepeatStrategy, index)
        if (isPlaying) {
            return
        }
        isPlaying = true
        if (drawThread?.isAlive == true) {
            relayDraw = true
        } else {
            draw()
        }
    }

    /**
     * stop the animation async
     */
    override fun stopAnimation(): Int {
        if (!isPlaying) {
            return 0
        }
        mBitmapPool.release()
        try {
            drawThread?.interrupt()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
        mPaths = null
        isPlaying = false
        animationListener?.onAnimationEnd()
        if (clearViewAfterStop) {
            mBitmapDrawer.clear()
        }
        return drawIndex
    }

    private fun draw() {
        animationListener?.onAnimationStart()
        drawThread = thread(start = true) {
            while (isPlaying) {
                val startTime = SystemClock.uptimeMillis()
                val bitmap = mBitmapPool.take()
                if (bitmap == null) {
                    //the animation has finished playing, set the index 0
                    drawIndex = 0
                    mHandler.sendEmptyMessage(MSG_STOP)
                    continue
                }
                configureDrawMatrix(bitmap, mSurfaceView ?: mTextureView!!)
                val canvas = mBitmapDrawer.draw(bitmap, mDrawMatrix) ?: continue
                val interval = SystemClock.uptimeMillis() - startTime
                if (interval < frameInterval) {
                    try {
                        Thread.sleep(frameInterval - interval)
                    } catch (e: InterruptedException) {
                        e.printStackTrace()
                    }
                }
                drawIndex++
                mBitmapDrawer.unlockAndPost(canvas)
                if (mBitmapPool.getRepeatStrategy() != null) {
                    val strategy=mBitmapPool.getRepeatStrategy()
                    if (strategy!!.getTotalFrames() == FRAMES_INFINITE) {
                        animationListener?.onProgress(0f, drawIndex, strategy.getTotalFrames())
                    } else {
                        animationListener?.onProgress(drawIndex.toFloat() / strategy.getTotalFrames().toFloat(), drawIndex, strategy.getTotalFrames())
                    }
                }
                if (supportInBitmap) {
                    mBitmapPool.recycle(bitmap)
                }
                if (!isPlaying && clearViewAfterStop) {
                    mBitmapDrawer.clear()
                }
            }
            if (relayDraw) {
                draw()
                relayDraw = false
            }
        }
    }

    /**
     * set the bitmap scale type
     * @see ScaleType
     */
    override fun setScaleType(scaleType: ScaleType) {
        mScaleType = scaleType
    }

    /**
     * set the bitmap transform matrix
     * @see setScaleType
     */
    override fun setScaleType(matrix: Matrix) {
        mScaleType = ScaleType.MATRIX
        mDrawMatrix = matrix
    }

    /**
     * set the animation repeat mode
     * Works before the animation plays, if called when animation playing, it won't take effect until the next playing.
     * @see RepeatMode
     */
    override fun setRepeatMode(repeatMode: RepeatMode) {
        mRepeatStrategy = when (repeatMode) {
            RepeatMode.INFINITE -> RepeatInfinite()
            RepeatMode.REVERSE_ONCE -> RepeatReverse()
            RepeatMode.REVERSE_INFINITE -> RepeatReverseInfinite()
            else -> RepeatOnce()
        }
    }

    /**
     * @see setRepeatMode
     */
    override fun setRepeatMode(repeatMode: RepeatStrategy) {
        mRepeatStrategy = repeatMode
    }

    private val MATRIX_SCALE_ARRAY =
            arrayOf(Matrix.ScaleToFit.FILL, Matrix.ScaleToFit.START, Matrix.ScaleToFit.CENTER, Matrix.ScaleToFit.END)

    private var mScaleType = ScaleType.CENTER

    private var mDrawMatrix = Matrix()
    private var lastSrcWidth = 0
    private var lastDstWidth = 0
    private var lastSrcHeight = 0
    private var lastDstHeight = 0
    private var lastScaleType: ScaleType? = null
    /**
     * 根据ScaleType配置绘制bitmap的Matrix
     *
     * @param bitmap
     */
    private fun configureDrawMatrix(bitmap: Bitmap, view: View) {
        val srcWidth = bitmap.width
        val dstWidth = view.width
        val srcHeight = bitmap.height
        val dstHeight = view.height
        val nothingChanged = lastScaleType == mScaleType &&
                lastSrcWidth == srcWidth && dstWidth == lastDstWidth && lastSrcHeight == srcHeight && lastDstHeight == dstHeight
        if (nothingChanged) {
            return
        }
        lastSrcWidth = srcWidth
        lastDstWidth = dstWidth
        lastSrcHeight = srcHeight
        lastDstHeight = dstHeight
        lastScaleType = mScaleType
        when (mScaleType) {
            ScaleType.MATRIX -> return
            ScaleType.CENTER -> mDrawMatrix.setTranslate(
                    Math.round((dstWidth - srcWidth) * 0.5f).toFloat(),
                    Math.round((dstHeight - srcHeight) * 0.5f).toFloat()
            )
            ScaleType.CENTER_CROP -> {
                val scale: Float
                var dx = 0f
                var dy = 0f
                //按照高缩放
                if (dstHeight * srcWidth > dstWidth * srcHeight) {
                    scale = dstHeight.toFloat() / srcHeight.toFloat()
                    dx = (dstWidth - srcWidth * scale) * 0.5f
                } else {
                    scale = dstWidth.toFloat() / srcWidth.toFloat()
                    dy = (dstHeight - srcHeight * scale) * 0.5f
                }
                mDrawMatrix.setScale(scale, scale)
                mDrawMatrix.postTranslate(dx, dy)
            }
            ScaleType.CENTER_INSIDE -> {
                //小于dst时不缩放
                val scale: Float = if (srcWidth <= dstWidth && srcHeight <= dstHeight) {
                    1.0f
                } else {
                    Math.min(
                            dstWidth.toFloat() / srcWidth.toFloat(),
                            dstHeight.toFloat() / srcHeight.toFloat()
                    )
                }
                val dx = Math.round((dstWidth - srcWidth * scale) * 0.5f).toFloat()
                val dy = Math.round((dstHeight - srcHeight * scale) * 0.5f).toFloat()
                mDrawMatrix.setScale(scale, scale)
                mDrawMatrix.postTranslate(dx, dy)
            }
            else -> {
                val srcRect = RectF(0f, 0f, bitmap.width.toFloat(), bitmap.height.toFloat())
                val dstRect = RectF(0f, 0f, view.width.toFloat(), view.height.toFloat())
                mDrawMatrix.setRectToRect(srcRect, dstRect, MATRIX_SCALE_ARRAY[mScaleType.value - 1])
            }
        }
    }

    interface FrameAnimationListener {
        /**
         * notifies the start of the animation.
         */
        fun onAnimationStart()

        /**
         *  notifies the end of the animation.
         */
        fun onAnimationEnd()

        /**
         * callback for animation playing progress
         * @param progress 0-1, if the animation played infinitely, always 0
         * @param frameIndex the current frame index
         * @param totalFrames the total frames of the animation, -1 if the animation played infinitely
         */
        fun onProgress(progress: Float, frameIndex: Int, totalFrames: Int)
    }

    private var animationListener: FrameAnimationListener? = null

    override fun setAnimationListener(listener: FrameAnimationListener) {
        animationListener = listener
    }

    enum class ScaleType(val value: Int) {

        /**
         * scale using the bitmap matrix when drawing.
         */
        MATRIX(0),

        /**
         * @see Matrix.ScaleToFit.FILL
         */
        FIT_XY(1),

        /**
         * @see Matrix.ScaleToFit.START
         */
        FIT_START(2),

        /**
         * @see Matrix.ScaleToFit.CENTER
         */
        FIT_CENTER(3),

        /**
         * @see Matrix.ScaleToFit.END
         */
        FIT_END(4),

        /**
         * Center the image in the view, but perform no scaling.
         */
        CENTER(5),

        /**
         * Scale the image uniformly (maintain the image's aspect ratio) so that both dimensions (width and height) of the image
         * will be equal to or larger than the corresponding dimension of the view.
         */
        CENTER_CROP(6),

        /**
         * Scale the image uniformly (maintain the image's aspect ratio) so that both dimensions (width and height) of the image
         * will be equal to or less than the corresponding dimension of the view.
         */
        CENTER_INSIDE(7)
    }

    enum class RepeatMode {
        /**
         * play once
         */
        ONCE,

        /**
         * play infinity
         */
        INFINITE,

        /**
         * the original order 1 2 3 4 5
         * playing order  5 4 3 2 1 ...
         */
        REVERSE_ONCE,

        /**
         * the original order 1 2 3 4 5
         * playing order  1 2 3 4 5 4 3 2 1 2 3 4 5 ...
         */
        REVERSE_INFINITE
    }

    companion object {
        val PATH_FILE = 0x00
        val PATH_ASSETS = 0x01
        val FRAMES_INFINITE = -0x01
    }

    /**
     *
     * @param type the path type ,file or assets
     * @see PATH_FILE
     * @see PATH_ASSETS
     */
    data class PathData(val path: String, val type: Int)
}