package com.yuyashuai.surfaceviewanimation;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.Rect;
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
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by yuyashuai on 2016/11/28 0028.
 * use SurfaceView play Frame Animation
 */

public final class SurfaceViewAnimation {


    private final SparseArray<Bitmap> mBitmapCache;
    private SurfaceView mSurfaceView;
    private SurfaceHolder mSurfaceHolder;
    private List<String> mPathList;
    private MyCallBack mCallBack;
    private int mode = MODE_INFINITE;
    private final String TAG="SurfaceViewAnimation";
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
     * number of frames resides in memory.
     */
    private int mCacheCount = 5;

    /**
     * callback of animation state.
     */
    private AnimationStateListener mAnimationStateListener;

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

    private SurfaceViewAnimation() {
        mBitmapCache=new SparseArray<>();
    }

    @IntDef({MODE_INFINITE, MODE_ONCE})
    @Retention(RetentionPolicy.SOURCE)
    public @interface RepeatMode {
    }

    public static class Builder {

        private final String TAG="SurfaceViewAnimation";

        private SurfaceViewAnimation mAnimation;

        public Builder( @NonNull SurfaceView surfaceView,@NonNull List<String> pathList) {
            mAnimation=new SurfaceViewAnimation();
            mAnimation.init(surfaceView,pathList);
        }

        /**
         *
         * @param surfaceView
         * @param file must be a directory
         */
        public Builder(@NonNull SurfaceView surfaceView,@NonNull File file)
        {
            List<String> list=new ArrayList<>();
            if(file!=null)
            {
                if(file.exists()&&file.isDirectory())
                {
                    File[] files=file.listFiles();
                    for(File mFrameFile:files )
                    {
                        list.add(mFrameFile.getAbsolutePath());
                    }
                }else if(!file.exists())
                {
                    Log.e(TAG,"file doesn't exists, there is no frame to display");
                }else
                {
                    Log.e(TAG,"file isn't a directory, there is no frame to display");
                }
            }else
            {
                Log.e(TAG,"file is null, there is no frame to display");
            }
            mAnimation=new SurfaceViewAnimation();
            mAnimation.init(surfaceView,list);
        }


        public Builder setFrameInterval(int timeMillisecond) {
            mAnimation.setFrameInterval(timeMillisecond);
            return this;
        }

        public Builder setCacheCount(@IntRange(from = 1) int count) {
            mAnimation.setCacheCount(count);
            return this;
        }

        public Builder setAnimationListener(AnimationStateListener listener) {
            mAnimation.setAnimationStateListener(listener);
            return this;
        }

        public Builder setRepeatMode(@RepeatMode int mode) {
            mAnimation.setRepeatMode(mode);
            return this;
        }

        public SurfaceViewAnimation build()
        {
            return mAnimation;
        }

    }

    private void init(SurfaceView surfaceView ,List<String> pathList) {
        this.mSurfaceView = surfaceView;
        this.mSurfaceHolder = surfaceView.getHolder();
        mCallBack = new MyCallBack();
        mSurfaceHolder.setFormat(PixelFormat.TRANSLUCENT);
        mSurfaceView.setZOrderOnTop(true);
        mSurfaceHolder.addCallback(mCallBack);
        this.mPathList=pathList;
    }


    public void start() {
        if (mCallBack.isDrawing) {
            return;
            //stopAnimation();
        }
        if (mPathList == null) {
            throw new NullPointerException("pathList can not be null.");
        }
        if(mPathList.size()==0)
        {
            return;
        }
        File file = new File(mPathList.get(0));
        if (!file.exists()) {
            return;
        }
        mTotalCount = mPathList.size();
        startDecodeThread();
    }

    private void setFrameInterval(int time) {
        this.mFrameInterval = time;
    }

    public void stop() {
        mCallBack.stopAnim();
    }

    private void setCacheCount(int count) {
        mCacheCount = count;
    }

    private void setRepeatMode(@RepeatMode int mode)
    {
        this.mode=mode;
    }

    public boolean isDrawing() {
        return mCallBack.isDrawing;
    }

