package com.yuyashuai.frameanimation

import android.content.Context
import android.util.AttributeSet
import android.view.SurfaceView

/**
 * the frame animation view to handle the animation life circle
 * @see SurfaceView
 * @author yuyashuai   2019-05-16.
 */
class FrameAnimationSurfaceView private constructor(context: Context, attributeSet: AttributeSet?, defStyle: Int, private val animation: FrameAnimation)
    : SurfaceView(context, attributeSet, defStyle), AnimationController by animation {

    @JvmOverloads
    constructor(context: Context, attributeSet: AttributeSet? = null, defStyle: Int = 0)
            : this(context, attributeSet, defStyle, FrameAnimation(context))

    private val lifecycleHandler: LifecycleHandler
    var restoreEnable: Boolean
        get() {
            return lifecycleHandler.restoreEnable
        }
        set(value) {
            lifecycleHandler.restoreEnable = value
        }

    init {
        animation.bindView(this)
        lifecycleHandler = LifecycleHandler(animation)
    }

    fun onResume() {
        lifecycleHandler.resume()
    }

    /**
     * bind animation's life circle with activity or fragment...
     * do it or crash
     *
     *  override fun onPause() {
     *    animation.onPause()
     *    super.onPause()
     *  }
     *
     */
    fun onPause() {
        lifecycleHandler.pause()
    }

    /**
     * Whether to release animation in [onDetachedFromWindow].
     * If your animation plays in [android.app.Dialog] or [android.widget.PopupWindow],
     * you should set it false, otherwise, playing animation again will throw
     * IllegalStateException after the window dismiss.
     */
    var autoRelease = true

    override fun onDetachedFromWindow() {
        if(autoRelease){
            lifecycleHandler.release()
        }
        super.onDetachedFromWindow()
    }

}