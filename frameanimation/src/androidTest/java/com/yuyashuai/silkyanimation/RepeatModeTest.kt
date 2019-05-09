package com.yuyashuai.silkyanimation

import android.support.test.runner.AndroidJUnit4
import com.yuyashuai.frameanimation.FrameAnimation
import com.yuyashuai.frameanimation.repeatmode.RepeatInfinite
import com.yuyashuai.frameanimation.repeatmode.RepeatOnce
import com.yuyashuai.frameanimation.repeatmode.RepeatReverse
import com.yuyashuai.frameanimation.repeatmode.RepeatReverseInfinite
import org.junit.Test
import org.junit.runner.RunWith

/**
 * @author yuyashuai   2019-05-08.
 */
@RunWith(AndroidJUnit4::class)
class RepeatModeTest {
    @Test
    fun testRepeatOrder() {
        val paths = ArrayList<FrameAnimation.PathData>()
        repeat(5) {
            paths.add(FrameAnimation.PathData(it.toString(), 1))
        }
        val repeatReverseInfinite = RepeatReverseInfinite().apply {
            setPaths(paths)
        }
        val repeatInfinite = RepeatInfinite().apply {
            setPaths(paths)
        }
        val repeatOnce = RepeatOnce().apply {
            setPaths(paths)
        }
        val repeatReverse = RepeatReverse().apply {
            setPaths(paths)
        }
        val arr1 = IntArray(100)
        val arr2 = IntArray(100)
        val arr3 = IntArray(100)
        val arr4 = IntArray(100)
        repeat(100) {
            arr1[it] = repeatReverseInfinite.getNextFrameResource(it)?.path?.toInt() ?: -1
            arr2[it] = repeatInfinite.getNextFrameResource(it)?.path?.toInt() ?: -1
            arr3[it] = repeatOnce.getNextFrameResource(it)?.path?.toInt() ?: -1
            arr4[it] = repeatReverse.getNextFrameResource(it)?.path?.toInt() ?: -1
        }
        System.out.println("repeatInfinite: [${intArrayToString(arr2)}]")
        System.out.println("repeatOnce: [${intArrayToString(arr3)}]")
        System.out.println("repeatReverse: [${intArrayToString(arr4)}]")
        System.out.println("RepeatReverseInfinite: [${intArrayToString(arr1)}]")
    }

    private fun intArrayToString(arr: IntArray): String {
        val sb = StringBuilder()
        arr.forEach {
            sb.append("$it ")
        }
        return sb.toString()
    }
}