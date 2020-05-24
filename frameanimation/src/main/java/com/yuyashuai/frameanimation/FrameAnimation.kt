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
import com.yuyashuai.frameanimation.io.BitmapPool
import com.yuyashuai.frameanimation.io.BitmapPoolImpl
import com.yuyashuai.frameanimation.repeatmode.*
import java.io.File
import java.util.concurrent.atomic.AtomicInteger
import kotlin.concurrent.thread
import kotlin.math.roundToInt

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

    /**
     * must call [bindView] before playing animation if you create objects with this constructor
     */
    constructor(context: Context) : this(null, null, null, context)

    private lateinit var mBitmapDrawer: BitmapDrawer

    private val TAG = javaClass.simpleName

    private var mBitmapPool: BitmapPool

    /**
     * Indicates whether the animation is playing
     */
    @Volatile
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

    private var relayDraw = false

    private var mRepeatStrategy: RepeatStrategy = RepeatOnce()

    private var temporaryStopSignal = AtomicInteger(0)

    /**
     * don't clear the last frame
     */
    @Volatile
    var temporaryStop = false
        set(value) {
            if (value) {
                temporaryStopSignal.set(2)
            }
            field = value
        }

    /**
     * Whether to clear the view when the animation finished
     * if false the view will display the last frame
     */
    private var freezeLastFrame = false

    private val MSG_STOP = 0X01
    private val MSG_ANIMATION_START = 0X02

    private val mHandler = Handler(Handler.Callback { msg ->
        if (msg.what == MSG_STOP) {
            stopAnimation()
        } else if (msg.what == MSG_ANIMATION_START) {
            animationListener?.onAnimationStart()
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
        mBitmapPool = BitmapPoolImpl(mContext)
    }

    override fun isPlaying() = isPlaying

    override fun getFrameInterval() = frameInterval

    override fun setFrameInterval(frameInterval: Int) {
        this.frameInterval = frameInterval.coerceAtLeast(0)
    }

    private fun mayResetTemporaryStopSignal() {
        if (!temporaryStop) {
            return
        }
        if (temporaryStopSignal.decrementAndGet() == 0) {
            temporaryStop = false
        }
    }

    /**
     * only use for delegation
     */
    fun bindView(textureView: TextureView) {
        isTextureViewMode = true
        mTextureView = textureView
        mBitmapDrawer = TextureBitmapDrawer(textureView)
    }

    /**
     * only use for delegation
     */
    fun bindView(surfaceView: SurfaceView) {
        isTextureViewMode = false
        mSurfaceView = surfaceView
        mBitmapDrawer = SurfaceViewBitmapDrawer(surfaceView)
    }

    override fun supportInBitmap() = supportInBitmap

    override fun setSupportInBitmap(supportInBitmap: Boolean) {
        this.supportInBitmap = supportInBitmap
    }

    override fun freezeLastFrame(freezeLastFrame: Boolean) {
        this.freezeLastFrame = freezeLastFrame
    }

    override fun freezeLastFrame() = freezeLastFrame

    override fun playAnimationFromAssets(assetsPath: String) = playAnimationFromAssets(assetsPath, 0)

    override fun playAnimationFromFile(filePath: String) = playAnimationFromFile(filePath, 0)

    override fun playAnimationFromAssets(assetsPath: String, index: Int) {
        playAnimation(FrameAnimationUtil.getPathList(mContext, assetsPath), index)
    }

    override fun playAnimationFromFile(filePath: String, index: Int) {
        playAnimation(FrameAnimationUtil.getPathList(File(filePath)), index)
    }

    var mPaths: MutableList<PathData>? = null
        private set


    override fun setBitmapPool(bitmapPool: BitmapPool) {
        mBitmapPool = bitmapPool
    }

    override fun playAnimation(paths: MutableList<PathData>) = playAnimation(paths, 0)

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
     * @return the frame index when the animation stops
     */
    override fun stopAnimation(): Int = stopAnimation(false)

    private fun stopAnimation(stopSafely: Boolean): Int {
        if (!isPlaying) {
            return 0
        }
        isPlaying = false
        drawThread?.interrupt()
        mBitmapPool.stop()
        //wait for the animation to finish
        if (stopSafely) {
            drawThread?.join()
        }
        mPaths = null
        mRepeatStrategy.clear()
        if (!temporaryStop) {
            animationListener?.onAnimationEnd()
        }
        mayResetTemporaryStopSignal()
        return drawIndex
    }

    override fun stopAnimationSafely() = stopAnimation(true)

    override fun getBitmapPool(): BitmapPool {
        return mBitmapPool
    }

    private fun draw() {
        mHandler.sendEmptyMessage(MSG_ANIMATION_START)
        drawThread = thread(start = true, name = "FA-DrawThread") {
            var drawing = true
            try {
                while (isPlaying && drawing && !Thread.currentThread().isInterrupted) {
                    val startTime = SystemClock.uptimeMillis()
                    val bitmap = mBitmapPool.take()
                    val interval = SystemClock.uptimeMillis() - startTime
                    if (bitmap == null) {
                        //the last frame time
                        if (interval < frameInterval) {
                            Thread.sleep(frameInterval - interval)
                        }
                        mHandler.sendEmptyMessage(MSG_STOP)
                        drawing = false
                        continue
                    }
                    configureDrawMatrix(bitmap, mSurfaceView ?: mTextureView!!)
                    val canvas = mBitmapDrawer.draw(bitmap, mDrawMatrix) ?: continue
                    try {
                        if (interval < frameInterval) {
                            Thread.sleep(frameInterval - interval)
                        }
                    } catch (e: InterruptedException) {
                        mBitmapDrawer.unlockAndPost(canvas)
                        throw e
                    }
                    mBitmapDrawer.unlockAndPost(canvas)
                    drawIndex++
                    animationListener?.let { listener ->
                        mBitmapPool.getRepeatStrategy()?.let { strategy ->
                            listener.onProgress(
                                    if (strategy.getTotalFrames() == FRAMES_INFINITE) {
                                        0f
                                    } else {
                                        drawIndex.toFloat() / strategy.getTotalFrames().toFloat()
                                    }
                                    , drawIndex, strategy.getTotalFrames())

                        }
                    }

                    if (supportInBitmap) {
                        mBitmapPool.recycle(bitmap)
                    }
                    if (!isPlaying && !freezeLastFrame) {
                        mBitmapDrawer.clear()
                    }
                }

            } catch (e: InterruptedException) {
                Thread.currentThread().interrupt()
            }

            if (!freezeLastFrame && !temporaryStop) {
                mBitmapDrawer.clear()
            }
            mayResetTemporaryStopSignal()
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
        mRepeatStrategy.setPaths(mutableListOf())
    }

    /**
     * @see setRepeatMode
     */
    override fun setRepeatMode(repeatStrategy: RepeatStrategy) {
        mRepeatStrategy = repeatStrategy
        mRepeatStrategy.setPaths(mutableListOf())
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
                    ((dstWidth - srcWidth) * 0.5f).roundToInt().toFloat(),
                    ((dstHeight - srcHeight) * 0.5f).roundToInt().toFloat()
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
                    (dstWidth.toFloat() / srcWidth.toFloat()).coerceAtMost(dstHeight.toFloat() / srcHeight.toFloat())
                }
                val dx = ((dstWidth - srcWidth * scale) * 0.5f).roundToInt().toFloat()
                val dy = ((dstHeight - srcHeight * scale) * 0.5f).roundToInt().toFloat()
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
         * notifies the end of the animation.
         */
        fun onAnimationEnd()

        /**
         * callback for animation playing progress not in UI thread
         * @param progress 0-1, if the animation played infinitely, always 0
         * @param frameIndex the current frame index
         * @param totalFrames the total frames of the animation, -1 if the animation played infinitely
         */
        fun onProgress(progress: Float, frameIndex: Int, totalFrames: Int)
    }

    override fun release() {
        stopAnimationSafely()
        mBitmapPool.release()
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
        @JvmStatic
        val PATH_FILE = 0x00

        @JvmStatic
        val PATH_ASSETS = 0x01

        @JvmStatic
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