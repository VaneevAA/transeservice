package com.example.ekotransservice_routemanager.DataBaseInterface

import androidx.room.*
import com.example.ekotransservice_routemanager.DataClasses.PhotoOrder
import com.example.ekotransservice_routemanager.DataClasses.Point
import com.example.ekotransservice_routemanager.DataClasses.PointFile
import com.example.ekotransservice_routemanager.DataClasses.Route

@Dao
interface RouteDaoInterface {

    @Query("SELECT * from pointList_table ORDER BY rowNumber")
    fun getCurrentList(): MutableList<Point>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertPoint(point: Point)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertPointListWithReplace(pointList: ArrayList<Point>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertPointListOnlyNew(pointList: ArrayList<Point>)

    @Query("SELECT * from currentRoute_table ") //ORDER BY addressName ASC")
    fun getCurrentRoute(): MutableList<Route>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertRouteWithReplace(route: Route)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertPointFile(pointFile: PointFile)

    @Query("SELECT * from pointFiles_table where lineUID = :lineUID AND photoOrder =:photoOrder") //ORDER BY addressName ASC")
    fun getPointFilesByOrder(lineUID: String, photoOrder: PhotoOrder): MutableList<PointFile>

    @Query("SELECT * from pointFiles_table where lineUID = :lineUID") //ORDER BY addressName ASC")
    fun getAllPointFiles(lineUID: String): MutableList<PointFile>

    @Query("SELECT * from pointFiles_table") //ORDER BY addressName ASC")
    fun getRoutePointFiles(): List<PointFile>

    @Transaction
    fun getPointFiles(lineUID: String, photoOrder: PhotoOrder?): MutableList<PointFile>{
        return if (photoOrder == null) {
            getAllPointFiles(lineUID)
        } else {
            getPointFilesByOrder(lineUID,photoOrder)
        }
    }

    @Query("UPDATE currentRoute_table SET countPointDone = :countPointDone")
    fun updateCountPointDone(countPointDone: Int)

    @Query("SELECT COUNT(1) as countDone from pointList_table where done Group By docUID")
    fun countPointDone(): Int

    @Update
    fun updatePoint(point: Point)

    @Transaction
    fun updatePointWithRoute(point: Point) {
        updatePoint(point)
        val countDone = countPointDone()
        updateCountPointDone(countDone)
    }

    @Query("SELECT DISTINCT pointList_table.* from pointList_table INNER JOIN pointFiles_table on pointList_table.docUID = pointFiles_table.docUID AND pointList_table.lineUID = pointFiles_table.lineUID ORDER BY pointList_table.rowNumber")
    fun getPointsWithFiles(): MutableList<Point>

}
