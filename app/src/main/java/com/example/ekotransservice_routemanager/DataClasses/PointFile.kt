package com.example.ekotransservice_routemanager.DataClasses

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.BitmapShader
import android.os.Build
import android.util.Base64.encodeToString
import androidx.annotation.RequiresApi
import androidx.room.*
import androidx.room.ForeignKey.CASCADE
import java.io.*
import java.math.BigInteger
import java.util.*
import kotlin.experimental.and


@Entity(
    tableName = "pointFiles_table",
    foreignKeys = [ForeignKey(
        entity = Point::class,
        parentColumns = ["docUID", "lineUID"],
        childColumns = ["docUID", "lineUID"],
        onDelete = CASCADE
    )],
    indices = arrayOf(Index("docUID", "lineUID", "docUID", "lineUID"))
)
class PointFile(
    val docUID: String,
    val lineUID: String,
    val timeDate: Date,
    var photoOrder: PhotoOrder,
    val lat: Double,
    val lon: Double,
    val filePath: String,
    val fileName: String,
    val fileExtension: String
) : Serializable {
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0L

    private fun getByteArray(filePath: String) : ByteArray{
        val file = File(filePath)
        val bytes = ByteArray(file.length().toInt())
        try {
            val buf = BufferedInputStream(FileInputStream(file))
            buf.read(bytes, 0, bytes.size)
            buf.close()
        } catch (e: FileNotFoundException) {
            // TODO Auto-generated catch block
            e.printStackTrace()
        } catch (e: IOException) {
            // TODO Auto-generated catch block
            e.printStackTrace()
        }
        return bytes
    }


    fun getHexString():String{
        val imageBytes = getByteArray(this.filePath)
        val bigInteger = BigInteger(1, imageBytes)
        return java.lang.String.format("%0" + (imageBytes.size shl 1).toString() + "x", bigInteger)
        //return bytesToHex(imageBytes)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getCompresedBase64():String {
        val mBitmap = BitmapFactory.decodeFile(this.filePath)
        val stream = ByteArrayOutputStream()
        mBitmap.compress(Bitmap.CompressFormat.JPEG,50,stream)
        val imageBytes = stream.toByteArray()
        return Base64.getEncoder().encodeToString(imageBytes)
    }

}