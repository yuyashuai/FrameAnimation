package com.yuyashuai.surfaceviewanimation;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;

import com.yuyashuai.silkyanimation.SilkyAnimation;

/**
 * @author yuyashuai 2016-11-27 15:43:51
 */
public class MainActivity extends AppCompatActivity {


    private SilkyAnimation silkyAnimation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SurfaceView mSurfaceView = (SurfaceView) findViewById(R.id.sv_main);
        Button btnStart = (Button) findViewById(R.id.btn_start);
        Button btnStop = (Button) findViewById(R.id.btn_stop);
        silkyAnimation = new SilkyAnimation.Builder(mSurfaceView, "blacktest")
                .build();
        //如果需要在onCreate方法中直接调用，通过handler.postDelayed添加一个时延
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                silkyAnimation.start();
            }
        }, 40);
        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                silkyAnimation.start();
            }
        });
        btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                silkyAnimation.stop();
            }
        });
    }

}
