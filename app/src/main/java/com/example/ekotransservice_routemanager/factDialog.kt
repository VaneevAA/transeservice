package com.example.ekotransservice_routemanager

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import com.example.ekotransservice_routemanager.DataClasses.Point

class FactDialog(parentFragment : Fragment,val point : MutableLiveData<Point>, val mainFragment : point_action,val mainParentView : View) : DialogFragment() {

    var plan = point.value!!.getCountPlan()
    var fact : Int = point.value!!.getCountFact()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(activity)
        val inflater = activity?.layoutInflater
        val mainView = inflater?.inflate(R.layout.fact_dialog,null)

        mainView?.findViewById<TextView>(R.id.planCount)?.text = plan.toString()

        builder.setView(mainView)

        builder.setPositiveButton("OK") { dialogInterface: DialogInterface, i: Int ->
            fact = mainView?.findViewById<EditText>(R.id.factCount)?.text.toString().toInt()
            point.value!!.setCountFact(fact)

            mainFragment.endOfDialog(mainParentView)

            dialogInterface.dismiss()
            //TODO make callback in point action
        }

        builder.setNegativeButton("Отмена") { dialogInterface: DialogInterface, i: Int ->
            //TODO not fact set
            dialogInterface.cancel()
        }

        mainView?.findViewById<Button>(R.id.planToFact)?.setOnClickListener {
            mainView.findViewById<EditText>(R.id.factCount)?.setText(plan)
        }


        return builder.create()


    }
}