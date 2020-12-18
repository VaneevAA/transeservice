package com.example.ekotransservice_routemanager.DataBaseInterface

import android.net.Uri
import android.os.Build
import android.util.JsonWriter
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.ekotransservice_routemanager.*
import com.example.ekotransservice_routemanager.DataClasses.*
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.json.JSONArray
import org.json.JSONObject
import java.io.*
import java.net.MalformedURLException
import java.net.URI
import java.net.URL
import java.security.Key
import java.text.SimpleDateFormat
import java.util.*
import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.SSLSocketFactory
import kotlin.collections.ArrayList

class RouteServerConnection {
    private var urlName:String = ""
    private var urlPort:Int =80
    private var authPass:String = ""
    private var sslSocketFactory = SSLSocketFactory.getDefault()
    private final val TAG = MainActivity.TAG
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

    fun setSSl(sslSocketFactory: SSLSocketFactory){
        this.sslSocketFactory = sslSocketFactory
    }

    private fun writeJSONArrayToStream(jsonWriter: JsonWriter, jsonArray: JSONArray){
        jsonWriter.beginArray()
        for (i in 0 until jsonArray.length()) {
            val item = jsonArray.getJSONObject(i)
            writeJSONObjectToStream(jsonWriter, item)
        }
        jsonWriter.endArray()
    }

    private fun writeJSONObjectToStream(jsonWriter: JsonWriter, jsonObject: JSONObject) {
        val jsonKeys = jsonObject.keys()
        jsonWriter.beginObject()
        while (jsonKeys.hasNext()){
            val currentKey = jsonKeys.next()
            val currentValue = jsonObject.get(currentKey)
            jsonWriter.name(currentKey)
            if (!(currentValue is JSONArray || currentValue is JSONObject)){
                jsonWriter.value(currentValue.toString())
            }else {
                if (currentValue is JSONObject) {
                    writeJSONObjectToStream(jsonWriter, currentValue)
                } else{
                    writeJSONArrayToStream(jsonWriter, currentValue as JSONArray)
                }
            }
        }
        jsonWriter.endObject()
    }


