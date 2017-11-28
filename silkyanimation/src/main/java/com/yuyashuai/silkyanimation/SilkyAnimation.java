package com.yuyashuai.silkyanimation;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.IntDef;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.util.Log;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by yuyashuai on 2016/11/28 0028.
 * use SurfaceView play Frame Animation
 */

public final class SilkyAnimation {

    /**
     * 缓存的图片
     */
    private final SparseArray<Bitmap> mBitmapCache;
    private SurfaceView mSurfaceView;
    private SurfaceHolder mSurfaceHolder;
    /**
     * 存储图片的所有路径
     */
    private List<String> mPathList;
    private MyCallBack mCallBack;
    private int mode = MODE_ONCE;
    /**
     * 是否从asset中读取资源
     */
    private boolean isAssetResource = false;
    private AssetManager mAssetManager;
    private final String TAG = "SilkyAnimation";
    private Matrix mDrawMatrix;
    private int mScaleType;
    private Context mContext;
    /**
     * total frames.
     */
    private int mTotalCount;

    /**
     * handler of the thread that in charge of loading bitmap.
     */
    private Handler mDecodeHandler;

    /**
     * time interval between two frames.
     */
    private int mFrameInterval = 100;
    /**
     * number of frames resides in memory. real cache count
     */
    private int mCacheCount = 5;

    /**
     * pass cache count
     */
    private int mPassCacheCount = 5;
    /**
     * callback of animation state.
     */
    private AnimationStateListener mAnimationStateListener;

    /**
     * callback of unexcepted stop
     */
    private UnexceptedStopListener mUnexceptedListener;

    /**
     * start animation command.
     */
    private final int CMD_START_ANIMATION = -1;

    /**
     * stop animation command.
     */
    private final int CMD_STOP_ANIMATION = -2;

    /**
     * Repeat the animation once.
     */
    public static final int MODE_ONCE = 1;
    /**
     * Repeat the animation indefinitely.
     */
    public static final int MODE_INFINITE = 2;

    private SilkyAnimation() {
        mBitmapCache = new SparseArray<>();
    }

    @IntDef({MODE_INFINITE, MODE_ONCE})
    @Retention(RetentionPolicy.SOURCE)
    public @interface RepeatMode {
    }

    /**
     * 给定的matrix
     */
    private final int SCALE_TYPE_MATRIX = 0;
    /**
     * 完全拉伸，不保持原始图片比例，铺满
     */
    public static final int SCALE_TYPE_FIT_XY = 1;

    /**
     * 保持原始图片比例，整体拉伸图片至少填充满X或者Y轴的一个
     * 并最终依附在视图的上方或者左方
     */
    public static final int SCALE_TYPE_FIT_START = 2;

    /**
     * 保持原始图片比例，整体拉伸图片至少填充满X或者Y轴的一个
     * 并最终依附在视图的中心
     */
    public static final int SCALE_TYPE_FIT_CENTER = 3;

    /**
     * 保持原始图片比例，整体拉伸图片至少填充满X或者Y轴的一个
     * 并最终依附在视图的下方或者右方
     */
    public static final int SCALE_TYPE_FIT_END = 4;

    /**
     * 将图片置于视图中央，不缩放
     */
    public static final int SCALE_TYPE_CENTER = 5;

    /**
     * 整体缩放图片，保持原始比例，将图片置于视图中央，
     * 确保填充满整个视图，超出部分将会被裁剪
     */
    public static final int SCALE_TYPE_CENTER_CROP = 6;

    /**
     * 整体缩放图片，保持原始比例，将图片置于视图中央，
     * 确保X或者Y至少有一个填充满屏幕
     */
    public static final int SCALE_TYPE_CENTER_INSIDE = 7;

    /**
     * 第一帧动画的偏移量
     */
    private int startOffset = 0;

    @IntDef({SCALE_TYPE_FIT_XY, SCALE_TYPE_FIT_START, SCALE_TYPE_FIT_CENTER, SCALE_TYPE_FIT_END,
            SCALE_TYPE_CENTER, SCALE_TYPE_CENTER_CROP, SCALE_TYPE_CENTER_INSIDE})
    @Retention(RetentionPolicy.SOURCE)
    public @interface ScaleType {

    }

    public static class Builder {

        private SilkyAnimation mAnimation;

        @Deprecated
        public Builder(@NonNull SurfaceView surfaceView, @NonNull List<String> pathList) {
            mAnimation = new SilkyAnimation();
            mAnimation.init(surfaceView);
            mAnimation.initPathList(pathList);
        }

