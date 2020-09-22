package com.example.ekotransservice_routemanager.DataBaseInterface

import android.app.Application
import android.app.Notification
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.preference.PreferenceManager
import com.example.ekotransservice_routemanager.DataClasses.Point
import com.example.ekotransservice_routemanager.DataClasses.Region
import com.example.ekotransservice_routemanager.DataClasses.Route
import com.example.ekotransservice_routemanager.DataClasses.Vehicle
import com.example.ekotransservice_routemanager.ErrorMessage
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import org.json.JSONObject
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

class RouteRepository constructor(application: Application) {

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

   init {
       // инициальзация базы данных Room
       db = RouteRoomDatabase.getDatabase(application.applicationContext)
       mRoutesDao = db!!.routesDao()

       // Получение текущих настроек
       val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(application.applicationContext)
       val urlName = sharedPreferences.getString("URL_NAME","") as String
       val urlPort = sharedPreferences.getString("URL_PORT","") as String
       val urlPass = sharedPreferences.getString("URL_AUTHPASS","") as String
       val vehicleString = sharedPreferences.getString("VEHICLE", "") as String
       vehicle = Vehicle(vehicleString)

       // установка параметров подключения
       serverConnector.setConnectionParams(urlName,urlPort)
       serverConnector.setAuthPass(urlPass)
    }

    // Загрузка списка точек
    // reload - требуется загрузка с  Postgres
    suspend fun getPointList(reload: Boolean): MutableList<Point>? {
        if (reload) {
            val currentRoute = GlobalScope.async { getCurrentRoute() }
            val dataLoaded = GlobalScope.async { loadTaskFromServer(currentRoute.await()) }
            val tracklist = GlobalScope.async { loadTrackListFromRoom(dataLoaded.await()) }
            return tracklist.await()
        } else {
            val tracklist = GlobalScope.async { loadTrackListFromRoom(true) }
            return tracklist.await()
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
        val resultList = mRoutesDao!!.getCurrentRoute()
        if (resultList.size==0) {
            return null
        }else{
            return resultList[0]
        }
    }

    // Загрузка данных путевого листа с сервера и сохранение их в локальную базу Room
    private fun loadTaskFromServer(currentRoute: Route?): Boolean {
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
            try {
                mRoutesDao!!.insertPointListWithReplace(trackList)
                return true
            } catch (e: java.lang.Exception){
                errorArrayList.add(ErrorMessage("Ошибка работы с локальной базой данных", "Ошибка записи данных",e))
                return false
            }
        }
        return false
    }

    private fun saveRouteIntoRoom(data: ArrayList<Point>, vehicle: Vehicle, dateTask: Date ):Boolean {
            try {
                val route = Route()
                route.setCountPoint(data.size)
                route.setVehicle(vehicle)
                route.setRouteDate(dateTask)
                route.setDocUid(data[0].getDocUID())
                mRoutesDao!!.insertRouteWithReplace(route)
                return true
            } catch (e: java.lang.Exception){
                errorArrayList.add(ErrorMessage("Ошибка работы с локальной базой данных", "Ошибка записи данных",e))
                return false
            }
        return false
    }


    // Получение списка точек из локальной базы
    private fun loadTrackListFromRoom(dataLoaded: Boolean): MutableList<Point>? {
        if (dataLoaded) {
            try {
                val data = mRoutesDao!!.getCurrentList()
                return data
            } catch (e: Exception) {
                errorArrayList.add(ErrorMessage("Ошибка работы с локальной базой данных", "Ошибка чтения данных",e))
                return null
            }
        }else {
            return null
        }

    }

}