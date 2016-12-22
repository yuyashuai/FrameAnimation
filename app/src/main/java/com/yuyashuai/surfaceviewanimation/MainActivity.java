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
        final SurfaceViewAnimation mSva=new SurfaceViewAnimation();
        Button button= (Button) findViewById(R.id.bt_start);
        final File file =new File(Environment.getExternalStorageDirectory()+"/zzzz");
        mSva.setSurfaceView(mSurfaceView,getApplicationContext());
        if(!file.exists())
        {
            throw new UnsupportedOperationException("you should put your frame animation in ExternalStorageDirectory/zzzz/");
        }

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //you should put your frame animation in ExternalStorageDirectory/zzzz/
                final List<String> mPathList= new ArrayList<>();
                if(file.isDirectory())
                {
                    File[] files=file.listFiles();
                    for(File mFrameFile:files )
                    {
                        mPathList.add(mFrameFile.getAbsolutePath());
                    }
                }
                mSva.startAnimation(mPathList);
            }
        });

    }
}
