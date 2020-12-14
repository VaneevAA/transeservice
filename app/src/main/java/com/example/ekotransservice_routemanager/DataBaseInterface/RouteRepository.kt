package com.example.ekotransservice_routemanager.DataBaseInterface

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.preference.PreferenceManager
import com.example.ekotransservice_routemanager.DataClasses.*
import com.example.ekotransservice_routemanager.ErrorMessage
import com.example.ekotransservice_routemanager.ErrorTypes
import com.example.ekotransservice_routemanager.R
import com.example.ekotransservice_routemanager.UploadResult
import kotlinx.coroutines.*
import org.json.JSONObject
import java.io.BufferedInputStream
import java.io.InputStream
import java.security.KeyStore
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.text.SimpleDateFormat
import java.util.*
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.TrustManagerFactory
import kotlin.collections.ArrayList

class RouteRepository constructor(val context: Context){

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

    private val STATUS_LOAD_IN_DEVICE: Int = 1
    private val STATUS_UPLOAD_TO_SERVER: Int = 2

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

    fun getErrors() : java.util.ArrayList<ErrorMessage>{
        return errorArrayList
    }

    fun getErrorsCount() : Int{
        return errorArrayList.size
    }

    init {
        // инициальзация базы данных Room
        db = RouteRoomDatabase.getDatabase(context)
        mRoutesDao = db!!.routesDao()

        /*// Получение текущих настроек
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context.applicationContext)
        val urlName = sharedPreferences.getString("URL_NAME","") as String
        val urlPort = sharedPreferences.getString("URL_PORT","80") as String
        val urlPass = sharedPreferences.getString("URL_AUTHPASS","") as String
        val vehicleString = sharedPreferences.getString("VEHICLE", "") as String
        vehicle = Vehicle(vehicleString)

        // установка параметров подключения
        serverConnector.setConnectionParams(urlName,urlPort.toInt())
        serverConnector.setAuthPass(urlPass)*/
        setPrefernces()
    }


    fun setPrefernces() {
        // Получение текущих настроек
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this.context.applicationContext)
        val urlName = sharedPreferences.getString("URL_NAME","") as String
        val urlPort = sharedPreferences.getString("URL_PORT","443") as String
        val urlPass = sharedPreferences.getString("URL_AUTHPASS","") as String
        val vehicleString = sharedPreferences.getString("VEHICLE", "") as String
        vehicle = Vehicle(vehicleString)

