package com.example.android_bleed.android_legends.view

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AppCompatDialogFragment
import com.example.android_bleed.android_legends.legends.AndroidLegend
import kotlin.reflect.KClass

open class LegendsDialogFragment : AppCompatDialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        /**
         *  NOTIFY COMPLETED
         */

        val bundle = arguments ?: Bundle()
        (activity as LegendsActivity).notifyFlowStepCompleted(
            bundle,
            bundle.getString(LegendsActivity.DIALOG_FRAGMENT_TRANSITION_BUNDLE)!!
        )
        return super.onCreateDialog(savedInstanceState)
    }


    fun <L : AndroidLegend> startLegend(legendKlas: KClass<L>, bundle: Bundle = Bundle()) {
        (activity as LegendsActivity).startLegend(legendKlas, bundle)
    }

    fun <L : AndroidLegend> executeLegend(
        legendKlass: KClass<L>,
        vectorTag: String = AndroidLegend.ACTION_START_LEGEND,
        bundle: Bundle = Bundle()
    ) {
        (activity as LegendsActivity).executeLegend(legendKlass, vectorTag, bundle)
    }



}