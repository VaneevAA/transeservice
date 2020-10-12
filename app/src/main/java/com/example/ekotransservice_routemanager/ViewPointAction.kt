package com.example.ekotransservice_routemanager

import android.app.Application
import androidx.lifecycle.*
import com.example.ekotransservice_routemanager.DataBaseInterface.RouteRepository
import com.example.ekotransservice_routemanager.DataClasses.PhotoOrder
import com.example.ekotransservice_routemanager.DataClasses.Point
import com.example.ekotransservice_routemanager.DataClasses.PointFile
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.io.File
import java.lang.IllegalArgumentException
import java.util.*

class ViewPointAction(application: Application,point: Point) : AndroidViewModel(application) {
    var pointAction : MutableLiveData<Point> = MutableLiveData(point)


   private val routeRepository: RouteRepository = RouteRepository(application)

    //var fileBeforeIsDone: LiveData<Boolean> = liveData { false }
    var fileBeforeIsDone: LiveData<Boolean> = liveData {
        val data = routeRepository.getFilesFromDBAsync(point,PhotoOrder.PHOTO_BEFORE)
        emit( data!!.size>0 )
    }
    var fileAfterIsDone: LiveData<Boolean> = liveData {
        val data = routeRepository.getFilesFromDBAsync(point,PhotoOrder.PHOTO_AFTER)
        emit( data!!.size>0 ) }

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

    fun setFilesIsDone(point: Point) {

        fileBeforeIsDone = liveData {
            val data = routeRepository.getFilesFromDBAsync(point,PhotoOrder.PHOTO_BEFORE)
            emit( data!!.size>0 )
        }

        fileAfterIsDone = liveData {
            val data = routeRepository.getFilesFromDBAsync(point,PhotoOrder.PHOTO_AFTER)
            emit( data!!.size>0 )
        }
    }

    fun saveFile(file: File, point: Point, fileOrder: PhotoOrder){
        val exifInterface = androidx.exifinterface.media.ExifInterface(file.absoluteFile)
        val latLon = exifInterface.latLong
        val pointFile: PointFile = PointFile(point!!.getLineUID(), Date(file.lastModified()), fileOrder,
            latLon!!.get(0), latLon!!.get(1))
        //viewModelScope.launch { routeRepository.saveFileIntoDBAsync(pointFile) }
        if (fileOrder == PhotoOrder.PHOTO_BEFORE) {
            fileBeforeIsDone  = liveData {
                val data = routeRepository.saveFileIntoDBAsync(pointFile)
                emit(data)
            }
        } else {
            fileAfterIsDone = liveData { routeRepository.saveFileIntoDBAsync(pointFile) }
        }
    }

    private suspend fun saveFileIntDB(pointFile: PointFile): Boolean {
        val data = viewModelScope.async {routeRepository.saveFileIntoDBAsync(pointFile)}
        return data.await()
    }

}