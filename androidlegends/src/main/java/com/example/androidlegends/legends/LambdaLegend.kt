package com.example.android_bleed.android_legends.legends

import android.app.Application

class LambdaLegend(application: Application, flowGraph: FlowGraph) : AndroidLegend(application) {

    init {
        mFlowGraph = flowGraph
    }

}