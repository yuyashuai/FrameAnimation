package com.yuyashuai.silkyanimation;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.util.SparseArray;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.yuyashuai.silkyanimation.SilkyAnimation.MODE_INFINITE;
import static com.yuyashuai.silkyanimation.SilkyAnimation.MODE_ONCE;

/**
 * @author yuyashuai   2018/7/31.
 */
public class BitmapDecoderImpl implements BitmapDecoder {
    private SparseArray<Bitmap> mBitmapList;
    private final Context mContext;
    private boolean isAssetResouces = false;
    private List<String> mPathList;
    private int position = 0;
    private AssetManager mAssetManager;
    private final String TAG = "BitmapDecoderImpl";
    private boolean isSupportInBitmap = true;
    private int mCacheCount = 5;
    private int mTotalFramesCount;
    private Handler mDecodeHandler;
    private int startOffset;
    private final DecoderListener mDecoderListener;
    private int mode;

    public BitmapDecoderImpl(Context context, DecoderListener listener) {
        mContext = context;
        mBitmapList = new SparseArray<>();
        mPathList = new ArrayList<>();
        mAssetManager = context.getAssets();
        mDecoderListener = listener;
    }

    @Override
    public Bitmap get() {
        return mBitmapList.get(position, null);
    }

    @Override
    public void notifyUpdate() {

    }

    @Override
    public void stopAndRecycle() {

    }

    @Override
    public void startDecodeFilesDirectory(int startOffset, File... files) {
        isAssetResouces = false;
        mPathList.clear();
        for (File file : files) {
            mPathList.addAll(getPathList(file));
        }
        initDecodeBitmap();
    }

    @Override
    public void startDecodeAssetsDirectory(int startOffset, String... files) {
        isAssetResouces = true;
        mPathList.clear();
        for (String assetsFile : files) {
            mPathList.addAll(getPathList(assetsFile));
        }
        initDecodeBitmap();
    }

    @Override
    public void startDecodeFilesPath(int startOffset, List<String> filePathList) {
        isAssetResouces = true;
        mPathList.clear();
        mPathList.addAll(filePathList);
        initDecodeBitmap();
    }

    @Override
    public void startDecodeAssetsPath(int startOffset, List<String> filePathList) {
        isAssetResouces = true;
        mPathList.clear();
        mPathList.addAll(filePathList);
        initDecodeBitmap();
    }

    /**
     * 通过assets资源转换pathList
     *
     * @param assetsPath assets resource path, must be a directory
     * @return if assets  does not exist return a empty list
     */
    private List<String> getPathList(String assetsPath) {
        AssetManager assetManager = mContext.getAssets();
        try {
            String[] assetFiles = assetManager.list(assetsPath);
            if (assetFiles.length == 0) {
                Log.e(TAG, "no file in this asset directory");
                return new ArrayList<>(0);
            }
            //转换真实路径
            for (int i = 0; i < assetFiles.length; i++) {
                assetFiles[i] = assetsPath + File.separator + assetFiles[i];
            }
            return Arrays.asList(assetFiles);
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
            e.printStackTrace();
        }
        return new ArrayList<>(0);
    }

    /**
     * 通过File资源转换pathList
     *
     * @param file the resources directory
     * @return if file does not exist return a empty list
     */
    private List<String> getPathList(File file) {
        List<String> list = new ArrayList<>();
        if (file != null) {
            if (file.exists() && file.isDirectory()) {
                File[] files = file.listFiles();
                for (File mFrameFile : files) {
                    list.add(mFrameFile.getAbsolutePath());
                }
            } else if (!file.exists()) {
                Log.e(TAG, "file doesn't exists");
            } else {
                Log.e(TAG, "file isn't a directory");
            }
        } else {
            Log.e(TAG, "file is null");
        }
        return list;
    }


    @Override
    public void setMemoryCacheCount(int count) {
        if (mCacheCount < 1) {
            mCacheCount = 1;
        } else {
            mCacheCount = count;
        }
    }

