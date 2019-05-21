package com.yuyashuai.frameanimation.io

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
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
            checkException(e)
        }
        return null
    }

    private fun checkException(e: Exception) {
        if (e.message?.contains("Problem decoding into existing bitmap") == true) {
            Log.e(
                TAG,
                "Make sure the resolution of all images is the same, if not call 'setSupportInBitmap(false)'.\n but this will lead to frequent gc "
            )
        }
        throw e
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
            checkException(e)
        }
        return null
    }
}