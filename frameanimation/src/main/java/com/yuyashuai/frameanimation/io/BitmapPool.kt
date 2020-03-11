package com.yuyashuai.frameanimation.io

import android.graphics.Bitmap
import com.yuyashuai.frameanimation.repeatmode.RepeatStrategy

/**
 * @author yuyashuai   2019-04-24.
 * a pool store and reuse the bitmaps resident in memory
 */
interface BitmapPool {
    /**
     * take an bitmap from the pool
     * don't allocate objects here
     * @return null a animation stop signal
     */
    @Throws(InterruptedException::class)
    fun take(): Bitmap?

    /**
     * Start running,
     * @param repeatStrategy bitmap order
     */
    fun start(repeatStrategy: RepeatStrategy, index: Int)

    /**
     * recycler the bitmap for reuse
     */
    fun recycle(bitmap: Bitmap)

    /**
     * stop decode bitmap
      */
    fun stop()

    /**
     * release all resources, like thread, bitmap...
     */
    fun release()

    /**
     * used for animation listener
     */
    fun getRepeatStrategy(): RepeatStrategy?
}