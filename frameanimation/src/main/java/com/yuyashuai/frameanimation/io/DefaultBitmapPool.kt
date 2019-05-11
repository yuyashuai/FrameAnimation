package com.yuyashuai.frameanimation.io

import android.content.Context
import android.graphics.Bitmap
import com.yuyashuai.frameanimation.repeatmode.RepeatStrategy
import java.lang.ref.WeakReference
import java.util.concurrent.*
import java.util.concurrent.atomic.AtomicInteger

/**
 * @author yuyashuai   2019-04-24.
 */
class DefaultBitmapPool(context: Context) : BitmapPool {
    private val mContext = context
    /**
     * the pool store bitmap that has not yet been used
     */
    private val mPool = LinkedBlockingDeque<Bitmap>(5)

    /**
     * the pool store bitmap that will be reused
     */
    private val mInBitmapPool = LinkedBlockingDeque<WeakReference<Bitmap>>(10)

    /**
     * the decode thread pool
     */
    private var mDecodeExecutors: ExecutorService? = null

    /**
     * current bitmap index
     */
    private var mIndex = AtomicInteger()

    /**
     * Indicates whether the pool is running
     */
    @Volatile
    private var isWorking = false

    /**
     *Indicates whether the pool is trying to stop
     */
    @Volatile
    private var isStopping = false

    private val mDecoderPool = LinkedBlockingQueue<DefaultBitmapDecoder>()

    private var mRepeatStrategy: RepeatStrategy? = null

    private var decodeThread: Thread? = null

    private var waitToStart = false

    private var tempStrategy: RepeatStrategy? = null

    private var skipInBitmapCount = 5

    private val tempMap = ConcurrentHashMap<Int, Bitmap?>()

    override fun start(strategy: RepeatStrategy) {
        //if the pool is Stopping, wait until it stops
        if (isStopping) {
            waitToStart(strategy)
            return
        }
        //if the pool is running normally, relay to play
        if (isWorking) {
            relay(strategy)
            return
        }

        repeat((0 until 5-mDecoderPool.size).count()) {
            mDecoderPool.offer(DefaultBitmapDecoder(mContext))
        }
        isWorking = true
        mRepeatStrategy = strategy
        mDecodeExecutors = Executors.newFixedThreadPool(6)
        decodeThread = Thread {
            decodeBitmap()
        }
        decodeThread?.start()
    }

    private fun waitToStart(strategy: RepeatStrategy) {
        waitToStart = true
        tempStrategy = strategy
    }

    @Volatile
    private var restartNextDecode = false

    /**
     * If the second animation picture consumes less memory than the first animation
     * will throw Problem decoding into existing bitmap exception
     * so if the two animations pictures have different resolutions call 'animation.stop()' first
     */
    private fun relay(strategy: RepeatStrategy) {
        mRepeatStrategy = strategy
        restartNextDecode = true
    }

    private fun decodeBitmap() {
        if (!isWorking || isStopping) {
            clearAndStop()
            System.out.println("decodeBitmap------------clearAndStop")
            return
        }
        if (restartNextDecode) {
            mIndex.set(0)
            restartNextDecode = false
        }
        val mCountDownLatch = CountDownLatch(5)
        repeat(5) {
            try {
                mDecodeExecutors?.execute {
                    val index = mIndex.getAndIncrement()
                    val decoder = if (mDecoderPool.isNotEmpty()) {
                        mDecoderPool.poll()
                    } else {
                        DefaultBitmapDecoder(mContext)
                    }
                    if (!Thread.currentThread().isInterrupted) {
                        val path = mRepeatStrategy?.getNextFrameResource(index)
                        if (path == null) {
                            release()
                            return@execute
                        }
                        val bitmap =
                                decoder.decodeBitmap(path, mInBitmapPool.poll()?.get())
                        if (!isStopping && isWorking) {
                            tempMap[index] = bitmap
                        }
                    }
                    mDecoderPool.offer(decoder)
                    mCountDownLatch.countDown()
                }
            } catch (e: RejectedExecutionException) {
                e.printStackTrace()
            }
        }
        try {
            mCountDownLatch.await()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
        insertPool(tempMap)
        decodeBitmap()
    }

    private fun clearAndStop() {
        mPool.clear()
        mInBitmapPool.clear()
        mIndex.set(0)
        isWorking = false
        isStopping = false
        tempMap.clear()
        mDecoderPool.clear()
        if (waitToStart && tempStrategy != null) {
            waitToStart = false
            start(tempStrategy!!)
        }
    }

    private fun insertPool(map: ConcurrentHashMap<Int, Bitmap?>) {
        if (!isWorking || isStopping) {
            map.clear()
            return
        }
        map.keys().toList().sorted().forEach {
            try {
                if (isWorking && !isStopping) {
                    mPool.put(map[it])
                }
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
        }
        map.clear()
    }

    override fun take(): Bitmap? {
        return try {
            if (isWorking && !isStopping) {
                mPool.take()
            } else {
                null
            }
        } catch (e: InterruptedException) {
            e.printStackTrace()
            null
        }
    }

    override fun recycle(bitmap: Bitmap) {
        if (!isWorking || skipInBitmapCount > mIndex.get() || isStopping) {
            return
        }
        mInBitmapPool.offer(WeakReference(bitmap))
    }

    override fun isReleased(): Boolean {
        return isStopping || !isWorking
    }

    override fun release() {
        if (isStopping || !isWorking) {
            return
        }
        isStopping = true
        decodeThread?.interrupt()
        mDecodeExecutors?.shutdownNow()
    }
}