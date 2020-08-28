package com.example.ekotransservice_routemanager.DataClasses


import java.util.*
import kotlin.collections.ArrayList

public class Point constructor(name : String,lon : Double,
                               lan : Double, done : Boolean,
                               contCount : Int, contType : String){
    private val name : String = name;
    private val lon : Double = lon;
    private val lan : Double = lan;
    private var done : Boolean = false;
    private val contCount : Int = contCount;
    private val contType : String = contType; //TODO: поменять на какой-то ограниченный тип
    private val pointActionsArray: ArrayList<PointActoins> = ArrayList()
    private val pointActionsCancelArray : ArrayList<PointActoins> = ArrayList()
    private val status : PointStatuses = PointStatuses.NOT_VISITED
    init {
        pointActionsArray.add(PointActoins.TAKE_PHOTO_AFTER)
        pointActionsArray.add(PointActoins.TAKE_PHOTO_BEFORE)
        pointActionsArray.add(PointActoins.SET_VOLUME)

        pointActionsCancelArray.add(PointActoins.TAKE_PHOTO_BEFORE)
    }

    override fun toString(): String {
        return "$name, $contCount контейнеров типа $contType"
    }

    public fun getName () : String{
        return this.name
    }

    public fun getLon () : Double{
        return this.lon
    }

    public fun getLan () : Double{
        return this.lan;
    }

    public fun getDone (): Boolean{
        return this.done
    }

    public fun setDone (done : Boolean){
        this.done = done
    }

    public fun getContCount (): Int{
        return this.contCount
    }

    public fun getContType (): String{
        return this.contType
    }

    fun getStatus():PointStatuses{
        return this.status
    }

    fun getActions():ArrayList<PointActoins>{
        return this.pointActionsArray
    }

    fun getCancelActions() : ArrayList<PointActoins>{
        return this.pointActionsCancelArray
    }
}


