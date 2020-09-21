package com.example.ekotransservice_routemanager.DataClasses

import org.json.JSONException
import org.json.JSONObject

class Vehicle {

    private var number: String = ""
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
            // TODO Обработка исключение конструктор
        }
    }

    private fun fillFromJSONObject(properties: JSONObject) {

        try {
            this.number = properties.getString("number").trim { it <= ' ' }
            this.uid = properties.getString("uid").trim { it <= ' ' }

        } catch (e: Exception) {
            //TODO error parsing JSON
        }
    }

    override fun toString(): String {
        return number
    }

    fun toJSONString():String? {
        val jsonObject = JSONObject()
        return try {
            jsonObject.put("number", number)
            jsonObject.put("uid", uid)
            jsonObject.toString()
        } catch (e: JSONException) {
            // TODO Auto-generated catch block
            e.printStackTrace()
            null
        }
    }

    fun getName(): String {return this.number}

}