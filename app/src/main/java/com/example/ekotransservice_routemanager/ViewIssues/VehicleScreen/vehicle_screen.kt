package com.example.ekotransservice_routemanager.ViewIssues.VehicleScreen

import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AutoCompleteTextView
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import androidx.preference.PreferenceManager
import com.example.ekotransservice_routemanager.DataClasses.Region
import com.example.ekotransservice_routemanager.DataClasses.Vehicle
import com.example.ekotransservice_routemanager.MainActivity
import com.example.ekotransservice_routemanager.R
import com.google.android.material.transition.MaterialContainerTransform
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class vehicle_screen : Fragment() {

    private var mViewVehicle : ViewVehicle? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedElementEnterTransition = MaterialContainerTransform()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view : View = inflater.inflate(R.layout.fragment_vehicle_screen, container, false)
        try {
            mViewVehicle = ViewModelProvider(
                this.requireActivity(),
                ViewVehicle.ViewVehicleFactory(this.requireActivity().application)
            ).get(ViewVehicle::class.java)
        } catch (e:Exception) {
            Log.d("error", "Vehicle $e")
        }
        val currentRegion = mViewVehicle!!.currentRegion
        val currentVehicle = mViewVehicle!!.currentVehicle

        val regionName: AutoCompleteTextView = view.findViewById(R.id.AutoCompleteTextViewRegionName)
        val dataList: ArrayList<Region> = ArrayList()
        val adapter = RegionListAdapter(view.context, R.layout.regionlist_item,dataList,requireActivity() as MainActivity)

        if (currentRegion != null && currentRegion.getUid() != "" ) {
            setVehicleAdapter(currentRegion,view)
        }

        regionName.setAdapter(adapter)
        regionName.setOnItemClickListener { parent, _, position, id ->
            val selectedItem = parent.adapter.getItem(position) as Region?
            regionName.setText(selectedItem?.toString())
            savePreference("REGION",selectedItem!!.toJSONString())
            mViewVehicle!!.currentRegion = selectedItem
            setVehicleAdapter(selectedItem,view)
        }

        val vehicleName: AutoCompleteTextView = view.findViewById(R.id.AutoCompleteTextViewVehicle)
        vehicleName.setOnItemClickListener { parent, _, position, id ->
            val selectedItem = parent.adapter.getItem(position) as Vehicle?
            vehicleName.setText(selectedItem?.toString())
            savePreference("VEHICLE",selectedItem?.toJSONString())
            mViewVehicle!!.currentVehicle = selectedItem
        }
        if (currentRegion!=null) {
            regionName.setText(currentRegion.toString())
        }
        if (currentVehicle!=null) {
            vehicleName.setText(currentVehicle.toString())
        }

        val datePref: TextView = view.findViewById(R.id.editTextDate)
        /*
        datePref.setText(SimpleDateFormat(
            "YYYY.MM.dd",
            Locale("ru")
        ).format(mViewVehicle!!.currentDate))

        datePref.addTextChangedListener {
            try {
                mViewVehicle!!.currentDate = SimpleDateFormat(
                    "yyyy.MM.dd",
                    Locale("ru")
                ).parse(it.toString())

            } catch(e: Exception) {

            }
            savePreference("DATE", it.toString())
        }*/
        datePref.setOnClickListener {
            setDate(it)
        }
        datePref.text = SimpleDateFormat(
            "YYYY.MM.dd",
            Locale("ru")
        ).format(mViewVehicle!!.currentDate)
         return view
    }

    private fun savePreference(prefName: String, prefValue: String?){
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireActivity().application)
        val prefEditor = sharedPreferences.edit()
        if (prefValue!=null){
            prefEditor.putString(prefName,prefValue)
            prefEditor.commit()
        }
    }

    private fun setVehicleAdapter(region: Region, view: View){
        val adapter = VehicleListAdapter(view.context,
            R.layout.regionlist_item, ArrayList(),region)
        val vehicleName: AutoCompleteTextView = view.findViewById(R.id.AutoCompleteTextViewVehicle)
        vehicleName.setAdapter(adapter)
    }

    private fun setDate(v:View){
        val tempCalendar = Calendar.getInstance()
        tempCalendar.time = mViewVehicle!!.currentDate
        DatePickerDialog(requireContext()
            ,DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->
                val calendar = Calendar.getInstance()
                calendar.set(year,month,dayOfMonth)
                mViewVehicle!!.currentDate = calendar.time
                savePreference("DATE", SimpleDateFormat(
                    "YYYY.MM.dd",
                    Locale("ru")
                ).format(mViewVehicle!!.currentDate))
                (v as TextView).text = SimpleDateFormat(
                    "YYYY.MM.dd",
                    Locale("ru")
                ).format(mViewVehicle!!.currentDate)
            },tempCalendar.get(Calendar.YEAR),
            tempCalendar.get(Calendar.MONTH),
            tempCalendar.get(Calendar.DAY_OF_MONTH)).show()
    }
}