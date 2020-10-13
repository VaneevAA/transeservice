package com.example.ekotransservice_routemanager

import android.app.Application
import android.content.Context
import androidx.annotation.UiThread
import androidx.lifecycle.*
import com.example.ekotransservice_routemanager.DataBaseInterface.RouteRepository
import com.example.ekotransservice_routemanager.DataClasses.PhotoOrder
import com.example.ekotransservice_routemanager.DataClasses.Point
import com.example.ekotransservice_routemanager.DataClasses.PointFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.io.File
import java.lang.IllegalArgumentException
import java.util.*

class ViewPointAction(application: Application,point: Point) : AndroidViewModel(application) {

   private val routeRepository: RouteRepository = RouteRepository(application)

    val currentPoint: MutableLiveData<Point> = MutableLiveData(point)
    var currentFile: PointFile? = null

    var fileBeforeIsDone: MutableLiveData<Boolean> = MutableLiveData()
    var fileAfterIsDone: MutableLiveData<Boolean> = MutableLiveData()

    init {
        viewModelScope.launch {
            var data = routeRepository.getFilesFromDBAsync(currentPoint.value!!, PhotoOrder.PHOTO_AFTER)
            fileAfterIsDone.value = data!!.size > 0
            data = routeRepository.getFilesFromDBAsync(currentPoint.value!!, PhotoOrder.PHOTO_BEFORE)
            fileBeforeIsDone.value = data!!.size > 0
        }
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
        emit( data!!.size>0 )
    }
    var fileAfterIsDone: LiveData<Boolean> = liveData {
        val data = routeRepository.getFilesFromDBAsync(point,PhotoOrder.PHOTO_AFTER)
        emit( data!!.size>0 ) }*/


    class ViewPointsFactory(private val application: Application,val point: Point): ViewModelProvider.Factory{
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if(modelClass.isAssignableFrom(ViewPointAction::class.java)){
                return ViewPointAction(application,point) as T
            }
            throw IllegalArgumentException("Unknown class")
        }

    }

    fun getPoint() : MutableLiveData<Point> {
        return currentPoint
    }

    /*fun setPoint(point:Point){
        currentPoint.value = point
    }

    fun setPointData(){
        viewModelScope.launch {
            var data =
                routeRepository.getFilesFromDBAsync(currentPoint.value!!, PhotoOrder.PHOTO_AFTER)
            fileAfterIsDone.value = data!!.size > 0
            data =
                routeRepository.getFilesFromDBAsync(currentPoint.value!!, PhotoOrder.PHOTO_BEFORE)
            fileBeforeIsDone.value = data!!.size > 0
        }
    }*/

    fun saveFile(file: File, point: Point, fileOrder: PhotoOrder) {
        val exifInterface = androidx.exifinterface.media.ExifInterface(file.absoluteFile)
        val latLon = exifInterface.latLong
        val pointFile: PointFile = PointFile(point!!.getLineUID(), Date(file.lastModified()), fileOrder,
            latLon!!.get(0), latLon!!.get(1))
        currentFile = pointFile
        saveCurrentFile()

    }

    /*fun saveCurrentFile() {
        viewModelScope.launch {
            if (currentFile!!.photoOrder == PhotoOrder.PHOTO_BEFORE) {
                val result = kotlin.runCatching { routeRepository.saveFileIntoDBAsync(currentFile!!)}
                result.onSuccess {
                    fileBeforeIsDone.postValue(true)
                }.onFailure {
                    fileBeforeIsDone.postValue(false) }
            } else {
                val result = routeRepository.saveFileIntoDBAsync(currentFile!!)
                fileAfterIsDone.postValue(result)
            }
        }
    }*/

    @UiThread
    fun saveCurrentFile(): MutableLiveData<Boolean>{

            GlobalScope.launch(Dispatchers.Main) {
                if (currentFile!!.photoOrder == PhotoOrder.PHOTO_BEFORE) {
                    val result = async(Dispatchers.IO) {
                        return@async routeRepository.saveFileIntoDBAsync(currentFile!!)
                    }.await()
                    fileBeforeIsDone.value = result

                } else {
                    val result = routeRepository.saveFileIntoDBAsync(currentFile!!)
                    fileAfterIsDone.postValue(result)
                }
            }

        return fileBeforeIsDone
    }



    private suspend fun saveFileIntDB(pointFile: PointFile): Boolean {
        val data = viewModelScope.async {routeRepository.saveFileIntoDBAsync(pointFile)}
        return data.await()
    }

}