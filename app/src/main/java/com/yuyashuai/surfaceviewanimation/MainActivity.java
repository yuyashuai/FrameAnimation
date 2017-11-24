package com.yuyashuai.surfaceviewanimation;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;

import com.yuyashuai.surfaceanimation.SurfaceViewAnimation;


public class MainActivity extends AppCompatActivity {


    private SurfaceViewAnimation surfaceViewAnimation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SurfaceView mSurfaceView = (SurfaceView) findViewById(R.id.sv_main);
        Button btnStart = (Button) findViewById(R.id.btn_start);
        Button btnStop = (Button) findViewById(R.id.btn_stop);
        surfaceViewAnimation = new SurfaceViewAnimation.Builder(mSurfaceView, "crow")
                .setRepeatMode(SurfaceViewAnimation.MODE_INFINITE)
                .build();
                /*new SurfaceViewAnimation.Builder(mSurfaceView,file)
                .setRepeatMode(SurfaceViewAnimation.MODE_INFINITE)
                .setFrameInterval(80)
                .setCacheCount(8)
                .build();*/


        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //you should put your frame animation in ExternalStorageDirectory/zzzz/
                surfaceViewAnimation.start();
            }
        });
        btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                surfaceViewAnimation.stop();
            }
        });

    }
}
