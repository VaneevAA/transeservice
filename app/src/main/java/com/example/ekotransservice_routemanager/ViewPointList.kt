package com.example.ekotransservice_routemanager

import android.app.Application
import androidx.lifecycle.*
import com.example.ekotransservice_routemanager.DataBaseInterface.RouteRepository
import com.example.ekotransservice_routemanager.DataClasses.Point
import kotlinx.coroutines.async
import java.lang.IllegalArgumentException

class ViewPointList(application: Application, activity: MainActivity):AndroidViewModel(application) {
    //var pointsList = MutableLiveData<MutableList<Point>>()
    private val result : LiveData<MutableList<Point>> = liveData {
        activity.mSwipeRefreshLayout!!.isRefreshing = true
        emit(loadDataFromDB())
        activity.mSwipeRefreshLayout!!.isRefreshing = false
    }
    private val routeRepository: RouteRepository = RouteRepository(application)
    /*init {
        pointsList.value = mutableListOf()
        viewModelScope.launch { loadData() }

    }*/

    class ViewPointsFactory(private val application: Application, private  val activity: MainActivity):ViewModelProvider.Factory{
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if(modelClass.isAssignableFrom(ViewPointList::class.java)){
                return ViewPointList(application,activity) as T
            }
            throw IllegalArgumentException("Unknown class")
        }

    }

    /*private suspend fun loadData() {

        val trackList = viewModelScope.async {routeRepository.getPointList(true)}
        val result = trackList.await()
        if (result != null){
            for (point: Point in result){
                pointsList.value?.add(point)
            }
        }
    }*/

    private suspend fun loadDataFromDB() : MutableList<Point>{

        val trackList = viewModelScope.async {routeRepository.getPointList(true)}
        return trackList.await() ?: mutableListOf()
    }

    fun getList () : LiveData<MutableList<Point>> {
        return result
    }


}