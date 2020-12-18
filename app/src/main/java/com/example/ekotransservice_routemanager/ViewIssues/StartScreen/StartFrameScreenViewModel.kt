package com.example.ekotransservice_routemanager.ViewIssues.StartScreen

import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat.startActivity
import androidx.lifecycle.*
import androidx.preference.PreferenceManager
import com.example.ekotransservice_routemanager.DataBaseInterface.RouteRepository
import com.example.ekotransservice_routemanager.DataClasses.Route
import com.example.ekotransservice_routemanager.DataClasses.Vehicle
import com.example.ekotransservice_routemanager.MainActivity
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.lang.IllegalArgumentException

class StartFrameScreenViewModel (private val activity: MainActivity): ViewModel() {
    // TODO: Implement the ViewModel
    val errorLiveData : MutableLiveData<Boolean> = MutableLiveData<Boolean>(false)
    val routeRepository = RouteRepository.getInstance(activity.applicationContext)
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

    var fileApk: MutableLiveData<File> = MutableLiveData()

    class StartFrameScreenModelFactory(private  val activity: MainActivity): ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if(modelClass.isAssignableFrom(StartFrameScreenViewModel::class.java)){
                return StartFrameScreenViewModel(activity) as T
            }
            throw IllegalArgumentException("Unknown class")
        }
    }

    fun onRefresh (reload: Boolean = false){
        /*routeLiveData = liveData {
            try {
                 emit(routeRepository.getCurrentRoute()!!)
            }catch (e:Exception){
                Toast.makeText(activity.applicationContext, "Нет подключения к БД", Toast.LENGTH_LONG).show()
            }
        }*/
        viewModelScope.launch {
            if (reload) {
                val pointList = routeRepository.getPointList(true)
            }
            val valueRoute = routeRepository.getCurrentRoute()
            if(valueRoute != null){
                routeLiveData.value = valueRoute
            }else if(routeRepository.getErrorsCount() > 0){
                Toast.makeText(activity.applicationContext, "Маршрут не загружен", Toast.LENGTH_LONG).show()
                errorLiveData.value = true
            } else {
                Toast.makeText(activity.applicationContext, "Нет маршрута на заданную дату", Toast.LENGTH_LONG).show()
            }

            val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity.applicationContext)
            vehicle.value = Vehicle(sharedPreferences.getString("VEHICLE","") as String)
            routeRepository.setVehicle(vehicle.value)
            activity.mSwipeRefreshLayout!!.isRefreshing = false

        }
        /*vehicle = liveData<Vehicle> {
            val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity.applicationContext)
            emit(Vehicle(sharedPreferences.getString("VEHICLE","") as String))
        }*/

    }

    fun finishRoute(){
        viewModelScope.launch {
            val result = routeRepository.uploadTrackListToServerAsync()
            if (result) {
                Toast.makeText(activity.applicationContext,"Данные выгружены успешно",Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(activity.applicationContext,"Ошибка выгрузки",Toast.LENGTH_LONG).show()
            }
            routeLiveData.value = routeRepository.getCurrentRoute()
        }
    }

    fun loadApk(){
        val dir = activity.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
        val fileName = "apk_release.apk"
        viewModelScope.launch {
            fileApk.value = routeRepository.loadApkAsync(dir!!,fileName)
        }
    }
}
