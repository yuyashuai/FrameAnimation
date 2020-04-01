package com.yuyashuai.frameanimation

import android.view.View
import kotlin.properties.Delegates

/**
 * @author yuyashuai 2020-03-19
 */
class LifeCircleHandler(private val animation: FrameAnimation) {
    /**
     * whether to resume playback
     */
    var restoreEnable = true
    private var lastStopIndex = 0
    private var lastStopPaths: MutableList<FrameAnimation.PathData>? = null

    /**
     * stop the animation, save the index when the animation stops playing
     */
    fun pause() {
        if (!animation.isPlaying()) {
            return
        }
        animation.temporaryStop = restoreEnable
        lastStopPaths = animation.mPaths?.toMutableList()
        lastStopIndex = animation.stopAnimationSafely()
    }

    /**
     * resume animation
     */
    fun resume() {
        val paths = lastStopPaths
        if (paths != null && restoreEnable) {
            animation.playAnimation(paths, lastStopIndex)
        }
        lastStopPaths = null
    }

    fun release() {
        animation.release()
    }
}