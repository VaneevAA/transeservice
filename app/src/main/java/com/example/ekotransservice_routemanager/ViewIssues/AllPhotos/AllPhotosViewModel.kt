package com.example.ekotransservice_routemanager.ViewIssues.AllPhotos

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ekotransservice_routemanager.DataBaseInterface.RouteRepository
import com.example.ekotransservice_routemanager.DataClasses.Point
import com.example.ekotransservice_routemanager.MainActivity
import kotlinx.coroutines.launch

class AllPhotosViewModel (private val activity: MainActivity) : ViewModel() {
    val allPoints : MutableLiveData<MutableList<Point>> = MutableLiveData()
    private val routeRepository = RouteRepository.getInstance(activity.applicationContext)

    fun loadDataFromDB() {

        viewModelScope.launch {
            val result = routeRepository.getPointsWithFilesAsync()
            if(result != null){
                allPoints.value = result
            }
        }

    }

    fun getList () : MutableLiveData<MutableList<Point>> {
        return allPoints
    }
}