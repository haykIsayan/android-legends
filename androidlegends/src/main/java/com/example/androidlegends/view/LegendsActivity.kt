package com.example.android_bleed.android_legends.view

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.Observer
import com.example.android_bleed.R
import com.example.android_bleed.android_legends.legends.AndroidLegend
import com.example.android_bleed.android_legends.utilities.LegendResult
import com.example.android_bleed.android_legends.flowsteps.ActivityDestination
import com.example.android_bleed.android_legends.flowsteps.DialogDismisser
import com.example.android_bleed.android_legends.flowsteps.DialogOpener
import com.example.android_bleed.android_legends.flowsteps.LegendStarter
import com.example.android_bleed.android_legends.flowsteps.fragment.CustomAnimation
import com.example.android_bleed.android_legends.legends.LambdaLegend
import com.example.android_bleed.android_legends.utilities.CurrentLegendManager
import java.lang.Exception
import java.lang.IllegalArgumentException
import kotlin.reflect.KClass

abstract class LegendsActivity : AppCompatActivity(), Observer<LegendResult> {

    private val mFlowMap = mutableMapOf<String, AndroidLegend>()

    private val mFlowData = MediatorLiveData<LegendResult>()

    private var mFragmentContainerId: Int = -1

    companion object {
        const val RECEIVER_LEGEND_BUNDLE = "Starter.Legend.Bundle"
        const val FRAGMENT_TRANSITION_BUNDLE = "Fragment.Transition.Bundle"
        const val DIALOG_FRAGMENT_TRANSITION_BUNDLE = "Dialog.Fragment.Transition.Bundle"
    }
    protected abstract fun getFragmentContainerId(): Int

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_legends)
        this.mFragmentContainerId = getFragmentContainerId()

        registerLauncherLegend()

        this.mFlowData.observe(this, this)
    }

    /**
     * UTILITY FUNCTIONS
     */

    // todo/ code improvements

    private fun registerLauncherLegend() {
        val bundle = intent.extras

        // CHECK IF IS FROM RECEIVER

        var receiverLegend = bundle?.getSerializable(RECEIVER_LEGEND_BUNDLE) as AndroidLegend?

        receiverLegend?.apply {
            receiverLegend = initAndroidLegend(this::class)
            registerLegend(receiverLegend!!)
            receiverLegend?.execute(AndroidLegend.ACTION_START_LEGEND, bundle)
            return
        }

        // not from receiver

        var starterLegend = CurrentLegendManager.sCurrentLegend
        starterLegend?.apply {
            registerLegend(legend = starterLegend!!)
            starterLegend!!.execute(AndroidLegend.ACTION_START_LEGEND, bundle ?: Bundle())
            return
        }

    }

    private fun <L : AndroidLegend> initAndroidLegend(flowKlass: KClass<L>) = flowKlass.constructors.first().call(application)

    private fun <L : AndroidLegend> registerAndGetLegend(flowKlass: KClass<L>): AndroidLegend {
        val flowName = flowKlass.java.name

        var flow = mFlowMap[flowName]
        flow.apply {

            flow = initAndroidLegend(flowKlass)
            registerLegend(flow!!)
        }
        return flow!!
    }

    private fun registerLegend(legend: AndroidLegend) {
        mFlowMap[legend::class.java.name] = legend
        mFlowData.apply {
            addSource(legend.getFlowData()) {
                this.value = it
            }
        }
    }

    /**
     * START A NEW LEGEND FROM ITS ROOT
     */

    fun <L : AndroidLegend> startLegend(legendKlass: KClass<L>, bundle: Bundle = Bundle()) {
        val legend = initAndroidLegend(legendKlass)
        processLegend(legend, legendKlass, bundle)
    }

    /**
     * EXECUTE A SPECIFIC FLOW VECTOR OF THE GIVEN LEGEND
     */

    fun <L : AndroidLegend> executeLegend(flowKlass: KClass<L>, vectorTag: String = AndroidLegend.ACTION_START_LEGEND, bundle: Bundle = Bundle()) {
        val flow = registerAndGetLegend(flowKlass)
        flow.execute(vectorTag, bundle)
    }

    /**
     * UTILITY FUNCTIONS
     */

    private fun <L : AndroidLegend> processLegend(legend: AndroidLegend, legendKlass: KClass<L>? = null, bundle: Bundle) {

        val legendsDestination = legend.getRoot()

        legendsDestination?.apply {
            when (this) {
                is ActivityDestination<*> -> {

                    if (this.getActivityKlass() == this@LegendsActivity::class) return@apply

                    val resource =
                        LegendResult.ActivityTransitionResource(this.getActivityKlass(), this.customAnimation)
                    resource.bundle = bundle

                    CurrentLegendManager.sCurrentLegend = legend
                    executeActivityTransition(resource)
                }
            }
            return
        }
        legendKlass?.apply {
            executeLegend(legendKlass, bundle = bundle)
        }
    }

    fun getLegendData(): LiveData<LegendResult> = mFlowData

    /**
     * LEGEND RESULT CONTROL
     */

    final override fun onChanged(legendResult: LegendResult?) {
        legendResult ?: return
        when (legendResult) {
            is LegendResult.FragmentTransitionResource<*> -> executeFragmentTransition(legendResult)
            is LegendResult.ActivityTransitionResource<*> -> executeActivityTransition(legendResult)
            is LegendResult.FragmentPopResource<*> -> executeFragmentPop(legendResult)
            is LegendStarter.LegendStarterResult<*> -> startLegend(legendKlass = legendResult.flowKlass)
            is LegendResult.LambdaStarterResult -> executeStartLambda(legendResult.flowGraph)
            is DialogOpener.DialogOpenerResult<*> -> executeOpenDialog(legendResult)
            is DialogDismisser.DialogDismissResult<*> -> executeDismissDialog(legendResult)
            else -> {
                if (legendResult.status == LegendResult.Status.COMPLETED) {
                    notifyFlowStepCompleted(legendResult.bundle, legendResult.flowName)
                }
            }
        }
    }

    /**
     * ON FLOW STEP COMPLETED
     */

    fun notifyFlowStepCompleted(bundle: Bundle, flowTag: String) {
        this.mFlowMap[flowTag]?.notifyFlowStepCompleted(bundle)
    }

    /**
     * Handle Activity Destination in a Flow
     */

    private fun executeActivityTransition(activityTransitionResource: LegendResult.ActivityTransitionResource<*>) {
        val intent = Intent(this, activityTransitionResource.activityKlass.java)
        intent.putExtras(activityTransitionResource.bundle)
        startActivity(intent)

        val customAnimation = activityTransitionResource.customAnimation
        // ANIMATION HANDLING
        customAnimation?.apply {
            overridePendingTransition(customAnimation.enterAnimation, customAnimation.exitAnimation)
        }
    }

    /**
     * Handle Fragment Destination in a Flow
     */

    // TODO remove the need for illegal Argument Exception

    @SuppressLint("WrongConstant")
    private fun executeFragmentTransition(fragmentTransitionResource: LegendResult.FragmentTransitionResource<*>) {
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()

        // ANIMATION CONTROL
        val fragmentAnimation = fragmentTransitionResource.fragmentAnimation
        fragmentAnimation?.apply {
            when (fragmentAnimation) {
                is CustomAnimation -> {
                    if (fragmentAnimation.popEnterAnimation == -1 || fragmentAnimation.popExitAnimation == -1) {
                        fragmentTransaction.setCustomAnimations(fragmentAnimation.enterAnimation, fragmentAnimation.exitAnimation)
                    } else {
                        fragmentTransaction.setCustomAnimations(
                            fragmentAnimation.enterAnimation,
                            fragmentAnimation.exitAnimation,
                            fragmentAnimation.popEnterAnimation,
                            fragmentAnimation.popExitAnimation
                        )
                    }
                }
            }
        }

        if (fragmentTransitionResource.enterAnimationId != -1) {
            try {
                fragmentTransaction.setTransition(fragmentTransitionResource.enterAnimationId)
            } catch (e: Exception) {
                throw IllegalArgumentException("FragmentDestination enterAnimationId must be a FragmentTransaction Animation")
            }
        }

        val fragmentKlass = fragmentTransitionResource.fragmentKlass
        val forceRecreate = fragmentTransitionResource.forceRecreate

        /**
         * Either create instance or find fragment by tag
         */

        val fragment = if (forceRecreate) {
            fragmentKlass.constructors.first().call()
        } else {
            supportFragmentManager.findFragmentByTag(fragmentKlass.java.name) ?: fragmentKlass.constructors.first().call()
        }

        /**
         * Commit fragment
         */

        val bundle = fragmentTransitionResource.bundle
        bundle.putString(FRAGMENT_TRANSITION_BUNDLE, fragmentTransitionResource.flowName)
        // ADD FLOW STEP ARGUMENT
        fragment.arguments = bundle
        // BACK STACK CONTROL
        fragmentTransaction.addToBackStack(
            if (fragmentTransitionResource.addToBackStack) {
                fragmentTransitionResource.fragmentKlass.java.name
            } else null
        )
        fragmentTransaction.replace(mFragmentContainerId, fragment).commit()
    }

    private fun executeFragmentPop(fragmentPopResource: LegendResult.FragmentPopResource<*>) {
        val fragmentManager = supportFragmentManager
        if (fragmentPopResource.fragmentKlass != null) {
//            fragmentManager.popBackStack(fragmentPopResource.fragmentKlass.java.name)
        }
        fragmentManager.popBackStack()
        notifyFlowStepCompleted(fragmentPopResource.bundle, fragmentPopResource.flowName)
    }

    private fun executeStartLambda(flowGraph: AndroidLegend.FlowGraph, bundle: Bundle = Bundle()) {
        val lambdaLegend = LambdaLegend(application, flowGraph)
        processLegend<LambdaLegend>(lambdaLegend, bundle = bundle)
    }

    private fun executeOpenDialog(dialogOpenerResult: DialogOpener.DialogOpenerResult<*>) {
        val legendsDialog = dialogOpenerResult.dialogKlass.constructors.first().call()
        val bundle = dialogOpenerResult.bundle
        bundle.putString(DIALOG_FRAGMENT_TRANSITION_BUNDLE, dialogOpenerResult.flowName)
        legendsDialog.arguments = bundle
        val transaction =  supportFragmentManager.beginTransaction()
        transaction.addToBackStack(null)
        legendsDialog.show(transaction, legendsDialog.javaClass.name)
    }

    private fun executeDismissDialog(dialogDismissResult: DialogDismisser.DialogDismissResult<*>) {
        val legendsDialog = supportFragmentManager.findFragmentByTag(dialogDismissResult.dialogKlass.java.name)
        when (legendsDialog) {
            is LegendsDialogFragment -> {
                legendsDialog.dismiss()
            }
        }
    }
}
