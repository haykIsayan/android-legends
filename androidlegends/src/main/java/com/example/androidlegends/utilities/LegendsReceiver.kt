package com.example.android_bleed.android_legends.utilities

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Intent
import android.os.Bundle
import com.example.android_bleed.android_legends.flowsteps.ActivityDestination
import com.example.android_bleed.android_legends.legends.AndroidLegend
import com.example.android_bleed.android_legends.view.LegendsActivity
import kotlin.reflect.KClass

abstract class LegendsReceiver : BroadcastReceiver() {

    private fun <L : AndroidLegend> initAndroidLegend(flowKlass: KClass<L>, application: Application) =
        flowKlass.constructors.first().call(application)

    private fun processLegend(legend: AndroidLegend, bundle: Bundle, application: Application): Intent? {
        val legendsDestination = legend.getRoot()
        legendsDestination?.apply {
            when (this) {
                is ActivityDestination<*> -> {
                    val intent = Intent(application, this.getActivityKlass().java)
                    bundle.putSerializable(LegendsActivity.RECEIVER_LEGEND_BUNDLE, legend)
                    bundle.putBoolean("IS_FROM_RECEIVER", true)
                    intent.putExtras(bundle)
                    return intent
                }
            }
        }
        return null
    }

    fun <L : AndroidLegend> startLegend(legendKlass: KClass<L>, bundle: Bundle = Bundle(), application: Application): Intent? {
        val legend = initAndroidLegend(legendKlass, application)
        return processLegend(legend, bundle, application)
    }
}