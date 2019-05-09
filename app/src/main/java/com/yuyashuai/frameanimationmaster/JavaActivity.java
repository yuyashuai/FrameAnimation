package com.yuyashuai.frameanimationmaster;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.TextureView;
import android.view.View;

import com.yuyashuai.frameanimation.FrameAnimation;

/**
 * @author yuyashuai 2016-11-27 15:43:51
 */
public class JavaActivity extends AppCompatActivity implements View.OnClickListener {
    FrameAnimation frameAnimation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        TextureView textureView = findViewById(R.id.texture_view_java);
        frameAnimation = new FrameAnimation(textureView);
        frameAnimation.setScaleType(FrameAnimation.ScaleType.FIT_CENTER);
        frameAnimation.setRepeatMode(FrameAnimation.RepeatMode.REVERSE_INFINITE);
        frameAnimation.playAnimationFromAssets("zone720p");
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
        frameAnimation.stopAnimation();
    }
}
