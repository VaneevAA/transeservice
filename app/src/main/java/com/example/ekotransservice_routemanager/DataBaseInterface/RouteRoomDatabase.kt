package com.example.ekotransservice_routemanager.DataBaseInterface

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteException
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
    version = 3,
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

        val MIGRATION_2_3: Migration = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {

                database.execSQL("""CREATE TABLE IF NOT EXISTS 'currentRoute_table_temp' (
                                'docUid' TEXT NOT NULL,
                                'regionName' TEXT,
                                'routeName' TEXT,
                                'routeDate' INTEGER,
                                'vehicle_uid' TEXT,
                                'vehicle_number' TEXT,
                                'route_uid' TEXT,
                                'route_name' TEXT,
                                'countPoint' INTEGER NOT NULL,
                                'countPointDone' INTEGER NOT NULL,
                                PRIMARY KEY('docUid'))""".trimIndent())
                database.execSQL("""INSERT INTO 'currentRoute_table_temp' (
                                'docUid','regionName','routeName', 'routeDate',
                                'vehicle_uid','vehicle_number',
                                'route_uid','route_name',
                                'countPoint','countPointDone')
                                SELECT docUID, regionName, routeName, routeDate, uid, number,'','', countPoint, countPointDone
                                FROM currentRoute_table
                                """.trimIndent())

                database.execSQL("DROP TABLE currentRoute_table")
                database.execSQL("ALTER TABLE currentRoute_table_temp RENAME TO currentRoute_table")

                /*try {
                    val c = database.query("SELECT * FROM currentRoute_table")
                    c.use {
                        if (c.moveToFirst()) {
                            val cv = ContentValues()
                            cv.put("docUid", c.getLong(c.getColumnIndex("docUid")))
                            cv.put("regionName", c.getString(c.getColumnIndex("regionName")))
                            cv.put("routeName", c.getString(c.getColumnIndex("routeName")))
                            cv.put("vehicle_uid", c.getString(c.getColumnIndex("uid")))
                            cv.put("vehicle_number", c.getString(c.getColumnIndex("number")))
                            cv.put("countPoint", c.getInt(c.getColumnIndex("countPoint")))
                            cv.put("countPointDone", c.getInt(c.getColumnIndex("countPointDone")))
                            database.execSQL("DROP TABLE IF EXISTS 'currentRoute_table'")
                            createCurrentRouteTable(database)
                            database.insert("customer", 0, cv)
                        } else {
                            database.execSQL("DROP TABLE IF EXISTS 'currentRoute_table'")
                            createCurrentRouteTable(database)
                        }
                    }
                } catch (e: SQLiteException) {
                    Timber.e(e, "SQLiteException in migrate from database version 1 to version 2")
                } catch (e: Exception) {
                    Timber.e(e, "Failed to migrate database version 1 to version 2")
                }
                database.execSQL("ALTER TABLE currentRoute_table ADD COLUMN route_uid STRING DEFAULT '00000000-0000-0000-0000-000000000000' NOT NULL")
                database.execSQL("ALTER TABLE currentRoute_table ADD COLUMN route_name STRING DEFAULT '' NOT NULL")
                database.execSQL("ALTER TABLE currentRoute_table RENAME COLUMN number TO vehicle_number")
                database.execSQL("ALTER TABLE currentRoute_table RENAME COLUMN uid TO vehicle_uid")*/
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
                        MIGRATION_1_2, MIGRATION_2_3)
                    .build()
                INSTANCE = instance
                return instance
            }
        }

        fun createCurrentRouteTable(database: SupportSQLiteDatabase){
            database.execSQL("""CREATE TABLE IF NOT EXISTS 'currentRoute_table' (
                                'docUid' STRING NOT NULL,
                                'regionName' STRING NOT NULL,
                                'routeName' STRING NOT NULL,
                                'vehicle_uid' STRING NOT NULL,
                                'vehicle_number' STRING NOT NULL,
                                'route_uid' STRING NOT NULL,
                                'route_name' STRING NOT NULL,
                                'countPoint' INTEGER NOT NULL,
                                'countPointDone' INTEGER NOT NULL,
                                PRIMARY KEY('docUid'))""".trimIndent())
        }

    }


}