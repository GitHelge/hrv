package com.bauerapps.breathingrhythm

/**
 * Created by Christian on 27.03.18.
 */

/*interface BPAddPhaseClickedInterface {
    fun addPhase()
}

class AddBPPhaseAdapter(private var bpPhaseList: ArrayList<PatternPhase>, private val bpAlertView: View): BaseAdapter() {

    var bpAddPhaseClickedInterface: BPAddPhaseClickedInterface? = null

    fun updatePatternPhaseList(pp: PatternPhase) {
        bpPhaseList.add(pp)
    }

    private fun updateFullDuration() {

        var sum = 0

        for (element in bpPhaseList) {
            sum += element.duration
        }

        bpAlertView.textViewAddBPDurationValue.text = sum.durationString()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {

        val bpPhaseView: View?
        val addBPPhaseViewHolder: AddBPPhaseViewHolder

        if (convertView == null) {

            bpPhaseView = parent.inflate(R.layout.listviewrow_add_phase, false)
            addBPPhaseViewHolder = AddBPPhaseViewHolder(bpPhaseView, position)
            bpPhaseView.tag = addBPPhaseViewHolder

        } else {
            bpPhaseView = convertView
            addBPPhaseViewHolder = bpPhaseView.tag as AddBPPhaseViewHolder

            updateFullDuration()
        }

        addBPPhaseViewHolder.editTextRR?.afterTextChanged {
            val update = addBPPhaseViewHolder.updateRR()
            bpPhaseList[update.second].respiratoryRate = update.first
            addBPPhaseViewHolder.textViewDuration?.text = bpPhaseList[update.second].duration.durationString()
            updateFullDuration()
        }
        addBPPhaseViewHolder.editTextRCC?.afterTextChanged {
            val update = addBPPhaseViewHolder.updateRCC()
            bpPhaseList[update.second].breathingCycles = update.first
            addBPPhaseViewHolder.textViewDuration?.text = bpPhaseList[update.second].duration.durationString()
            updateFullDuration()
        }

        addBPPhaseViewHolder.textViewDuration?.text = bpPhaseList[position].duration.durationString()
        addBPPhaseViewHolder.textViewHeader?.text = String.format("Phase %d", position+1)

        /*if (addBPPhaseViewHolder.textWatcher != null) {
            addBPPhaseViewHolder.removeTextChangedListener()
        }

        addBPPhaseViewHolder.addTextChangedListeners()*/

        return bpPhaseView
    }

    override fun getItem(index: Int): Any {
        return bpPhaseList[index]
    }

    override fun getItemId(index: Int): Long {
        return index.toLong()
    }

    override fun getCount(): Int {
        return bpPhaseList.size
    }

    private class AddBPPhaseViewHolder(row: View?, private val position: Int) {

        // Respiratory Rate SeekBar
        var seekbarRespiratoryRate: SeekBar? = null

        // Phase Duration SeekBar
        var seekBarPhaseDuration: SeekBar? = null

        /*
        // Respiratory Rate
        var editTextRR: TextInputEditText? = null

        // Respiratory Cycle Count
        var editTextRCC: TextInputEditText? = null
        */

        // Duration of Phase
        var textViewDuration: TextView? = null

        // Value of Respiratory Rate
        var textViewRespiratoryRate: TextView? = null

        // Phase Name
        var textViewHeader: TextView? = null

        //var textWatcher: TextWatcher? = null

        init {
            /*this.editTextRR = row?.editTextAddRR
            this.editTextRCC = row?.editTextAddRCC
            this.textViewDuration = row?.textViewAddDurationValue*/
            this.seekbarRespiratoryRate = row?.seekBarRR
            this.seekBarPhaseDuration = row?.seekBarDuration
            this.textViewDuration = row?.textViewDurationValue
            this.textViewRespiratoryRate = row?.textViewRRValue
            this.textViewHeader = row?.textViewAddPhaseRowHeader
        }

        /*fun updateRR() : Pair<Int, Int> {

            var rr = 0
            editTextRR.notNull { it.text.toString().toIntOrNull().notNull { rr = it } }
            return Pair(rr, position)
        }

        fun updateRCC() : Pair<Int, Int> {

            var rcc = 0
            editTextRCC.notNull { it.text.toString().toIntOrNull().notNull { rcc = it } }
            return Pair(rcc, position)
        }

        fun updateDuration(rr: Int, rcc: Int) {
            val duration = (60.0 / rr * rcc).roundToInt()
            textViewDuration?.text = duration.durationString()
        }*/
    }
}*/
