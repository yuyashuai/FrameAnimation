package com.yuyashuai.frameanimation.repeatmode

import com.yuyashuai.frameanimation.FrameAnimation

/**
 * @author yuyashuai   2019-05-08.
 * the original order 1 2 3 4 5
 * playing order  1 2 3 4 5 1 2 3 4 5 ...
 */
class RepeatInfinite : RepeatStrategy {
    private lateinit var paths: MutableList<FrameAnimation.PathData>
    override fun setPaths(list: MutableList<FrameAnimation.PathData>) {
        paths = list.toMutableList()
    }

    override fun getNextFrameResource(frameIndex: Int): FrameAnimation.PathData? {
        return if (paths.isEmpty()) {
            null
        } else {
            paths[frameIndex % paths.size]
        }
    }
}