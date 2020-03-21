package com.yuyashuai.frameanimationmaster

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Debug
import kotlinx.android.synthetic.main.activity_second.*

/**
 * second activity for test
 */
class SecondActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_second)
        fav_second.playAnimationFromAssets("zone720p")
        btn_action_second.setOnClickListener {

        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        //Debug.stopMethodTracing()
    }

    override fun onPause() {
        fav_second.onPause()
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        fav_second.onResume()
    }

}

