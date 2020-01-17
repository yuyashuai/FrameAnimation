package com.yuyashuai.frameanimation.io

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.yuyashuai.frameanimation.FrameAnimation
import java.io.IOException

/**
 * @author yuyashuai   2019-04-24.
 */
open class DefaultBitmapDecoder(context: Context) : BitmapDecoder {
    private val assets = context.assets
    private val TAG = javaClass.simpleName

    override fun decodeBitmap(path: FrameAnimation.PathData, inBitmap: Bitmap?): Bitmap? {
        return when (path.type) {
            FrameAnimation.PATH_FILE -> decodeFileBitmap(path.path, inBitmap)
            FrameAnimation.PATH_ASSETS -> decodeAssetBitmap(path.path, inBitmap)
            else -> null
        }
    }

    override fun decodeBitmap(path: FrameAnimation.PathData): Bitmap? {
        return decodeBitmap(path, null)
    }

    private fun decodeAssetBitmap(assetPath: String, inBitmap: Bitmap?): Bitmap? {
        val options = BitmapFactory.Options()
        options.inSampleSize = 1
        options.inMutable = true
        if (inBitmap != null) {
            options.inBitmap = inBitmap
        }

        try {
            return BitmapFactory.decodeStream(assets.open(assetPath), null, options)
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: IllegalArgumentException) {
            if (e.message?.contains("Problem decoding into existing bitmap") == true) {
                options.inBitmap = null
                return BitmapFactory.decodeStream(assets.open(assetPath), null, options)
            } else {
                throw e
            }
        }
        return null
    }

    private fun decodeFileBitmap(filePath: String, inBitmap: Bitmap?): Bitmap? {
        val options = BitmapFactory.Options()
        if (inBitmap != null) {
            options.inBitmap = inBitmap
        }
        try {
            return BitmapFactory.decodeFile(filePath, options)
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: IllegalArgumentException) {
            if (e.message?.contains("Problem decoding into existing bitmap") == true) {
                options.inBitmap = null
                return BitmapFactory.decodeFile(filePath, options)
            } else {
                throw e
            }
        }
        return null
    }
}