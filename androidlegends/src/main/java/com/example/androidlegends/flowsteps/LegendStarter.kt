package com.example.android_bleed.android_legends.flowsteps

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.android_bleed.android_legends.utilities.LegendResult
import com.example.android_bleed.android_legends.legends.AndroidLegend
import kotlin.reflect.KClass

class LegendStarter<F : AndroidLegend>(val flowKlass: KClass<F>) : FlowStep() {

    override fun execute(): LiveData<LegendResult> {
        val data = MutableLiveData<LegendResult>()
        data.postValue(LegendStarterResult(flowKlass))
        return data
    }

    data class LegendStarterResult<F : AndroidLegend> (val flowKlass: KClass<F>): LegendResult()

}