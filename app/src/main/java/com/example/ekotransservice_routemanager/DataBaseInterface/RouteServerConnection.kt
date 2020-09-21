package com.example.ekotransservice_routemanager.DataBaseInterface

import android.util.Log
import com.example.ekotransservice_routemanager.DataClasses.Point
import com.example.ekotransservice_routemanager.DataClasses.Region
import com.example.ekotransservice_routemanager.DataClasses.Vehicle
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.json.JSONArray
import org.json.JSONObject
import java.io.*
import java.lang.invoke.MethodType
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL
import java.security.Key
import java.util.*
import kotlin.collections.ArrayList

class RouteServerConnection {
    private var urlName:String = ""
    private var urlPort:String = ""
    private var authPass:String = ""

    fun setAuthPass(authPass: String) {
        this.authPass = encodeToken(authPass)!!
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
            // TODO обработать исключение
        }
        return token
    }

    fun setConnectionParams(urlName: String, urlPort: String) {
        this.urlName = urlName
        this.urlPort = urlPort
    }

    private fun getData(methodName: String, requestMethod:String,  postParam: JSONObject?): JSONArray? {

        val url = URL("http://$urlName:$urlPort/$methodName")
        val connector: HttpURLConnection = url.openConnection() as HttpURLConnection
        connector.setRequestProperty("Content-Type", "application/json")
        connector.setRequestProperty("Authorization", "Bearer $authPass")
        connector.requestMethod = requestMethod
        connector.connectTimeout = 30000
        if (requestMethod == "POST" && postParam!=null ){
            val wr = OutputStreamWriter(connector.getOutputStream())
            wr.write(postParam.toString())
            wr.flush()
        }

        return try {
            val code = connector.responseCode
            if (code == 200) {
                try {
                    JSONArray(connector.inputStream.bufferedReader().readText())
                } catch (e: java.lang.Exception) {
                    null
                    // TODO обработка исключений
                } finally {
                    connector.disconnect()
                }
            } else {
                // TODO Требуется обработка Код возврата
                val msg = connector.responseMessage
                null
            }
        } catch (e: MalformedURLException) {
            //TODO Требуется обработка Плохой URL
            connector.disconnect()
            null
        } catch (e: IOException) {
            //TODO Требуется обработка Проблемя с сетью
            connector.disconnect()
            null
        } catch (e: java.lang.Exception) {
            Log.d("connection error", "error: $e")
           null
        }
        finally {
            connector.disconnect()
        }
    }

    @Throws(IOException::class)
    private fun readStream(`is`: InputStream): String? {
        val `in` = BufferedReader(InputStreamReader(`is`))
        var inputLine: String?
        val response = StringBuilder()
        while (`in`.readLine().also { inputLine = it } != null) {
            response.append(inputLine)
        }
        `in`.close()
        return response.toString()
    }

    fun getTrackList(postParam: JSONObject?): ArrayList<Point>? {
        val methodName = "rpc/getTaskList"
        /*var postParam: JSONObject = JSONObject()
        postParam.put("dateTask", "2020-09-03 00:10:10")
        postParam.put("vehicle", "Р204ЕЕ72")*/
        val data = getData(methodName,"POST",postParam)
        val pointArrayList: ArrayList<Point> = ArrayList<Point>()
        if (data!=null) {
            for (i in 0 until data.length()) {
                try {
                    pointArrayList.add(Point(data.getJSONObject(i)))
                } catch (e: java.lang.Exception) {
                    return null
                }
            }
        }
        return pointArrayList
    }

    fun getRegions() : ArrayList<Region>? {
        val methodName = "regions"
        val data = getData(methodName,"GET", null)
        val regionArrayList: ArrayList<Region> = ArrayList<Region>()
        if (data!=null) {
            for (i in 0 until data.length()) {
                try {
                    regionArrayList.add(Region(data.getJSONObject(i)))
                } catch (e: java.lang.Exception) {
                    return null
                }
            }
        }
        return regionArrayList
    }

    fun getVehicles(regionUID:String) : ArrayList<Vehicle>? {
        val methodName = "vehicle?regionUID=eq.$regionUID"
        val data = getData(methodName,"GET", null)
        val dataArrayList: ArrayList<Vehicle> = ArrayList<Vehicle>()
        if (data!=null) {
            for (i in 0 until data.length()) {
                try {
                    dataArrayList.add(Vehicle(data.getJSONObject(i)))
                } catch (e: java.lang.Exception) {
                    return null
                }
            }
        }
        return dataArrayList
    }

}