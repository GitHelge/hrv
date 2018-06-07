package com.bauerapps.breathingrhythm

import android.os.SystemClock
import android.graphics.drawable.AnimationDrawable
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioFormat.ENCODING_PCM_16BIT
import android.media.AudioFormat.CHANNEL_OUT_STEREO
import android.media.AudioManager
import android.media.AudioTrack
import android.os.Build
import android.support.annotation.RequiresApi
import android.util.Log
import kotlin.concurrent.thread
import kotlin.math.exp
import kotlin.math.max


interface AnimationDrawableInterface {
    fun expirationStarted()
    fun inspirationStarted()
}

/**
 * Created by Christian on 30.03.18.
 */
class MyAnimationDrawable(animationDrawable: AnimationDrawable) : AnimationDrawable() {

    companion object {
        const val INSP_FRAME = 0
        const val EXP_FRAME = 1
    }

    var animationDrawableInterface: AnimationDrawableInterface? = null

    private var inspDuration: Int
    private var expDuration: Int
    private var currentFrame: Int

    private var isInitialFrame: Boolean

    private var audioTrack: AudioTrack? = null

    init {
        // Initialisation with Inspiration
        currentFrame = INSP_FRAME
        isInitialFrame = true

        // Initialisation with 2 sec inspiration and 3.4 sec expiration. (11.1 1/min)
        inspDuration = 2000
        expDuration = 3400

        /* Add each frame to our animation drawable */
        for (i in 0 until animationDrawable.numberOfFrames) {
            this.addFrame(animationDrawable.getFrame(i), animationDrawable.getDuration(i))
        }
    }


    override fun run() {

        // Sets the current frame to the following frame. Insp -> Exp and vice versa.
        currentFrame = if (currentFrame == INSP_FRAME) {
            EXP_FRAME
        } else {
            INSP_FRAME
        }

        // Resets the initial frame to the inspiration frame
        if (isInitialFrame) {
            currentFrame = INSP_FRAME
            isInitialFrame = false
        }

        selectDrawable(currentFrame)
        val duration = durationOfFrame(currentFrame)
        scheduleSelf(this, SystemClock.uptimeMillis() + duration)

        /* These methods are called in the BreathingIndicatorActivity to indicate
        the start of inspiration or expiration */

        audioTrack.notNull { it.release() }
        audioTrack = generateTone(currentFrame, duration)
        audioTrack?.play()

        if (currentFrame == EXP_FRAME) {
            animationDrawableInterface?.expirationStarted()
        } else {
            animationDrawableInterface?.inspirationStarted()
        }

    }

    /**
     * This method returns the duration of the specified frame.
     * */
    private fun durationOfFrame(frame: Int): Int {
        return if (frame == INSP_FRAME) {
            inspDuration
        } else {
            expDuration
        }
    }

    /**
     * This method updates the duration of the inspiration.
     * */
    fun updateInspDuration(duration: Int) {
        this.inspDuration = duration
    }

    fun stopAudio() {
        audioTrack?.stop()
        audioTrack?.release()
    }

    /**
     * This method updates the duration of the expiration.
     * */
    fun updateExpDuration(duration: Int) {
        this.expDuration = duration
    }

    private var inspSampleList: ShortArray? = null
    private var expSampleList: ShortArray? = null
    private var oldInspDuration: Int = 0
    private var oldExpDuration: Int = 0

    /*private fun calculateSamples(freqHz: Double, durationMs: Int): ShortArray {

        val count = (44100.0 * 2.0 * (durationMs / 1000.0)).toInt() and 1.inv()
        val samples = ShortArray(count)

        var i = 0
        val maxVolume = 0x7FFF
        var volume: Int
        while (i < count) {

            // This "when"-condition sets the volume to the calculated value.
            // The calculation uses the exponential 
            volume = when {
                i < count / 1.1 -> (maxVolume * (1 - exp(-i/(count/5.5)))).toInt()
                else -> {
                    val t1 = (count - count / 1.1).toInt()
                    (maxVolume * exp(-(i-t1)/(count/0.1))).toInt()
                }
                //else -> maxVolume
            }

            val sample = (Math.sin(2.0 * Math.PI * i.toDouble() / (44100.0 / freqHz)) * volume).toShort()
            samples[i + 0] = sample
            samples[i + 1] = sample
            i += 2
        }

        return samples
    }*/

    private fun calculateSamples(freqHz: Double ,durationMs: Int): ShortArray {

        val count = (44100.0 * 2.0 * (durationMs / 1000.0)).toInt() and 1.inv()
        val samples = ShortArray(count)

        var i = 0
        val maxVolume = 0x7FFF
        var volume: Int
        while (i < count) {

            volume = when {
                i < count / 5.0 -> (maxVolume * (1 - exp(-i/(count/25.0)))).toInt()
                i > count - count / 5.0 -> {
                    val t1 = (count - count / 5.0).toInt()
                    (maxVolume * exp(-(i-t1)/(count/25.0))).toInt()
                }
                else -> maxVolume
            }

            /*volume = if (i < count / 5.0) {
                (maxVolume * (1 - exp(-i/(count/10.0)))).toInt()
            } else {
                maxVolume
            }*/

            val sample = (Math.sin(2.0 * Math.PI * i.toDouble() / (44100.0 / freqHz)) * volume).toShort()
            samples[i + 0] = sample
            samples[i + 1] = sample
            i += 2
        }

        return samples
    }

    private fun generateTone(frame: Int, durationMs: Int): AudioTrack? {

        val samples: ShortArray?

        if (frame == INSP_FRAME && durationMs != oldInspDuration) {
            // recalculate inspiratory Arrays

            inspSampleList = calculateSamples(175.0, durationMs)
            samples = inspSampleList

            oldInspDuration = durationMs
        } else if (frame == EXP_FRAME && durationMs != oldExpDuration) {
            // recalculate expiratory Arrays

            expSampleList = calculateSamples(147.0, durationMs)
            samples = expSampleList

            oldExpDuration = durationMs
        } else {
            samples = if (frame== INSP_FRAME) {
                inspSampleList
            } else {
                expSampleList
            }
        }

        if (samples == null) {
            return null
        }

        @Suppress("DEPRECATION")
        val audioTrack = AudioTrack(AudioManager.STREAM_MUSIC, 44100,
                AudioFormat.CHANNEL_OUT_STEREO, AudioFormat.ENCODING_PCM_16BIT,
                samples.size * (java.lang.Short.SIZE / 8), AudioTrack.MODE_STATIC)

        val response = audioTrack.write(samples, 0, samples.size)

        if (response == AudioTrack.ERROR_INVALID_OPERATION) {
            return null
        }

        return audioTrack
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun getAudioTrack(): AudioTrack {
        val minBuffSize = AudioTrack.getMinBufferSize(44100,
                AudioFormat.CHANNEL_OUT_STEREO,
                AudioFormat.ENCODING_PCM_16BIT)

        return AudioTrack.Builder()
                .setAudioAttributes(AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build())
                .setAudioFormat(AudioFormat.Builder()
                        .setEncoding(ENCODING_PCM_16BIT)
                        .setSampleRate(44100)
                        .setChannelMask(CHANNEL_OUT_STEREO)
                        .build())
                .setBufferSizeInBytes(minBuffSize)
                .build()
    }

}