package com.yuyashuai.surfaceviewanimation;

import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SurfaceView mSurfaceView=  (SurfaceView) findViewById(R.id.sv_main);
        Button btnStart= (Button) findViewById(R.id.btn_start);
        Button btnStop= (Button) findViewById(R.id.btn_stop);
        final File file =new File(Environment.getExternalStorageDirectory()+"/zzzz");
       // mSva.setSurfaceView(mSurfaceView,getApplicationContext());
        if(!file.exists())
        {
            throw new UnsupportedOperationException("you should put your frame animation in ExternalStorageDirectory/zzzz/");
        }
        final List<String> mPathList= new ArrayList<>();

        final SurfaceViewAnimation surfaceViewAnimation=
                new SurfaceViewAnimation.Builder(mSurfaceView,file)
                .setRepeatMode(SurfaceViewAnimation.MODE_ONCE)
                .setFrameInterval(80)
                .setCacheCount(8)
                .build();

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
