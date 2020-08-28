package com.example.ekotransservice_routemanager

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.ekotransservice_routemanager.DataClasses.Point
import java.lang.IllegalArgumentException

class ViewPointList(application: Application):AndroidViewModel(application) {
    var pointsList = MutableLiveData<MutableList<Point>>()

    init {
        pointsList.value = ArrayList<Point>()
        loadData()
    }

    class ViewPointsFactory(private val application: Application):ViewModelProvider.Factory{
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if(modelClass.isAssignableFrom(ViewPointList::class.java)){
                return ViewPointList(application) as T
            }
            throw IllegalArgumentException("Unknown class")
        }

    }

    private fun loadData(){
        pointsList.value?.add(Point("test point 1",65.77777,58.88888,false,1,"контейнер 1.1м3"))
        pointsList.value?.add(Point("test point 2",65.77777,58.88888,false,2,"пакет 0.1м3"))

    }

    fun getList () : MutableLiveData<MutableList<Point>> {
        return pointsList
    }
}