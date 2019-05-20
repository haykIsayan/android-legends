package com.example.android_bleed.android_legends.flowsteps

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.android_bleed.android_legends.utilities.LegendResult
import com.example.android_bleed.android_legends.legends.AndroidLegend

class LambdaStarter(val flowGraph: AndroidLegend.FlowGraph) : FlowStep() {

    override fun execute(): LiveData<LegendResult> {
        val data = MutableLiveData<LegendResult>()
        data.postValue(LegendResult.LambdaStarterResult(flowGraph))
        return data
    }
}