package com.example.ekotransservice_routemanager.DataBaseInterface

import android.app.Application
import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.ekotransservice_routemanager.DataClasses.Point
import com.example.ekotransservice_routemanager.DataClasses.Route

@Database(entities = [Point::class,Route::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class RouteRoomDatabase : RoomDatabase() {

    abstract fun routesDao(): RouteDaoInterface

    companion object {
        // Singleton prevents multiple instances of database opening at the
        // same time.
        @Volatile
        private var INSTANCE: RouteRoomDatabase? = null
        private val LOCK = Any()

        fun getDatabase(context: Context): RouteRoomDatabase {
            val tempInstance = INSTANCE
            if (tempInstance != null) {
                return tempInstance
            }
            synchronized(LOCK) {
                INSTANCE = Room.databaseBuilder(
                    context,
                    RouteRoomDatabase::class.java,
                    "trackList_database.db"
                ).build()
                return INSTANCE as RouteRoomDatabase
            }
        }
    }
}