package com.bauerapps.breathingrhythm

import android.app.Dialog
import android.content.pm.ActivityInfo
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.alertdialog_add_pattern.view.*
import kotlinx.android.synthetic.main.alertdialog_breathingpattern.view.*
import kotlinx.android.synthetic.main.alertdialog_information.view.*
import kotlinx.android.synthetic.main.alertdialog_settings.view.*
import java.io.File

class MainActivity : AppCompatActivity(), BPClickedInterface, PatternPhaseInterface {

    companion object {
        private const val AUDIO_SETTING = "audioSetting"
        private const val VIBRATION_SETTING = "vibrationSetting"
    }

    private lateinit var bpRecyclerAdapter: BPRecyclerAdapter
    private lateinit var bpGridLayoutManager: GridLayoutManager

    private var bpAlertDialog: Dialog? = null
    private var bpAlertViewStart: View? = null
    //private var patternPhaseAdapter: PatternPhaseAdapter? = null
    private var addPatternPhaseAdapter: AddPatternPhaseAdapter? = null
    private var settingList: ArrayList<Setting>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Sets interface to portrait or landscape
        if(resources.getBoolean(R.bool.forcePortrait)){
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }

        // 1. Set Layout Manager
        bpGridLayoutManager = GridLayoutManager(this, 2)
        recyclerViewBreathingPattern.layoutManager = bpGridLayoutManager

        // 2. Set RecyclerAdapter
        val bpList = readSavedBPList() ?: initBPList()

        bpRecyclerAdapter = BPRecyclerAdapter(bpList)
        bpRecyclerAdapter.bpClickedInterface = this
        recyclerViewBreathingPattern.adapter = bpRecyclerAdapter

        settingList = readSavedSettings() ?: initSettingList()

        frameLayoutInformation1.setOnClickListener { startConfigDialog() }

