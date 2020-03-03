package com.yuyashuai.frameanimation.repeatmode

import com.yuyashuai.frameanimation.FrameAnimation

/**
 * @author yuyashuai   2019-06-21.
 * the original order 1 2 3 4 5
 * playing order  1 2 3 4 5 3 4 5 3 4 5 ...
 */
class RepeatTail(private val repeatStartPosition: Int) : RepeatStrategy {
    private lateinit var paths: MutableList<FrameAnimation.PathData>

    override fun getNextFrameResource(frameIndex: Int): FrameAnimation.PathData? {
        if (repeatStartPosition >= paths.size) {
            throw IllegalArgumentException("illegal start position")
        }
        return if (frameIndex < repeatStartPosition) {
            paths[frameIndex]
        } else {
            val offset = frameIndex - repeatStartPosition
            val repeatCount = paths.size - repeatStartPosition
            paths[repeatStartPosition + offset % repeatCount]
        }
    }

    override fun clear() {
        paths.clear()
    }

    override fun getTotalFrames(): Int {
        return FrameAnimation.FRAMES_INFINITE
    }

    override fun setPaths(list: MutableList<FrameAnimation.PathData>) {
        paths = list.toMutableList()
    }
}