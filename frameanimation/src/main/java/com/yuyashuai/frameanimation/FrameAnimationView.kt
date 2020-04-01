package com.yuyashuai.frameanimation

import android.app.Dialog
import android.content.Context
import android.graphics.SurfaceTexture
import android.util.AttributeSet
import android.view.TextureView
import android.view.View
import android.widget.PopupWindow
import kotlin.properties.Delegates

/**
 * the frame animation view to handle the animation life circle
 * @see TextureView
 * @author yuyashuai   2019-05-16.
 */
class FrameAnimationView private constructor(context: Context, attributeSet: AttributeSet?, defStyle: Int, private val animation: FrameAnimation)
    : TextureView(context, attributeSet, defStyle), AnimationController by animation {

    @JvmOverloads
    constructor(context: Context, attributeSet: AttributeSet? = null, defStyle: Int = 0)
            : this(context, attributeSet, defStyle, FrameAnimation(context))

    private val lifeCircleHandler: LifeCircleHandler

    init {
        animation.bindView(this)
        lifeCircleHandler = LifeCircleHandler(animation)
    }

    var restoreEnable: Boolean
        get() {
            return lifeCircleHandler.restoreEnable
        }
        set(value) {
            lifeCircleHandler.restoreEnable = value
        }

    /**
     * Whether to release animation in [onDetachedFromWindow].
     * If your animation plays in [android.app.Dialog] or [android.widget.PopupWindow],
     * you should set it false, otherwise, playing animation again will throw
     * IllegalStateException after the window dismiss.
     * @see stopAnimationSafely
     */
    var autoRelease = true


    override fun onDetachedFromWindow() {
        if (autoRelease) {
            lifeCircleHandler.release()
        }
        super.onDetachedFromWindow()
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

}