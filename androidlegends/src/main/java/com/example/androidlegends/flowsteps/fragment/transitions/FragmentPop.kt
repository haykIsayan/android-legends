package com.example.android_bleed.android_legends.flowsteps.fragment.transitions

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.android_bleed.android_legends.utilities.LegendResult
import com.example.android_bleed.android_legends.flowsteps.FlowStep
import com.example.android_bleed.android_legends.view.LegendsFragment
import kotlin.reflect.KClass

class FragmentPop<F : LegendsFragment> (val fragmentKlass : KClass<F>? = null) : FlowStep() {
    override fun execute(): LiveData<LegendResult> {
        val data = MutableLiveData<LegendResult>()
        data.postValue(LegendResult.FragmentPopResource(fragmentKlass))
        return data
    }
}