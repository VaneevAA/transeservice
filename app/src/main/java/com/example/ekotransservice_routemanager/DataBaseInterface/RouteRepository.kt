package com.example.ekotransservice_routemanager.DataBaseInterface

import android.annotation.SuppressLint
import android.app.Application
import android.app.Notification
import android.content.Context
import android.os.Build
import android.util.JsonWriter
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.preference.PreferenceManager
import androidx.room.Room
import com.example.ekotransservice_routemanager.DataClasses.*
import com.example.ekotransservice_routemanager.ErrorMessage
import com.example.ekotransservice_routemanager.UploadResult
import kotlinx.coroutines.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.*
import java.lang.NumberFormatException
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.concurrent.Executors
import javax.net.ssl.HttpsURLConnection
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.reflect.jvm.internal.pcollections.HashPMap

class RouteRepository constructor(context: Context) {

    private val serverConnector: RouteServerConnection = RouteServerConnection()
    /*private val serverConnector: RouteServerConnection = RouteServerConnection(
        "192.168.106.248",
        "3000",
        "z5FYg733jGUwjmabuGdmZvfAkDHnh2Wj"
    )*/
    private var db: RouteRoomDatabase? = null
    private var mRoutesDao: RouteDaoInterface? = null
    private var vehicle: Vehicle? = null
    private var errorArrayList: ArrayList<ErrorMessage> = ArrayList()

