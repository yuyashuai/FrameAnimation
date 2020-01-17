package com.yuyashuai.frameanimation.io

import android.content.Context
import android.graphics.Bitmap
import com.yuyashuai.frameanimation.repeatmode.RepeatStrategy
import java.lang.ref.WeakReference
import java.util.concurrent.*
import java.util.concurrent.atomic.AtomicInteger

private const val WORKING = 0
private const val STOPPING = 1
private const val TERMINATED = 2

/**
 * @author yuyashuai   2019-04-24.
 */
open class DefaultBitmapPool(context: Context) : BitmapPool {
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
     * Indicates whether the pool is trying to stop
     */
    @Volatile
    private var isStopping = false

    private val mDecoderPool = LinkedBlockingQueue<DefaultBitmapDecoder>()

    private var mRepeatStrategy: RepeatStrategy? = null

    private var decodeThread: Thread? = null

    private var mInteractionListener: AnimationInteractionListener? = null

    private val state = AtomicInteger(TERMINATED)

    /**
     * If the pool is stopping, wait until it stops completely before working
     */
    private var waitToStart = false

    private var tempStrategy: RepeatStrategy? = null

    private var skipInBitmapCount = 5

    private val tempMap = ConcurrentHashMap<Int, Bitmap?>()

    @Volatile
    private var releaseWhenEmpty = false

    override fun start(repeatStrategy: RepeatStrategy, index: Int) {
        //if the pool is Stopping, wait until it stops
        if (isStopping) {
            waitToStart(repeatStrategy)
            return
        }
        //if the pool is running normally, relay to play
        if (isWorking) {
            relay(repeatStrategy)
            return
        }
        isWorking = true
        mIndex.set(index)
        repeat((0 until 5 - mDecoderPool.size).count()) {
            mDecoderPool.offer(DefaultBitmapDecoder(mContext))
        }
        mRepeatStrategy = repeatStrategy
        mDecodeExecutors =
                ThreadPoolExecutor(0, 16, 10000,
                        TimeUnit.MILLISECONDS, SynchronousQueue<Runnable>())
        decodeThread = Thread {
            while (isWorking && !isStopping) {
                decodeBitmap()
            }
            mDecodeExecutors!!.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS)
            clearAndStop()
        }
        decodeThread?.start()
    }

    private fun setState(newState:Int){
        state.set(newState)
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
        if (restartNextDecode) {
            mIndex.set(0)
            restartNextDecode = false
        }
        val mCountDownLatch = CountDownLatch(5)
        repeat(5) {
            try {
                mDecodeExecutors?.execute {
                    val index = mIndex.getAndIncrement()
                    val decoder = mDecoderPool.poll() ?: DefaultBitmapDecoder(mContext)
                    if (!Thread.currentThread().isInterrupted) {
                        val path = mRepeatStrategy?.getNextFrameResource(index)
                        if (isStopping) {
                            return@execute
                        }
                        if (path == null) {
                            releaseWhenEmpty()
                            mDecoderPool.offer(decoder)
                            mCountDownLatch.countDown()
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
                //e.printStackTrace()
            }
        }
        try {
            mCountDownLatch.await()
        } catch (e: InterruptedException) {
            //e.printStackTrace()
        }
        insertPool(tempMap)
    }

    private fun releaseWhenEmpty() {
        releaseWhenEmpty = true
    }

    /**
     * clear the resource
     * call this method When all the threads stop
     */
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
            start(tempStrategy!!, 0)
        }
    }

    private fun insertPool(map: ConcurrentHashMap<Int, Bitmap?>) {
        if (!isWorking || isStopping) {
            map.clear()
            return
        }
        //sort by index
        map.keys().toList().sorted().forEach {
            try {
                if (isWorking && !isStopping) {
                    mPool.put(map[it])
                }
            } catch (e: InterruptedException) {
                //e.printStackTrace()
            }
        }
        map.clear()
    }

    override fun take(): Bitmap? {
        if (releaseWhenEmpty) {
            if (mPool.isEmpty()) {
                release()
                mInteractionListener?.stopAnimationFromPool()
                return null
            }
        }
        return try {
            if (isWorking && !isStopping) {
                mPool.take()
            } else {
                null
            }
        } catch (e: InterruptedException) {
            //e.printStackTrace()
            null
        }
    }

    override fun recycle(bitmap: Bitmap) {
        if (!isWorking || skipInBitmapCount > mIndex.get() || isStopping) {
            return
        }
        mInBitmapPool.offer(WeakReference(bitmap))
    }

    override fun setInteractionListener(listener: AnimationInteractionListener?) {
        mInteractionListener = listener
    }

    override fun getRepeatStrategy(): RepeatStrategy? {
        return mRepeatStrategy
    }

    override fun release() {
        if (isStopping || !isWorking) {
            return
        }
        releaseWhenEmpty = false
        isStopping = true
        decodeThread?.interrupt()
        mDecodeExecutors?.shutdownNow()
    }
}