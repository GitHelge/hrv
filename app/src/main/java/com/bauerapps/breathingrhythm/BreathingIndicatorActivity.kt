package com.bauerapps.breathingrhythm

import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.drawable.AnimationDrawable
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.support.v4.content.ContextCompat
import android.view.View
import android.view.WindowManager
import com.bauerapps.breathingrhythm.R.drawable.gradient_animation_list
import kotlinx.android.synthetic.main.activity_breathing_indicator.*
import java.util.*
import kotlin.concurrent.schedule


class BreathingIndicatorActivity : AppCompatActivity(), AnimationDrawableInterface {

    companion object {

        private const val BREATHING_PATTERN = "bp"
        fun intent(context: Context, bp: BreathingPattern): Intent {
            val intent = Intent(context, BreathingIndicatorActivity::class.java)
            intent.putExtra(BREATHING_PATTERN, bp)
            return intent
        }
    }

    private var preparationTimer: Timer = Timer()

    @Volatile
    private var shouldUpdateAnimationOfPhase = false

    @Volatile
    private var phaseIndex = 0

    @Volatile
    private var shouldEndAnimation = false

    /**
     * This property stores the AnimationDrawable to be shown as an breathing indicator.
     * It is marked as an optional, as it is null while the preparation timer runs.
     * */
    private var animationDrawable: MyAnimationDrawable? = null

    /**
     * This property stores the chosen breathing pattern to be animated.
     * It is marked as an optional, because the deserialization might fail.
     * */
    private var bp: BreathingPattern? = null

    /**
     * This property
     * */
    //private var activePhase: PatternPhase? = null

    private var phaseDurationHandler = Handler()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_breathing_indicator)

        // Sets interface to portrait or landscape
        if(resources.getBoolean(R.bool.forcePortrait)){
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }

        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN)

        bp = intent.getSerializableExtra(BREATHING_PATTERN) as? BreathingPattern

        prepareBreathingTest()
    }

    /**
     * In this method, a countdown is started and shown on the display,
     * before the animation starts.
     * */
    private fun prepareBreathingTest() {
        var counter = 5

        // This timer fires at 0ms and repeatedly after 1000ms each.
        preparationTimer.schedule(0,1000, {
            updatePrep(counter)
            counter--
        })
    }

    private fun updatePrep(counter: Int) {
        if (counter > 0) {

            // Update preparation countdown
            runOnUiThread {
                textViewPreparationTimer.text =
                        getString(R.string.starting_in, counter.durationString())
            }
            return
        }

        // Remove preparation timer
        preparationTimer.cancel()

        // Hide preparation countdown
        runOnUiThread { textViewPreparationTimer.text = "" }

        // Start Breathing Animation
        startBreathingAnimation()
    }

    /**
     * This method initializes and starts the breathing indicator animation.
     * */
    private fun startBreathingAnimation() {

        animationDrawable = MyAnimationDrawable(ContextCompat.getDrawable(this,
                gradient_animation_list) as AnimationDrawable)
        animationDrawable?.animationDrawableInterface = this

        runOnUiThread { imageViewBreathingIndicator.background = animationDrawable }

        // setting enter fade animation duration to 0.5 seconds.
        animationDrawable?.setEnterFadeDuration(300)

        // setting exit fade animation duration to 0.5 seconds.
        animationDrawable?.setExitFadeDuration(300)

        bp.notNull {
            val initPhase = it.patternPhaseList[phaseIndex]
            updateAnimationOfPhase(initPhase)

            runOnUiThread { animationDrawable?.start() }

            startPhaseHandler(initPhase)
        }
    }

    /**
     * This method receives a phase and starts an task to fire at the end of the specified phase.
     * */
    private fun startPhaseHandler(phase: PatternPhase) {
        runOnUiThread { phaseDurationHandler.postDelayed({ phaseEnded() }, (phase.phaseDuration * 1000).toLong()) }
    }

    /**
     * This method is called after the end of each phase. (When the phaseDurationHandler fires)
     * */
    private fun phaseEnded() {

        bp.notNull {
            if (phaseIndex < (it.patternPhaseList.size - 1)) {
                phaseIndex++
                startPhaseHandler(it.patternPhaseList[phaseIndex])
            } else {
                shouldEndAnimation = true
            }
        }

        /* Because an update on the durations of inspiration and expiration
        should only happen synchronized on the beginning of the expiration,
        there is a marker set here */
        shouldUpdateAnimationOfPhase = true
    }

    /**
     * This method is called to end the animation altogether.
     * */
    private fun endAnimation() {
        animationDrawable?.stop()
        animationDrawable?.stopAudio()
        animationDrawable = null
        imageViewBreathingIndicator.background = null
    }

    private fun updateAnimationOfPhase(phase: PatternPhase?) {

        val inspDuration = phase?.inspDuration?.toInt()
        val expDuration = phase?.expDuration?.toInt()

        inspDuration.notNull { animationDrawable?.updateInspDuration(it) }
        expDuration.notNull { animationDrawable?.updateExpDuration(it) }
    }

    /**
     * In this method, the duration values are updated, in order to effect the next cycle.
     * */
    override fun expirationStarted() {
        // This is the time to update the duration values before the next cycle starts.
        if (shouldUpdateAnimationOfPhase) {
            bp.notNull { updateAnimationOfPhase(it.patternPhaseList[phaseIndex]) }
            shouldUpdateAnimationOfPhase = false
        }
    }

    /**
     * This method is called, to end the animation after the last phase.
     * */
    override fun inspirationStarted() {
        if (shouldEndAnimation) {
            endAnimation()
            // TODO: There must be a finishing indicator or something.
            textViewPreparationTimer.text = getString(R.string.ended)
        }
    }

    override fun onResume() {
        super.onResume()
        // Starts the Animation.
        animationDrawable.notNull { if (!it.isRunning) it.start() }
    }

    override fun onPause() {
        super.onPause()
        // Stops the Animation.
        animationDrawable.notNull { if (it.isRunning) it.stop() }
    }

    override fun onBackPressed() {

        val view = findViewById<View>(android.R.id.content)
        view.snack(getString(R.string.stop_bp_and_go_back)) {
            action(getString(R.string.back)) {

                // Remove preparation Timer of animation did not start yet.
                preparationTimer.cancel()

                endAnimation()

                // Removes any callbacks (stops the postDelayed cycle)
                phaseDurationHandler.removeCallbacksAndMessages(null)

                super.onBackPressed()
            }
        }

    }

}
