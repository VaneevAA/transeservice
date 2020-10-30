package com.example.ekotransservice_routemanager.ViewIssues.PointAction

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import com.example.ekotransservice_routemanager.DataClasses.Point
import com.example.ekotransservice_routemanager.MainActivity
import com.example.ekotransservice_routemanager.R
import kotlinx.android.synthetic.main.fact_dialog.*
import java.util.*


class FactDialog(
    parentFragment: Fragment,
    val point: MutableLiveData<Point>,
    val mainFragment: point_action,
    val mainParentView: View
) : DialogFragment() {

    var plan = point.value!!.getCountPlan()
    private var fact : Double = if (point.value!!.getCountFact()==-1.0) {0.0} else {point.value!!.getCountFact()}

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val inflater = activity?.layoutInflater
        val mainView = inflater?.inflate(R.layout.fact_dialog, null)

        mainView?.findViewById<TextView>(R.id.planCount)?.text = plan.toString()
        mainView?.findViewById<EditText>(R.id.factCount)?.setText(
            fact.toString(),
            TextView.BufferType.EDITABLE
        )

        val positiveButtonClick = { dialog: DialogInterface, which: Int ->
            try {
                mainFragment.okFactDialogClicked(mainView?.findViewById<EditText>(R.id.factCount)?.text.toString())
            }catch (e: Exception){
                Toast.makeText(activity, "Число введено неправильно", Toast.LENGTH_LONG).show()
            }
        }

        val negativeButtonClick = { dialog: DialogInterface, which: Int ->
            this.dismiss()
        }

        val builder = AlertDialog.Builder(activity, R.style.ThemeOverlay_AppCompat_Dialog)
        builder.setView(mainView)
            .setPositiveButton("ОК", DialogInterface.OnClickListener(positiveButtonClick))
            .setNegativeButton("Отмена", DialogInterface.OnClickListener(negativeButtonClick))

        val dialog = builder.create()

        dialog.window?.setLayout(50, 100)

        mainView?.findViewById<ImageButton>(R.id.planToFact)?.setOnClickListener {
            mainView.findViewById<EditText>(R.id.factCount)?.setText(
                plan.toString(),
                TextView.BufferType.EDITABLE
            )
        }

        /*mainView?.findViewById<ImageButton>(R.id.OK)?.setOnClickListener {
            try {
                fact = mainView.findViewById<EditText>(R.id.factCount)?.text.toString().toDouble()
            }catch (e: Exception){
                Toast.makeText(activity, "Число введено неправильно", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            point.value!!.setCountFact(fact)
            // Отметим выполнение точки.
            // Если количество равно 0, то считаем точку выполненной, даже если не сделано фото после
            // Если количество не равно 0, то считаем точку выполненной только при начлии фото после
            if (fact==0.0) {
                point.value!!.setDone(true)
            }else{
                val valueBefore = point.value!!.getDone()
                point.value!!.setDone(fileAfterIsDone)
                mainFragment.doneValueIsChanged = valueBefore!=point.value!!.getDone()
            }
            point.value!!.setCountOverFromPlanAndFact()
            point.value!!.setTimestamp(Date())

            mainFragment.getViewModel().getRepository().updatePointAsync(point.value!!)

            mainFragment.endOfDialog(mainParentView)

            this.dismiss()
        }

        mainView?.findViewById<ImageButton>(R.id.cancel)?.setOnClickListener {

            this.dismiss()
        }

        */
        return dialog


    }

}