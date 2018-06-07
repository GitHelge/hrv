package com.bauerapps.breathingrhythm

import android.content.res.Resources
import android.support.annotation.LayoutRes
import android.support.design.widget.Snackbar
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.text.Html
import android.os.Build
import android.text.Spanned
import android.widget.AdapterView
import android.widget.Spinner


/**
 * Created by Christian on 24.03.18.
 */

fun ViewGroup.inflate(@LayoutRes layoutRes: Int, attachToRoot: Boolean = false): View {
    return LayoutInflater.from(context).inflate(layoutRes, this, attachToRoot)
}

fun Int.durationString(): String {

    var newSeconds = this
    var minutes = 0
    while(newSeconds>=60) {
        newSeconds -= 60
        minutes ++
    }

    return String.format("%d:%02d", minutes, newSeconds)
}

fun <T : Any> T?.notNull(f: (it: T) -> Unit) {
    if (this != null) f(this)
}

fun Spinner.onItemSelected(onItemSeleted: (Int) -> Unit) {
    this.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
        override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
            // Display the selected item text on text view
            onItemSeleted.invoke(position)
        }

        override fun onNothingSelected(parent: AdapterView<*>) {
            // Another interface callback
        }
    }
}

fun SeekBar.onProgressChanged(onProgressChanged: (Int) -> Unit) {
    this.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
            onProgressChanged.invoke(progress)
        }
        override fun onStartTrackingTouch(p0: SeekBar?) {}
        override fun onStopTrackingTouch(p0: SeekBar?) {}
    })
}

fun Int.rrFromSeekBarProgress(): Int {
    return this + Constants.MIN_RESPIRATORY_RATE
}

fun Int.durationFromSeekBarProgress(): Int {
    // This returns the amount in Seconds
    return this * Constants.STEPSIZE_PHASE_DURATION + Constants.MIN_PHASE_DURATION
}

fun View.snack(message: String, length: Int = Snackbar.LENGTH_LONG, f: Snackbar.() -> Unit) {
    val snack = Snackbar.make(this, message, length)
    snack.f()
    snack.show()
}

fun Snackbar.action(action: String, color: Int? = null, listener: (View) -> Unit) {
    setAction(action, listener)
    color?.let { setActionTextColor(color) }
}

val Int.dp: Int get() = (this / Resources.getSystem().displayMetrics.density).toInt()

@Suppress("DEPRECATION")
fun fromHtml(html: String): Spanned {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY)
    } else {
        Html.fromHtml(html)
    }
}


