package com.example.ekotransservice_routemanager.DataClasses

import android.os.Build
import androidx.annotation.NonNull
import androidx.annotation.RequiresApi
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

@Entity(tableName = "currentRoute_table")
class Route {

    @PrimaryKey
    @NonNull
    private var docUid: String = ""
    //private var vehicleNumber: String = ""
   //private var vehicleUID: String = ""
    private var regionName: String = ""
    private var routeDate: Date = Date()
    private var routeName: String = ""

    @Embedded(prefix = "vehicle_")
    private var vehicle:Vehicle? = null
    @Embedded(prefix = "route_")
    private var routeRef:RouteRef? = null

    private var countPoint: Int = 0
    private var countPointDone: Int = 0


    //region Получение свойств класса - обязательно для классов Room Entity, переименовывать нельзя, критично для Room

    fun getVehicleNumber(): String { return this.vehicle!!.getName()}
    fun getDocUid(): String { return this.docUid }
    fun getVehicle(): Vehicle? {return this.vehicle}
    fun getRouteRef(): RouteRef? {return this.routeRef}
    fun getRegionName(): String { return this.regionName}
    fun getRouteDate(): Date { return this.routeDate}
    fun getCountPoint(): Int { return this.countPoint}
    fun getCountPointDone(): Int { return this.countPointDone}
    fun getRouteName(): String {return this.routeName}

    //endregion

    //region Установка свойств класса - обязательно для классов Room Entity, переименовывать нельзя, критично для Room

    //fun setVehicleNumber(vehicleNumber: String) {this.vehicleNumber = vehicleNumber }
    //fun setVehicleUID(vehicleUID: String) { this.vehicleUID = vehicleUID}
    fun setVehicle(vehicle: Vehicle) {this.vehicle=vehicle}
    fun setRouteRef(routeRef: RouteRef) {this.routeRef=routeRef}
    fun setRegionName(regionName: String) { this.regionName = regionName}
    fun setRouteDate(routeDate: Date) { this.routeDate = routeDate}
    fun setCountPoint(countPoint: Int) { this.countPoint = countPoint}
    fun setCountPointDone(countPointDone: Int) { this.countPointDone = countPointDone}
    fun setDocUid(docUid: String) { this.docUid = docUid}
    fun setRouteName(routeName: String) {this.routeName = routeName}

    //endregion

    override fun toString(): String {
        return this.regionName
    }

}