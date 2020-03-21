package com.yuyashuai.frameanimationmaster

import android.content.Intent
import android.os.Bundle
import android.os.Debug
import android.view.Choreographer
import android.view.View
import android.view.WindowManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.yuyashuai.frameanimation.FrameAnimation
import com.yuyashuai.frameanimation.repeatmode.RepeatTail
import kotlinx.android.synthetic.main.activity_mian.*

class MainActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener, FrameAnimation.FrameAnimationListener, SeekBar.OnSeekBarChangeListener {

    private val resources =
            listOf("zone720p",
                    "traffic720p",
                    "crow")
    private val scaleTypes =
            listOf("CENTER",
                    "CENTER_INSIDE",
                    "CENTER_CROP",
                    "FIT_END",
                    "FIT_CENTER",
                    "FIT_START",
                    "FIT_XY")
    private val repeatModes =
            listOf("REVERSE_ONCE",
                    "REVERSE_INFINITE",
                    "INFINITE",
                    "REPEAT_TAIL(10)",
                    "ONCE")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        setContentView(R.layout.activity_mian)
        acs_repeat_mode.adapter = ArrayAdapter(this, R.layout.spinner_text_view, repeatModes)
        acs_resource.adapter = ArrayAdapter(this, R.layout.spinner_text_view, resources)
        acs_scale_type.adapter = ArrayAdapter(this, R.layout.spinner_text_view, scaleTypes)
        acs_resource.onItemSelectedListener = this
        acs_repeat_mode.onItemSelectedListener = this
        acs_scale_type.onItemSelectedListener = this
        acs_resource.onItemSelectedListener = this
        sb_frame_interval.max = 300
        sb_frame_interval.setOnSeekBarChangeListener(this)
        animationView.setAnimationListener(this)
        btn_start.setOnClickListener {
            animationView.playAnimationFromAssets((acs_resource.selectedView as TextView).text.toString())
        }

        btn_stop.setOnClickListener {
            animationView.stopAnimation()
        }
        btn_jump.setOnClickListener {
            startActivity(Intent(this, SecondActivity::class.java))
        }

    }

    override fun onAnimationStart() {
    }

    override fun onAnimationEnd() {
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        if (parent == null || view == null) {
            return
        }
        val text = (view as TextView).text.toString()
        when (parent.id) {
            R.id.acs_scale_type -> {
                val scaleType = when (text) {
                    "CENTER" -> FrameAnimation.ScaleType.CENTER
                    "CENTER_INSIDE" -> FrameAnimation.ScaleType.CENTER_INSIDE
                    "CENTER_CROP" -> FrameAnimation.ScaleType.CENTER_CROP
                    "FIT_END" -> FrameAnimation.ScaleType.FIT_END
                    "FIT_CENTER" -> FrameAnimation.ScaleType.FIT_CENTER
                    "FIT_START" -> FrameAnimation.ScaleType.FIT_START
                    else -> FrameAnimation.ScaleType.FIT_XY
                }
                animationView.setScaleType(scaleType)
            }
            R.id.acs_repeat_mode -> {
                if (text == "REPEAT_TAIL(10)") {
                    animationView.setRepeatMode(RepeatTail(10))
                    return
                }
                val repeatMode = when (text) {
                    "REVERSE_ONCE" -> FrameAnimation.RepeatMode.REVERSE_ONCE
                    "REVERSE_INFINITE" -> FrameAnimation.RepeatMode.REVERSE_INFINITE
                    "INFINITE" -> FrameAnimation.RepeatMode.INFINITE
                    else -> FrameAnimation.RepeatMode.ONCE
                }
                animationView.setRepeatMode(repeatMode)
            }
            R.id.acs_resource -> {

            }
        }
    }

    override fun onPause() {
        animationView.onPause()
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        animationView.onResume()
    }

    override fun onProgress(progress: Float, frameIndex: Int, totalFrames: Int) {
    }

    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
        tv_frame_interval.text = if (progress == 0) {
            "frame interval: max"
        } else {
            "frame interval: ${progress}ms"
        }
        animationView.setFrameInterval(progress)
    }

    override fun onStartTrackingTouch(seekBar: SeekBar?) {
    }

    override fun onStopTrackingTouch(seekBar: SeekBar?) {
    }

}
