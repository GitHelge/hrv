package com.bauerapps.breathingrhythm

import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.TextView
import kotlinx.android.synthetic.main.listviewrow_add_phase.view.*

/**
 * Created by Christian on 29.03.18.
 */

class AddPatternPhaseAdapter(val patternPhaseList: ArrayList<PatternPhase>) :
        RecyclerView.Adapter<AddPatternPhaseAdapter.AddPatternViewHolder>() {

    fun addItem() {
        patternPhaseList.add(PatternPhase())
        notifyItemInserted(patternPhaseList.size - 1)
    }

    fun removeItem() {
        if (patternPhaseList.size > 1) {
            patternPhaseList.remove(patternPhaseList.last())
            notifyItemRemoved(patternPhaseList.size)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AddPatternViewHolder {
        val inflatedView = parent.inflate(R.layout.listviewrow_add_phase, false)
        return AddPatternViewHolder(inflatedView)
    }

    override fun getItemCount() = patternPhaseList.size

    override fun onBindViewHolder(holder: AddPatternViewHolder?, position: Int) {
        holder?.textViewHeader?.text = String.format("Phase %d", position+1)
        holder?.bindPhase(patternPhaseList[position])

        holder?.seekBarRespiratoryRate?.onProgressChanged {
            if (holder.seekBarRespiratoryRate!!.isShown) {
                val rr = it.rrFromSeekBarProgress()
                patternPhaseList[position].respiratoryRate = rr
                holder.updateTextViewRR(rr)
            }
        }
        holder?.seekBarPhaseDuration?.onProgressChanged {
            if (holder.seekBarPhaseDuration!!.isShown) {
                val duration = it.durationFromSeekBarProgress()
                patternPhaseList[position].phaseDuration = duration
                holder.updateTextViewDuration(duration)
            }
        }
    }


    class AddPatternViewHolder(v: View): RecyclerView.ViewHolder(v) {

        // Respiratory Rate SeekBar
        var seekBarRespiratoryRate: SeekBar? = null

        // Phase Duration SeekBar
        var seekBarPhaseDuration: SeekBar? = null

        // Duration of Phase
        private var textViewDuration: TextView? = null

        // Value of Respiratory Rate
        private var textViewRespiratoryRate: TextView? = null

        // Phase Name
        var textViewHeader: TextView? = null

        init {
            this.seekBarRespiratoryRate = v.seekBarRR
            this.seekBarPhaseDuration = v.seekBarDuration
            this.textViewDuration = v.textViewDurationValue
            this.textViewRespiratoryRate = v.textViewRRValue
            this.textViewHeader = v.textViewAddPhaseRowHeader
        }

        fun updateTextViewRR(rr: Int) {
            this.textViewRespiratoryRate?.text = rr.toString()
        }

        fun updateTextViewDuration(duration: Int) {
            this.textViewDuration?.text = duration.durationString()
        }

        fun bindPhase(bp: PatternPhase) {
            this.seekBarPhaseDuration?.progress = bp.seekBarValueFromDuration
            this.seekBarRespiratoryRate?.progress = bp.seekBarValueFromRR
            this.textViewDuration?.text = bp.phaseDuration.durationString()
            this.textViewRespiratoryRate?.text = bp.respiratoryRate.toString()
        }

    }
}