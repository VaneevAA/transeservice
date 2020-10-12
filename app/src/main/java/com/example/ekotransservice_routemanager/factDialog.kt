package com.example.ekotransservice_routemanager

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import com.example.ekotransservice_routemanager.DataClasses.Point

class FactDialog(parentFragment : Fragment, point : Point) : DialogFragment() {

    var plan = point.getCountPlan()
    var fact : Double = point.getCountFact() as Double

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(activity)
        val inflater = activity?.layoutInflater
        val mainView = inflater?.inflate(R.layout.fact_dialog,null)

        mainView?.findViewById<TextView>(R.id.planCount)?.text = plan.toString()

        builder.setView(mainView)

        builder.setPositiveButton("OK") { dialogInterface: DialogInterface, i: Int ->
            fact = mainView?.findViewById<EditText>(R.id.factCount)?.text.toString().toDouble()

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