package com.yuyashuai.frameanimation

import android.content.Context
import android.util.Log
import java.io.File
import java.io.IOException
import java.util.*

/**
 * @author yuyashuai   2019-04-25.
 */
object FrameAnimationUtil {
    private val TAG = javaClass.simpleName
    /**
     * get the path list from assets
     *
     * @param assetsPath assets resource path, must be a directory
     * @return if assets does not exist return a empty list
     */
    fun getPathList(context: Context, assetsPath: String): MutableList<FrameAnimation.PathData> {
        val assetManager = context.assets
        try {
            val assetFiles = assetManager.list(assetsPath)
            if (assetFiles.isNullOrEmpty()) {
                Log.e(TAG, "no file in this asset directory")
                return mutableListOf()
            }
            //转换真实路径
            for (i in assetFiles.indices) {
                assetFiles[i] = assetsPath + File.separator + assetFiles[i]
            }
            return assetFiles.map { FrameAnimation.PathData(it, FrameAnimation.PATH_ASSETS) }.toMutableList()
        } catch (e: IOException) {
            Log.e(TAG, e.message)
            e.printStackTrace()
        }
        return mutableListOf()
    }

    /**
     * get the path list from file
     *
     * @param file the resources directory
     * @return if file does not exist return a empty list
     */
    fun getPathList(file: File?): MutableList<FrameAnimation.PathData> {
        val list = mutableListOf<FrameAnimation.PathData>()
        if (file != null) {
            if (file.exists() && file.isDirectory) {
                val files = file.listFiles() ?: return list
                list.addAll(files.map { FrameAnimation.PathData(it.absolutePath, FrameAnimation.PATH_FILE) })
            } else if (!file.exists()) {
                Log.e(TAG, "file doesn't exists")
            } else {
                Log.e(TAG, "file isn't a directory")
            }
        } else {
            Log.e(TAG, "file is null")
        }
        return list
    }

}