package com.example.ekotransservice_routemanager.ViewIssues.PointAction

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.os.Handler
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import com.example.ekotransservice_routemanager.DataClasses.Point
import com.example.ekotransservice_routemanager.R


class FactDialog(
    parentFragment: Fragment,
    val point: MutableLiveData<Point>,
    val mainFragment: point_action,
    val mainParentView: View
) : DialogFragment() {

    var plan = point.value!!.getCountPlan()
    private var fact : Double = if (point.value!!.getCountFact()==-1.0) {0.0} else {point.value!!.getCountFact()}

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val inflater = activity?.layoutInflater
        val mainView = inflater?.inflate(R.layout.fact_dialog, null)

        /*mainView?.findViewById<TextView>(R.id.planCount)?.text = plan.toString()
        mainView?.findViewById<EditText>(R.id.factCount)?.setText(
            fact.toString(),
            TextView.BufferType.EDITABLE
        )*/
        val factCountView = mainView?.findViewById<TextView>(R.id.factTextSet)
        factCountView?.text = fact.toString()
        //if(fact == 0.0){
        //    fact = plan
        //}
        mainView?.findViewById<ImageButton>(R.id.setHigh)?.setOnTouchListener(RepeatListener(400,100
        ) {
            fact += 0.5
            factCountView?.text = fact.toString()
        })

        mainView?.findViewById<ImageButton>(R.id.setLow)?.setOnTouchListener(RepeatListener(400,100
        ) {
            if (fact >= 0.5){
                fact -= 0.5
                factCountView?.text = fact.toString()
            }

        })
        /*mainView?.findViewById<ImageButton>(R.id.setHigh)?.setOnClickListener {
            fact += 0.5
            factCountView?.text = fact.toString()
        }

        mainView?.findViewById<ImageButton>(R.id.setLow)?.setOnClickListener {
            fact -= 0.5
            factCountView?.text = fact.toString()
        }*/

        val positiveButtonClick = { dialog: DialogInterface, which: Int ->
            try {

                mainFragment.okFactDialogClicked(fact)
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

        val editText = mainView?.findViewById<EditText>(R.id.factTextSet)

        editText?.addTextChangedListener {
            try {
                fact = if (editText.text.toString() != ""){
                    editText.text.toString().toDouble()
                } else {
                    0.0
                }

            } catch (e : Exception) {
                Toast.makeText(requireContext(),"Факт введен некорректно",Toast.LENGTH_SHORT).show()
            }
        }

       /* mainView?.findViewById<ImageButton>(R.id.planToFact)?.setOnClickListener {
            mainView.findViewById<EditText>(R.id.factCount)?.setText(
                plan.toString(),
                TextView.BufferType.EDITABLE
            )
        }*/



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

    class RepeatListener(
        private val initialInterval: Long, private val normalInterval: Long,
        val clickListener: View.OnClickListener
    ) : OnTouchListener {
        private val handler: Handler = Handler()
        private var touchedView: View? = null
        private var wasLongClick : Boolean = false

        private val handlerRunnable: Runnable = object : Runnable {
            override fun run() {
                if (touchedView!!.isEnabled) {
                    handler.postDelayed(this, normalInterval)
                    clickListener.onClick(touchedView)
                    wasLongClick = true

                } else {
                    // if the view was disabled by the clickListener, remove the callback
                    handler.removeCallbacks(this)
                    touchedView!!.isPressed = false
                    touchedView = null
                }
            }
        }

        override fun onTouch(view: View, motionEvent: MotionEvent): Boolean {
            when (motionEvent.action) {
                MotionEvent.ACTION_DOWN -> {
                    handler.removeCallbacks(handlerRunnable)
                    handler.postDelayed(handlerRunnable, initialInterval)
                    touchedView = view
                    touchedView!!.isPressed = true
                    clickListener.onClick(view)
                    return true
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    handler.removeCallbacks(handlerRunnable)
                    touchedView!!.isPressed = false
                    touchedView = null
                    return true
                }
            }
            return false
        }

        init {
            require(!(initialInterval < 0 || normalInterval < 0)) { "negative interval" }

        }
    }

}