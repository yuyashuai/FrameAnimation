package com.yuyashuai.surfaceviewanimation;

import android.content.Context;
import android.content.res.AssetManager;
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
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by yuyashuai on 2016/11/28 0028.
 * use SurfaceView play Frame Animation
 */

public class SurfaceViewAnimation {
    private Map<String,Bitmap> mBitmapCache =new HashMap<>();
    private SurfaceView mSurfaceView;
    private SurfaceHolder mSurfaceHolder;
    private List<String> mPathList;
    private MyCallBack mCallBack;
    private int mTotalCount;
    private Handler mDecodeHandler;
    private Thread mDecodeThread;
    private int mFrameTime=100;
    private int mCacheCount=5;
    private AssetManager assetManager;
    private AnimationStateListener mAnimationStateListener;

    public void setSurfaceView(SurfaceView surfaceView, Context context)
    {
        this.mSurfaceView=surfaceView;
        this.mSurfaceHolder=surfaceView.getHolder();
        mCallBack=new MyCallBack();
        mSurfaceHolder.setFormat(PixelFormat.TRANSLUCENT);
        mSurfaceView.setZOrderOnTop(true);
        mSurfaceHolder.addCallback(mCallBack);
        assetManager=context.getAssets();
    }
    public void startAnimation(List<String> pathList)
    {
        if (mCallBack.isDrawing)
        {
            return;
            //stopAnimation();
        }
        if(pathList==null||pathList.size()<=1)
        {
            return;
        }
        File file=new File(pathList.get(0));
        if(!file.exists())
        {
            return;
        }
        this.mPathList=pathList;
        mTotalCount=pathList.size();
        startDecodeThread();
    }
    public void setFrameTime(int time)
    {
        this.mFrameTime=time;
    }
    public void stopAnimation()
    {
        mCallBack.stopAnim();
    }
    public void setCacheCount(int count)
    {
        mCacheCount=count;
    }
    public boolean getDrawingState()
    {
        return mCallBack.isDrawing;
    }
    public void addAnimationStateListener(AnimationStateListener animationStateListener)
    {
        this.mAnimationStateListener=animationStateListener;
    }
    public interface AnimationStateListener
    {
        void onStart();
        void onFinish();
    }

    private class MyCallBack implements SurfaceHolder.Callback
    {
        private Canvas mCanvas;
        private Bitmap mCurrentBitmap;
        private int position=0;
        public boolean isDrawing=false;
        private Thread drawThread;
        private Rect rect=new Rect();

        @Override
        public void surfaceCreated(SurfaceHolder holder) {

        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            rect.set(0,0,width,height);
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {

        }

        private void drawBitmap()
        {
            if(position>=mTotalCount)
            {
                isDrawing=false;
                mDecodeHandler.sendEmptyMessage(-2);
                mCanvas=mSurfaceHolder.lockCanvas();
                //clear surfaceView
                mCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                mSurfaceHolder.unlockCanvasAndPost(mCanvas);
                return;
            }
            if(!mBitmapCache.containsKey(mPathList.get(position)))
            {
                mCanvas=mSurfaceHolder.lockCanvas();
                if(mCanvas==null)
                {
                    return;
                }
                //clear surfaceView
                mCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                mSurfaceHolder.unlockCanvasAndPost(mCanvas);
                return;
            }
            //System.out.println("开始绘制:   "+position);
            mCurrentBitmap=mBitmapCache.get(mPathList.get(position));
            mDecodeHandler.sendEmptyMessage(position);
            mCanvas=mSurfaceHolder.lockCanvas(rect);
            if(mCanvas==null)
            {
                return;
            }
            mCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
            mCanvas.drawBitmap(mCurrentBitmap,null,rect,null);
            mSurfaceHolder.unlockCanvasAndPost(mCanvas);
            mCurrentBitmap.recycle();
            position++;
        }

        private void startAnim()
        {
            if(mAnimationStateListener!=null)
            {
                mAnimationStateListener.onStart();
            }
            isDrawing=true;
            position=0;

            drawThread=new Thread()
            {
                @Override
                public void run()
                {
                    super.run();
                    while (isDrawing)
                    {
                        try
                        {
                            long now = System.currentTimeMillis();
                            drawBitmap();
                            //100ms draw one frame , you can change this time
                            sleep(mFrameTime - (System.currentTimeMillis() - now)>0?mFrameTime - (System.currentTimeMillis() - now):0);
                        } catch (InterruptedException e1)
                        {
                            e1.printStackTrace();
                        }
                    }
                }
            };
            drawThread.start();
        }
        private void stopAnim()
        {
            isDrawing=false;
            position=0;
            mBitmapCache.clear();
            mPathList.clear();
            //this is necessary
            drawThread.interrupt();
            if(mAnimationStateListener!=null)
            {
                mAnimationStateListener.onFinish();
            }

        }
    }
    private void startDecodeThread()
    {
        new Thread()
        {
            @Override
            public void run()
            {
                super.run();
                Looper.prepare();

                mDecodeHandler=new Handler(Looper.myLooper())
                {
                    @Override
                    public void handleMessage(Message msg)
                    {
                        super.handleMessage(msg);
                        if(msg.what==-2)
                        {
                            decodeBitmap(-2);
                            getLooper().quit();
                            return;
                        }
                        decodeBitmap(msg.what);
                    }
                };
                decodeBitmap(-1);
                Looper.loop();
            }
        }.start();
    }

    private void decodeBitmap(int position)
    {

        if(position==-1)
        {
            for(int i=0;i<mCacheCount;i++)
            {
                mBitmapCache.put(mPathList.get(i), BitmapFactory.decodeFile(mPathList.get(i)));

            }
            mCallBack.startAnim();
        }else if(position==-2)
        {
            mCallBack.stopAnim();
        }else
        {
            if(position+mCacheCount<=mTotalCount-1)
            {
                mBitmapCache.remove(mPathList.get(position));
                mBitmapCache.put(mPathList.get(position+mCacheCount),BitmapFactory.decodeFile(mPathList.get(position+mCacheCount)));
            }
        }


    }
}
