package com.example.ekotransservice_routemanager

import android.widget.Toast
import androidx.lifecycle.*
import androidx.preference.PreferenceManager
import com.example.ekotransservice_routemanager.DataBaseInterface.RouteRepository
import com.example.ekotransservice_routemanager.DataClasses.Route
import com.example.ekotransservice_routemanager.DataClasses.Vehicle
import kotlinx.coroutines.launch
import java.lang.IllegalArgumentException

class StartFrameScreenViewModel (private val activity: MainActivity): ViewModel() {
    // TODO: Implement the ViewModel
    private val routeRepository = RouteRepository(activity.application)
    var routeLiveData : MutableLiveData<Route> = MutableLiveData()/*liveData {
        activity.mSwipeRefreshLayout!!.isRefreshing = true
        try {
            emit(routeRepository.getCurrentRoute()!!)
        }catch (e:Exception){
            Toast.makeText(activity.applicationContext, "Нет подключения к БД", Toast.LENGTH_LONG).show()
        }
        activity.mSwipeRefreshLayout!!.isRefreshing = false
    }*/

    var vehicle : MutableLiveData<Vehicle> = MutableLiveData()/*liveData<Vehicle> {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity.applicationContext)
        emit(Vehicle(sharedPreferences.getString("VEHICLE","") as String))
    }*/

    class StartFrameScreenModelFactory(private  val activity: MainActivity): ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if(modelClass.isAssignableFrom(StartFrameScreenViewModel::class.java)){
                return StartFrameScreenViewModel(activity) as T
            }
            throw IllegalArgumentException("Unknown class")
        }
    }

    fun onRefresh (){
        /*routeLiveData = liveData {
            try {
                 emit(routeRepository.getCurrentRoute()!!)
            }catch (e:Exception){
                Toast.makeText(activity.applicationContext, "Нет подключения к БД", Toast.LENGTH_LONG).show()
            }
        }*/
        viewModelScope.launch {
            val valueRoute = routeRepository.getCurrentRoute()!!
            if(valueRoute != null){
                routeLiveData.value = valueRoute
            }else{
                Toast.makeText(activity.applicationContext, "Нет подключения к БД", Toast.LENGTH_LONG).show()
            }

            val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity.applicationContext)
            vehicle.value = Vehicle(sharedPreferences.getString("VEHICLE","") as String)
            activity.mSwipeRefreshLayout!!.isRefreshing = false
        }
        /*vehicle = liveData<Vehicle> {
            val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity.applicationContext)
            emit(Vehicle(sharedPreferences.getString("VEHICLE","") as String))
        }*/

    }
}