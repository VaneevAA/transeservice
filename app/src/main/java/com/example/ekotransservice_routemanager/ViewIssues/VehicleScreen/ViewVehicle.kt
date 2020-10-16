package com.example.ekotransservice_routemanager.ViewIssues.VehicleScreen

import android.app.Application
import androidx.lifecycle.*
import androidx.preference.PreferenceManager
import com.example.ekotransservice_routemanager.DataBaseInterface.RouteRepository
import com.example.ekotransservice_routemanager.DataClasses.Region
import com.example.ekotransservice_routemanager.DataClasses.Vehicle
import kotlinx.coroutines.async
import java.lang.IllegalArgumentException

class ViewVehicle (application: Application): AndroidViewModel(application) {

    var regionList = MutableLiveData<MutableList<Region>>()
    var vehicleList =  MutableLiveData<MutableList<Vehicle>>()
    var currentRegion: Region? = null
    var currentVehicle: Vehicle? = null

    private val routeRepository: RouteRepository = RouteRepository.getInstance(application.applicationContext)
    //val routeRepository: RouteRepository = RouteRepository(application)

    init {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(application.applicationContext)
        currentRegion = Region(sharedPreferences.getString("REGION","") as String)
        currentVehicle = Vehicle(sharedPreferences.getString("VEHICLE","") as String)
       /* regionList.value = ArrayList<Region>()
        viewModelScope.launch { loadRegion() }*/
    }

    class ViewVehicleFactory(private val application: Application): ViewModelProvider.Factory{
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if(modelClass.isAssignableFrom(ViewVehicle::class.java)){
                return ViewVehicle(application) as T
            }
            throw IllegalArgumentException("Unknown class")
        }

    }

    private suspend fun loadRegion() {
        //regionList.value?.add(currentRegion)
        val serverList = viewModelScope.async {routeRepository.getRegionList()}
        val result = serverList.await()
        if (result != null){
            for (region: Region in result){
                regionList.value?.add(region)
            }
        }
    }


    fun getList () : MutableLiveData<MutableList<Region>> {
        return regionList
    }
}
