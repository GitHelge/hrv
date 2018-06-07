package com.bauerapps.breathingrhythm

import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import kotlinx.android.synthetic.main.cardview_breathing_rhythm.view.*

/**
 * Created by Christian on 24.03.18.
 */

interface BPClickedInterface {
    fun bpItemClicked(bp: BreathingPattern, position: Int)
    fun bpItemLongClicked(bp: BreathingPattern, position: Int)
    fun startBPAddDialog()
}

class BPRecyclerAdapter(var bpList: ArrayList<BreathingPattern>): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun getItemCount() = bpList.size + 1

    companion object {
        const val BP_ITEM_TYPE = 1
        const val BP_ADD_ITEM_TYPE = 2
    }

    var bpClickedInterface: BPClickedInterface? = null

    override fun getItemViewType(position: Int): Int {
        return if (position < bpList.size) BP_ITEM_TYPE else BP_ADD_ITEM_TYPE
    }

    fun addItem(bp: BreathingPattern) {
        bpList.add(bp)
        notifyItemInserted(bpList.size - 1)
    }

    fun removeItem(bp: BreathingPattern) {
        val index = bpList.indexOf(bp)
        bpList.remove(bp)
        notifyItemRemoved(index)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        if(viewType == BP_ITEM_TYPE) {
            val inflatedView = parent.inflate(R.layout.cardview_breathing_rhythm, false)
            return BPHolder(inflatedView)
        }
        val inflatedView = parent.inflate(R.layout.cardview_add_breathing_rythm, false)
        return BPAddHolder(inflatedView)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder?, position: Int) {
        if (position < bpList.size) {
            val bpHolder = holder as? BPHolder
            val bp = bpList[position]
            bpHolder?.bindBreathingPattern(bp)
            bpHolder?.itemView?.setOnClickListener { bpClickedInterface?.bpItemClicked(bp, position) }
            bpHolder?.itemView?.setOnLongClickListener {
                bpClickedInterface?.bpItemLongClicked(bp, position)
                true
            }
        } else {
            val bpAddHolder = holder as? BPAddHolder
            bpAddHolder?.itemView?.setOnClickListener { bpClickedInterface?.startBPAddDialog() }
        }
    }

    class BPAddHolder(v: View) : RecyclerView.ViewHolder(v)

    class BPHolder(v: View) : RecyclerView.ViewHolder(v) {

        private var bp: BreathingPattern? = null
        private var textViewFullDurationValue: TextView? = null
        private var textViewPhaseCountValue: TextView? = null
        private var textViewNameHeader: TextView? = null

        init {
            textViewFullDurationValue = v.textViewDurationValue
            textViewNameHeader = v.textViewNameHeader
        }

        fun bindBreathingPattern(bp: BreathingPattern) {
            this.bp = bp
            this.textViewFullDurationValue?.text = bp.fullDuration.durationString()
            this.textViewPhaseCountValue?.text = bp.patternPhaseList.count().toString()
            this.textViewNameHeader?.text = bp.name
        }
    }

}