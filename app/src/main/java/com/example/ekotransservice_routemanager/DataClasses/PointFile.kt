package com.example.ekotransservice_routemanager.DataClasses

import androidx.room.*
import androidx.room.ForeignKey.CASCADE
import java.util.*

@Entity(tableName = "pointFiles_table",
        foreignKeys = [ForeignKey(entity = Point::class,
            parentColumns = ["lineUID"],
            childColumns = ["lineUID"],
            onDelete = CASCADE)],
    indices = arrayOf(Index("lineUID","lineUID")))
class PointFile(val lineUID: String, val timeDate: Date, var photoOrder: PhotoOrder, val lat: Double, val lon: Double, val filePath: String)  {
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0L

}