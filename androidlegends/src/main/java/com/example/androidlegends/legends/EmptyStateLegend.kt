package com.example.android_bleed.android_legends.legends

import android.app.Application

class EmptyStateLegend(application: Application) : AndroidLegend(application){
    override fun onCreateFlowGraph(): FlowGraph {
        return FlowGraph()

    }
}