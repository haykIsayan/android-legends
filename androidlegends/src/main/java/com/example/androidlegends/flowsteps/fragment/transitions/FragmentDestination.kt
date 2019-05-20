package com.example.android_bleed.android_legends.flowsteps.fragment.transitions

import androidx.annotation.TransitionRes
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.android_bleed.android_legends.utilities.LegendResult
import com.example.android_bleed.android_legends.flowsteps.FlowStep
import com.example.android_bleed.android_legends.flowsteps.fragment.FragmentAnimation
import kotlin.reflect.KClass

class FragmentDestination<F : Fragment> (val fragmentKlass: KClass<F>,
                                         @TransitionRes val enterAnimationId: Int = -1,
                                         val addToBackStack: Boolean = true,
                                         val forceRecreate: Boolean = false,
                                         val fragmentAnimation: FragmentAnimation? = null) : FlowStep() {

    override fun execute(): LiveData<LegendResult> {
        val data = MutableLiveData<LegendResult>()

        val fragmentTransitionResource = LegendResult.FragmentTransitionResource(
            fragmentKlass = fragmentKlass
            , enterAnimationId = enterAnimationId,
            addToBackStack = addToBackStack,
            forceRecreate = forceRecreate,
            fragmentAnimation = fragmentAnimation
        )

        fragmentTransitionResource.bundle = dataBundle

        data.postValue(fragmentTransitionResource)
        return data
    }

}