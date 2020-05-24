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
     * Works before the animation plays, if called when animation
     * playing, it won't take effect until the next playing.
     * @see RepeatMode
     */
    fun setRepeatMode(repeatMode: RepeatMode)

    /**
     * @see setRepeatMode
     */
    fun setRepeatMode(repeatStrategy: RepeatStrategy)

    /**
     * stop the animation asynchronously.
     * @return the frame index when the animation stops
     */
    fun stopAnimation(): Int

    /**
     * stop the animation synchronously to prevent drawing after
     * surface released. Block the main thread until the animation
     * is completely stopped.(average time:~12ms). If you bind the
     * [FrameAnimationView.onPause] function with `Activity.onPause`
     * or `Fragment.onPause`, this function will be called in
     * [LifeCircleHandler.pause] automatically, Normally you don't
     * need to call this method manually. But if your animation plays in
     * [android.app.Dialog] or [android.widget.PopupWindow], better
     * to use this to stop animation.
     * @return the frame index when the animation stops
     */
    fun stopAnimationSafely(): Int

    /**
     * release the [BitmapPool]'s ThreadPool, you can't play
     * animation after release
     */
    fun release()

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
     * Binds an animation listener to this animation. The animation
     * listener is notified of animation events such as the end of
     * the animation or the repetition of the animation.
     */
    fun setAnimationListener(listener: FrameAnimation.FrameAnimationListener)

    fun setBitmapPool(bitmapPool: BitmapPool)

    fun getBitmapPool(): BitmapPool
}