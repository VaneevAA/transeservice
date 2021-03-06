package com.example.ekotransservice_routemanager.DataClasses

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Resources
import android.graphics.*
import android.location.Geocoder
import android.location.Location
import android.media.ExifInterface.TAG_APERTURE
import android.os.Build
import android.util.TypedValue
import android.view.View
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.exifinterface.media.ExifInterface
import androidx.recyclerview.widget.RecyclerView
import androidx.room.*
import androidx.room.ForeignKey.CASCADE
import com.example.ekotransservice_routemanager.R
import java.io.*
import java.math.BigInteger
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs
import kotlin.math.roundToInt


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
    val fileExtension: String,
    val uploaded: Boolean = false
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

        /*val mBitmap = BitmapFactory.decodeFile(this.filePath)
        val stream = ByteArrayOutputStream()
        mBitmap.compress(Bitmap.CompressFormat.JPEG, 50, stream)
        val imageBytes = stream.toByteArray()*/

        val f = File(this.filePath)
        return if (f.exists()){
            val imageBytes = ByteArray(f.length().toInt())
            val stream = FileInputStream(this.filePath)
            stream.read(imageBytes)
            stream.close()

            Base64.getEncoder().encodeToString(imageBytes)
        }else{
            ""
        }
    }

    fun exists(): Boolean{
        val f = File(this.filePath)
        return f.exists()
    }

    @SuppressLint("InflateParams")
    fun createResultImageFile(lat: Double, lon: Double, point: Point, context: Context) {

        // Основное изображение
        val currentFile = File(this.filePath)
        if (currentFile.absolutePath.isNullOrEmpty()){
            return // Проблема с обработкой файла, файл не найден
        }
        var originalBitmap: Bitmap = BitmapFactory.decodeFile(currentFile.absolutePath)
        val degree = getRotateDegreeFromExif(currentFile.absolutePath)
        //Если угол не нулевой, то сначала повернем картинку
        if (degree != 0) {
            //Получим ориентацию картинки и определим матрицу трансформаци
            val matrix = Matrix()
            matrix.postRotate(degree.toFloat())
            val rotatedBitmap = Bitmap.createBitmap(
                originalBitmap,
                0, 0,
                originalBitmap.width, originalBitmap.height,
                matrix, true
            )
            originalBitmap = rotatedBitmap
        }
        // Итоговая картинка (результат)
        val overlayBitmap =
            Bitmap.createBitmap(
                originalBitmap.width,
                originalBitmap.height,
                originalBitmap.config
            )
        /*
        // Изображение из макета photo_data
        val inflater = activity?.layoutInflater
        val photoDataView: View? = inflater?.inflate(R.layout.photo_data, null)
        photoDataView!!.findViewById<TextView>(R.id.addressTextView).text = getAddressNameFromLocation(
            location!!
        )
        photoDataView.findViewById<TextView>(R.id.latTextView).text =
            location!!.latitude.toString()
        photoDataView.findViewById<TextView>(R.id.lonTextView).text =
            location!!.longitude.toString()
        photoDataView.findViewById<TextView>(R.id.dateTextView).text = SimpleDateFormat(
            "yyyy-MM-dd (EEE) HH:mm:ss",
            Locale("ru")
        ).format(Date())

        val widthSpec =
            View.MeasureSpec.makeMeasureSpec(originalBitmap.width, View.MeasureSpec.AT_MOST)
        val heightSpec =
            View.MeasureSpec.makeMeasureSpec(
                originalBitmap.height / 4,
                View.MeasureSpec.AT_MOST
            )
        photoDataView.measure(widthSpec, heightSpec)
        photoDataView.layout(
            0,
            0,
            photoDataView.measuredWidth,
            photoDataView.measuredHeight
        )*/

        // Вывод в холст
        val canvas = Canvas(overlayBitmap)
        val paint = Paint()
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_OVER) // Text Overlapping Pattern
        canvas.drawBitmap(originalBitmap, 0F, 0F, paint)

        val rt = Rect(
            0,
            originalBitmap.height,
            originalBitmap.width,
            originalBitmap.height - originalBitmap.height / 4
        )
        paint.style = Paint.Style.FILL
        paint.color = ContextCompat.getColor(context, R.color.colorGrayBack)
        canvas.drawRect(rt, paint)

        paint.color = Color.WHITE
        paint.textSize = originalBitmap.height / 30F

        //Вывод адреса
        var addressText = ""
        var latText = ""
        var lonText = ""

        if (lat == 0.0 || lon == 0.0) {
            addressText = point.getAddressName()
        } else {
            addressText = getAddressNameFromLocation(lat, lon, context)
            if (addressText=="") {
                addressText = point.getAddressName()
            }

            latText = String.format("%.6f",lat) // 6 decimal digits
            lonText =  String.format("%.6f",lon)
        }

        val textBorderSpace = 10
        val textSpace = rt.width()/30

        printText(addressText, rt.width() - 2*textBorderSpace, textBorderSpace, rt.bottom + 20, paint, canvas)

        // Вывод координат и даты
        val textWidth = (rt.width()-textSpace*2 - textBorderSpace*2)/3
        val textLine = originalBitmap.height - abs(rt.height() / 2) +20
        //Широта
        printText(latText, textWidth, textBorderSpace, textLine, paint, canvas)

        //Долгота
        printText(lonText, textWidth, textBorderSpace + textSpace + textWidth, textLine, paint, canvas)

        //Дата время
        printText(
            SimpleDateFormat(
                "yyyy-MM-dd (EEE) HH:mm:ss",
                Locale("ru")
            ).format(timeDate), textWidth, textBorderSpace + 2*textSpace + 2*textWidth, textLine, paint, canvas
        )

        //Сохранение в файл
        val exifData = getExifData(currentFile)
        currentFile.delete()
        val out = FileOutputStream(currentFile)
        overlayBitmap.compress(Bitmap.CompressFormat.JPEG, 50, out)
        out.flush()
        out.close()
        setExifData(currentFile,exifData)
    }

    @Throws(IOException::class)
    private fun getExifData(currentFile: File): MutableMap<String,String> {
        val oldExif = ExifInterface(
            currentFile
        )

        val attributes = arrayOf(
            ExifInterface.TAG_APERTURE_VALUE,
            ExifInterface.TAG_DATETIME,
            ExifInterface.TAG_DATETIME_DIGITIZED,
            ExifInterface.TAG_EXPOSURE_TIME,
            ExifInterface.TAG_FLASH,
            ExifInterface.TAG_FOCAL_LENGTH,
            ExifInterface.TAG_GPS_ALTITUDE,
            ExifInterface.TAG_GPS_ALTITUDE_REF,
            ExifInterface.TAG_GPS_DATESTAMP,
            ExifInterface.TAG_GPS_LATITUDE,
            ExifInterface.TAG_GPS_LATITUDE_REF,
            ExifInterface.TAG_GPS_LONGITUDE,
            ExifInterface.TAG_GPS_LONGITUDE_REF,
            ExifInterface.TAG_GPS_PROCESSING_METHOD,
            ExifInterface.TAG_GPS_TIMESTAMP,
            ExifInterface.TAG_IMAGE_LENGTH,
            ExifInterface.TAG_IMAGE_WIDTH,
            ExifInterface.TAG_RW2_ISO,
            ExifInterface.TAG_MAKE,
            ExifInterface.TAG_MODEL,
            ExifInterface.TAG_ORIENTATION,
            ExifInterface.TAG_SUBSEC_TIME,
            ExifInterface.TAG_SUBSEC_TIME_DIGITIZED,
            ExifInterface.TAG_SUBSEC_TIME_ORIGINAL,
            ExifInterface.TAG_WHITE_BALANCE
        )

        val exifData: MutableMap<String,String> = mutableMapOf()
        for (i in attributes.indices) {
            val value = oldExif.getAttribute(attributes[i])
            if (value != null) exifData[attributes[i]] = value
        }
        return  exifData
    }

    private fun setExifData(currentFile: File,exifData: MutableMap<String,String>){
        val currentFileExif = ExifInterface(
            currentFile
        )
        exifData.forEach {
            currentFileExif.setAttribute(it.key,it.value)
        }
        currentFileExif.saveAttributes()
    }

    private fun getAddressNameFromLocation(lat: Double, lon: Double, context: Context): String {

        val geocoder = Geocoder(context, Locale("ru"))
        return try {
            val addresses = geocoder.getFromLocation(lat, lon, 1)
            addresses[0].getAddressLine(0) // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
        } catch (e: Exception) {
           ""
        }

        /*val city: String = addresses.get(0).getLocality()
        val state: String = addresses.get(0).getAdminArea()
        val country: String = addresses.get(0).getCountryName()
        val postalCode: String = addresses.get(0).getPostalCode()
        val knownName: String = addresses.get(0).getFeatureName()*/

    }
    private fun printText(
        currentText: String,
        textWidth: Int,
        startPointX: Int,
        startPointY: Int,
        paint: Paint,
        canvas: Canvas
    ) {
        val rectText = Rect()
        paint.getTextBounds(currentText, 0, currentText.length, rectText)
        val textHeight = rectText.height()

        val stringArray = stringArray(currentText, textWidth.toFloat(), paint)
        var stringLineY = (startPointY + textHeight).toFloat()

        stringArray.forEach {
            canvas.drawText(
                it, startPointX.toFloat(),
                stringLineY,
                paint
            )
            stringLineY += textHeight
        }
    }

    private fun stringArray(originalString: String, width: Float, paint: Paint): ArrayList<String>{
        var currentString = originalString
        val stringArrayList: ArrayList<String> = ArrayList()
        var doLoop = true
        do {
            val measuredWidth = FloatArray(1)
            val cntSymbols = paint.breakText(currentString, true, width, measuredWidth)
            if (cntSymbols < currentString.length) {
                stringArrayList.add(currentString.substring(0, cntSymbols))
                currentString = currentString.substring(cntSymbols, currentString.length)
            }else{
                stringArrayList.add(currentString.substring(0, cntSymbols))
                doLoop = false
            }
        } while (doLoop)
        return stringArrayList
    }

    @SuppressLint("MissingPermission")
    fun setGeoTag(location: Location) : Boolean {
        if (this.filePath.isEmpty()) {
            return false //Проверка, если не указан путь до файла
        }
        val exifInterface =
            ExifInterface(this.filePath)
        exifInterface.setGpsInfo(location)
        exifInterface.saveAttributes()
        return true
    }


    private fun getRotateDegreeFromExif(filePath: String): Int {
        var degree = 0
        try {
            val exifInterface = ExifInterface(filePath)
            val orientation: Int = exifInterface.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_UNDEFINED
            )
            when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> {
                    degree = 90
                }
                ExifInterface.ORIENTATION_ROTATE_180 -> {
                    degree = 180
                }
                ExifInterface.ORIENTATION_ROTATE_270 -> {
                    degree = 270
                }
            }
            if (degree != 0) {
                exifInterface.setAttribute(
                    ExifInterface.TAG_ORIENTATION,
                    "0"
                )
                exifInterface.saveAttributes()
            }
        } catch (e: IOException) {
            degree = -1
            e.printStackTrace()
        }
        return degree
    }

    private fun convertDpToPixels(dp: Float): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp, Resources.getSystem().displayMetrics
        ).roundToInt()
    }

}