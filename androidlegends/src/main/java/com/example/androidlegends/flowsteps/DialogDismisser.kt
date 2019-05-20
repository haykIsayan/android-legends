package com.example.android_bleed.android_legends.flowsteps

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.android_bleed.android_legends.utilities.LegendResult
import com.example.android_bleed.android_legends.view.LegendsDialogFragment
import kotlin.reflect.KClass

class DialogDismisser<D : LegendsDialogFragment>(val dialogKlass: KClass<D>) : FlowStep() {

    override fun execute(): LiveData<LegendResult> {
        val data = MutableLiveData<LegendResult>()
        data.postValue(DialogOpener.DialogOpenerResult(dialogKlass))
        return data
    }

    data class DialogDismissResult<D : LegendsDialogFragment>(val dialogKlass: KClass<D>) : LegendResult()
}