    private fun getData(
        methodName: String,
        requestMethod: String,
        postParam: JSONObject?,
        errorArrayList: ArrayList<ErrorMessage>
    ): JSONArray? {
        //val url = URL("http://$urlName:$urlPort/$methodName")
        //val url = URL("http",urlName, urlPort,"mobileapp/$methodName")
        //log
        Log.i(
            TAG,
            "" + this::class.java + " getData method: " + methodName + " requestMethod " + requestMethod
        )
        val url = URL("https", urlName, urlPort, "mobileapp/$methodName")
        var connector: HttpsURLConnection? = null
        try {
            connector = url.openConnection() as HttpsURLConnection
        }catch (e: Exception){
            //log
            Log.e(TAG, "" + this::class.java + " getData connector url " + url, e)
            errorArrayList.add(
                ErrorMessage(
                    ErrorTypes.DOWNLOAD_ERROR,
                    "Ошибка соединения с сервером",
                    e
                )
            )
        }
        if (connector==null) { return null}
        connector.sslSocketFactory = sslSocketFactory as SSLSocketFactory?
        connector.setRequestProperty("Content-Type", "application/json")
        connector.setRequestProperty("Authorization", "Bearer $authPass")
        connector.requestMethod = requestMethod
        connector.connectTimeout = 20000
        return try {
            if (requestMethod == "POST" && postParam!=null ){
                val wr =BufferedWriter(OutputStreamWriter(connector.outputStream))
                val writer = JsonWriter(wr)
                writer.setIndent("  ");
                writeJSONObjectToStream(writer, postParam)
                writer.close()
                //wr.write(postParam.toString())
                wr.close()
            }
            val code = connector.responseCode
            if (code == 200) {
                try {
                    val outputString: String = connector.inputStream.bufferedReader().readText()
                    if (outputString == "null") {
                        errorArrayList.add(
                            ErrorMessage(
                                ErrorTypes.DOWNLOAD_ERROR,
                                "Отсутствуют данные для загрузки",
                                null
                            )
                        )
                        null
                    }else {
                        JSONArray(outputString)
                    }

                } catch (e: java.lang.Exception) {
                    //log
                    Log.e(TAG, "" + this::class.java + " getData JSON reader ", e)
                    errorArrayList.add(
                        ErrorMessage(
                            ErrorTypes.DOWNLOAD_ERROR,
                            "Ошибка чтения потока/формата данных",
                            e
                        )
                    )
                    null
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
                //log
                Log.w(TAG, "" + this::class.java + " getData connector code " + code.toString())
                null
            }
        } catch (e: MalformedURLException) {
            errorArrayList.add(ErrorMessage(ErrorTypes.DOWNLOAD_ERROR, "Плохой URL", e))
            //log
            Log.e(TAG, "" + this::class.java + " getData connector bad url " + url, e)
            connector.disconnect()
            null
        } catch (e: IOException) {
            errorArrayList.add(ErrorMessage(ErrorTypes.DOWNLOAD_ERROR, "Проблемы с сетью", e))
            //log
            Log.e(TAG, "" + this::class.java + " getData connector bad net ", e)
            connector.disconnect()
            null
        } catch (e: java.lang.Exception) {
            errorArrayList.add(ErrorMessage(ErrorTypes.DOWNLOAD_ERROR, "Ошибка обработки кода", e))
            //log
            Log.e(TAG, "" + this::class.java + " getData connector ", e)
           null
        }
        finally {
            connector.disconnect()
        }
    }

    fun getTrackList(postParam: JSONObject?): DownloadResult {
        //log
        Log.i(TAG,"" + this::class.java + " getTrackList start")
        val methodName = "rpc/getTaskList"
        val errorArrayList: ArrayList<ErrorMessage> = ArrayList()
        val data = getData(methodName, "POST", postParam, errorArrayList)
        val pointArrayList: ArrayList<Point> = ArrayList()
        if (data != null) {
            for (i in 0 until data.length()) {
                try {
                    pointArrayList.add(Point(data.getJSONObject(i)))
                } catch (e: java.lang.Exception) {
                    //log
                    Log.e(TAG,"" + this::class.java + " getTrackList point error ",e)
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
        //log
        Log.i(TAG,"" + this::class.java + " getTrackList end")
        return DownloadResult(pointArrayList as ArrayList<Any>, errorArrayList)
    }

    fun getRegions() : DownloadResult {
        //log
        Log.i(TAG,"" + this::class.java + " getRegions start")
        val errorArrayList: ArrayList<ErrorMessage> = ArrayList()
        val methodName = "regions"
        val regionArrayList: ArrayList<Region> = ArrayList()
        var data: JSONArray? = null
        try {
            data = getData(methodName, "GET", null, errorArrayList)
        }catch (e: java.lang.Exception){
            //log
            Log.e(TAG,"" + this::class.java + " getRegions getData error ",e)
            errorArrayList.add(
                ErrorMessage(
                    ErrorTypes.DOWNLOAD_ERROR,
                    "Ошибка получения данных с сервера",
                    e
                )
            )
        }
        if (data != null) {
            for (i in 0 until data.length()) {
                try {
                    regionArrayList.add(Region(data.getJSONObject(i)))
                } catch (e: java.lang.Exception) {
                    //log
                    Log.e(TAG,"" + this::class.java + " getRegions region create ",e)
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
        //log
        Log.i(TAG,"" + this::class.java + " getRegions end")
        return DownloadResult(regionArrayList as ArrayList<Any>, errorArrayList)
    }

    fun getVehicles(regionUID: String) : DownloadResult {
        //log
        Log.i(TAG,"" + this::class.java + " getVehicles start")
        val errorArrayList: ArrayList<ErrorMessage> = ArrayList()
        val methodName = "vehicle?regionUID=eq.$regionUID"
        val data = getData(methodName, "GET", null, errorArrayList)
        val dataArrayList: ArrayList<Vehicle> = ArrayList()
        if (data != null) {
            for (i in 0 until data.length()) {
                try {
                    dataArrayList.add(Vehicle(data.getJSONObject(i)))
                } catch (e: java.lang.Exception) {
                    //log
                    Log.e(TAG,"" + this::class.java + " getVehicles vehicle create ",e)
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
        //log
        Log.i(TAG,"" + this::class.java + " getVehicles end")
        return DownloadResult(dataArrayList as ArrayList<Any>, errorArrayList)
    }

    fun uploadTrackList(trackList: ArrayList<Point>): UploadResult {
        //log
        Log.i(TAG,"" + this::class.java + " uploadTrackList start")
        val jsonArray = JSONArray()
        trackList.forEach(){
            //log
            Log.i(TAG,"" + this::class.java + " uploadTrackList point: " + it.getAddressName())
            val jo = JSONObject()
            jo.put("docUID", it.getDocUID())
            jo.put("lineUID", it.getLineUID())
            jo.put("countFact", it.getCountFact())
            jo.put("countOver", it.getCountOver())
            jo.put("done", it.getDone())
            jo.put("reasonComment", it.getReasonComment())
            val timestamp = it.getTimestamp()
            if (timestamp != null) {
                jo.put(
                    "timestamp",
                    SimpleDateFormat(
                        "yyyy-MM-dd HH:mm:ss",
                        Locale("RU")
                    ).format(it.getTimestamp())
                )
            }else{
                jo.put("timestamp", "")
            }
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
        //log
        Log.i(TAG,"" + this::class.java + " uploadTrackList end")
        return UploadResult(result, errorArrayList)
    }


    @RequiresApi(Build.VERSION_CODES.O)

    fun uploadFilesPortion(data: List<PointFile>, startPos: Int, endPos: Int,deletedFiles: ArrayList<Long>): UploadResult {
        //log
        Log.i(TAG,"" + this::class.java + " uploadFilesPortion start")
        val jsonArray = JSONArray()
        for (j in startPos..endPos){


            val jo = JSONObject()
            val it = data[j]
            //log
            Log.i(TAG,"" + this::class.java + " uploadFilesPortion file: " + it.filePath)

            jo.put("docUID", it.docUID)
            jo.put("lineUID", it.lineUID)
            jo.put("lat", it.lat)
            jo.put("lon", it.lon)
            jo.put("fileName", it.fileName)
            jo.put("fileExtension", it.fileExtension)
            jo.put(
                "timestamp",
                SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale("RU")).format(it.timeDate)
            )
            if (it.photoOrder == PhotoOrder.PHOTO_BEFORE) {
                jo.put("photoOrder", 0)
            }else{
                jo.put("photoOrder", 1)
            }
            jo.put("fileBase64", it.getCompresedBase64())
            jsonArray.put(jo)
            deletedFiles.add(it.id)
        }
        val postParam = JSONObject()
        postParam.put("files", jsonArray)
        val methodName = "rpc/loadFiles"
        val errorArrayList: ArrayList<ErrorMessage> = ArrayList()
        val uploadResult = getData(methodName, "POST", postParam, errorArrayList)
        var result = false
        if ( uploadResult != null && uploadResult.length() != 0 && uploadResult.getJSONObject(0).has(
                "result"
            )) {
            result = true
        }
        //log
        Log.i(TAG,"" + this::class.java + " uploadFilesPortion end")
        return UploadResult(result, errorArrayList)
    }

    fun setStatus(docUID: String, status: Int): UploadResult {
        //log
        Log.i(TAG,"" + this::class.java + " setStatus start")
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
        if ( uploadResult != null && uploadResult.length() != 0 && uploadResult.getJSONObject(0).has(
                "result"
            )) {
            result = true
        }

        //log
        Log.i(TAG,"" + this::class.java + " setStatus end")
        return UploadResult(result, errorArrayList)
    }

    fun downloadApk(dir: File): File? {
        val errorArrayList: ArrayList<ErrorMessage> = ArrayList()
        val url = URL("https", urlName, urlPort, "/apk/app-release.apk")
        var connector: HttpsURLConnection? = null
        try {
            connector = url.openConnection() as HttpsURLConnection
        }catch (e: Exception){
            //log
            Log.e(TAG, "" + this::class.java + " getData connector url " + url, e)
            errorArrayList.add(
                ErrorMessage(
                    ErrorTypes.DOWNLOAD_ERROR,
                    "Ошибка соединения с сервером",
                    e
                )
            )
        }

        if (connector==null) {
            errorArrayList.add(
                ErrorMessage(
                    ErrorTypes.DOWNLOAD_ERROR,
                    "Невозможно установить подключение",
                    null
                )
            )
            return null
        }
        connector.sslSocketFactory = sslSocketFactory as SSLSocketFactory?
        connector.requestMethod = "GET"
        connector.connectTimeout = 20000
        return try {
            val code = connector.responseCode
            if (code == 200) {
                val file = File(dir,"apk_release.apk")
                /*if (file.exists()) {
                    val writer = FileOutputStream(file)
                    writer.write("".toByteArray())
                    writer.close()
                }*/
                val outputStream = FileOutputStream(file)
                //connector.inputStream.bufferedReader().copyTo(outputStream.writer(), DEFAULT_BUFFER_SIZE)
                outputStream.write(connector.inputStream.readBytes())
                outputStream.flush()
                file
            }else{
                    val outputString: String = connector.errorStream.bufferedReader().readText()
                    errorArrayList.add(
                        ErrorMessage(
                            ErrorTypes.DOWNLOAD_ERROR,
                            outputString,
                            null
                        )

                    )
                    //log
                    Log.w(TAG, "" + this::class.java + " getData connector code " + code.toString())
                    null
            }
        } catch (e: MalformedURLException) {
            errorArrayList.add(ErrorMessage(ErrorTypes.DOWNLOAD_ERROR, "Плохой URL", e))
            //log
            Log.e(TAG, "" + this::class.java + " getData connector bad url " + url, e)
            connector.disconnect()
            null
        } catch (e: IOException) {
            errorArrayList.add(ErrorMessage(ErrorTypes.DOWNLOAD_ERROR, "Проблемы с сетью", e))
            //log
            Log.e(TAG, "" + this::class.java + " getData connector bad net ", e)
            connector.disconnect()
           null
        } catch (e: java.lang.Exception) {
            errorArrayList.add(ErrorMessage(ErrorTypes.DOWNLOAD_ERROR, "Ошибка обработки кода", e))
            //log
            Log.e(TAG, "" + this::class.java + " getData connector ", e)
            null
        } finally {
            connector.disconnect()
        }
    }
}