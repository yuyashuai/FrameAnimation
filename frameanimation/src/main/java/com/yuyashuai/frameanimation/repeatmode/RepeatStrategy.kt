package com.yuyashuai.frameanimation.repeatmode

import com.yuyashuai.frameanimation.FrameAnimation

/**
 * @author yuyashuai   2019-05-08.
 *  you can customize the playback or repeat order of animations by implementing this interface
 *  @see RepeatInfinite
 *  @see RepeatReverse
 *  @see RepeatReverseInfinite
 *  @see RepeatOnce
 *  @see RepeatTail
 */
interface RepeatStrategy {
    /**
     * this method will be called automatically
     * so just get the paths data then return it in the getNextFrameResource() method
     */
    fun setPaths(list: MutableList<FrameAnimation.PathData>)

    /**
     * when animation stops, clear the path data for GC
     */
    fun clear()

    /**
     * get the next frame bitmap path
     * @param frameIndex
     * @return the path data for the specific frame, if want to stop the animation just return null
     */
    fun getNextFrameResource(frameIndex: Int): FrameAnimation.PathData?

    /**
     * get the total frames of the animation
     * use for progress listener
     * @see FrameAnimation.animationListener
     */
    fun getTotalFrames(): Int
}