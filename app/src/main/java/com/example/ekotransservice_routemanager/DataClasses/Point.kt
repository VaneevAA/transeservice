package com.example.ekotransservice_routemanager.DataClasses


import androidx.annotation.NonNull
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.Index
import androidx.room.PrimaryKey
import org.json.JSONException
import org.json.JSONObject
import java.io.Serializable
import java.util.*
import kotlin.collections.ArrayList

@Entity(tableName = "pointList_table", indices = arrayOf(Index("lineUID","lineUID")))
class Point : Serializable{
    @PrimaryKey
    @NonNull
    private var lineUID: String         = ""

    private var docUID: String          = ""
    private var dateStart: Date?        = null
    private var dateEnd: Date?          = null
    private var driverName:String       = ""

    private var addressUID : String     = ""
    private var addressName: String     = ""
    private var addressLon: Double      = 0.0
    private var addressLat: Double      = 0.0

    private var containerUID : String   = ""
    private var containerName: String   = ""
    private var containerSize: Double   = 0.0

    private var agentUID : String   = ""
    private var agentName: String   = ""

    private var countPlan: Int      = 0
    private var countFact: Int      = 0



    /* private val name : String = name;
    private val lon : Double = lon;
    private val lan : Double = lan;*/
    private var done : Boolean = false
  /*  private val contCount : Int = contCount;
    private val contType : String = contType; //TODO: поменять на какой-то ограниченный тип*/

    @Ignore
    private var pointActionsArray: ArrayList<PointActoins> = ArrayList()
    @Ignore
    private var pointActionsCancelArray : ArrayList<PointActoins> = ArrayList()
    @Ignore
    private var status : PointStatuses = PointStatuses.NOT_VISITED


    init {
        pointActionsArray.add(PointActoins.TAKE_PHOTO_AFTER)
        pointActionsArray.add(PointActoins.TAKE_PHOTO_BEFORE)
        pointActionsArray.add(PointActoins.SET_VOLUME)

        pointActionsCancelArray.add(PointActoins.TAKE_PHOTO_BEFORE)
    }

    constructor(){

    }
    // Конструктор на основании объекта JSON
    constructor(properties: JSONObject) {
        fillFromJSONObject(properties)
    }

    // Конструктор на основании строки JSON
    @Throws(JSONException::class)
    constructor(jsonString: String) {
        val jsonObject = JSONObject(jsonString)
        fillFromJSONObject(jsonObject)
    }

    constructor(
        name: String, lon: Double,
        lat: Double, done: Boolean,
        contCount: Int, contType: String
    ){
        this.addressName = name
        this.containerName = contType
        this.addressLat = lat
        this.addressLon = lon
        this.done = done
        this.countPlan = contCount
    }

    // Заполняет объект данными переданными JSON
    private fun fillFromJSONObject(properties: JSONObject) {
       /*val fields = Point::class.memberProperties
        for ( f in fields){
            f.name
        }*/
        try {
            this.addressName = properties.getString("addressName").trim { it <= ' ' }
            this.addressUID = properties.getString("addressUID").trim { it <= ' ' }
            this.docUID = properties.getString("docUID").trim { it <= ' ' }
            this.lineUID = properties.getString("lineUID").trim { it <= ' ' }
            this.containerName = properties.getString("containerName").trim { it <= ' ' }
            this.containerUID = properties.getString("containerUID").trim { it <= ' ' }
            this.containerSize = properties.getDouble("containerSize")
            this.countPlan = properties.getInt("countPlan")

        } catch (e: Exception) {
            //TODO error parsing JSON
        }
    }

    override fun toString(): String {
        return "$addressName, $countPlan контейнеров типа $containerName"
    }

    //region Получение свойств класса - обязательно для классов Room Entity, переименовывать нельзя, критично для Room

    fun getAddressName() : String { return this.addressName }
    fun getAddressLon() : Double { return this.addressLon }
    fun getAddressLat() : Double { return this.addressLat; }
    fun getAddressUID() : String { return this.addressUID }
    fun getLineUID() : String { return this.lineUID}
    fun getDocUID() : String { return this.docUID}
    fun getDateStart() : Date? { return this.dateStart}
    fun getDateEnd() : Date? { return this.dateEnd}
    fun getDriverName() : String { return this.driverName}
    fun getContainerUID() : String { return this.containerUID}
    fun getContainerName() : String { return this.containerName}
    fun getContainerSize() :Double { return this.containerSize}
    fun getAgentUID() : String { return this.agentUID}
    fun getAgentName() : String { return this.agentName}
    fun getDone (): Boolean { return this.done }
    fun getCountPlan (): Int { return this.countPlan }
    fun getCountFact (): Int { return this.countFact }
    fun getStatus():PointStatuses{ return this.status }
    fun getPointActionsArray(): ArrayList<PointActoins>{ return this.pointActionsArray }
    fun getPointActionsCancelArray() : ArrayList<PointActoins>{ return this.pointActionsCancelArray }

    //endregion

    //region Установка свойств класса - обязательно для классов Room Entity, переименовывать нельзя, критично для Room
    fun setAddressName(addressName: String) { this.addressName = addressName}
    fun setAddressLon(addressLon: Double) { this.addressLon = addressLon}
    fun setAddressLat(addressLat: Double) { this.addressLat = addressLat}
    fun setAddressUID(addressUID: String) { this.addressUID = addressUID}
    fun setLineUID(lineUID: String) { this.lineUID = lineUID}
    fun setDocUID(docUID: String) { this.docUID = docUID}
    fun setDateStart(dateStart: Date?) { this.dateStart = dateStart}
    fun setDateEnd(dateEnd: Date?) { this.dateEnd = dateEnd}
    fun setDriverName(driverName: String) { this.driverName = driverName}
    fun setContainerUID(containerUID: String) { this.containerUID = containerUID}
    fun setContainerName(containerName: String) { this.containerName = containerName}
    fun setContainerSize(containerSize: Double) { this.containerSize = containerSize}
    fun setAgentUID(agentUID: String) { this.agentUID = agentUID}
    fun setAgentName(agentName: String) { this.agentName = agentName}

    fun setCountPlan(countPlan: Int) { this.countPlan = countPlan}
    fun setCountFact(countFact: Int) { this.countFact = countFact}

    fun setPointActionsArray(pointActionsArray: ArrayList<PointActoins>) { this.pointActionsArray = pointActionsArray}
    fun setPointActionsCancelArray(pointActionsCancelArray: ArrayList<PointActoins>) { this.pointActionsCancelArray = pointActionsCancelArray}
    fun setStatus(status: PointStatuses) { this.status = status}
    fun setDone(done: Boolean){ this.done = done }
    //endregion

    fun getContCount (): Int{
        return this.countPlan
    }

    fun getContType (): String{
        return this.containerName
    }

}


