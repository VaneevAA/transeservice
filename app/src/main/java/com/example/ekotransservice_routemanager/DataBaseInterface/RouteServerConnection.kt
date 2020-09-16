package com.example.ekotransservice_routemanager.DataBaseInterface

import com.example.ekotransservice_routemanager.DataClasses.Point
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.json.JSONArray
import org.json.JSONObject
import java.io.*
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL
import java.security.Key
import java.util.*

class RouteServerConnection constructor(urlName: String, urlPort: String, authPass: String){
    private var urlName:String = urlName
    private var urlPort:String = urlPort
    private var authPass:String = encodeToken(authPass)!!

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

    private fun getData(methodName: String): JSONArray? {

        val url = URL("http://$urlName:$urlPort/$methodName")
        val connector: HttpURLConnection = url.openConnection() as HttpURLConnection
        connector.setRequestProperty("Content-Type", "application/json")
        connector.setRequestProperty("Authorization", "Bearer $authPass")
        connector.requestMethod = "POST"
        connector.connectTimeout = 30000
        // TODO Передача параметров
        var postParam: JSONObject = JSONObject()
        postParam.put("dateTask", "2020-09-03 00:10:10")
        postParam.put("vehicle", "Р204ЕЕ72")
        val wr = OutputStreamWriter(connector.getOutputStream())
        wr.write(postParam.toString())
        wr.flush()

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
        } finally {
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

    fun getTrackList(): ArrayList<Point>? {
        val methodName = "rpc/getTaskList"
        val data = getData(methodName)
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

}