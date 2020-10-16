package com.example.ekotransservice_routemanager.ViewIssues.VehicleScreen

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AutoCompleteTextView
import androidx.lifecycle.ViewModelProvider
import androidx.preference.PreferenceManager
import com.example.ekotransservice_routemanager.DataClasses.Region
import com.example.ekotransservice_routemanager.DataClasses.Vehicle
import com.example.ekotransservice_routemanager.R
import com.example.ekotransservice_routemanager.RegionListAdapter
import java.lang.Exception

class vehicle_screen : Fragment() {

    private var mViewVehicle : ViewVehicle? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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

        val RegionName: AutoCompleteTextView = view.findViewById(R.id.AutoCompleteTextViewRegionName)
        val dataList: ArrayList<Region> = ArrayList<Region>()
        val adapter = RegionListAdapter(view.context, R.layout.regionlist_item,dataList)

        RegionName.setAdapter(adapter)
        RegionName.setOnItemClickListener() { parent, _, position, id ->
            val selectedItem = parent.adapter.getItem(position) as Region?
            RegionName.setText(selectedItem?.toString())
            savePrefernce("REGION",selectedItem!!.toJSONString())
            mViewVehicle!!.currentRegion = selectedItem
            if (selectedItem!=null){
                val adapter = VehicleListAdapter(view.context,
                    R.layout.regionlist_item, ArrayList<Vehicle>(),selectedItem)
                val VehicleName: AutoCompleteTextView = view.findViewById(R.id.AutoCompleteTextViewVehicle)
                VehicleName.setAdapter(adapter)
            }
        }

        val VehicleName: AutoCompleteTextView = view.findViewById(R.id.AutoCompleteTextViewVehicle)
        VehicleName.setOnItemClickListener { parent, _, position, id ->
            val selectedItem = parent.adapter.getItem(position) as Vehicle?
            VehicleName.setText(selectedItem?.toString())
            savePrefernce("VEHICLE",selectedItem?.toJSONString())
            mViewVehicle!!.currentVehicle = selectedItem
        }
        if (currentRegion!=null) {
            RegionName.setText(currentRegion.toString())
        }
        if (currentVehicle!=null) {
            VehicleName.setText(currentVehicle.toString())
        }

         return view
    }

    private fun savePrefernce(prefName: String, prefValue: String?){
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireActivity().application)
        val prefEditor = sharedPreferences.edit()
        if (prefValue!=null){
            prefEditor.putString(prefName,prefValue)
            prefEditor.commit()
        }
    }

}