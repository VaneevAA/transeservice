package com.example.ekotransservice_routemanager

import android.app.Application
import androidx.lifecycle.*
import com.example.ekotransservice_routemanager.DataBaseInterface.RouteRepository
import com.example.ekotransservice_routemanager.DataClasses.Point
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.lang.IllegalArgumentException

class ViewPointList(application: Application):AndroidViewModel(application) {
    var pointsList = MutableLiveData<MutableList<Point>>()
    val routeRepository: RouteRepository = RouteRepository(application)
    init {
        pointsList.value = ArrayList<Point>()
        viewModelScope.launch { loadData() }
    }

    class ViewPointsFactory(private val application: Application):ViewModelProvider.Factory{
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if(modelClass.isAssignableFrom(ViewPointList::class.java)){
                return ViewPointList(application) as T
            }
            throw IllegalArgumentException("Unknown class")
        }

    }

    private suspend fun loadData() {
        pointsList.value?.add(Point("test point 1",65.77777,58.88888,false,1,"контейнер 1.1м3"))
        pointsList.value?.add(Point("test point 2",65.77777,58.88888,false,2,"пакет 0.1м3"))
        /*val trackList = viewModelScope.async {routeRepository.getPointList(true)}
        val result = trackList.await()
        if (result != null){
            for (point: Point in result){
                pointsList.value?.add(point)
            }
        }*/
            //pointsList = routeRepository.getPointList(true)
    }

    fun getList () : MutableLiveData<MutableList<Point>> {
        return pointsList
    }
}