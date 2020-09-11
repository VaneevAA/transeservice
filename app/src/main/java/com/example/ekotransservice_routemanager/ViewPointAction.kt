package com.example.ekotransservice_routemanager

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.ekotransservice_routemanager.DataClasses.Point
import java.lang.IllegalArgumentException

class ViewPointAction(application: Application,point: Point) : AndroidViewModel(application) {
    var pointAction : MutableLiveData<Point> = MutableLiveData(point)

    class ViewPointsFactory(private val application: Application,val point: Point): ViewModelProvider.Factory{
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if(modelClass.isAssignableFrom(ViewPointAction::class.java)){
                return ViewPointAction(application,point) as T
            }
            throw IllegalArgumentException("Unknown class")
        }

    }

    fun getPoint() : MutableLiveData<Point> {
        return pointAction
    }

    fun setPoint(point:Point){
        pointAction.value = point
    }
}