    companion object {
        // Singleton prevents multiple instances of database opening at the
        // same time.
        @Volatile
        private var INSTANCE: RouteRepository? = null

        fun getInstance(context: Context): RouteRepository {
            val tempInstance = INSTANCE
            if (tempInstance != null) {
                return tempInstance
            }
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: RouteRepository(context.applicationContext)
                    .also { INSTANCE = it }
            }

        }
    }

    init {
        // инициальзация базы данных Room
        db = RouteRoomDatabase.getDatabase(context)
        mRoutesDao = db!!.routesDao()

        // Получение текущих настроек
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context.applicationContext)
        val urlName = sharedPreferences.getString("URL_NAME","") as String
        val urlPort = sharedPreferences.getString("URL_PORT","80") as String
        val urlPass = sharedPreferences.getString("URL_AUTHPASS","") as String
        val vehicleString = sharedPreferences.getString("VEHICLE", "") as String
        vehicle = Vehicle(vehicleString)

        // установка параметров подключения
        serverConnector.setConnectionParams(urlName,urlPort.toInt())
        serverConnector.setAuthPass(urlPass)
    }


    // Загрузка списка точек
    // reload - требуется загрузка с  Postgres
    suspend fun getPointList(reload: Boolean): MutableList<Point>? {
        return try {
            if (reload) {
                val currentRoute = GlobalScope.async { getCurrentRoute() }
                val dataLoaded = GlobalScope.async { loadTaskFromServer(currentRoute.await()) }
                val tracklist = GlobalScope.async { loadTrackListFromRoom(dataLoaded.await()) }
                tracklist.await()
            } else {
                val tracklist = GlobalScope.async { loadTrackListFromRoom(true) }
                tracklist.await()
            }
        }catch (e: java.lang.Exception){
            null
        }
    }

    suspend fun getPointsWithFilesAsync(): MutableList<Point>? {
        val result = GlobalScope.async { getPointsWithFiles() }
        return result.await()
    }

    private fun getPointsWithFiles():MutableList<Point>? {
        return try {
            val data = mRoutesDao!!.getPointsWithFiles()
            data
        } catch (e: java.lang.Exception) {
            null
        }
    }

    suspend fun getRegionList(): ArrayList<Region> {
        val serverData = GlobalScope.async { serverConnector.getRegions() }
        val downloadResult = serverData.await()
        errorArrayList.union(downloadResult.log)
        return downloadResult.data as ArrayList<Region>
    }

    suspend fun getVehiclesList(region: Region): ArrayList<Vehicle> {
        val serverData = GlobalScope.async { serverConnector.getVehicles(region.getUid()) }
        val downloadResult = serverData.await()
        errorArrayList.union(downloadResult.log)
        return downloadResult.data as ArrayList<Vehicle>
    }

    suspend fun getCurrentRoute(): Route?{
        val resultList = GlobalScope.async {  mRoutesDao!!.getCurrentRoute()}
        val currentRoute = resultList.await()
        if (currentRoute.size==0) {
            return null
        }else{
            return currentRoute[0]
        }
    }

    // Загрузка данных путевого листа с сервера и сохранение их в локальную базу Room
    private fun loadTaskFromServer(currentRoute: Route?): Boolean {
        errorArrayList.clear()
        val vehicleNumber: String? = if (currentRoute != null ) {currentRoute.getVehicleNumber() } else {this.vehicle!!.getName()}
        val dateTask: Date = if (currentRoute != null ) { currentRoute.getRouteDate() } else { SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2020-09-03 00:00:00") }
        if (vehicleNumber != null) {
            val postParam = JSONObject()
            postParam.put("dateTask",  SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(dateTask)) //"2020-09-03 00:00:00")//dateTask.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
            postParam.put("vehicle", vehicleNumber)
            val serverData = serverConnector.getTrackList(postParam)
            errorArrayList.union(serverData.log)
            val result = saveTrackListIntoRoom(serverData.data as ArrayList<Point>?)
            if (result && serverData.data.size!=0) {
                saveRouteIntoRoom(serverData.data,vehicle!!,dateTask)
            }
            return result
        } else {
            return false
        }
    }

    // Cохранение массива точек в локальную базу
    private fun saveTrackListIntoRoom(trackList: ArrayList<Point>?):Boolean {
        if (trackList != null) {
            return try {
                mRoutesDao!!.insertPointListOnlyNew(trackList)
                true
            } catch (e: java.lang.Exception){
                errorArrayList.add(ErrorMessage("Ошибка работы с локальной базой данных", "Ошибка записи данных",e))
                false
            }
        }
        return false
    }

    // Сохранение маршрута
    private fun saveRouteIntoRoom(data: ArrayList<Point>, vehicle: Vehicle, dateTask: Date ):Boolean {
        return try {
            val route = Route()
            route.setCountPoint(data.size)
            route.setVehicle(vehicle)
            route.setRouteDate(dateTask)
            route.setDocUid(data[0].getDocUID())
            mRoutesDao!!.insertRouteWithReplace(route)
            true
        } catch (e: java.lang.Exception){
            errorArrayList.add(ErrorMessage("Ошибка работы с локальной базой данных", "Ошибка записи данных",e))
            false
        }
    }


    // Получение списка точек из локальной базы
    private fun loadTrackListFromRoom(dataLoaded: Boolean): MutableList<Point>? {
        return if (dataLoaded) {
            try {
                val data = mRoutesDao!!.getCurrentList()
                data
            } catch (e: Exception) {
                errorArrayList.add(ErrorMessage("Ошибка работы с локальной базой данных", "Ошибка чтения данных",e))
                null
            }
        }else {
            null
        }

    }

    suspend fun uploadTrackListToServerAsync(): Boolean {
        errorArrayList.clear()
        val uploadResult = GlobalScope.async {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                uploadTrackListToServer()
            } else {
                TODO("VERSION.SDK_INT < O")
            }
        }
        return uploadResult.await().success
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun uploadTrackListToServer(): UploadResult {
        val trackList = loadTrackListFromRoom(true)
        return if (trackList != null) {
            val result = serverConnector.uploadTrackList(trackList as ArrayList<Point>)
            if (result.success) {
                val data = mRoutesDao!!.getRoutePointFiles()
                serverConnector.uploadFiles(data)
            }else{
                result
            }

        }else {
            val errorArray = ArrayList<ErrorMessage>()
            UploadResult(false,ArrayList<ErrorMessage>())
        }

    }

    // Сохранение файлов в локальную базу данных. Асинхронный вызов
    suspend fun saveFileIntoDBAsync(pointFile: PointFile): Boolean = withContext(Dispatchers.IO){
        val result =  GlobalScope.async { saveFileIntoDB(pointFile) }
        result.await()
        //return result.await()
    }

    private fun saveFileIntoDB(pointFile: PointFile): Boolean {
        return try {
            mRoutesDao!!.insertPointFile(pointFile)
            true
        } catch (e: java.lang.Exception) {
            false
        }
    }

    //Получение файлов из локальной базы данных
    suspend fun getFilesFromDBAsync(point: Point, photoOrder: PhotoOrder? = null): MutableList<PointFile>? {
        val result = GlobalScope.async { getFilesFromDB(point, photoOrder) }
        return result.await()
    }

    private fun getFilesFromDB(point: Point, photoOrder: PhotoOrder?): MutableList<PointFile>? {
        return try {
            val data = mRoutesDao!!.getPointFiles(point.getLineUID(),photoOrder)
            data
        } catch (e: java.lang.Exception) {
            null
        }
    }


    // Обновление выполнения / данных по точке
    fun updatePointAsync(point: Point) {
        GlobalScope.launch {
            mRoutesDao!!.updatePointWithRoute(point)
        }
    }

    fun setVehicle (vehicle: Vehicle?){
        this.vehicle = vehicle
    }



}