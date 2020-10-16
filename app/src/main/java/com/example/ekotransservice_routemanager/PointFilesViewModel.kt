package com.example.ekotransservice_routemanager

import android.app.Application
import androidx.lifecycle.*
import com.example.ekotransservice_routemanager.DataBaseInterface.RouteRepository
import com.example.ekotransservice_routemanager.DataClasses.PhotoOrder
import com.example.ekotransservice_routemanager.DataClasses.Point
import com.example.ekotransservice_routemanager.DataClasses.PointFile
import kotlinx.coroutines.launch

class PointFilesViewModel(private val activity: MainActivity, val point : Point) : ViewModel() {
    val files : MutableLiveData<MutableList<PointFile>> = MutableLiveData()
    private val routeRepository = RouteRepository.getInstance(activity.applicationContext)

    fun loadDataFromDB() {
        viewModelScope.launch {
            val resultBefore = routeRepository.getFilesFromDBAsync(point)
            if (resultBefore != null) {
                if (files.value == null) {
                    files.value = resultBefore as MutableList<PointFile>
                } else {
                    files.value?.addAll(resultBefore)
                }
            }
        }


    }


    fun getList () : MutableLiveData<MutableList<PointFile>>{
        return files

    }
}