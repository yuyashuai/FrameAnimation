package com.yuyashuai.silkyanimation;

/**
 * @author yuyashuai   2018/8/1.
 */
public interface DecoderListener {

    /**
     * init complete
     */
    void onPrepared();

    /**
     * decode finished
     */
    void onFinished();
}