    private void setAnimationStateListener(AnimationStateListener animationStateListener) {
        this.mAnimationStateListener = animationStateListener;
    }

    public interface AnimationStateListener {
        void onStart();

        void onFinish();
    }

    private class MyCallBack implements SurfaceHolder.Callback {
        private Canvas mCanvas;
        private Bitmap mCurrentBitmap;
        private int position = 0;
        public boolean isDrawing = false;
        private Thread drawThread;
        private Rect rect = new Rect();

        @Override
        public void surfaceCreated(SurfaceHolder holder) {

        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            rect.set(0, 0, width, height);
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {

        }

        private void drawBitmap() {

            if (mode == MODE_INFINITE && position >= mTotalCount) {
                position = position % mTotalCount;
            }

            if (position >= mTotalCount) {
                isDrawing = false;
                mDecodeHandler.sendEmptyMessage(-2);
                //clear surfaceView
                clearSurface();
                return;
            }
            if (mBitmapCache.get(position,null)==null) {
                mCanvas = mSurfaceHolder.lockCanvas();
                if (mCanvas == null) {
                    return;
                }
                //clear surfaceView
                clearSurface();
                return;
            }
            mCurrentBitmap = mBitmapCache.get(position);
            mDecodeHandler.sendEmptyMessage(position);
            mCanvas = mSurfaceHolder.lockCanvas(rect);
            if (mCanvas == null) {
                return;
            }
            mCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
            mCanvas.drawBitmap(mCurrentBitmap, null, rect, null);
            mSurfaceHolder.unlockCanvasAndPost(mCanvas);
            mCurrentBitmap.recycle();
            position++;
        }

        private void clearSurface()
        {
            mCanvas = mSurfaceHolder.lockCanvas();
            //clear surfaceView
            mCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
            mSurfaceHolder.unlockCanvasAndPost(mCanvas);
        }

        private void startAnim() {
            if (mAnimationStateListener != null) {
                mAnimationStateListener.onStart();
            }
            isDrawing = true;
            position = 0;

            drawThread = new Thread() {
                @Override
                public void run() {
                    super.run();
                    while (isDrawing) {
                        try {
                            long now = System.currentTimeMillis();
                            drawBitmap();
                            sleep(mFrameInterval - (System.currentTimeMillis() - now) > 0 ? mFrameInterval - (System.currentTimeMillis() - now) : 0);
                        } catch (InterruptedException e1) {
                            e1.printStackTrace();
                        }
                    }
                }
            };
            drawThread.start();
        }

        private void stopAnim() {
            isDrawing = false;
            position = 0;
            mBitmapCache.clear();
            clearSurface();
            //mPathList.clear();
            drawThread.interrupt();
            if (mAnimationStateListener != null) {
                mAnimationStateListener.onFinish();
            }

        }
    }

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

    private void decodeBitmap(int position) {
        if (position == CMD_START_ANIMATION) {
            for (int i = 0; i < mCacheCount; i++) {
                if(mPathList.size()<=i) break;
                mBitmapCache.put(i, BitmapFactory.decodeFile(mPathList.get(i)));
            }
            mCallBack.startAnim();
        } else if (position == CMD_STOP_ANIMATION) {
            mCallBack.stopAnim();
        } else if (mode == MODE_ONCE) {
            if (position + mCacheCount <= mTotalCount - 1) {
                mBitmapCache.remove(position);
                mBitmapCache.put(position + mCacheCount, BitmapFactory.decodeFile(mPathList.get(position + mCacheCount)));
            }
        } else if (mode == MODE_INFINITE)
        {
            if(position+mCacheCount>mTotalCount-1)
            {
                mBitmapCache.remove(position);
                mBitmapCache.put((position+mCacheCount)%mTotalCount,BitmapFactory.decodeFile(mPathList.get((position+mCacheCount)%mTotalCount)));
            }else
            {
                mBitmapCache.remove(position);
                mBitmapCache.put(position + mCacheCount, BitmapFactory.decodeFile(mPathList.get(position + mCacheCount)));
            }
        }
    }
}
