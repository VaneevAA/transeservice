package com.example.ekotransservice_routemanager

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.widget.EditText
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager

class FactDialog(parentFragment : Fragment) : DialogFragment() {

    var mListener = parentFragment as PreferenceManager.OnDisplayPreferenceDialogListener
    var fact : Double = 0.0

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(activity)
        val inflater = activity?.layoutInflater
        val mainView = inflater?.inflate(R.layout.fact_dialog,null)
        builder.setView(mainView)
        builder.setPositiveButton("OK") { dialogInterface: DialogInterface, i: Int ->
            fact = mainView?.findViewById<EditText>(R.id.factCount)?.text as Double

            //TODO make callback in point action
        }

        builder.setNegativeButton("Отмена") { dialogInterface: DialogInterface, i: Int ->
            //TODO no fact set
            dialogInterface.cancel()
        }



        return builder.create()


    }
}