        /**
         * @param surfaceView
         * @param assetPath   asset resource path, must be a directory
         */
        @Deprecated
        public Builder(@NonNull SurfaceView surfaceView, @NonNull String assetPath) {
            mAnimation = new SilkyAnimation();
            mAnimation.init(surfaceView);
            mAnimation.initPathList(mAnimation.getPathList(assetPath));
        }

        /**
         * @param surfaceView
         * @param file        must be a directory
         */
        @Deprecated
        public Builder(@NonNull SurfaceView surfaceView, @NonNull File file) {
            mAnimation = new SilkyAnimation();
            mAnimation.init(surfaceView);
            mAnimation.initPathList(mAnimation.getPathList(file));
        }

        /**
         * @param surfaceView
         */
        public Builder(@NonNull SurfaceView surfaceView) {
            mAnimation = new SilkyAnimation();
            mAnimation.init(surfaceView);
        }

        /**
         * set time interval between two frames.
         *
         * @param timeMillisecond time interval between two frames.
         * @return
         */
        public Builder setFrameInterval(@IntRange(from = 1) int timeMillisecond) {
            mAnimation.setFrameInterval(timeMillisecond);
            return this;
        }

        /**
         * set number of frames resides in memory
         *
         * @param count number of frames resides in memory.
         * @return
         */
        public Builder setCacheCount(@IntRange(from = 1) int count) {
            mAnimation.setCacheCount(count);
            return this;
        }

        /**
         * set Matrix
         *
         * @param matrix matrix hold the shape
         * @return
         */
        public Builder setMatrix(@NonNull Matrix matrix) {
            mAnimation.setMatrix(matrix);
            return this;
        }

        /**
         * 设置AnimationStateListener
         *
         * @param listener
         * @return
         */
        public Builder setAnimationListener(@NonNull AnimationStateListener listener) {
            mAnimation.setAnimationStateListener(listener);
            return this;
        }

        public Builder setUnexceptedStopListener(@NonNull UnexceptedStopListener listener) {
            mAnimation.setUnexceptedStopListener(listener);
            return this;
        }

        /**
         * set repeat mode
         *
         * @param mode
         * @return
         */
        public Builder setRepeatMode(@RepeatMode int mode) {
            mAnimation.setRepeatMode(mode);
            return this;
        }

        /**
         * set scale type,same as ImageView
         *
         * @param type
         * @return
         */
        public Builder setScaleType(@ScaleType int type) {
            mAnimation.setScaleType(type);
            return this;
        }

        public SilkyAnimation build() {
            return mAnimation;
        }

    }

    /**
     * 初始化
     *
     * @param surfaceView
     */
    private void init(SurfaceView surfaceView) {
        this.mSurfaceView = surfaceView;
        this.mSurfaceHolder = surfaceView.getHolder();
        mContext = surfaceView.getContext();
        mDrawMatrix = new Matrix();
        mScaleType = SCALE_TYPE_FIT_CENTER;
        mCallBack = new MyCallBack();
        mSurfaceHolder.setFormat(PixelFormat.TRANSLUCENT);
        mSurfaceView.setZOrderOnTop(true);
        mSurfaceHolder.addCallback(mCallBack);
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
            List<String> mAssertList = Arrays.asList(assetFiles);
            isAssetResource = true;
            setAssetManager(assetManager);
            return mAssertList;
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
        isAssetResource = false;
        return list;
    }

    private void initPathList(List<String> pathList) {
        this.mPathList = pathList;
        //theoretically this must be not null, this exception will never happen
        if (mPathList == null) {
            throw new NullPointerException("pathList is null. ensure you have configured the resources correctly");
        }
        mCacheCount = mPassCacheCount;
        if (mCacheCount > mPathList.size()) {
            mCacheCount = mPathList.size();
        }
        Collections.sort(pathList);
    }

    /**
     * start animation
     *
     * @param file the resources directory
     */
    public void start(File file) {
        if (mCallBack.isDrawing) {
            stop();
        }
        initPathList(getPathList(file));
        start(0);
    }

    /**
     * start animation
     *
     * @param assetsPath assets resource path, must be a directory
     */
    public void start(String assetsPath) {
        if (mCallBack.isDrawing) {
            stop();
        }
        initPathList(getPathList(assetsPath));
        start(0);
    }

    /**
     * start animation
     *
     * @param file     the resources directory
     * @param position start offset
     */
    public void start(File file, int position) {
        if (mCallBack.isDrawing) {
            stop();
        }
        initPathList(getPathList(file));
        start(position);
    }

