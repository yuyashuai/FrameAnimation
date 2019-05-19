package com.yuyashuai.frameanimationmaster

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.*
import com.yuyashuai.frameanimation.FrameAnimation
import kotlinx.android.synthetic.main.activity_kotlin.*

class KotlinActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener, FrameAnimation.FrameAnimationListener, SeekBar.OnSeekBarChangeListener {

    private val resources =
            listOf("zone720p",
                    "traffic720p")
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
                    "ONCE")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_kotlin)
        acs_repeat_mode.adapter = ArrayAdapter<String>(this, R.layout.spinner_text_view, repeatModes)
        acs_resource.adapter = ArrayAdapter<String>(this, R.layout.spinner_text_view, resources)
        acs_scale_type.adapter = ArrayAdapter<String>(this, R.layout.spinner_text_view, scaleTypes)
        acs_resource.onItemSelectedListener = this
        acs_repeat_mode.onItemSelectedListener = this
        acs_scale_type.onItemSelectedListener = this
        acs_resource.onItemSelectedListener = this
        sb_frame_interval.max = 300
        sb_frame_interval.setOnSeekBarChangeListener(this)
        animation_view.setAnimationListener(this)
        animation_view.clearViewAfterStop(false)
        btn_start.setOnClickListener {
            animation_view.playAnimationFromAssets((acs_resource.selectedView as TextView).text.toString(),20)
        }

        btn_stop.setOnClickListener {
            animation_view.stopAnimation()
        }
    }

    override fun onAnimationStart() {
        Toast.makeText(applicationContext, "onAnimationStart", Toast.LENGTH_SHORT).show()
    }

    override fun onAnimationEnd() {
        Toast.makeText(applicationContext, "onAnimationFinish", Toast.LENGTH_SHORT).show()
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
                animation_view.setScaleType(scaleType)
            }
            R.id.acs_repeat_mode -> {
                val repeatMode = when (text) {
                    "REVERSE_ONCE" -> FrameAnimation.RepeatMode.REVERSE_ONCE
                    "REVERSE_INFINITE" -> FrameAnimation.RepeatMode.REVERSE_INFINITE
                    "INFINITE" -> FrameAnimation.RepeatMode.INFINITE
                    else -> FrameAnimation.RepeatMode.ONCE
                }
                animation_view.setRepeatMode(repeatMode)
            }
            R.id.acs_resource -> {

            }
        }
    }

    override fun onProgress(progress: Float, frameIndex: Int, totalFrames: Int) {
        //System.out.println("progress:$progress  frameIndex:$frameIndex  totalFrames:$totalFrames")
    }

    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
        tv_frame_interval.text = if (progress == 0) {
            "frame interval: max"
        } else {
            "frame interval: ${progress}ms"
        }
        animation_view.setFrameInterval(progress)
    }

    override fun onStartTrackingTouch(seekBar: SeekBar?) {
    }

    override fun onStopTrackingTouch(seekBar: SeekBar?) {
    }

}
