package com.example.ekotransservice_routemanager.ViewIssues.PointAction

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import com.example.ekotransservice_routemanager.DataClasses.Point
import com.example.ekotransservice_routemanager.R

class FactDialog(parentFragment : Fragment, val point : MutableLiveData<Point>, val mainFragment : point_action, val mainParentView : View) : DialogFragment() {

    var plan = point.value!!.getCountPlan()
    var fact : Double = point.value!!.getCountFact()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(activity, R.style.ThemeOverlay_AppCompat_Dialog)
        val inflater = activity?.layoutInflater
        val mainView = inflater?.inflate(R.layout.fact_dialog,null)

        mainView?.findViewById<TextView>(R.id.planCount)?.text = plan.toString()
        mainView?.findViewById<EditText>(R.id.factCount)?.setText(fact.toString(),TextView.BufferType.EDITABLE)
        builder.setView(mainView)

        /*builder.setPositiveButton("OK") { dialogInterface: DialogInterface, i: Int ->
            fact = mainView?.findViewById<EditText>(R.id.factCount)?.text.toString().toInt()
            point.value!!.setCountFact(fact)

            mainFragment.endOfDialog(mainParentView)

            dialogInterface.dismiss()
            //TODO make callback in point action
        }

        builder.setNegativeButton("Отмена") { dialogInterface: DialogInterface, i: Int ->
            //TODO not fact set
            dialogInterface.cancel()
        }*/
        mainView?.findViewById<ImageButton>(R.id.OK)?.setOnClickListener {
            fact = mainView?.findViewById<EditText>(R.id.factCount)?.text.toString().toDouble()
            point.value!!.setCountFact(fact)
            point.value!!.setDone(true)

            mainFragment.getViewModel().getRepository().updatePointAsync(point.value!!)

            mainFragment.endOfDialog(mainParentView)

            this.dismiss()
        }

        mainView?.findViewById<ImageButton>(R.id.cancel)?.setOnClickListener {

            this.dismiss()
        }

        mainView?.findViewById<ImageButton>(R.id.planToFact)?.setOnClickListener {
            mainView.findViewById<EditText>(R.id.factCount)?.setText(plan.toString(),TextView.BufferType.EDITABLE)
        }

        val dialog = builder.create()

        dialog.window?.setLayout(50,100)

        return dialog


    }
}