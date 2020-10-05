package com.example.ekotransservice_routemanager.DataBaseInterface

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.ekotransservice_routemanager.DataClasses.Point
import com.example.ekotransservice_routemanager.DataClasses.PointFile
import com.example.ekotransservice_routemanager.DataClasses.Route

@Dao
interface RouteDaoInterface {

    @Query("SELECT * from pointList_table ") //ORDER BY addressName ASC")
    fun getCurrentList(): MutableList<Point>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertPoint(point: Point)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertPointListWithReplace(pointList: ArrayList<Point>)

    @Query("SELECT * from currentRoute_table ") //ORDER BY addressName ASC")
    fun getCurrentRoute(): MutableList<Route>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertRouteWithReplace(route: Route)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertPointFile(pointFile: PointFile)
}
