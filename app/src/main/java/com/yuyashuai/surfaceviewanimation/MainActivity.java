package com.yuyashuai.surfaceviewanimation;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private SurfaceView sv_main;
    private SurfaceHolder surfaceHolder;
    private SvCallback callback;

    private Map<String,Bitmap> bitmapCache =new HashMap<>();
    private String[] assets;
    private String folderName="daku";
    private  AssetManager assetManager;
    private int totalCount;
    private Handler decodeHandler;
    private Thread decodeThread;

    private Button btn_am1;
    private Button btn_am2;
    private Button btn_am3;
    private Button btn_am4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        initData();
    }

    private void initView() {
        sv_main = (SurfaceView) findViewById(R.id.sv_main);
        btn_am1 = (Button) findViewById(R.id.btn_am1);
        btn_am2 = (Button) findViewById(R.id.btn_am2);
        btn_am3 = (Button) findViewById(R.id.btn_am3);
        btn_am4 = (Button) findViewById(R.id.btn_am4);

        btn_am1.setOnClickListener(this);
        btn_am2.setOnClickListener(this);
        btn_am3.setOnClickListener(this);
        btn_am4.setOnClickListener(this);

    }
    private void initData()
    {
        assetManager=getAssets();
        surfaceHolder=sv_main.getHolder();
        callback=new SvCallback();
        sv_main.setZOrderOnTop(true);
        surfaceHolder.setFormat(PixelFormat.TRANSLUCENT);
        surfaceHolder.addCallback(callback);

    }
    @Override
    public void onClick(View v) {
        if(callback.isDrawing)
        {
            callback.stopAnim();
        }

        switch (v.getId()) {
            case R.id.btn_am1:
                folderName="daku";
                break;
            case R.id.btn_am2:
                folderName="crow";
                break;
            case R.id.btn_am3:
                folderName="bullshit";
                break;
            case R.id.btn_am4:
                folderName="huabanyu";
                break;
        }

        startDecodeThread();
    }
    private class SvCallback implements SurfaceHolder.Callback
    {
        private Canvas canvas;
        private Bitmap currentBitmap;
        private int position=0;
        public boolean isDrawing=false;
        private Thread drawThread;
        private Rect rect;

        @Override
        public void surfaceCreated(SurfaceHolder surfaceHolder) {
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            //full screen
            rect=new Rect(0,0,width,height);
        }

        private void drawBitmap()
        {
            if(position>=totalCount)
            {
                isDrawing=false;
                decodeHandler.sendEmptyMessage(-2);
                canvas=surfaceHolder.lockCanvas();
                //clear surfaceView
                canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                surfaceHolder.unlockCanvasAndPost(canvas);
                return;
            }
            if(!bitmapCache.containsKey(assets[position]))
            {
                return;
            }
            System.out.println("准备绘制："+assets[position]);
            currentBitmap=bitmapCache.get(assets[position]);
            decodeHandler.sendEmptyMessage(position);
            canvas=surfaceHolder.lockCanvas(rect);
            canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
            canvas.drawBitmap(currentBitmap,null,rect,null);
            surfaceHolder.unlockCanvasAndPost(canvas);
            currentBitmap.recycle();
            position++;
        }


        @Override
        public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
            stopAnim();
        }
        public void startAnim()
        {
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
                            sleep(100 - (System.currentTimeMillis() - now)>0?100 - (System.currentTimeMillis() - now):0);
                        } catch (InterruptedException e1)
                        {
                            e1.printStackTrace();
                        }
                    }
                }
            };
            drawThread.start();
        }
        public void stopAnim()
        {
            isDrawing=false;
            position=0;
            bitmapCache.clear();
            assets=null;
            //this is necessary
            drawThread.interrupt();
        }
    }

    private void startDecodeThread()
    {
        decodeThread=new Thread()
        {
            @Override
            public void run()
            {
                super.run();
                Looper.prepare();

                decodeHandler=new Handler(Looper.myLooper())
                {
                    @Override
                    public void handleMessage(Message msg)
                    {
                        super.handleMessage(msg);
                        if(msg.what==-2)
                        {
                            getLooper().quit();
                            return;
                        }
                        decodeBitmap(msg.what);
                    }
                };
                decodeBitmap(-1);
                Looper.loop();
            }
        };
        decodeThread.start();
    }

    private void decodeBitmap(int position)
    {

        try {
            assets=assetManager.list(folderName);
            totalCount=assets.length;
            if(position==-1)
            {
                for(int i=0;i<5;i++)
                {
                    bitmapCache.put(assets[i], BitmapFactory.decodeStream(assetManager.open(folderName + "/" + assets[i])));
                }
                callback.startAnim();
            }else if(position==-2)
            {
                callback.stopAnim();
            }else
            {
                if(position+5<=totalCount-1)
                {
                    bitmapCache.remove(assets[position]);
                    bitmapCache.put(assets[position+5],BitmapFactory.decodeStream(assetManager.open(folderName + "/" + assets[position+5])));
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
