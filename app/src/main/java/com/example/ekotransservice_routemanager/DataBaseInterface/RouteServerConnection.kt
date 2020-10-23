package com.example.ekotransservice_routemanager.DataBaseInterface

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.ekotransservice_routemanager.DataClasses.*
import com.example.ekotransservice_routemanager.DownloadResult
import com.example.ekotransservice_routemanager.ErrorMessage
import com.example.ekotransservice_routemanager.ErrorTypes
import com.example.ekotransservice_routemanager.UploadResult
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL
import java.security.Key
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class RouteServerConnection {
    private var urlName:String = ""
    private var urlPort:Int =80
    private var authPass:String = ""

    fun setAuthPass(authPass: String) {
        val token = encodeToken(authPass)
        if (token!=null) {
            this.authPass = token
        }
    }

    private fun encodeToken(authPass: String): String? {
        var token:String? = null
        if (authPass.toByteArray().size < 32) return token
        try {
            val key: Key = Keys.hmacShaKeyFor(authPass.toByteArray(charset("UTF-8")))
            token = Jwts.builder()
                .claim("role", "api_user")
                .signWith(key)
                .compact()
        } catch (e: Exception) {
            // TODO обработать исключение некорректный формат пароля для токена
        }
        return token
    }

    fun setConnectionParams(urlName: String, urlPort: Int) {
        this.urlName = urlName
        this.urlPort = urlPort
    }

    private fun getData(
        methodName: String,
        requestMethod: String,
        postParam: JSONObject?,
        errorArrayList: ArrayList<ErrorMessage>
    ): JSONArray? {
        //val url = URL("http://$urlName:$urlPort/$methodName")
        //val url = URL("http",urlName, urlPort,"mobileapp/$methodName")
        val url = URL("http", urlName, urlPort, methodName)
        var connector: HttpURLConnection? = null
        try {
            connector = url.openConnection() as HttpURLConnection
        }catch (e: Exception){
            errorArrayList.add(
                ErrorMessage(
                    ErrorTypes.DOWNLOAD_ERROR,
                    "Ошибка соединения с сервером",
                    e
                )
            )
        }
        if (connector==null) { return null}

        connector.setRequestProperty("Content-Type", "application/json")
        connector.setRequestProperty("Authorization", "Bearer $authPass")
        connector.requestMethod = requestMethod
        connector.connectTimeout = 30000
        if (requestMethod == "POST" && postParam!=null ){
            val wr = OutputStreamWriter(connector.outputStream)
            wr.write(postParam.toString())
            wr.flush()
        }

        return try {
            val code = connector.responseCode
            if (code == 200) {
                try {
                    val outputString: String = connector.inputStream.bufferedReader().readText()
                    JSONArray(outputString)
                } catch (e: java.lang.Exception) {
                    errorArrayList.add(
                        ErrorMessage(
                            ErrorTypes.DOWNLOAD_ERROR,
                            "Ошибка чтения потока/формата данных",
                            e
                        )
                    )
                    null
                    // TODO обработка исключений
                } finally {
                    connector.disconnect()
                }
            } else {
                val outputString: String = connector.errorStream.bufferedReader().readText()
                errorArrayList.add(
                    ErrorMessage(
                        ErrorTypes.DOWNLOAD_ERROR,
                        outputString,
                        null
                    )
                )
                // TODO Требуется обработка Код возврата
                //val msg = connector.responseMessage
                null
            }
        } catch (e: MalformedURLException) {
            //TODO Требуется обработка Плохой URL
            errorArrayList.add(ErrorMessage(ErrorTypes.DOWNLOAD_ERROR, "Плохой URL", e))
            connector.disconnect()
            null
        } catch (e: IOException) {
            errorArrayList.add(ErrorMessage(ErrorTypes.DOWNLOAD_ERROR, "Проблемы с сетью", e))
            connector.disconnect()
            null
        } catch (e: java.lang.Exception) {
            errorArrayList.add(ErrorMessage(ErrorTypes.DOWNLOAD_ERROR, "Ошибка обработки кода", e))
           null
        }
        finally {
            connector.disconnect()
        }
    }

    fun getTrackList(postParam: JSONObject?): DownloadResult {
        val methodName = "rpc/getTaskList"
        val errorArrayList: ArrayList<ErrorMessage> = ArrayList()
        val data = getData(methodName, "POST", postParam, errorArrayList)
        val pointArrayList: ArrayList<Point> = ArrayList()
        if (data!=null) {
            for (i in 0 until data.length()) {
                try {
                    pointArrayList.add(Point(data.getJSONObject(i)))
                } catch (e: java.lang.Exception) {
                    errorArrayList.add(
                        ErrorMessage(
                            ErrorTypes.DOWNLOAD_ERROR,
                            "Ошибка создания элемента класса",
                            e
                        )
                    )
                }
            }
        }
        return DownloadResult(pointArrayList as ArrayList<Any>, errorArrayList)
        //TODO Unchecked cast
    }

    fun getRegions() : DownloadResult {
        val errorArrayList: ArrayList<ErrorMessage> = ArrayList()
        val methodName = "regions"
        val regionArrayList: ArrayList<Region> = ArrayList()
        var data: JSONArray? =null
        try {
            data = getData(methodName, "GET", null, errorArrayList)
        }catch (e: java.lang.Exception){
            errorArrayList.add(
                ErrorMessage(
                    ErrorTypes.DOWNLOAD_ERROR,
                    "Ошибка получения данных с сервера",
                    e
                )
            )
        }
        if (data!=null) {
            for (i in 0 until data.length()) {
                try {
                    regionArrayList.add(Region(data.getJSONObject(i)))
                } catch (e: java.lang.Exception) {
                    errorArrayList.add(
                        ErrorMessage(
                            ErrorTypes.DOWNLOAD_ERROR,
                            "Ошибка создания элемента класса",
                            e
                        )
                    )
                }
            }
        }
        return DownloadResult(regionArrayList as ArrayList<Any>, errorArrayList)
    }

    fun getVehicles(regionUID: String) : DownloadResult {
        val errorArrayList: ArrayList<ErrorMessage> = ArrayList()
        val methodName = "vehicle?regionUID=eq.$regionUID"
        val data = getData(methodName, "GET", null, errorArrayList)
        val dataArrayList: ArrayList<Vehicle> = ArrayList()
        if (data!=null) {
            for (i in 0 until data.length()) {
                try {
                    dataArrayList.add(Vehicle(data.getJSONObject(i)))
                } catch (e: java.lang.Exception) {
                    errorArrayList.add(
                        ErrorMessage(
                            ErrorTypes.DOWNLOAD_ERROR,
                            "Ошибка создания элемента класса",
                            e
                        )
                    )
                }
            }
        }
        return DownloadResult(dataArrayList as ArrayList<Any>, errorArrayList)
    }

    fun uploadTrackList(trackList: ArrayList<Point>): UploadResult {
        val jsonArray = JSONArray()
        trackList.forEach(){
            val jo = JSONObject()
            jo.put("docUID", it.getDocUID())
            jo.put("lineUID", it.getLineUID())
            jo.put("countFact", it.getCountFact())
            jo.put("countOver", it.getCountOver())
            jo.put("done", it.getDone())
            jsonArray.put(jo)
        }

        val postParam = JSONObject()
        postParam.put("trackList", jsonArray)
        val methodName = "rpc/loadTaskResult"
        val errorArrayList: ArrayList<ErrorMessage> = ArrayList()
        val data = getData(methodName, "POST", postParam, errorArrayList)
        var result = false
        if ( data != null && data.length() != 0 && data.getJSONObject(0).has("result")) {
            result = true
        }
        return UploadResult(result, errorArrayList)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun uploadFiles(data: List<PointFile>):UploadResult{
        val jsonArray = JSONArray()
        data.forEach{
            val jo = JSONObject()
            jo.put("docUID", it.docUID)
            jo.put("lineUID", it.lineUID)
            jo.put("lat", it.lat)
            jo.put("lon", it.lon)
            jo.put("fileName",it.fileName)
            jo.put("fileExtension",it.fileExtension)
            jo.put("timestamp", SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(it.timeDate))
            if (it.photoOrder == PhotoOrder.PHOTO_BEFORE) {
                jo.put("photoOrder", 0)
            }else{
                jo.put("photoOrder", 1)
            }
            jo.put("fileBase64",it.getCompresedBase64())
            jsonArray.put(jo)
        }
        val postParam = JSONObject()
        postParam.put("files", jsonArray)
        val methodName = "rpc/loadFiles"
        val errorArrayList: ArrayList<ErrorMessage> = ArrayList()
        val uploadResult = getData(methodName, "POST", postParam, errorArrayList)
        var result = false
        if ( uploadResult != null && uploadResult.length() != 0 && uploadResult.getJSONObject(0).has("result")) {
            result = true
        }
        return UploadResult(result, errorArrayList)
    }

    fun setStatus(docUID: String, status: Int): UploadResult {
        val jsonArray = JSONArray()
        val jo = JSONObject()
        jo.put("docUID", docUID)
        jo.put("docStatus", status)
        jsonArray.put(jo)
        val postParam = JSONObject()
        postParam.put("dList", jsonArray)
        val methodName = "rpc/setDocStatus"
        val errorArrayList: ArrayList<ErrorMessage> = ArrayList()
        val uploadResult = getData(methodName, "POST", postParam, errorArrayList)
        var result = false
        if ( uploadResult != null && uploadResult.length() != 0 && uploadResult.getJSONObject(0).has("result")) {
            result = true
        }
        return UploadResult(result, errorArrayList)
    }

}