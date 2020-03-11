package com.yuyashuai.frameanimation.io

import java.util.concurrent.locks.AbstractQueuedSynchronizer

/**
 * @author yuyashuai 2020-03-08 22:10:10
 *
 */
class ReusableCountDownLatch {
    private val sync: Sync

    var count: Int = 0
        set(value) {
            if (value < 1) {
                throw IllegalArgumentException("count < 1")
            }
            //must waiting for the end of last task
            if (sync.getCount() != 0) {
                throw IllegalStateException("last task has not ended")
            }
            sync.setCount(value)
            field = value
        }

    init {
        sync = Sync()
        sync.setCount(count)
    }

    private class Sync() : AbstractQueuedSynchronizer() {

        fun setCount(count: Int) {
            state = count
        }

        fun getCount() = state

        override fun tryAcquireShared(arg: Int): Int {
            return if (state == 0) {
                1
            } else {
                -1
            }
        }

        override fun tryReleaseShared(arg: Int): Boolean {
            while (true) {
                val count = state
                if (count == 0) {
                    return false
                }
                val nextCount = count - 1
                if (compareAndSetState(count, nextCount)) {
                    return nextCount == 0
                }
            }
        }
    }

    fun countDown() {
        sync.releaseShared(1)
    }

    @Throws(InterruptedException::class)
    fun await() {
        try {
            sync.acquireSharedInterruptibly(1)
        } catch (e: InterruptedException) {
            //reset state for next use
            sync.setCount(0)
            throw e
        }
    }
}