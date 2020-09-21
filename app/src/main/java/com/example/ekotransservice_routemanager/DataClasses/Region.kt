package com.example.ekotransservice_routemanager.DataClasses

import org.json.JSONException
import org.json.JSONObject

class Region {
    private var name: String = ""
    private var uid: String = ""

    constructor(){

    }
    // Конструктор на основании объекта JSON
    constructor(properties: JSONObject) {
        fillFromJSONObject(properties)
    }
    // Конструктор на основании строки JSON
    @Throws(JSONException::class)
    constructor(jsonString: String) {
        try {
            val jsonObject = JSONObject(jsonString)
            fillFromJSONObject(jsonObject)
        } catch (e: JSONException){

        }

    }

    constructor(name: String, uid: String){
        this.name = name
        this.uid = uid
    }
    private fun fillFromJSONObject(properties: JSONObject) {

        try {
            this.name = properties.getString("name").trim { it <= ' ' }
            this.uid = properties.getString("uid").trim { it <= ' ' }

        } catch (e: Exception) {
            //TODO error parsing JSON
        }
    }

    override fun toString(): String {
        return name
    }

    fun toJSONString():String? {
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

    fun getName(): String {return this.name}
    fun getUid(): String {return this.uid}
}