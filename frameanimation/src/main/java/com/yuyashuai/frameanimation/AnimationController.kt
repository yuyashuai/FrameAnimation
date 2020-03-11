package com.yuyashuai.frameanimation

import android.graphics.Matrix
import com.yuyashuai.frameanimation.FrameAnimation.RepeatMode
import com.yuyashuai.frameanimation.FrameAnimation.ScaleType
import com.yuyashuai.frameanimation.io.BitmapPool
import com.yuyashuai.frameanimation.repeatmode.RepeatStrategy

/**
 * @author yuyashuai   2019-05-16.
 * control animation playing and related configurations
 */
interface AnimationController {

    /**
     * play animation from assets files
     * @param assetsPath must be a directory
     */
    fun playAnimationFromAssets(assetsPath: String)

    /**
     * play animation from assets files
     * @param assetsPath must be a directory
     * @param index the start frame index
     */
    fun playAnimationFromAssets(assetsPath: String, index: Int)

    /**
     * play animation from a file directory path
     * @param filePath must be a directory
     */
    fun playAnimationFromFile(filePath: String)

    /**
     * play animation from a file directory path
     * @param filePath must be a directory
     * @param index the start frame index
     */
    fun playAnimationFromFile(filePath: String, index: Int)

    /**
     * start playing animations
     * @param paths the path data
     */
    fun playAnimation(paths: MutableList<FrameAnimation.PathData>, index: Int)

    /**
     * start playing animations
     * @param paths the path data
     */
    fun playAnimation(paths: MutableList<FrameAnimation.PathData>)

    /**
     * set the bitmap scale type
     * @see ScaleType
     */
    fun setScaleType(scaleType: ScaleType)

    /**
     * set the bitmap transform matrix
     * @see setScaleType
     */
    fun setScaleType(matrix: Matrix)

    /**
     * set the animation repeat mode
     * Works before the animation plays, if called when animation playing, it won't take effect until the next playing.
     * @see RepeatMode
     */
    fun setRepeatMode(repeatMode: RepeatMode)

    /**
     * @see setRepeatMode
     */
    fun setRepeatMode(repeatStrategy: RepeatStrategy)

    /**
     * stop the animation async
     * @return the frame index when the animation stops
     */
    fun stopAnimation(): Int

    /**
     * @return Whether the animation is playing
     */
    fun isPlaying(): Boolean

    /**
     * @return Whether the animation support inBitmap
     */
    fun supportInBitmap(): Boolean

    /**
     * set whether inBitmap supported, default is true
     */
    fun setSupportInBitmap(supportInBitmap: Boolean)

    /**
     * set the frame interval between two frames
     * @param frameInterval unit millisecond
     */
    fun setFrameInterval(frameInterval: Int)

    /**
     * get the frame interval between two frames
     */
    fun getFrameInterval(): Int

    /**
     * Whether to clear the view when the animation finished
     * if false the view will display the last frame
     */
    fun freezeLastFrame(freezeLastFrame: Boolean)

    /**
     * @see freezeLastFrame
     */
    fun freezeLastFrame(): Boolean

    /**
     * Binds an animation listener to this animation. The animation listener
     * is notified of animation events such as the end of the animation or the
     * repetition of the animation.
     */
    fun setAnimationListener(listener: FrameAnimation.FrameAnimationListener)

    fun setBitmapPool(bitmapPool: BitmapPool)

    fun getBitmapPool(): BitmapPool
}