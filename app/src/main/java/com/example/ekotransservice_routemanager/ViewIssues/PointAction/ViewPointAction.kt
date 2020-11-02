package com.example.ekotransservice_routemanager.ViewIssues.PointAction

import android.app.Application
import androidx.lifecycle.*
import com.example.ekotransservice_routemanager.DataBaseInterface.RouteRepository
import com.example.ekotransservice_routemanager.DataClasses.PhotoOrder
import com.example.ekotransservice_routemanager.DataClasses.Point
import com.example.ekotransservice_routemanager.DataClasses.PointFile
import com.example.ekotransservice_routemanager.MainActivity
import kotlinx.coroutines.launch
import java.io.File
import java.lang.IllegalArgumentException
import java.util.*

class ViewPointAction(application: Application, activity: MainActivity, point: Point) : AndroidViewModel(application) {

    private val routeRepository: RouteRepository = RouteRepository.getInstance(application.applicationContext)

    val currentPoint: MutableLiveData<Point> = MutableLiveData(point)

    val fileBeforeIsDone: MutableLiveData<Boolean> = MutableLiveData(false)
    val fileAfterIsDone: MutableLiveData<Boolean> = MutableLiveData(false)

    /*init {
        currentPoint.value = point
        viewModelScope.launch {
            var data = routeRepository.getFilesFromDBAsync(currentPoint.value!!, PhotoOrder.PHOTO_AFTER)
            fileAfterIsDone.value = data!!.size > 0
            data = routeRepository.getFilesFromDBAsync(currentPoint.value!!, PhotoOrder.PHOTO_BEFORE)
            fileBeforeIsDone.value = data!!.size > 0
        }
    }*/

    fun setViewData(point: Point){
        currentPoint.value = point
        viewModelScope.launch {
            var data = routeRepository.getFilesFromDBAsync(currentPoint.value!!, PhotoOrder.PHOTO_AFTER)
            fileAfterIsDone.value = data!!.size > 0
            data = routeRepository.getFilesFromDBAsync(currentPoint.value!!, PhotoOrder.PHOTO_BEFORE)
            fileBeforeIsDone.value = data!!.size > 0
        }
    }

    fun getRepository(): RouteRepository{
        return routeRepository
    }

    /*var fileBeforeIsDone: LiveData<Boolean> = currentState.switchMap {
        liveData {
            val data = routeRepository.getFilesFromDBAsync(currentPoint.value!!,PhotoOrder.PHOTO_BEFORE)
            emit( data!!.size>0 )
        }
    }

    var fileAfterIsDone: LiveData<Boolean> = currentPoint.switchMap {
            liveData {
                val data = routeRepository.getFilesFromDBAsync(point,PhotoOrder.PHOTO_AFTER)
                emit( data!!.size>0 ) }
        }*/

    /*var fileBeforeIsDone: LiveData<Boolean> = liveData {
        val data = routeRepository.getFilesFromDBAsync(point,PhotoOrder.PHOTO_BEFORE)
        emit( data!!.size > 0 )
    }
    var fileAfterIsDone: LiveData<Boolean> = liveData {
        val data = routeRepository.getFilesFromDBAsync(point,PhotoOrder.PHOTO_AFTER)

        emit( data!!.size>0 ) }*/


    class ViewPointsFactory(private val application: Application, private val activity: MainActivity, val point: Point): ViewModelProvider.Factory{
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if(modelClass.isAssignableFrom(ViewPointAction::class.java)){
                return ViewPointAction(application,activity,point) as T
            }
            throw IllegalArgumentException("Unknown class")
        }
    }

    fun getPoint() : MutableLiveData<Point> {
        return currentPoint
    }


    fun saveFile(file: File, point: Point, fileOrder: PhotoOrder) {
        val exifInterface = androidx.exifinterface.media.ExifInterface(file.absoluteFile)
        val latLon = exifInterface.latLong
        var lat = 0.0
        var lon = 0.0
        if (latLon!=null && latLon.size>0) {
            lat = latLon[0]
            lon = latLon[1]
        }
        val pointFile = PointFile(
            point.getDocUID(), point.getLineUID(), Date(file.lastModified()), fileOrder,
            lat,
            lon,
            file.absolutePath, file.name, file.extension)
        viewModelScope.launch {
            val result = routeRepository.saveFileIntoDBAsync(pointFile)
            if (pointFile.photoOrder == PhotoOrder.PHOTO_BEFORE) {
                fileBeforeIsDone.value = result
            } else {
                fileAfterIsDone.value = result
            }
        }

    }

}
