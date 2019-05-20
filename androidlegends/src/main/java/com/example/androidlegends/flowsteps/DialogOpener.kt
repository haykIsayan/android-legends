package com.example.android_bleed.android_legends.flowsteps

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.android_bleed.android_legends.utilities.LegendResult
import com.example.android_bleed.android_legends.view.LegendsDialogFragment
import kotlin.reflect.KClass

class DialogOpener<D : LegendsDialogFragment>(val dialogKlass: KClass<D>) : FlowStep() {

    override fun execute(): LiveData<LegendResult> {
        val data = MutableLiveData<LegendResult>()
        val dialogOpenerResult = DialogOpenerResult(dialogKlass)
        dialogOpenerResult.bundle = dataBundle
        data.postValue(dialogOpenerResult)
        return data
    }

    data class DialogOpenerResult<D : LegendsDialogFragment>(val dialogKlass: KClass<D>) : LegendResult(Status.COMPLETED)

}