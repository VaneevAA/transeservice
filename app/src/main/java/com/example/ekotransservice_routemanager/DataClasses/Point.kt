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
import kotlin.math.max

@Entity(tableName = "pointList_table",primaryKeys = ["docUID","lineUID"], indices =arrayOf(Index("docUID","lineUID","docUID","lineUID")))
class Point : Serializable{

    @NonNull
    private var lineUID: String         = ""
    @NonNull
    private var docUID: String          = ""

    private var rowNumber: Int          = 0
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

    private var countPlan: Double      = 0.0
    private var countFact: Double      = -1.0
    private var countOver: Double      = 0.0
    private var status : PointStatuses = PointStatuses.NOT_VISITED

    private var done : Boolean = false
    private var tripNumber: Int = 0
    private var polygon: Boolean = false
    private var timestamp: Date? = null
    private var routeName: String = ""
    private var reasonComment: String = ""


    @Ignore
    private var pointActionsArray: ArrayList<PointActoins> = ArrayList()
    @Ignore
    private var pointActionsCancelArray : ArrayList<PointActoins> = ArrayList()

    init {
        pointActionsArray.add(PointActoins.TAKE_PHOTO_AFTER)
        pointActionsArray.add(PointActoins.TAKE_PHOTO_BEFORE)
        pointActionsArray.add(PointActoins.SET_VOLUME)

        pointActionsCancelArray.add(PointActoins.TAKE_PHOTO_BEFORE)
        pointActionsCancelArray.add(PointActoins.SET_REASON)
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
        contCount: Double, contType: String
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
            this.countPlan = properties.getDouble("countPlan")
            this.rowNumber = properties.getInt("rowNumber")
            this.tripNumber = properties.getInt("tripNumber")
            this.polygon = properties.getBoolean("polygon")
            this.routeName = properties.getString("routeName").trim { it <= ' ' }

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
    fun getCountPlan (): Double { return this.countPlan }
    fun getCountFact (): Double { return this.countFact }
    fun getCountOver (): Double { return this.countOver }
    fun getStatus():PointStatuses{ return this.status }
    fun getRowNumber():Int{ return this.rowNumber }
    fun getTripNumber():Int{ return this.tripNumber }
    fun getPolygon():Boolean { return this.polygon }
    fun getPointActionsArray(): ArrayList<PointActoins>{ return this.pointActionsArray }
    fun getPointActionsCancelArray() : ArrayList<PointActoins>{ return this.pointActionsCancelArray }
    fun getTimestamp() : Date? { return this.timestamp}
    fun getRouteName() : String { return this.routeName}
    fun getReasonComment() : String { return this.reasonComment}

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

    fun setCountPlan(count: Double) { this.countPlan = count}
    fun setCountFact(count: Double) { this.countFact = count}
    fun setCountOver(count: Double) { this.countOver = count}

    fun setRowNumber(rowNumber: Int) { this.rowNumber = rowNumber}
    fun setTripNumber(tripNumber: Int) { this.tripNumber = tripNumber}

    fun setStatus(status: PointStatuses) { this.status = status}
    fun setDone(done: Boolean){ this.done = done }
    fun setPolygon(polygon: Boolean){ this.polygon = polygon }
    fun setTimestamp(timestamp: Date?) { this.timestamp = timestamp}
    fun setRouteName(name: String) { this.routeName = name}
    fun setReasonComment(name: String) { this.reasonComment = name}

    //endregion

    fun getContCount (): Double{
        return this.countPlan
    }

    fun getContType (): String{
        return this.containerName
    }

    fun setCountOverFromPlanAndFact(){
        this.countOver = max(0.0, this.countFact - this.countPlan)
    }



}


