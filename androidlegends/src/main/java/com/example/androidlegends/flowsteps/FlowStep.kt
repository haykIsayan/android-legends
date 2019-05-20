package com.example.android_bleed.android_legends.flowsteps

import android.os.Bundle
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import com.example.android_bleed.android_legends.utilities.LegendResult

abstract class FlowStep {

    private val flowStepData = MediatorLiveData<LegendResult>()

    private val inputKeyList = mutableListOf<String>()

    lateinit var dataBundle: Bundle


//    fun invoke(): LiveData<LegendResult> {
//
//        flowStepData.apply {
//            val newData = execute()
//            addSource(newData) { newValue ->
//                postValue(newValue)
////                removeSource(newData)
//            }
//        }
//        return flowStepData
//    }




    abstract fun execute(): LiveData<LegendResult>

}