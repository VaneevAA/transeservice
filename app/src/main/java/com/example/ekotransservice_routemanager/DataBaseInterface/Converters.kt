package com.example.ekotransservice_routemanager.DataBaseInterface

import androidx.room.TypeConverter
import com.example.ekotransservice_routemanager.DataClasses.PointActoins
import com.example.ekotransservice_routemanager.DataClasses.PointStatuses
import java.time.Instant
import java.util.*
import kotlin.collections.ArrayList

class Converters {
    companion object {
        @TypeConverter
        @JvmStatic
        fun fromDate(value: Long?): Date? {
            return value?.let { Date(it) }
        }

        @TypeConverter
        @JvmStatic
        fun dateToTimestamp(date: Date?): Long? {
            return date?.time?.toLong()
        }

        @TypeConverter
        @JvmStatic
        fun arrayListToString(arrayList: ArrayList<PointActoins>): String {
            return arrayList.toString()
        }

        @TypeConverter
        @JvmStatic
        fun stringToArrayList(value: String): ArrayList<PointActoins> {
            return ArrayList()
        }

        @TypeConverter
        @JvmStatic
        fun pointStatusToString(pointStatuses: PointStatuses): String {
            return pointStatuses.toString()
        }

        @TypeConverter
        @JvmStatic
        fun stringToPointStatus(value: String): PointStatuses {
            return PointStatuses.NOT_VISITED
        }
    }
}