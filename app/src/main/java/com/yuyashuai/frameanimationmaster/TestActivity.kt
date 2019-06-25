package com.yuyashuai.frameanimationmaster

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.yuyashuai.frameanimation.FrameAnimation
import kotlinx.android.synthetic.main.activity_test.*

class TestActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test)
        btn1_test.setOnClickListener {
            startAnimation("crow")
        }
        btn2_test.setOnClickListener {
            startAnimation("zone720p")
        }
        btn3_test.setOnClickListener {
            fav_test.stopAnimation()
        }
    }

    private fun startAnimation(assetPath: String) {
        fav_test.setFrameInterval(60)
        fav_test.setRepeatMode(FrameAnimation.RepeatMode.INFINITE)
        fav_test.setScaleType(FrameAnimation.ScaleType.FIT_XY)
        if (fav_test.isPlaying()) {
            //fav_test.stopAnimation()
        }
        fav_test.playAnimationFromAssets(assetPath)
    }
}