        // Receive clicks on the info Button.
        frameLayoutInformation2.setOnClickListener { startInformationDialog() }

    }

    private fun startConfigDialog() {
        val bpAlertBuilder = AlertDialog.Builder(this/*, R.style.DialogTheme*/)
        val bpAlertView =  LayoutInflater.from(this)
                .inflate(R.layout.alertdialog_settings, null)

        val settingList = readSavedSettings() ?: initSettingList()

        // First position is Audio Settings, second is Vibration, if anything goes amiss,
        // the "Elvis" Operator (?:) comes into play
        bpAlertView.switchToggleAudio.isChecked = settingList.find {
            it.name == AUDIO_SETTING
        }?.isToggledOn ?: true
        bpAlertView.switchToggleVibration.isChecked = settingList.find {
            it.name == VIBRATION_SETTING
        }?.isToggledOn ?: false

        bpAlertView.linearLayoutInfoClose1.setOnClickListener { bpAlertDialog?.dismiss() }

        bpAlertView.switchToggleAudio.setOnCheckedChangeListener { _, isToggledOn ->
            settingList.find { it.name == AUDIO_SETTING }?.isToggledOn = isToggledOn
            writeSavedSettings(settingList)
        }

        bpAlertView.switchToggleVibration.setOnCheckedChangeListener { _, isToggledOn ->
            settingList.find { it.name == VIBRATION_SETTING }?.isToggledOn = isToggledOn
            writeSavedSettings(settingList)
        }

        // Save the settings globally
        this.settingList = settingList

        bpAlertBuilder.setView(bpAlertView)

        bpAlertDialog = bpAlertBuilder.create()
        bpAlertDialog?.show()
    }

    private fun startInformationDialog() {
        val bpAlertBuilder = AlertDialog.Builder(this/*, R.style.DialogTheme*/)
        val bpAlertView =  LayoutInflater.from(this).inflate(R.layout.alertdialog_information, null)

        bpAlertView.textViewWarning.text = fromHtml(getString(R.string.warning))

        bpAlertView.linearLayoutInfoClose.setOnClickListener { bpAlertDialog?.dismiss() }

        bpAlertBuilder.setView(bpAlertView)

        bpAlertDialog = bpAlertBuilder.create()
        bpAlertDialog?.show()
    }

    override fun onPause() {

        // Save Lists
        writeSavedBPList(bpRecyclerAdapter.bpList)

        super.onPause()
    }

    private fun initSettingList(): ArrayList<Setting> {
        val settingList = ArrayList<Setting>()

        settingList.add(Setting(AUDIO_SETTING, true))
        settingList.add(Setting(VIBRATION_SETTING, false))

        return settingList
    }

    /**
     * This Method generates and returns a predefined ArrayList of BreathingPatterns.
     * */
    private fun initBPList(): ArrayList<BreathingPattern> {
        // Init Breathing Pattern List
        val bpList = ArrayList<BreathingPattern>()

        // Standard Breathing Pattern
        val patternPhaseList1 = ArrayList<PatternPhase>()
        patternPhaseList1.add(PatternPhase(60,6))
        patternPhaseList1.add(PatternPhase(60,8))
        patternPhaseList1.add(PatternPhase(60,10))
        patternPhaseList1.add(PatternPhase(60,12))
        patternPhaseList1.add(PatternPhase(60,14))
        bpList.add(BreathingPattern("Standard", patternPhaseList1))

        return bpList
    }

    /**
     * This Method starts an AlertView to offer the possibility to add a new Breathing Pattern.
     * */
    override fun startBPAddDialog() {
        val bpAlertBuilder = AlertDialog.Builder(this/*, R.style.DialogTheme*/)
        val bpAlertView =  LayoutInflater.from(this).inflate(R.layout.alertdialog_add_pattern, null)

        val linearLayoutManager = LinearLayoutManager(this)
        bpAlertView.recyclerViewAddBP.layoutManager = linearLayoutManager

        // Set List of PhasePatterns
        val initList = ArrayList<PatternPhase>()
        initList.add(PatternPhase())
        addPatternPhaseAdapter = AddPatternPhaseAdapter(initList)

        bpAlertView.recyclerViewAddBP.adapter = addPatternPhaseAdapter

        bpAlertView.frameLayoutAddPhase.setOnClickListener { addPatternPhaseAdapter?.addItem() }

        bpAlertView.frameLayoutDeletePhase.setOnClickListener { addPatternPhaseAdapter?.removeItem() }

        // Set Cancel and Start Buttons
        bpAlertView.linearLayoutAddBPCancel.setOnClickListener { bpAlertDialog?.dismiss() }
        bpAlertView.linearLayoutAddBPStart.setOnClickListener {
            val name = bpAlertView.editTextAddBPName.text
            if (name.isEmpty()) {
                Toast.makeText(this, "Define a Name for this Breathing Pattern.", Toast.LENGTH_SHORT).show()
            } else {
                this.bpRecyclerAdapter.addItem(BreathingPattern(name.toString(),
                        addPatternPhaseAdapter!!.patternPhaseList))
                bpAlertDialog?.dismiss()
            }
        }

        bpAlertBuilder.setView(bpAlertView)

        bpAlertDialog = bpAlertBuilder.create()
        bpAlertDialog?.show()
    }

    override fun updateBPList(bp: BreathingPattern) {
        val index = bpRecyclerAdapter.bpList.indexOf(bp)
        index.notNull {
            bpRecyclerAdapter.bpList[it].patternPhaseList = bp.patternPhaseList
            bpRecyclerAdapter.notifyItemChanged(it)
            bpAlertViewStart?.textViewFullDurationValue?.text = bp.fullDuration.durationString()
        }
    }

    override fun bpItemClicked(bp: BreathingPattern, position: Int) {

        val bpAlertBuilder = AlertDialog.Builder(this/*, R.style.DialogTheme*/)
        bpAlertViewStart =  LayoutInflater.from(this).inflate(R.layout.alertdialog_breathingpattern, null)
        // Set Header
        bpAlertViewStart?.textViewBPHeader?.text = bp.name

        // Set Spinner View
        val values = IERatio.values().map { it.ratio }.toMutableList()
        val ieRatioSpinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, values)
        ieRatioSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line)
        bpAlertViewStart?.spinnerIERatio?.adapter = ieRatioSpinnerAdapter
        bpAlertViewStart?.spinnerIERatio?.setSelection(ieRatioSpinnerAdapter.getPosition("1:1.7"))

        // Set full duration value
        bpAlertViewStart?.textViewFullDurationValue?.text = bp.fullDuration.durationString()

        // Set List of PhasePatterns
        val patternPhaseAdapter = PatternPhaseAdapter(bp)
        patternPhaseAdapter.patternPhaseInterface = this
        bpAlertViewStart?.alertListViewPatternPhase?.adapter = patternPhaseAdapter

        // Set Cancel and Start Buttons
        bpAlertViewStart?.linearLayoutBPCancel?.setOnClickListener {
            bpAlertDialog?.dismiss()
        }
        bpAlertViewStart?.linearLayoutBPStart?.setOnClickListener { _ ->
            bpAlertDialog?.dismiss()
            bp.patternPhaseList.forEach { it.ie = IERatio.fromString(ieRatioSpinnerAdapter.getItem(bpAlertViewStart?.spinnerIERatio?.selectedItemPosition!!)) }
            startActivity(BreathingIndicatorActivity.intent(this, bp, settingList))
        }

        bpAlertBuilder.setView(bpAlertViewStart)

        bpAlertDialog = bpAlertBuilder.create()
        bpAlertDialog?.show()
    }

    /**
     * This Method receives a long click and starts a SnackBar to ask the User if the
     * specified BreathingPattern should be deleted.
     * */
    override fun bpItemLongClicked(bp: BreathingPattern, position: Int) {

        val view = findViewById<View>(android.R.id.content)
        view.snack("Delete Breathing Pattern " + bp.name + "?") {
            action("Delete") {
                bpRecyclerAdapter.removeItem(bp)
            }
        }
    }

    /**
     * This Method writes the currently known ArrayList of BreathingPattern
     * to the internal Files-Directory.
     * */
    private fun writeSavedBPList(bpList: ArrayList<BreathingPattern>) {
        val gson = Gson().toJson(bpList)
        val file = File(filesDir, "bpList")
        file.writeText(gson)
    }

    /**
     * This Method writes the currently known ArrayList of Settings
     * to the internal Files-Directory.
     */
    private fun writeSavedSettings(settingList: ArrayList<Setting>) {
        val gson = Gson().toJson(settingList)
        val file = File(filesDir, "settingList")
        file.writeText(gson)
    }

    /**
     * This Method reads an Json-String from the internal Files-Directory
     * and returns an ArrayList of Settings or null is none is found.
     * */
    private fun readSavedSettings(): ArrayList<Setting>? {
        val file = File(filesDir, "settingList")
        if (file.exists()) {
            return Gson().fromJson(file.readText(),
                    object : TypeToken<ArrayList<Setting>>() {}.type)
        }
        return null
    }

    /**
     * This Method reads an Json-String from the internal Files-Directory
     * and returns an ArrayList of BreathingPattern or null is none is found.
     * */
    private fun readSavedBPList(): ArrayList<BreathingPattern>? {
        val file = File(filesDir, "bpList")
        if (file.exists()) {
            return Gson().fromJson(file.readText(),
                    object : TypeToken<ArrayList<BreathingPattern>>() {}.type)
        }
        return null
    }
}

