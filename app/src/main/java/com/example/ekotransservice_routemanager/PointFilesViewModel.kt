package com.example.ekotransservice_routemanager

import android.app.Application
import androidx.lifecycle.*
import com.example.ekotransservice_routemanager.DataBaseInterface.RouteRepository
import com.example.ekotransservice_routemanager.DataClasses.PhotoOrder
import com.example.ekotransservice_routemanager.DataClasses.Point
import com.example.ekotransservice_routemanager.DataClasses.PointFile
import kotlinx.coroutines.launch

class PointFilesViewModel(val activity: MainActivity, val point : Point) : ViewModel() {
    val files : MutableLiveData<MutableList<PointFile>> = MutableLiveData()
    private val routeRepository = RouteRepository.getInstance(activity.applicationContext)

    fun loadDataFromDB() {
        viewModelScope.launch {
            val resultBefore = routeRepository.getFilesFromDBAsync(point, PhotoOrder.PHOTO_BEFORE)
            if (resultBefore != null) {
                if (files.value == null) {
                    files.value = resultBefore as MutableList<PointFile>
                } else {
                    files.value?.addAll(resultBefore)
                }
            }
        }

        viewModelScope.launch {
            val resultAfter = routeRepository.getFilesFromDBAsync(point, PhotoOrder.PHOTO_AFTER)
            if (resultAfter != null) {
                if (files.value == null) {
                    files.value = resultAfter as MutableList<PointFile>
                } else {
                    files.value?.addAll(resultAfter)
                }
            }
        }
        viewModelScope.launch {
            val resultDontSet = routeRepository.getFilesFromDBAsync(point, PhotoOrder.DONT_SET)
            if (resultDontSet != null) {
                if (files.value == null) {
                    files.value = resultDontSet as MutableList<PointFile>
                } else {
                    files.value?.addAll(resultDontSet)
                }
            }
            activity.mSwipeRefreshLayout!!.isRefreshing = false
        }

    }


    fun getList () : MutableLiveData<MutableList<PointFile>>{
        return files

    }
}