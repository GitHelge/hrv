package com.bauerapps.breathingrhythm

import java.io.Serializable

/**
 * Created by Christian on 24.03.18.
 */

enum class IERatio(val ratio: String, private val iVal: Double, private val eVal: Double) {
    // Short Expiration
    SE1("1:1", 1.0,1.0),
    SE2("1:1.1", 1.0,1.1),
    SE3("1:1.2", 1.0,1.2),
    SE4("1:1.3", 1.0,1.3),
    SE5("1:1.4", 1.0,1.4),
    SE6("1:1.5", 1.0,1.5),
    SE7("1:1.6", 1.0,1.6),
    // Normal
    N1("1:1.7", 1.0, 1.7),
    N2("1:1.8", 1.0, 1.8),
    N3("1:1.9", 1.0, 1.9),
    N4("1:2", 1.0, 2.0),
    N5("1:2.1", 1.0, 2.1),
    N6("1:2.2", 1.0, 2.2),
    N7("1:2.3", 1.0, 2.3),
    // Long Expiration
    LE1("1:2.4", 1.0, 2.4),
    LE2("1:2.5", 1.0, 2.5),
    LE3("1:2.6", 1.0, 2.6),
    LE4("1:2.7", 1.0, 2.7),
    LE5("1:2.8", 1.0, 2.8),
    LE6("1:2.9", 1.0, 2.9),
    LE7("1:3", 1.0, 3.0);

    companion object {
        fun fromString(ieRatio: String): IERatio {
            return IERatio.values().first { it.ratio == ieRatio }
        }
    }
    /**
     * This method expects a respiratory duration in milliseconds and returns the inspiration time.
     * */
    fun inspTime(msRRDuration: Double): Double {
        val ieVal = iVal / eVal
        return ieVal / (1 + ieVal) * msRRDuration
    }

    /**
     * This method expects a respiratory duration in milliseconds and returns the expiratory time.
     * */
    fun expTime(msRRDuration: Double): Double {
        return msRRDuration - inspTime(msRRDuration)
    }
}


data class BreathingPattern(val name: String, var patternPhaseList: ArrayList<PatternPhase>): Serializable {
    val fullDuration get() = patternPhaseList.map { it.phaseDuration }.sum()
}

data class PatternPhase(var phaseDuration: Int = 30, var respiratoryRate: Int = 12, var ie: IERatio = IERatio.N1): Serializable {

    /**
     * This Property returns a value, which represents the current duration
     * as a SeekBar progress or value.
     * */
    val seekBarValueFromDuration get() = phaseDuration / Constants.STEPSIZE_PHASE_DURATION - 1

    /**
     * This Property returns a value, which represents the current
     * respiratory rate as a SeekBar progress or value.
     * */
    val seekBarValueFromRR get() = respiratoryRate - Constants.MIN_RESPIRATORY_RATE

    /**
     * This Property calculates and returns a floating point duration value of the inspiration.
     * */
    val inspDuration get() = ie.inspTime(60.0 / respiratoryRate * 1000)

    /**
     * This Property calculates and returns a floating point duration value of the expiration.
     * */
    val expDuration get() = ie.expTime(60.0 / respiratoryRate * 1000)

}

