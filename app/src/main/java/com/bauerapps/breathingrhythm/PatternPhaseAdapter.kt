package com.bauerapps.breathingrhythm

import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import kotlinx.android.synthetic.main.listviewrow_show_phase.view.*

/**
 * Created by Christian on 25.03.18.
 */

interface PatternPhaseInterface {
    fun updateBPList(bp: BreathingPattern)
}

class PatternPhaseAdapter(private val bp: BreathingPattern): BaseAdapter() {

    var patternPhaseInterface: PatternPhaseInterface? = null

    private class PatternPhaseViewHolder(row: View?) {
        var textViewPhaseHeader: TextView? = null
        var textViewDurationValue: TextView? = null
        var textViewRRValue: TextView? = null

        init {
            this.textViewPhaseHeader = row?.textViewPhaseHeader
            this.textViewDurationValue = row?.textViewDurationValue
            this.textViewRRValue = row?.textViewRRValue
        }
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {

        val patternPhaseView: View?
        val ppViewHolder: PatternPhaseViewHolder

        if (convertView == null) {
            patternPhaseView = parent.inflate(R.layout.listviewrow_show_phase, false)
            ppViewHolder = PatternPhaseViewHolder(patternPhaseView)
            patternPhaseView.tag = ppViewHolder
        } else {
            patternPhaseView = convertView
            ppViewHolder = patternPhaseView.tag as PatternPhaseViewHolder
        }

        val patternPhase = bp.patternPhaseList[position]
        ppViewHolder.textViewPhaseHeader?.text = String.format("Phase %d", position+1)
        ppViewHolder.textViewDurationValue?.text = patternPhase.phaseDuration.durationString()
        ppViewHolder.textViewRRValue?.text = patternPhase.respiratoryRate.toString()
        patternPhaseView.setOnLongClickListener {
            if (bp.patternPhaseList.size > 1) {
                it.snack("Delete Phase Nr. " + (position + 1) + "?") {
                    action("Delete") {
                        bp.patternPhaseList.remove(bp.patternPhaseList[position])
                        notifyDataSetChanged()
                        patternPhaseInterface?.updateBPList(bp)
                    }
                }
            }
            true
        }

        return patternPhaseView
    }

    override fun getItem(index: Int): PatternPhase {
        return bp.patternPhaseList[index]
    }

    override fun getItemId(index: Int): Long {
        return index.toLong()
    }

    override fun getCount(): Int {
        return bp.patternPhaseList.size
    }

}