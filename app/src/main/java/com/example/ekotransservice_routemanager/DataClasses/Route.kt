package com.example.ekotransservice_routemanager.DataClasses

import androidx.annotation.NonNull
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity(tableName = "currentRoute_table")
class Route {

    @PrimaryKey
    @NonNull
    private var vehicleNumber: String = ""
    private var vehicleUID: String = ""
    private var regionName: String = ""
    private var routeDate: Date = Date()

    private var countPoint: Int = 0
    private var countPointDone: Int = 0

    //region Получение свойств класса - обязательно для классов Room Entity, переименовывать нельзя, критично для Room

    fun getVehicleNumber(): String { return this.vehicleNumber}
    fun getVehicleUID(): String { return this.vehicleUID}
    fun getRegionName(): String { return this.regionName}
    fun getRouteDate(): Date { return this.routeDate}
    fun getCountPoint(): Int { return this.countPoint}
    fun getCountPointDone(): Int { return this.countPointDone}

    //endregion

    //region Установка свойств класса - обязательно для классов Room Entity, переименовывать нельзя, критично для Room

    fun setVehicleNumber(vehicleNumber: String) {this.vehicleNumber = vehicleNumber }
    fun setVehicleUID(vehicleUID: String) { this.vehicleUID = vehicleUID}
    fun setRegionName(regionName: String) { this.regionName = regionName}
    fun setRouteDate(routeDate: Date) { this.routeDate = routeDate}
    fun setCountPoint(countPoint: Int) { this.countPoint = countPoint}
    fun setCountPointDone(countPointDone: Int) { this.countPointDone = countPointDone}

    //endregion

    override fun toString(): String {
        return this.regionName
    }
}