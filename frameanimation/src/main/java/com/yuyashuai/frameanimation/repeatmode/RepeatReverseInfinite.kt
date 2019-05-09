package com.yuyashuai.frameanimation.repeatmode

import com.yuyashuai.frameanimation.FrameAnimation

/**
 * @author yuyashuai   2019-05-08.
 * the original order 1 2 3 4 5
 * playing order  1 2 3 4 5 4 3 2 1 2 3 4 5 ...
 */
class RepeatReverseInfinite : RepeatStrategy {
    private lateinit var reversePaths: MutableList<FrameAnimation.PathData>
    private lateinit var paths: MutableList<FrameAnimation.PathData>
    override fun setPaths(list: MutableList<FrameAnimation.PathData>) {
        reversePaths = list.toMutableList().asReversed()
        paths = list.toMutableList()
        reversePaths.removeAt(reversePaths.size - 1)
        paths.removeAt(paths.size - 1)

    }

    override fun getNextFrameResource(frameIndex: Int): FrameAnimation.PathData? {
        if (paths.isNotEmpty()) {
            val repeatCount =
                frameIndex / paths.size
            if (repeatCount % 2 == 0) {
                return paths[frameIndex % paths.size]
            }
            return reversePaths[frameIndex % reversePaths.size]
        }
        return null
    }
}