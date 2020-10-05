package com.example.ekotransservice_routemanager

import android.app.Application
import androidx.lifecycle.*
import com.example.ekotransservice_routemanager.DataBaseInterface.RouteRepository
import com.example.ekotransservice_routemanager.DataClasses.PhotoOrder
import com.example.ekotransservice_routemanager.DataClasses.Point
import kotlinx.coroutines.launch
import java.io.File
import java.lang.IllegalArgumentException

class ViewPointAction(application: Application,point: Point) : AndroidViewModel(application) {
    var pointAction : MutableLiveData<Point> = MutableLiveData(point)

   private val routeRepository: RouteRepository = RouteRepository(application)

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

    /*fun saveFileIntoDB(file: File, point: Point, fileOrder: PhotoOrder){
        viewModelScope.launch { routeRepository.savePhotoInRoomDatabase(file,point,fileOrder) }
    }*/
}