        // установка параметров подключения
        serverConnector.setConnectionParams(urlName,urlPort.toInt())
        serverConnector.setAuthPass(urlPass)
        serverConnector.setSSl(getSSLSocketFactory())
    }

    // region LoadData

    // Загрузка списка точек
    // reload - требуется загрузка с  Postgres
    // Main fun
    suspend fun getPointList(reload: Boolean, doneOnly: Boolean = false): MutableList<Point>? {

        return try {
            if (reload) {
                val currentRoute = GlobalScope.async { getCurrentRoute() }
                val dataLoaded = GlobalScope.async { loadTaskFromServer(currentRoute.await()) }
                val tracklist = GlobalScope.async { loadTrackListFromRoom(dataLoaded.await(),doneOnly) }
                tracklist.await()
            } else {
                val tracklist = GlobalScope.async { loadTrackListFromRoom(true,doneOnly) }
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
        addErrors(downloadResult.log)
        return downloadResult.data as ArrayList<Region>
    }

    suspend fun getVehiclesList(region: Region): ArrayList<Vehicle> {
        val serverData = GlobalScope.async { serverConnector.getVehicles(region.getUid()) }
        val downloadResult = serverData.await()
        addErrors(downloadResult.log)
        return downloadResult.data as ArrayList<Vehicle>
    }

    suspend fun getCurrentRoute(): Route?{
        val resultList = GlobalScope.async {  mRoutesDao!!.getCurrentRoute()}
        val currentRoute = resultList.await()
        return if (currentRoute.size==0) {
            null
        }else{
            currentRoute[0]
        }

    }

    // Загрузка данных путевого листа с сервера и сохранение их в локальную базу Room
    private fun loadTaskFromServer(currentRoute: Route?): Boolean {
        errorArrayList.clear()
        val vehicleNumber: String = currentRoute?.getVehicleNumber() ?: this.vehicle!!.getName()
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        val datePrefValue = sharedPreferences.getString("DATE","")
        val dateTask: Date = currentRoute?.getRouteDate() ?: SimpleDateFormat("yyyy.MM.dd HH:mm:ss",
            Locale.getDefault()).parse("$datePrefValue 00:00:00")
        return run {
            val postParam = JSONObject()
            postParam.put("dateTask",  SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(dateTask)) //"2020-09-03 00:00:00")//dateTask.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
            postParam.put("vehicle", vehicleNumber)
            val serverData = serverConnector.getTrackList(postParam)
            addErrors(serverData.log)
            val result = saveTrackListIntoRoom(serverData.data as ArrayList<Point>?)
            if (result && serverData.data.size!=0 ) {
                if (currentRoute == null) {
                    saveRouteIntoRoom(serverData.data, vehicle!!, dateTask)
                } else {
                    currentRoute.setCountPoint(serverData.data.size)
                    mRoutesDao!!.insertRouteWithReplace(currentRoute) // Если обновляем текущий маршрут
                }
            }

            result
        }
    }

    // Cохранение массива точек в локальную базу
    private fun saveTrackListIntoRoom(trackList: ArrayList<Point>?):Boolean {
        if (trackList != null) {
            return try {
                mRoutesDao!!.insertPointListOnlyNew(trackList)
                true
            } catch (e: java.lang.Exception){
                errorArrayList.add(ErrorMessage(ErrorTypes.ROOM_ERROR, "Ошибка записи данных",e))
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
            serverConnector.setStatus(route.getDocUid(),STATUS_LOAD_IN_DEVICE)
            true
        } catch (e: java.lang.Exception){
            errorArrayList.add(ErrorMessage(ErrorTypes.ROOM_ERROR, "Ошибка записи данных",e))
            false
        }
    }

    // Получение списка точек из локальной базы
    private fun loadTrackListFromRoom(dataLoaded: Boolean, doneOnly: Boolean): MutableList<Point>? {
        return if (dataLoaded) {
            try {
                val data = mRoutesDao!!.getCurrentList(doneOnly)
                data
            } catch (e: Exception) {
                errorArrayList.add(ErrorMessage(ErrorTypes.ROOM_ERROR, "Ошибка чтения данных",e))
                null
            }
        }else {
            null
        }

    }

    //endregion LoadData

    // region UploadData

    // Async fun for final upload of the task and files. Call it from View
    // using coroutines
    suspend fun uploadTrackListToServerAsync(): Boolean {
        errorArrayList.clear()
        return try {
            val uploadResult = GlobalScope.async {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    uploadTrackListToServer()
                } else {
                    TODO("VERSION.SDK_INT < O")
                }
            }
            val dataResult = uploadResult.await()
            addErrors(dataResult.log)
            dataResult.success
        } catch(e: java.lang.Exception) {
            errorArrayList.add(ErrorMessage(ErrorTypes.OTHER,"Ошибка при выгрузке данных. Попробуйте снова",e))
            false
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun uploadTrackListToServer(): UploadResult {
        val trackList = loadTrackListFromRoom(dataLoaded = true, doneOnly = false)
        return if (trackList != null) {
            val data = mRoutesDao!!.getRoutePointFiles()
            //val resultFiles = serverConnector.uploadFiles(data)
            val resultFiles = uploadFiles(data)
            if (resultFiles.success) {
                val result = serverConnector.uploadTrackList(trackList as ArrayList<Point>)
                if (result.success) {
                    serverConnector.setStatus(trackList[0].getDocUID(),STATUS_UPLOAD_TO_SERVER)
                    mRoutesDao!!.deletePointList()
                    mRoutesDao!!.deleteCurrentRoute()
                }
                result
            } else {
                resultFiles
            }

        }else {
            val errorArray = ArrayList<ErrorMessage>()
            errorArray.add(ErrorMessage(ErrorTypes.ROOM_ERROR,"Отсутствуют данные для выгрузки",null))
            UploadResult(false,errorArray)
        }

    }

    // Async fun for uploading files to server
    // Check upload poinFile status and upload it to server if file was not uploaded before
    // Call if you need upload only files during the day
    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun uploadFilesAsync(): Boolean {
        errorArrayList.clear()
        return try {
            val uploadResult = GlobalScope.async {
                val data = mRoutesDao!!.getRouteNotUploadedPointFiles()
                if (data.isEmpty()){
                    return@async true
                }
                val resultFiles = uploadFiles(data,false)
                resultFiles.success
            }
            uploadResult.await()
        } catch(e: java.lang.Exception) {
            errorArrayList.add(ErrorMessage(ErrorTypes.OTHER,"Ошибка при выгрузке данных. Попробуйте снова",e))
            false
        }
    }

    // Upload files to server
    // Changes pointFile upload status in Room in case of the during the day upload
    // Delete uploaded files from Room in case of the Final upload
    // Doing it in portion, param: portionSize
    // Return UploadResult
    @RequiresApi(Build.VERSION_CODES.O)
    private fun uploadFiles(data: List<PointFile>, deleteUploaded: Boolean = true):UploadResult{

        val portionSize = 30
        val iterationCount = (data.size.toFloat()/portionSize)
        var startPos = 0
        var endPos =  if (portionSize - 1 > (data.size - 1)) {
            data.size - 1
        } else {portionSize-1}
        val errorArrayList: ArrayList<ErrorMessage> = ArrayList()
        var i = 0
        var result: Boolean
        do {
            i++
            val uploadedFiles = ArrayList<Long>()
            val resultPortion = serverConnector.uploadFilesPortion(data,startPos,endPos,uploadedFiles)
            addErrors(resultPortion.log)
            if (resultPortion.success) {
                if (deleteUploaded) {
                    mRoutesDao!!.deleteFiles(uploadedFiles)
                }else {
                    mRoutesDao!!.updatePointFileUploadStatus(uploadedFiles,true)
                }
            }
            result = resultPortion.success
            startPos = endPos + 1
            endPos += portionSize
            if (endPos > (data.size - 1)) {
                endPos = data.size - 1
            }
        } while (i<iterationCount && result)

        return UploadResult(result, errorArrayList)
    }

    //endregion

    // region WorkWithFiles
    // Сохранение файлов в локальную базу данных. Асинхронный вызов
    suspend fun saveFileIntoDBAsync(pointFile: PointFile): Boolean = withContext(Dispatchers.IO){
        val result =  GlobalScope.async { saveFileIntoDB(pointFile) }
        result.await()
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
    suspend fun getFilesFromDBAsync(point: Point, photoOrder: PhotoOrder? = null, withoutGeotag: Boolean = false): MutableList<PointFile>? {
        val result = GlobalScope.async { getFilesFromDB(point, photoOrder,withoutGeotag) }
        return result.await()
    }

    private fun getFilesFromDB(point: Point, photoOrder: PhotoOrder?,withoutGeotag: Boolean = false): MutableList<PointFile>? {
        return try {
            val data = if (withoutGeotag) {
                mRoutesDao!!.getGeolessPointFiles(point.getLineUID())
            }else{
                mRoutesDao!!.getPointFiles(point.getLineUID(),photoOrder)
            }
            data
        } catch (e: java.lang.Exception) {
            null
        }
    }

    fun updatePointFileLocationAsync(pointFile: PointFile,lat: Double, lon: Double ) {
        GlobalScope.launch {
            mRoutesDao!!.updatePointFileLocation(lat, lon, pointFile.id)
        }
    }

    //endregion

    // Обновление выполнения / данных по точке
    fun updatePointAsync(point: Point) {
        GlobalScope.launch {
            mRoutesDao!!.updatePointWithRoute(point)
        }
    }


    fun setVehicle (vehicle: Vehicle?){
        this.vehicle = vehicle
    }

    private fun getSSLSocketFactory(): SSLSocketFactory {
        //val keyStoreType = KeyStore.getDefaultType()
        val cert = context.resources.openRawResource(R.raw.apache_selfsigned)
        val caInput: InputStream = BufferedInputStream(cert)
        val cf: CertificateFactory = CertificateFactory.getInstance("X.509")
        val ca: X509Certificate = caInput.use {
            cf.generateCertificate(it) as X509Certificate
        }
        val keyStore = KeyStore.getInstance(KeyStore.getDefaultType()).apply {
            load(null, null)
            setCertificateEntry("ca", ca)
        }
        // Create a TrustManager that trusts the CAs inputStream our KeyStore
        val tmfAlgorithm: String = TrustManagerFactory.getDefaultAlgorithm()
        val tmf: TrustManagerFactory = TrustManagerFactory.getInstance(tmfAlgorithm).apply {
            init(keyStore)
        }

        // Create an SSLContext that uses our TrustManager
        val sslContext: SSLContext = SSLContext.getInstance("TLSv1.2").apply {
            init(null, tmf.trustManagers, null)
        }

        return sslContext.socketFactory
    }

    private fun addErrors(errors : ArrayList<ErrorMessage>){
        for(error in errors) {
            errorArrayList.add(error)
        }
    }
}