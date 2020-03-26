package com.yuyashuai.frameanimation.io

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.yuyashuai.frameanimation.repeatmode.RepeatStrategy
import java.lang.Exception
import java.lang.ref.WeakReference
import java.util.concurrent.*
import java.util.concurrent.atomic.AtomicInteger
import kotlin.concurrent.thread
import kotlin.math.min

private const val IDLE = 0
private const val PREPARING = 1
private const val WORKING = 2
private const val STOPPING = 3

/**
 * @author yuyashuai   2019-04-24.
 */
open class BitmapPoolImpl(context: Context) : BitmapPool {
    private val mContext = context

    private val poolSize: Int

    /**
     * the pool store bitmap that has not yet been used
     */
    private val mPool: LinkedBlockingQueue<Bitmap>

    /**
     * the pool store bitmap that will be reused
     */
    private val mInBitmapPool: LinkedBlockingQueue<WeakReference<Bitmap>>

    /**
     * the decode thread pool
     */
    private val mDecodeThreadPool: ThreadPoolExecutor

    /**
     * current bitmap index
     */
    private var mIndex = AtomicInteger()

    private var mRepeatStrategy: RepeatStrategy? = null

    private var dispatcherThread: Thread? = null

    @Volatile
    private var state = IDLE

    private val skipInBitmapCount: Int

    /**
     * permit to access animation
     */
    private val permit = Semaphore(1)

    /**
     * the bitmap decoder for every single thread
     */
    private var decoders: ThreadLocal<BitmapDecoder>? = null

    private val tempMap = ConcurrentHashMap<Int, Bitmap?>()

    private val workQueue: BlockingQueue<Runnable>

    private val TAG = javaClass.simpleName

    private val mCountDownLatch = ReusableCountDownLatch()

    init {
        val cpuCount = Runtime.getRuntime().availableProcessors()
        poolSize = min(cpuCount - 1, 4).coerceAtLeast(2)
        mPool = LinkedBlockingQueue(poolSize)
        mInBitmapPool = LinkedBlockingQueue(poolSize)
        workQueue = ArrayBlockingQueue(poolSize * 2)
        skipInBitmapCount = poolSize
        val ac = AtomicInteger()
        mDecodeThreadPool =
                ThreadPoolExecutor(poolSize, poolSize, 30,
                        TimeUnit.SECONDS, workQueue, ThreadFactory {
                    return@ThreadFactory Thread(it).apply { name = "FA-${ac.getAndIncrement()}DecodeThread" }
                }).apply {
                    allowCoreThreadTimeOut(true)
                }
    }

    override fun start(repeatStrategy: RepeatStrategy, index: Int) {
        if (mDecodeThreadPool.isShutdown) {
            throw IllegalStateException("can't start animation after release")
        }
        //if the pool is running normally, relay to play
        if (state == WORKING) {
            relay(repeatStrategy)
            return
        }
        //Waiting for the aftercare of the last play
        state = PREPARING
        //this should be completed in an instant normally
        //just in case we still set a timeout here
        val success = permit.tryAcquire(100, TimeUnit.MILLISECONDS)
        if (!success) {
            Log.e(TAG, "start failed, get acquire took too long time")
            stop()
            return
        }
        state = WORKING
        mIndex.set(index)
        decoders = ThreadLocal()
        mRepeatStrategy = repeatStrategy
        dispatcherThread = thread(start = true, name = "FA-DispatcherThread") {
            try {
                while (state == WORKING && !Thread.currentThread().isInterrupted) {
                    decodeBitmap()
                }
            } catch (e: InterruptedException) {
                Thread.currentThread().interrupt()
            }
            clearAndStop()
        }
    }


    @Volatile
    private var restartNextDecode = false

    private fun relay(strategy: RepeatStrategy) {
        mRepeatStrategy = strategy
        restartNextDecode = true
    }

    private fun decodeBitmap() {
        if (restartNextDecode) {
            mIndex.set(0)
            restartNextDecode = false
        }
        //val mCountDownLatch = CountDownLatch(poolSize)
        mCountDownLatch.count = poolSize
        for (i in 0 until poolSize) {
            try {
                mDecodeThreadPool.execute {
                    try {
                        val index = mIndex.getAndIncrement()
                        decoders ?: return@execute
                        var decoder = decoders!!.get()
                        if (decoder == null) {
                            decoder = BitmapDecoderImpl(mContext)
                            decoders!!.set(decoder)
                        }
                        val path = mRepeatStrategy?.getNextFrameResource(index)
                        //stop animation
                        if (path == null) {
                            stop()
                            return@execute
                        }
                        val bitmap = decoder.decodeBitmap(path, mInBitmapPool.poll()?.get())
                        if (state == WORKING && !Thread.currentThread().isInterrupted && bitmap != null) {
                            tempMap[index] = bitmap
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    } finally {
                        mCountDownLatch.countDown()
                    }
                }
            } catch (ignore: RejectedExecutionException) {

            }
        }
        mCountDownLatch.await()
        insertPool(tempMap)
    }

    /**
     * clear the resource after animation stop
     */
    private fun clearAndStop() {
        mPool.clear()
        decoders = null
        mRepeatStrategy = null
        mInBitmapPool.clear()
        mIndex.set(0)
        state = IDLE
        tempMap.clear()
        permit.release()
    }

    private fun insertPool(map: ConcurrentHashMap<Int, Bitmap?>) {
        //sort by index
        map.keys().toList().sorted().forEach {
            if (state == WORKING) {
                mPool.put(map[it])
            }
        }
        map.clear()
    }

    override fun take(): Bitmap? {
        return if (state == WORKING || state == PREPARING) {
            mPool.take()
        } else {
            null
        }
    }

    override fun recycle(bitmap: Bitmap) {
        if (state != WORKING || skipInBitmapCount > mIndex.get()) {
            return
        }
        mInBitmapPool.offer(WeakReference(bitmap))
    }

    override fun getRepeatStrategy(): RepeatStrategy? {
        return mRepeatStrategy
    }

    override fun stop() {
        if (state != WORKING) {
            return
        }
        //start stopping
        dispatcherThread?.interrupt()
        state = STOPPING
        workQueue.clear()
    }

    override fun release() {
        stop()
        mDecodeThreadPool.shutdownNow()
    }
}