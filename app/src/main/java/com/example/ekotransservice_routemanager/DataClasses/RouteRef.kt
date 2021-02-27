package com.example.ekotransservice_routemanager.DataClasses

import org.json.JSONException
import org.json.JSONObject

data class RouteRef(
    val uid: String,
    val name: String) {

    fun toJSONString(): String? {
        val jsonObject = JSONObject()
        return try {
            jsonObject.put("name", name)
            jsonObject.put("uid", uid)
            jsonObject.toString()
        } catch (e: JSONException) {
            // TODO Auto-generated catch block
            e.printStackTrace()
            null
        }
    }

    override fun toString(): String {
        return name
    }
    companion object Factory {
        fun makeRouteFromJSONString(jsonString: String): RouteRef?{
            return if (jsonString.isEmpty()) null else {
                try {
                    val jsonObject = JSONObject(jsonString)
                    RouteRef(jsonObject.getString("uid"), jsonObject.getString("name").trim())
                }catch (e: JSONException){
                    null
                }
            }
        }

        fun makeRouteFromJSONObject(jsonObject: JSONObject): RouteRef?{
            return RouteRef(jsonObject.getString("uid"), jsonObject.getString("name").trim())

        }
    }
}