    /**
     * start animation
     *
     * @param assetsPath assets resource path, must be a directory
     * @param position   start offset
     */
    public void start(String assetsPath, int position) {
        if (mCallBack.isDrawing) {
            stop();
        }
        initPathList(getPathList(assetsPath));
        start(position);
    }

    /**
     * start animation ,if you call this directly, you must initial the resources
     * from{@link Builder#Builder(SurfaceView, File)} or {@link Builder#Builder(SurfaceView, String)} or {@link Builder#Builder(SurfaceView, List)}
     */
    @Deprecated
    public void start() {
        if (mCallBack.isDrawing) {
            stop();
        }
        start(0);
    }

    /**
     * start animation ,if you call this directly, you must initial the resources
     * from{@link Builder#Builder(SurfaceView, File)} or {@link Builder#Builder(SurfaceView, String)} or {@link Builder#Builder(SurfaceView, List)}
     *
     * @param position start offset
     */
    public void start(int position) {
        if (mCallBack.isDrawing) {
            stop();
        }
        startOffset = position;
        if (mPathList == null) {
            throw new NullPointerException("the frame list is null. did you have configured the resources? if not please call start(file) or start(assetsPath)");
        }
        if (mPathList.isEmpty()) {
            Log.e(TAG, "pathList is empty, nothing to display. ensure you have configured the resources correctly. check you file or assets dir ");
            return;
        }
        if (startOffset >= mPathList.size()) {
            throw new IndexOutOfBoundsException("invalid startOffset index " + position + ", size is " + mPathList.size());
        }
        //从文件中读取
        if (!isAssetResource) {
            File file = new File(mPathList.get(0));
            if (!file.exists()) {
                return;
            }
        }
        mTotalCount = mPathList.size();
        startDecodeThread();
    }


    private void setAssetManager(AssetManager assetManager) {
        this.mAssetManager = assetManager;
    }

    private void setFrameInterval(int time) {
        if (time < 1) {
            throw new IllegalArgumentException("illegal interval");
        }
        this.mFrameInterval = time;
    }

    /**
     * 给定绘制bitmap的matrix不能和设置ScaleType同时起作用
     *
     * @param matrix 绘制bitmap时应用的matrix
     */
    public void setMatrix(@NonNull Matrix matrix) {
        if (matrix == null) {
            throw new NullPointerException("matrix can not be null");
        }
        mDrawMatrix = matrix;
        mScaleType = SCALE_TYPE_MATRIX;
    }

    public void stop() {
        if (!isDrawing()) {
            return;
        }
        mCallBack.stopAnim();
    }

    private void setScaleType(int type) {
        if (type < SCALE_TYPE_FIT_XY || type > SCALE_TYPE_CENTER_INSIDE) {
            throw new IllegalArgumentException("Illegal ScaleType");
        }
        if (mScaleType != type) {
            mScaleType = type;
        }
    }

    private int mLastFrameWidth = -1;
    private int mLastFrameHeight = -1;
    private int mLastFrameScaleType = -1;
    private int mLastSurfaceWidth;
    private int mLastSurfaceHeight;

