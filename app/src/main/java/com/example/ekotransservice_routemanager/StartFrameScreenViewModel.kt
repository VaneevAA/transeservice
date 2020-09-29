package com.example.ekotransservice_routemanager

import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.liveData
import androidx.preference.PreferenceManager
import com.example.ekotransservice_routemanager.DataBaseInterface.RouteRepository
import com.example.ekotransservice_routemanager.DataClasses.Route
import com.example.ekotransservice_routemanager.DataClasses.Vehicle
import java.lang.IllegalArgumentException

class StartFrameScreenViewModel (val activity: MainActivity): ViewModel() {
    // TODO: Implement the ViewModel
    private val routeRepository = RouteRepository(activity.application)
    var routeLiveData : LiveData<Route> = liveData {
        activity.mSwipeRefreshLayout!!.isRefreshing = true
        try {
            emit(routeRepository.getCurrentRoute()!!)
        }catch (e:Exception){
            Toast.makeText(activity.applicationContext, "Нет подключения к БД", Toast.LENGTH_LONG).show()
        }
        activity.mSwipeRefreshLayout!!.isRefreshing = false
    }

    var vehicle : LiveData<Vehicle> = liveData<Vehicle> {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity.applicationContext)
        emit(Vehicle(sharedPreferences.getString("VEHICLE","") as String))
    }

    class StartFrameScreenModelFactory(private  val activity: MainActivity): ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if(modelClass.isAssignableFrom(StartFrameScreenViewModel::class.java)){
                return StartFrameScreenViewModel(activity) as T
            }
            throw IllegalArgumentException("Unknown class")
        }
    }

    fun onRefresh (){
        routeLiveData = liveData {
            try {
                 emit(routeRepository.getCurrentRoute()!!)
            }catch (e:Exception){
                Toast.makeText(activity.applicationContext, "Нет подключения к БД", Toast.LENGTH_LONG).show()
            }
        }
        vehicle = liveData<Vehicle> {
            val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity.applicationContext)
            emit(Vehicle(sharedPreferences.getString("VEHICLE","") as String))
        }
        activity.mSwipeRefreshLayout!!.isRefreshing = false
    }
}