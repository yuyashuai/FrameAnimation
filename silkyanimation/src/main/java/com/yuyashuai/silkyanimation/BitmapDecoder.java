package com.yuyashuai.silkyanimation;

import android.graphics.Bitmap;
import android.support.annotation.Nullable;

import java.io.File;
import java.util.List;

/**
 * @author yuyashuai   2018/7/31.
 */
public interface BitmapDecoder {

    /**
     * get the bitmap surface need
     *
     * @return the last frame bitmap
     */
    @Nullable
    Bitmap get();

    /**
     * notify decode the bitmap of next frame
     */
    void notifyUpdate();

    /**
     * recycle all resources
     */
    void stopAndRecycle();

    /**
     * start decode bitmap to memory
     *
     * @param startOffset the start position of decoding
     * @param files    the resources directory
     */
    void startDecodeFilesDirectory(int startOffset, File... files);

    /**
     * start decode bitmap to memory
     *
     * @param startOffset the start position of decoding
     * @param files    the assets resources directory
     */
    void startDecodeAssetsDirectory(int startOffset, String... files);

    /**
     * start decode bitmap to memory
     *
     * @param startOffset     the start position of decoding
     * @param filePathList the absolutely path of all frame bitmaps
     */
    void startDecodeFilesPath(int startOffset, List<String> filePathList);

    /**
     * start decode bitmap to memory
     *
     * @param startOffset     the start position of decoding
     * @param filePathList the absolutely path of all frame bitmaps
     */
    void startDecodeAssetsPath(int startOffset, List<String> filePathList);

    /**
     * set number of frames resides in memory
     *
     * @param count number of frames resides in memory.
     */
    void setMemoryCacheCount(int count);

}