    /**
     * 根据ScaleType配置绘制bitmap的Matrix
     *
     * @param bitmap
     */
    private void configureDrawMatrix(Bitmap bitmap) {
        final int srcWidth = bitmap.getWidth();
        final int dstWidth = mSurfaceView.getWidth();
        final int srcHeight = bitmap.getHeight();
        final int dstHeight = mSurfaceView.getHeight();
        final boolean nothingChanged =
                srcWidth == mLastFrameWidth
                        && srcHeight == mLastFrameHeight
                        && mLastFrameScaleType == mScaleType
                        && mLastSurfaceWidth == dstWidth
                        && mLastSurfaceHeight == dstHeight;
        if (nothingChanged) {
            return;
        }
        mLastFrameScaleType = mScaleType;
        mLastFrameHeight = bitmap.getHeight();
        mLastFrameWidth = bitmap.getWidth();
        mLastSurfaceHeight = mSurfaceView.getHeight();
        mLastSurfaceWidth = mSurfaceView.getWidth();
        if (mScaleType == SCALE_TYPE_MATRIX) {
            return;
        } else if (mScaleType == SCALE_TYPE_CENTER) {
            mDrawMatrix.setTranslate(
                    Math.round((dstWidth - srcWidth) * 0.5f),
                    Math.round((dstHeight - srcHeight) * 0.5f));
        } else if (mScaleType == SCALE_TYPE_CENTER_CROP) {
            float scale;
            float dx = 0, dy = 0;
            //按照高缩放
            if (dstHeight * srcWidth > dstWidth * srcHeight) {
                scale = (float) dstHeight / (float) srcHeight;
                dx = (dstWidth - srcWidth * scale) * 0.5f;
            } else {
                scale = (float) dstWidth / (float) srcWidth;
                dy = (dstHeight - srcHeight * scale) * 0.5f;
            }
            mDrawMatrix.setScale(scale, scale);
            mDrawMatrix.postTranslate(dx, dy);
        } else if (mScaleType == SCALE_TYPE_CENTER_INSIDE) {
            float scale;
            float dx;
            float dy;
            //小于dst时不缩放
            if (srcWidth <= dstWidth && srcHeight <= dstHeight) {
                scale = 1.0f;
            } else {
                scale = Math.min((float) dstWidth / (float) srcWidth,
                        (float) dstHeight / (float) srcHeight);
            }
            dx = Math.round((dstWidth - srcWidth * scale) * 0.5f);
            dy = Math.round((dstHeight - srcHeight * scale) * 0.5f);

            mDrawMatrix.setScale(scale, scale);
            mDrawMatrix.postTranslate(dx, dy);
        } else {
            RectF srcRect = new RectF(0, 0, bitmap.getWidth(), bitmap.getHeight());
            RectF dstRect = new RectF(0, 0, mSurfaceView.getWidth(), mSurfaceView.getHeight());
            mDrawMatrix.setRectToRect(srcRect, dstRect, MATRIX_SCALE_ARRAY[mScaleType - 1]);
        }
    }

    private static final Matrix.ScaleToFit[] MATRIX_SCALE_ARRAY = {
            Matrix.ScaleToFit.FILL,
            Matrix.ScaleToFit.START,
            Matrix.ScaleToFit.CENTER,
            Matrix.ScaleToFit.END
    };

    private void setCacheCount(int count) {
        mPassCacheCount = count;
    }

    private void setRepeatMode(@RepeatMode int mode) {
        this.mode = mode;
    }

    public boolean isDrawing() {
        return mCallBack.isDrawing;
    }

    public void setAnimationStateListener(AnimationStateListener animationStateListener) {
        this.mAnimationStateListener = animationStateListener;
    }

    public void setUnexceptedStopListener(UnexceptedStopListener unexceptedStopListener) {
        this.mUnexceptedListener = unexceptedStopListener;
    }

    /**
     * Animation状态监听
     */
    public interface AnimationStateListener {
        /**
         * 动画开始
         */
        void onStart();

        /**
         * 动画结束
         */
        void onFinish();
    }

    /**
     * 异常停止监听
     */
    public interface UnexceptedStopListener {
        /**
         * 异常停止时触发，比如home键被按下，直接锁屏，旋转屏幕等
         * 记录此位置后，可以通过调用{@link #start(int)}恢复动画
         *
         * @param position 异常停止时，帧动画播放的位置
         */
        void onUnexceptedStop(int position);
    }

    private class MyCallBack implements SurfaceHolder.Callback {
        private Canvas mCanvas;
        private int position;
        private boolean isDrawing = false;
        private Thread drawThread;

        @Override
        public void surfaceCreated(SurfaceHolder holder) {
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            if (isDrawing) {
                stopAnim();
                if (mUnexceptedListener != null) {
                    mUnexceptedListener.onUnexceptedStop(getCorrectPosition());
                }
            }
        }

        /**
         * 绘制
         */
        private void drawBitmap() {
            //当循环播放时，获取真实的position
            if (mode == MODE_INFINITE && position >= mTotalCount) {
                position = position % mTotalCount;
            }
            if (position >= mTotalCount) {
                mDecodeHandler.sendEmptyMessage(CMD_STOP_ANIMATION);
                clearSurface();
                return;
            }
            if (mBitmapCache.get(position, null) == null) {
                Log.e(TAG, "get bitmap in position: " + position + " is null ,animation was forced to stop");
                stopAnim();
                return;
            }
            final Bitmap currentBitmap = mBitmapCache.get(position);
            mDecodeHandler.sendEmptyMessage(position);
            mCanvas = mSurfaceHolder.lockCanvas();
            if (mCanvas == null) {
                return;
            }
            mCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
            configureDrawMatrix(currentBitmap);
            mCanvas.drawBitmap(currentBitmap, mDrawMatrix, null);
            mSurfaceHolder.unlockCanvasAndPost(mCanvas);
            currentBitmap.recycle();
            position++;
        }