    private void startDecodeThread() {
        new Thread() {
            @Override
            public void run() {
                super.run();
                Looper.prepare();
                mDecodeHandler = new Handler(Looper.myLooper());
                initDecodeBitmap();
                Looper.loop();
            }
        }.start();
    }

    /**
     * in bitmap，避免频繁的GC
     */
    private Bitmap mInBitmap = null;
    /**
     * 作为一个标志位来标志是否应该初始化或者更新inBitmap，
     * 因为SurfaceView的双缓存机制，不能绘制完成直接就覆盖上一个bitmap
     * 此时surfaceView还没有post上一帧的数据，导致覆盖bitmap之后出现显示异常
     */
    private int mInBitmapFlag = 0;

    /**
     * 传入inBitmap时的decode参数
     */
    private BitmapFactory.Options mOptions;

    /**
     * 初始化加载
     */
    private void initDecodeBitmap() {
        if (isSupportInBitmap) {
            mOptions = new BitmapFactory.Options();
            mOptions.inMutable = true;
            mOptions.inSampleSize = 1;
        }
        for (int i = startOffset; i < mCacheCount + startOffset; i++) {
            int putPosition = i;
            if (putPosition > mTotalFramesCount - 1) {
                putPosition = putPosition % mTotalFramesCount;
            }
            mBitmapList.put(putPosition, decodeBitmapReal(mPathList.get(putPosition)));
        }
        mDecoderListener.onPrepared();
    }

    /**
     * 根据不同指令 进行不同操作，
     * 根据position的位置来缓存position后指定数量的图片
     *
     * @param position 小于0时，为handler发出的命令. 大于0时为当前帧
     */
    private void decodeBitmap(int position) {
        if (mode == MODE_ONCE) {
            if (position + mCacheCount <= mTotalFramesCount - 1) {
                //由于surface的双缓冲，不能直接复用上一帧的bitmap，因为上一帧的bitmap可能还没有post
                writeInBitmap(position);
                mBitmapList.put(position + mCacheCount, decodeBitmapReal(mPathList.get(position + mCacheCount)));
            }
            //循环播放
        } else if (mode == MODE_INFINITE) {
            //由于surface的双缓冲，不能直接复用上一帧的bitmap，上一帧的bitmap可能还没有post
            writeInBitmap(position);
            //播放到尾部时，取mod
            if (position + mCacheCount > mTotalFramesCount - 1) {
                mBitmapList.put((position + mCacheCount) % mTotalFramesCount, decodeBitmapReal(mPathList.get((position + mCacheCount) % mTotalFramesCount)));
            } else {
                mBitmapList.put(position + mCacheCount, decodeBitmapReal(mPathList.get(position + mCacheCount)));
            }
        }
    }

    /**
     * 更新inBitmap
     *
     * @param position
     */
    private void writeInBitmap(int position) {
        if (!isSupportInBitmap) {
            mBitmapList.remove(position);
            return;
        }
        mInBitmapFlag++;
        if (mInBitmapFlag > 1) {
            int writePosition = position - 2;
            //得到正确的position
            if (writePosition < 0) {
                writePosition = mTotalFramesCount + writePosition;
            }
            mInBitmap = mBitmapList.get(writePosition);
            mBitmapList.remove(writePosition);
        }
    }

    /**
     * 根据不同的情况，选择不同的加载方式
     *
     * @param path
     * @return
     */
    private Bitmap decodeBitmapReal(String path) {
        if (mInBitmap != null) {
            mOptions.inBitmap = mInBitmap;
        }
        if (isAssetResouces) {
            try {
                Bitmap bitmap = BitmapFactory.decodeStream(mAssetManager.open(path), null, mOptions);
                return bitmap;
            } catch (IOException e) {
                stopAndRecycle();
                e.printStackTrace();
            } catch (IllegalArgumentException e) {
                if (e.getMessage().contains("Problem decoding into existing bitmap") && isSupportInBitmap) {
                    Log.e(TAG, "Make sure the resolution of all images is the same, if not call 'setSupportInBitmap(false)'.\n but this will lead to frequent gc ");
                }
                throw e;
            }
        } else {
            return BitmapFactory.decodeFile(path, mOptions);
        }
        return null;
    }

}
