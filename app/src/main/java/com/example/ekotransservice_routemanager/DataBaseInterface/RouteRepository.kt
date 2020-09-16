package com.example.ekotransservice_routemanager.DataBaseInterface

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.ekotransservice_routemanager.DataClasses.Point
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import java.net.HttpURLConnection
import java.net.URL
import java.util.*
import java.util.concurrent.Executors
import javax.net.ssl.HttpsURLConnection
import kotlin.collections.ArrayList

class RouteRepository constructor(application: Application) {

    private val serverConnector: RouteServerConnection = RouteServerConnection(
        "192.168.106.248",
        "3000",
        "z5FYg733jGUwjmabuGdmZvfAkDHnh2Wj"
    )
    private var db: RouteRoomDatabase? = null
    private var mRoutesDao: RouteDaoInterface? = null

   init {
       // инициальзация базы данных Room
        db = RouteRoomDatabase.getDatabase(application.applicationContext)
        mRoutesDao = db!!.routesDao()
    }

    suspend fun getPointList(reload: Boolean): MutableList<Point>? {
        val dataLoaded = GlobalScope.async { loadTaskFromServer() }
        val tracklist = GlobalScope.async { loadTrackListFromRoom(dataLoaded.await()) }
        return tracklist.await()
    }

    // Загрузка данных путевого листа с сервера и сохранение их в локальную базу Room
    private fun loadTaskFromServer(): Boolean {
        val trackList = serverConnector.getTrackList()
        val result = saveTrackListIntoRoom(trackList)
        return result
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