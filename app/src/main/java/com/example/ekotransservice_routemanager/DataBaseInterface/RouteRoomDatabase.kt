package com.example.ekotransservice_routemanager.DataBaseInterface

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.ekotransservice_routemanager.DataClasses.Point
import com.example.ekotransservice_routemanager.DataClasses.PointFile
import com.example.ekotransservice_routemanager.DataClasses.Route


@Database(
    entities = [Point::class, Route::class, PointFile::class],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class RouteRoomDatabase : RoomDatabase() {

    abstract fun routesDao(): RouteDaoInterface

    companion object {
        // Singleton prevents multiple instances of database opening at the
        // same time.
        @Volatile
        private var INSTANCE: RouteRoomDatabase? = null

        val MIGRATION_1_2: Migration = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE pointFiles_table ADD COLUMN uploaded INTEGER DEFAULT 0 NOT NULL")
            }
        }

        fun getDatabase(context: Context): RouteRoomDatabase {
            val tempInstance = INSTANCE
            if (tempInstance != null) {
                return tempInstance
            }
            synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    RouteRoomDatabase::class.java,
                    "trackList_database.db"
                )
                    .addMigrations(
                        MIGRATION_1_2)
                    .build()
                INSTANCE = instance
                return instance
            }
        }

    }


}