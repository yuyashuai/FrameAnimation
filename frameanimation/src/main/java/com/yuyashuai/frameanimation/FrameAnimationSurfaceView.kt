package com.yuyashuai.frameanimation

import android.content.Context
import android.util.AttributeSet
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View

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

    private val lifeCircleHandler: LifeCircleHandler
    var restoreEnable: Boolean
        get() {
            return lifeCircleHandler.restoreEnable
        }
        set(value) {
            lifeCircleHandler.restoreEnable = value
        }

    init {
        animation.bindView(this)
        lifeCircleHandler = LifeCircleHandler(animation)
    }

    fun onResume() {
        lifeCircleHandler.resume()
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
        lifeCircleHandler.pause()
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
            lifeCircleHandler.release()
        }
        super.onDetachedFromWindow()
    }

}