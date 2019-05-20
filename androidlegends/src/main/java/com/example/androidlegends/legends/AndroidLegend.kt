package com.example.android_bleed.android_legends.legends

import android.app.Application
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import com.example.android_bleed.android_legends.utilities.LegendResult
import com.example.android_bleed.android_legends.flowsteps.*
import com.example.android_bleed.android_legends.flowsteps.fragment.CustomAnimation
import com.example.android_bleed.android_legends.flowsteps.fragment.FragmentAnimation
import com.example.android_bleed.android_legends.flowsteps.fragment.transitions.FragmentDestination
import com.example.android_bleed.android_legends.flowsteps.fragment.transitions.FragmentPop
import com.example.android_bleed.android_legends.view.LegendsActivity
import com.example.android_bleed.android_legends.view.LegendsDialogFragment
import com.example.android_bleed.android_legends.view.LegendsFragment
import java.io.Serializable
import kotlin.reflect.KClass

abstract class AndroidLegend(@Transient private val mApplication: Application) : Serializable{

    private val mFlowName = this::class.java.name
    @Transient
    protected lateinit var mFlowGraph: FlowGraph
    @Transient
    private lateinit var mCurrentVectorIterator: FlowVectorIterator

    @Transient
    private var mFlowData = MediatorLiveData<LegendResult>()


    companion object {
        const val ACTION_LAUNCH_ROOT = "Action.Launch.Root"
        const val ACTION_START_LEGEND = "Action.Launch.Flow" }

    init {
        onCreateFlow()
    }

    protected fun onCreateFlow() {
        mFlowGraph = onCreateFlowGraph()
    }

    protected open fun onCreateFlowGraph(): FlowGraph = FlowGraph()

    /**
     * /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
     * /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
     * /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
     * ////////////////////////////////////////// CONTROLLER FUNCTIONS /////////////////////////////////////////////////
     * /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
     * /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
     * /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
     */

    fun startWith(flowVector: FlowVector) {
        mFlowGraph.startWith(flowVector)
    }

    fun execute(vectorTag: String, bundle: Bundle) {
        val flowVector = mFlowGraph.getFlowVector(vectorTag)?:return
        invokeFlowVector(flowVector, bundle)
    }

    /**
     * /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
     * /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
     * /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
     * ////////////////////////////////////////// UTILITY FUNCTIONS ////////////////////////////////////////////////////
     * /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
     * /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
     * /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
     */

    private fun invokeFlowVector(flowVector: FlowVector, bundle: Bundle) {
        this.mCurrentVectorIterator = FlowVectorIterator(flowVector)
        executeVectorIterator(bundle)
    }

    /**
     * Notify that the last flow step has completed its task and
     * that the iterator must continue on with flow step execution
     */

    fun notifyFlowStepCompleted(bundle: Bundle) {
        executeVectorIterator(bundle)
    }

    private fun executeVectorIterator(bundle: Bundle = Bundle()) {
        val flowStep = mCurrentVectorIterator.getFlowStep()?:return
        flowStep.dataBundle = bundle
        val flowStepData = when(flowStep) {
            is UserAction.UserApplicationAction -> flowStep.execute(mApplication)
            else -> flowStep.execute()
        }
        mFlowData.apply {
            addSource(flowStepData) {
                it.flowName = mFlowName
                value = it
            }
        }
    }

    /**
     * ACCESSIBILITY FUNCTIONS
     */

    fun getFlowData(): LiveData<LegendResult> = mFlowData

    fun getRoot() = mFlowGraph.getRoot()


    /**
     * /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
     * /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
     * /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
     * //////////////////////////////////////// Utility Classes ////////////////////////////////////////////////////////
     * /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
     * /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
     * /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
     */

    class FlowGraph {

        private val mFlowVectorMap = mutableMapOf<String, FlowVector>()

        fun startWith(flowVector: FlowVector) = apply {
            mFlowVectorMap[ACTION_START_LEGEND] = flowVector
        }

        fun <L : LegendsActivity> setRoot(activityKlass : KClass<L>, customAnimation: CustomAnimation? = null) = apply {
            mFlowVectorMap[ACTION_LAUNCH_ROOT] = FlowVector()
                .startActivity(activityKlass = activityKlass, customAnimation = customAnimation)
        }

        fun addFlowVector(stepTag: String, flowVector: FlowVector) = apply { mFlowVectorMap[stepTag] = flowVector }

        fun getRoot (): FlowStep? = mFlowVectorMap[ACTION_LAUNCH_ROOT]?.getStepList()?.first()

        fun getFlowVector(stepTag: String) = mFlowVectorMap[stepTag]

    }

    class FlowVector {
        private val mFlowStepList = mutableListOf<FlowStep>()

        fun <A : LegendsActivity> startActivity(activityKlass: KClass<A>, customAnimation: CustomAnimation? = null) = apply {
            mFlowStepList.add(ActivityDestination(activityKlass = activityKlass, customAnimation = customAnimation))
        }

        /**
         * Used to transition to the given Fragment inheriting from LegendsFragment
         */

        fun <F : LegendsFragment> transitionTo(fragmentKlass: KClass<F>, addToBackStack : Boolean = true,
                                               forceRecreate: Boolean = false,
                                        fragmentAnimation: FragmentAnimation? = null) =
            apply {
                mFlowStepList.add(
                    FragmentDestination(
                        fragmentKlass = fragmentKlass,
                        addToBackStack = addToBackStack,
                        forceRecreate = forceRecreate,
                        fragmentAnimation = fragmentAnimation
                    )
                )
            }

        /**
         * Used to execute the given UserAction
         */

        fun execute(userAction: UserAction) =
            apply {
                mFlowStepList.add(userAction)
            }

        /**
         * Used to pop the given Fragment inheriting from LegendsFragment
         */

        fun <F : LegendsFragment> popBack(fragmentKlass: KClass<F>? = null) =
            apply {
                this.mFlowStepList.add(
                    FragmentPop(
                        fragmentKlass
                    )
                )
            }

        /**
         * Used to start a class inheriting from AndroidLegends
         */

        fun <F : AndroidLegend> startLegend(legendKlass: KClass<F>) =
                apply {
                    this.mFlowStepList.add(LegendStarter(flowKlass = legendKlass))
                }

        /**
         * Used to start a LambdaLegend with the given FlowGraph
         */

        fun startLegend(flowGraph: FlowGraph) =
                apply {
                    this.mFlowStepList.add(LambdaStarter(flowGraph))
                }

        /**
         * Used to open a Dialog inheriting from LegendsDialogFragment
         */

        fun <D : LegendsDialogFragment> openDialog(dialogKlass: KClass<D>) =
                apply {
                    this.mFlowStepList.add(DialogOpener(dialogKlass))
                }

        fun <D : LegendsDialogFragment> dismissDialog(dialogKlass: KClass<D>) =
                apply {
                    this.mFlowStepList.add(DialogOpener(dialogKlass))
                }

        fun getStepList() = this.mFlowStepList
    }

    inner class FlowVectorIterator (private val mFlowVector: FlowVector) {
        private var mFlowStepCounter = -1

        fun getFlowStep() = if (++mFlowStepCounter >= mFlowVector.getStepList().size) {
            null
        } else {
            mFlowVector.getStepList()[mFlowStepCounter]
        }
    }

}