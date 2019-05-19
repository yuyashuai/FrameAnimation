package com.yuyashuai.frameanimation.repeatmode

import com.yuyashuai.frameanimation.FrameAnimation

/**
 * @author yuyashuai   2019-05-08.
 * the original order 1 2 3 4 5
 * playing order  5 4 3 2 1 ...
 */
class RepeatReverse : RepeatStrategy {
    private lateinit var reversePaths: MutableList<FrameAnimation.PathData>

    override fun setPaths(list: MutableList<FrameAnimation.PathData>) {
        reversePaths = list.toMutableList().asReversed()
    }

    override fun getNextFrameResource(frameIndex: Int): FrameAnimation.PathData? {
        return if (frameIndex >= reversePaths.size) {
            null
        } else {
            reversePaths[frameIndex]
        }
    }

    override fun getTotalFrames(): Int {
        return reversePaths.size
    }
}