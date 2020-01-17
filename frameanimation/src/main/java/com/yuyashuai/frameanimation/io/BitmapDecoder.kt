package com.yuyashuai.frameanimation.io

import android.graphics.Bitmap
import com.yuyashuai.frameanimation.FrameAnimation

/**
 * @author yuyashuai   2019-04-24.
 * responsible for decoding picture
 */
interface BitmapDecoder {

    /**
     * decoding the bitmap from the specified path
     * don't allocate objects in this method
     * @param path the bitmap path
     * @param inBitmap the reuse bitmap
     * @return null when decode fails
     */
    fun decodeBitmap(path: FrameAnimation.PathData, inBitmap: Bitmap?): Bitmap?

    /**
     * @see decodeBitmap
     */
    fun decodeBitmap(path: FrameAnimation.PathData): Bitmap?
}