        private void clearSurface() {
            try {
                mCanvas = mSurfaceHolder.lockCanvas();
                if (mCanvas != null) {
                    mCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                    mSurfaceHolder.unlockCanvasAndPost(mCanvas);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private void startAnim() {
            if (mAnimationStateListener != null) {
                mAnimationStateListener.onStart();
            }
            isDrawing = true;
            position = startOffset;
            //绘制线程
            drawThread = new Thread() {
                @Override
                public void run() {
                    super.run();
                    while (isDrawing) {
                        try {
                            long now = System.currentTimeMillis();
                            drawBitmap();
                            //控制两帧之间的间隔
                            sleep(mFrameInterval - (System.currentTimeMillis() - now) > 0 ? mFrameInterval - (System.currentTimeMillis() - now) : 0);
                        } catch (InterruptedException e1) {
                            e1.printStackTrace();
                        }
                    }
                }
            };
            drawThread.start();
        }

        private int getCorrectPosition() {
            if (mode == MODE_INFINITE && position >= mTotalCount) {
                return position % mTotalCount;
            }
            return position;
        }

        private void stopAnim() {
            if (!isDrawing) {
                return;
            }
            isDrawing = false;
            position = 0;
            mBitmapCache.clear();
            clearSurface();
            if (mDecodeHandler != null) {
                mDecodeHandler.sendEmptyMessage(CMD_STOP_ANIMATION);
            }
            if (drawThread != null) {
                drawThread.interrupt();
            }
            if (mAnimationStateListener != null) {
                mAnimationStateListener.onFinish();
            }

        }
    }

    /**
     * decode线程
     */
    private void startDecodeThread() {
        new Thread() {
            @Override
            public void run() {
                super.run();
                Looper.prepare();

                mDecodeHandler = new Handler(Looper.myLooper()) {
                    @Override
                    public void handleMessage(Message msg) {
                        super.handleMessage(msg);
                        if (msg.what == CMD_STOP_ANIMATION) {
                            decodeBitmap(CMD_STOP_ANIMATION);
                            getLooper().quit();
                            return;
                        }
                        decodeBitmap(msg.what);
                    }
                };
                decodeBitmap(CMD_START_ANIMATION);
                Looper.loop();
            }
        }.start();
    }

    /**
     * 根据不同指令 进行不同操作，
     * 根据position的位置来缓存position后指定数量的图片
     *
     * @param position 小于0时，为handler发出的命令. 大于0时为当前帧
     */
    private void decodeBitmap(int position) {
        Log.i(TAG, "decode bitmap : " + position + "  " + mPathList.size() + "  " + startOffset);
        if (position == CMD_START_ANIMATION) {
            //初始化存储
            for (int i = startOffset; i < mCacheCount + startOffset; i++) {
                int putPosition = i;
                if (putPosition > mTotalCount - 1) {
                    putPosition = putPosition % mTotalCount;
                    Log.i(TAG, "huancun: " + putPosition + "   " + i);
                }
                mBitmapCache.put(putPosition, decodeBitmapReal(mPathList.get(putPosition)));
            }
            mCallBack.startAnim();
        } else if (position == CMD_STOP_ANIMATION) {
            mCallBack.stopAnim();
        } else if (mode == MODE_ONCE) {
            if (position + mCacheCount <= mTotalCount - 1) {
                mBitmapCache.remove(position);
                mBitmapCache.put(position + mCacheCount, decodeBitmapReal(mPathList.get(position + mCacheCount)));
            }
        } else if (mode == MODE_INFINITE) {
            if (position + mCacheCount > mTotalCount - 1) {
                mBitmapCache.remove(position);
                mBitmapCache.put((position + mCacheCount) % mTotalCount, decodeBitmapReal(mPathList.get((position + mCacheCount) % mTotalCount)));
            } else {
                mBitmapCache.remove(position);
                mBitmapCache.put(position + mCacheCount, decodeBitmapReal(mPathList.get(position + mCacheCount)));
            }
        }
    }

    /**
     * 根据不同的情况，选择不同的加载方式
     *
     * @param path
     * @return
     */
    private Bitmap decodeBitmapReal(String path) {
        if (isAssetResource) {
            try {
                return BitmapFactory.decodeStream(mAssetManager.open(path));
            } catch (IOException e) {
                stop();
                e.printStackTrace();
            }
        } else {
            return BitmapFactory.decodeFile(path);
        }
        return null;
    }
}
