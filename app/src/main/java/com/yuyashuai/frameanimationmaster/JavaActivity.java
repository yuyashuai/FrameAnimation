package com.yuyashuai.frameanimationmaster;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import com.yuyashuai.frameanimation.FrameAnimationSurfaceView;

/**
 * @author yuyashuai 2016-11-27 15:43:51
 */
public class JavaActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FrameAnimationSurfaceView fsv=findViewById(R.id.fsv_java_main);
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
    }

    @Override
    public void onClick(View v) {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
