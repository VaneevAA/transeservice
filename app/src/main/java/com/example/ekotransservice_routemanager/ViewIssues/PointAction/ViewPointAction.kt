package com.example.ekotransservice_routemanager.ViewIssues.PointAction

import android.app.Application
import android.location.Location
import androidx.lifecycle.*
import com.example.ekotransservice_routemanager.DataBaseInterface.RouteRepository
import com.example.ekotransservice_routemanager.DataClasses.PhotoOrder
import com.example.ekotransservice_routemanager.DataClasses.Point
import com.example.ekotransservice_routemanager.DataClasses.PointFile
import com.example.ekotransservice_routemanager.MainActivity
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.lang.IllegalArgumentException
import java.util.*

class ViewPointAction(application: Application, activity: MainActivity, point: Point) : AndroidViewModel(application) {

    private val routeRepository: RouteRepository = RouteRepository.getInstance(application.applicationContext)

    val currentPoint: MutableLiveData<Point> = MutableLiveData(point)
    private var phoneNumberData = ""
    val fileBeforeIsDone: MutableLiveData<Boolean> = MutableLiveData(false)
    val fileAfterIsDone: MutableLiveData<Boolean> = MutableLiveData(false)
    var geoIsRequired: Boolean = false

    /*init {
        currentPoint.value = point
        viewModelScope.launch {
            var data = routeRepository.getFilesFromDBAsync(currentPoint.value!!, PhotoOrder.PHOTO_AFTER)
            fileAfterIsDone.value = data!!.size > 0
            data = routeRepository.getFilesFromDBAsync(currentPoint.value!!, PhotoOrder.PHOTO_BEFORE)
            fileBeforeIsDone.value = data!!.size > 0
        }
    }*/

    fun setViewData(point: Point,canDone: Boolean){
        phoneNumberData = point.getPhoneFromComment()
        currentPoint.value = point
        viewModelScope.launch {
            var data = routeRepository.getFilesFromDBAsync(currentPoint.value!!, PhotoOrder.PHOTO_AFTER)
            fileAfterIsDone.value = data!!.size > 0
            data = routeRepository.getFilesFromDBAsync(currentPoint.value!!,if (canDone) {PhotoOrder.PHOTO_BEFORE} else {PhotoOrder.PHOTO_CANTDONE})
            fileBeforeIsDone.value = data!!.size > 0
            data = routeRepository.getFilesFromDBAsync(currentPoint.value!!,null,true)
            geoIsRequired = data!!.size > 0

        }

    }

    fun getRepository(): RouteRepository{
        return routeRepository
    }

    fun getPhoneNumber() : String{
        return this.phoneNumberData
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


    fun saveFile(file: File, point: Point, fileOrder: PhotoOrder): PointFile {
        val exifInterface = androidx.exifinterface.media.ExifInterface(file.absoluteFile)
        val latLon = exifInterface.latLong
        var lat = 0.0
        var lon = 0.0
        if (latLon!=null) {
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
            if (pointFile.photoOrder == PhotoOrder.PHOTO_BEFORE || pointFile.photoOrder == PhotoOrder.PHOTO_CANTDONE) {
                fileBeforeIsDone.value = result
            } else {
                fileAfterIsDone.value = result
            }
        }

        return pointFile
    }

    fun setPointFilesGeodata(location: Location) {
        GlobalScope.launch {
            val result = routeRepository.getFilesFromDBAsync(currentPoint.value!!,null,true)
            result?.forEach {
                setDataInfoOnFile(it,location)
            }
            geoIsRequired = true
        }
    }

    fun setDataInfoOnFile(pointFile: PointFile, location: Location?) {
        val lon = location?.longitude ?: pointFile.lon
        val lat = location?.latitude ?: pointFile.lat
        pointFile.createResultImageFile(lat,lon,currentPoint.value!!,getApplication())
        if (location != null) {
            pointFile.setGeoTag(location)
        }
        routeRepository.updatePointFileLocationAsync(pointFile, lat, lon)
    }

}
