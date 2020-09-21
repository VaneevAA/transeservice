package com.example.ekotransservice_routemanager.DataBaseInterface

import android.app.Application
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.preference.PreferenceManager
import com.example.ekotransservice_routemanager.DataClasses.Point
import com.example.ekotransservice_routemanager.DataClasses.Region
import com.example.ekotransservice_routemanager.DataClasses.Vehicle
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.util.*
import java.util.concurrent.Executors
import javax.net.ssl.HttpsURLConnection
import kotlin.collections.ArrayList

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

   init {
       // инициальзация базы данных Room
       db = RouteRoomDatabase.getDatabase(application.applicationContext)
       mRoutesDao = db!!.routesDao()
       val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(application.applicationContext)
       val urlName = sharedPreferences.getString("URL_NAME","") as String
       val urlPort = sharedPreferences.getString("URL_PORT","") as String
       val urlPass = sharedPreferences.getString("URL_AUTHPASS","") as String
       val vehicleString = sharedPreferences.getString("VEHICLE", "") as String
       vehicle = Vehicle(vehicleString)

       serverConnector.setConnectionParams(urlName,urlPort)
       serverConnector.setAuthPass(urlPass)
    }

    suspend fun getPointList(reload: Boolean): MutableList<Point>? {
        val dataLoaded = GlobalScope.async { loadTaskFromServer() }
        val tracklist = GlobalScope.async { loadTrackListFromRoom(dataLoaded.await()) }
        return tracklist.await()
    }

    suspend fun getRegionList(): ArrayList<Region>? {
        try {
            val serverData = GlobalScope.async { serverConnector.getRegions() }
            return serverData.await()
        } catch (e:Exception) {
            Log.d("ERROR","erorr $e")
            return null
        }

    }

    suspend fun getVehiclesList(region: Region): ArrayList<Vehicle>? {
        try {
            val serverData = GlobalScope.async { serverConnector.getVehicles(region.getUid()) }
            return serverData.await()
        } catch (e:Exception) {
            Log.d("ERROR","erorr $e")
            return null
        }

    }

    // Загрузка данных путевого листа с сервера и сохранение их в локальную базу Room
    private fun loadTaskFromServer(): Boolean {
        if (vehicle!=null) {
            var postParam: JSONObject = JSONObject()
            postParam.put("dateTask", "2020-09-03 00:10:10")
            postParam.put("vehicle", vehicle!!.getName())
            val trackList = serverConnector.getTrackList(postParam)
            val result = saveTrackListIntoRoom(trackList)
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
                return false
            }
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
                return null
            }
        }else {
            return null
        }

    }

}