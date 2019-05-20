package com.example.android_bleed.android_legends.flowsteps

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.android_bleed.android_legends.utilities.LegendResult

abstract class UserAction: FlowStep() {

    abstract class UserApplicationAction : UserAction() {
        final override fun execute(): LiveData<LegendResult> {
            return MutableLiveData<LegendResult>()
        }

        abstract fun execute(application: Application): LiveData<LegendResult>
    }

}