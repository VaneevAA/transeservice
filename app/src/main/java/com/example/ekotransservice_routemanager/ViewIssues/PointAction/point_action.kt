package com.example.ekotransservice_routemanager.ViewIssues.PointAction

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.content.res.Resources
import android.graphics.*
import android.location.Geocoder
import android.location.Location
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Looper
import android.provider.MediaStore
import android.text.Editable
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.os.bundleOf
import androidx.core.view.children
import androidx.core.widget.addTextChangedListener
import androidx.exifinterface.media.ExifInterface
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.ekotransservice_routemanager.DataClasses.PhotoOrder
import com.example.ekotransservice_routemanager.DataClasses.Point
import com.example.ekotransservice_routemanager.DataClasses.PointActoins
import com.example.ekotransservice_routemanager.MainActivity
import com.example.ekotransservice_routemanager.R
import com.google.android.gms.location.*
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.transition.MaterialContainerTransform
import kotlinx.android.synthetic.main.fragment_point_action.view.*
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.Serializable
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * A simple [Fragment] subclass.
 * Use the [point_action.newInstance] factory method to
 * create an instance of this fragment.
 */
class point_action : Fragment() {

    /*
        значения для списка невозможности вывоза
    */

    private final val NO_GARBEGE = "нет ТКО"
    private final val CARS_ON_POINT = "нет проезда к КП (заставлено автомашинами)"
    private final val ROAD_REPAER = "нет проезда (ремонт дороги)"
    private final val DOORS_CLOSED = "не открывают ворота (шлагбаум)"
    private final val CLIENT_DENIAL = "отказ Потребителя от вывоза ТКО"
    private final val NO_EQUIPMENT = "нет контейнерного оборудования"
    private final val EQUIPMENT_LOCKED = "контейнер(а) на замке"
    private final val OTHER = "другое"

    private var point: Point? = null
    private var canDone: Boolean = true
    private var viewPointModel : ViewPointAction? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    companion object {
        @JvmStatic
        fun newInstance(point: Point, canDone: Boolean) =
            point_action().apply {
                arguments = Bundle().apply {
                    putSerializable("point", point as Serializable)
                    putBoolean("canDone", canDone)
                }
            }
    }

    private var currentFile: File? = null
    private var currentFileOrder: PhotoOrder = PhotoOrder.DONT_SET
    private var currentFilePath: String =""
    private var location: Location? = null

    // Настройки обновления местоположения
    private val locationRequest  = LocationRequest.create().apply {
        fastestInterval = 2000
        interval = 2000
        priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        smallestDisplacement = 1.0f
    }

    //
    private val locationUpdatesCallback = object : LocationCallback() {
        override fun onLocationResult(lr: LocationResult) {
            try {
                location = lr.locations.last()
            } catch (e: java.lang.Exception) {
                Toast.makeText(
                    requireContext(),
                    "Присвоить координаты не получилось",
                    Toast.LENGTH_SHORT
                ).show()
                //TODO обработка ошибки получения координат
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedElementEnterTransition = MaterialContainerTransform()
        arguments?.let {
            point  = it.getSerializable("point") as Point
            canDone = it.getBoolean("canDone")

            if (ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissions(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ), 101
                );
                // TODO: Обработка результата запроса разрешения
            }

            if (ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissions(
                    arrayOf(
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    ), 101
                )
                // TODO: Обработка результата запроса разрешения
            }

            fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        }
        startLocationUpdates()
    }

    @SuppressLint("ObjectAnimatorBinding", "ResourceType")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
        ): View? {

            val mainFragment = inflater.inflate(R.layout.fragment_point_action, container, false)
            viewPointModel = ViewModelProvider(
                this.requireActivity(),
                ViewPointAction.ViewPointsFactory(
                    this.requireActivity().application,
                    requireActivity() as MainActivity,
                    point!!
                )
            )
                .get(ViewPointAction::class.java)


            val observerPoint = Observer<Point> { pointValue -> (
                    fillFragment(mainFragment)
                    )
            }

            viewPointModel!!.currentPoint.observe(requireActivity(), observerPoint)

            val observerBefore = Observer<Boolean> { fileBeforeIsDone -> (
                    if (fileBeforeIsDone != null && fileBeforeIsDone) {
                        mainFragment.findViewById<ImageView>(R.id.doneTakePhotoBefore).visibility = View.VISIBLE
                    } else {
                        mainFragment.findViewById<ImageView>(R.id.doneTakePhotoBefore).visibility  = View.INVISIBLE
                    }
                    )
            }

            viewPointModel!!.fileBeforeIsDone.observe(requireActivity(), observerBefore)

            val observerAfter = Observer<Boolean> { fileAfterIsDone -> (
                    if (fileAfterIsDone != null && fileAfterIsDone) {
                        mainFragment.findViewById<ImageView>(R.id.doneTakePhotoAfter).visibility = View.VISIBLE
                    } else {
                        mainFragment.findViewById<ImageView>(R.id.doneTakePhotoAfter).visibility  = View.INVISIBLE
                    }
                    )
            }

            viewPointModel!!.fileAfterIsDone.observe(requireActivity(), observerAfter)

            viewPointModel!!.setViewData(point!!)

            fillFragment(mainFragment)

            mainFragment.findViewById<Button>(R.id.takePhotoBefore)!!.setOnClickListener {
                takePicture(PhotoOrder.PHOTO_BEFORE)
            }
            mainFragment.findViewById<Button>(R.id.takePhotoAfter).setOnClickListener {
                if (viewPointModel!!.fileBeforeIsDone.value!!
                    && viewPointModel!!.currentPoint.value!!.getCountFact()!=-1.0) {
                    takePicture(PhotoOrder.PHOTO_AFTER)
                }else {
                    Toast.makeText(
                        requireContext(),
                        "Предыдущие действия не выполнены",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

            mainFragment.findViewById<Button>(R.id.setCountFact).setOnClickListener {
                if(viewPointModel!!.fileBeforeIsDone.value!!){
                    val dialog = FactDialog(
                        requireParentFragment(),
                        viewPointModel!!.currentPoint,
                        this,
                        mainFragment
                    )
                    dialog.show(requireActivity().supportFragmentManager, "factDialog")
                }else{
                    Toast.makeText(requireContext(), "Нет фото до", Toast.LENGTH_LONG).show()
                }

            }

            mainFragment.findViewById<ImageView>(R.id.doneTakePhotoBefore).setOnClickListener {
                if(viewPointModel!!.fileBeforeIsDone.value!!){
                    val bundle = bundleOf("point" to point!!)
                    (requireActivity() as MainActivity).navController.navigate(R.id.pointFiles, bundle)
                }
            }

            mainFragment.findViewById<ImageView>(R.id.doneTakePhotoAfter).setOnClickListener {
                if(viewPointModel!!.fileBeforeIsDone.value!!){
                    val bundle = bundleOf("point" to point!!)
                    (requireActivity() as MainActivity).navController.navigate(R.id.pointFiles, bundle)
                }
            }

            mainFragment.showRouteButton.setOnClickListener{

                if (location != null && location!!.latitude != 0.0  && location!!.longitude != 0.0 ) {
                    val startlat = location!!.latitude
                    val startlon = location!!.longitude
                    val endlat = point!!.getAddressLat()
                    val endlon = point!!.getAddressLon()
                    val uri =
                        Uri.parse("yandexmaps://maps.yandex.ru/?rtext=$startlat,$startlon~$endlat,$endlon&rtt=auto")
                    var intent = Intent(Intent.ACTION_VIEW, uri)
                    val packageManager: PackageManager = requireContext().packageManager
                    val activities: List<ResolveInfo> = packageManager.queryIntentActivities(intent, 0)
                    val isIntentSafe: Boolean = activities.isNotEmpty()
                    if (isIntentSafe) {
                        startActivity(intent)
                    } else {
                        // Открываем страницу приложения Яндекс.Карты в Google Play.
                        intent = Intent(Intent.ACTION_VIEW)
                        intent.data = Uri.parse("market://details?id=ru.yandex.yandexmaps")
                        startActivity(intent)
                    }
                }else {
                    Toast.makeText(
                        requireContext(),
                        "Текущее местоположение не определено",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

        val reasonArray = mutableListOf<String>(
            NO_GARBEGE,
            CARS_ON_POINT,
            ROAD_REPAER,
            DOORS_CLOSED,
            CLIENT_DENIAL,
            NO_EQUIPMENT,
            EQUIPMENT_LOCKED,
            OTHER
        )
        val spinner = mainFragment.findViewById<Spinner>(R.id.reasonSpinner)
        val arrayAdapter = ArrayAdapter<String>(
            requireContext(),
            android.R.layout.simple_spinner_item,
            reasonArray
        )
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        val comment = mainFragment.findViewById<TextInputEditText>(R.id.reasonInput)
        arrayAdapter.setNotifyOnChange(true)
        spinner.adapter = arrayAdapter
        val itemSelectedListener: AdapterView.OnItemSelectedListener = object :
            AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {

                // Получаем выбранный объект
                val item = parent.getItemAtPosition(position) as String
                /*
                    вот тут можно записать в точку!!!!!
                */
                if (item == OTHER) {
                    comment.visibility = ViewGroup.VISIBLE
                } else {
                    comment.visibility = ViewGroup.GONE
                    comment.text?.clear()
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
        spinner.onItemSelectedListener = itemSelectedListener

        comment.addTextChangedListener {
            val commentText = it.toString()
            // здесь можно писать в точку
        }
        return mainFragment
    }


    private fun showButtons(mainFragment: View, listOfActions: ArrayList<PointActoins>){
        for(child in (mainFragment.findViewById<View>(R.id.buttonsToDo) as ConstraintLayout).children){
            child.visibility = View.GONE
        }

        for(action in listOfActions){
            when (action){
                PointActoins.TAKE_PHOTO_BEFORE
                -> mainFragment.findViewById<View>(R.id.layoutTakePhotoBefore).visibility =
                    View.VISIBLE
                PointActoins.TAKE_PHOTO_AFTER
                -> mainFragment.findViewById<View>(R.id.layoutTakePhotoAfter).visibility =
                    View.VISIBLE
                PointActoins.SET_VOLUME
                -> mainFragment.findViewById<View>(R.id.layoutSetCountFact).visibility =
                    View.VISIBLE
                PointActoins.SET_REASON
                -> mainFragment.findViewById<View>(R.id.reasonLayout).visibility =
                    View.VISIBLE
            }
        }
    }

    /*fun endOfDialog(mainFragment: View){
        fillFragment(mainFragment)
    }*/

    private fun fillFragment(mainFragment: View){

        val addressText = mainFragment.findViewById<TextView>(R.id.pointAdress)
        addressText.text = viewPointModel!!.getPoint().value!!.getAddressName()

        val agentText = mainFragment.findViewById<TextView>(R.id.agentName)
        agentText.text = viewPointModel!!.getPoint().value!!.getAgentName()

        val contNameText = mainFragment.findViewById<TextView>(R.id.containerName)
        contNameText.text = viewPointModel!!.getPoint().value!!.getContainerName()

        val contCountText = mainFragment.findViewById<TextView>(R.id.containerCount)
        contCountText.text = viewPointModel!!.getPoint().value!!.getContCount().toString()

        val textSetCountFact = mainFragment.findViewById<TextView>(R.id.textSetCountFact)
        val countFact = viewPointModel!!.getPoint().value!!.getCountFact()
        textSetCountFact.text = if (countFact==-1.0) {"0.0"} else {countFact.toString()}

        val listOfActions: ArrayList<PointActoins> = if(canDone){
            viewPointModel!!.getPoint().value!!.getPointActionsArray()
        }else{
            viewPointModel!!.getPoint().value!!.getPointActionsCancelArray()
        }

        showButtons(mainFragment, listOfActions)
    }

    private fun takePicture(fileOrder: PhotoOrder){
        currentFileOrder = fileOrder
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent -> takePictureIntent.resolveActivity(
            requireActivity().packageManager
        )?.also {
            val pictureFile = createFile()
            pictureFile?.also {
                val pictureUri: Uri = FileProvider.getUriForFile(
                    requireContext(),
                    "com.example.ekotransservice_routemanager.fileprovider",
                    it
                )
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, pictureUri)
                /*takePictureIntent.clipData = ClipData.newRawUri(null, pictureUri)
                takePictureIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                takePictureIntent.setFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)*/
                startActivityForResult(takePictureIntent, 1)
            }
        }
        }
    }

    @SuppressLint("SimpleDateFormat")
    private fun createFile() : File?{
        val timeCreated = SimpleDateFormat("yyyyMMddHHmmss").format(Date())
        val storage = requireActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        try {
            currentFile = File.createTempFile(
                "${point!!.getAddressName()}_($timeCreated)_${currentFileOrder.string}",
                ".jpg",
                storage
            )
                .apply { currentFilePath = absolutePath }
            return currentFile
        }catch (e: Exception){
            Toast.makeText(requireContext(), "Неудалось записать файл", Toast.LENGTH_LONG).show()
            //TODO create exception behavior
        }

        return null
    }

    @SuppressLint("MissingPermission")
    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        //try {
            if (resultCode == Activity.RESULT_OK) {
                if (currentFile==null || currentFile?.length() == 0L) {
                    Toast.makeText(
                        activity,
                        "Ошибка работы камеры, вернулся пустой файл",
                        Toast.LENGTH_LONG
                    ).show()
                    return
                }

                if (location == null || location!!.latitude == 0.0 || location!!.longitude == 0.0) {
                    fusedLocationClient.lastLocation
                        .addOnSuccessListener { location: Location? ->
                            this.location = location

                        }

                    if (location == null) {
                        Toast.makeText(
                            activity,
                            "Ошибка работы камеры, местоположение не определено",
                            Toast.LENGTH_LONG
                        ).show()
                    } else {
                        Toast.makeText(
                            activity,
                            "По последнему местоположению",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    //return
                }

                createResultImageFile()
                setGeoTag()

                if (currentFile != null) {
                    viewPointModel!!.saveFile(currentFile!!, point!!, currentFileOrder)
                    if (currentFileOrder == PhotoOrder.PHOTO_AFTER && !point!!.getDone()) {
                        point!!.setDone(true)
                        Toast.makeText(requireContext(), "Точка выполнена!", Toast.LENGTH_LONG)
                            .show()
                    }
                }
                // Фотографию надо делать всегда не зависимо от возможности присвоения геометки
            }
        /*} catch (e: java.lang.Exception) {
            Log.e("Ошибка фото", "Ошибка фото $e")
            Toast.makeText(requireContext(), "Ошибка $e", Toast.LENGTH_LONG).show()
        }*/

    }

    private fun getAddressNameFromLocation(location: Location): String {

            val geocoder = Geocoder(requireContext(), Locale("ru"))
            val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
            return addresses[0].getAddressLine(0) // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()

        /*val city: String = addresses.get(0).getLocality()
        val state: String = addresses.get(0).getAdminArea()
        val country: String = addresses.get(0).getCountryName()
        val postalCode: String = addresses.get(0).getPostalCode()
        val knownName: String = addresses.get(0).getFeatureName()*/

    }

    @SuppressLint("InflateParams")
    private fun createResultImageFile() {

        // Основное изображение
        var originalBitmap: Bitmap = BitmapFactory.decodeFile(currentFile!!.absolutePath)
        val degree = getRotateDegreeFromExif(currentFile!!.absolutePath)
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

        /*canvas.save()
        //TODO Маштабирование макета при выводе на холст
        canvas.translate(
            0F,
            (originalBitmap.height - photoDataView.measuredHeight).toFloat()
        )
        photoDataView.draw(canvas)
        canvas.restore()*/

        val rt = Rect(
            0,
            originalBitmap.height,
            originalBitmap.width,
            originalBitmap.height - originalBitmap.height / 4
        )
        paint.style = Paint.Style.FILL
        paint.color = ContextCompat.getColor(requireContext(), R.color.colorGrayBack)
        canvas.drawRect(rt, paint)

        paint.color = Color.WHITE
        paint.textSize = originalBitmap.height / 30F

        //Вывод адреса
        var addressText = ""
        var latText = ""
        var lonText = ""

        if (location == null || location!!.latitude == 0.0 || location!!.longitude == 0.0) {
            addressText = point!!.getAddressName()
        } else {
            addressText = getAddressNameFromLocation(location!!)
            latText = location!!.latitude.toString()
            lonText = location!!.latitude.toString()
        }

        printText(addressText, rt.width() - 40, 10, rt.bottom + 20, paint, canvas)

        // Вывод координат и даты
        val textWidth = rt.width()/3-40
        val textLine = originalBitmap.height - abs(rt.height() / 2) +20
        //Долгота
        printText(latText, textWidth, 10, textLine, paint, canvas)

        //Широта
        printText(lonText, textWidth, 10 + textWidth, textLine, paint, canvas)

        //Дата время
        printText(
            SimpleDateFormat(
                "yyyy-MM-dd (EEE) HH:mm:ss",
                Locale("ru")
            ).format(Date()), textWidth, 10 + 2 * textWidth, textLine, paint, canvas
        )

        //Сохранение в файл
        currentFile!!.delete()
        val out = FileOutputStream(currentFile)
        overlayBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
        out.flush()
        out.close()

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
    private fun setGeoTag() : Boolean {

        val exifInterface =
            ExifInterface(currentFile!!.absoluteFile)
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
   /* fun showEnableLocationSetting() {
        activity?.let {
            val locationRequest = LocationRequest.create()
            locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

            val builder = LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest)

            val task = LocationServices.getSettingsClient(it)
                .checkLocationSettings(builder.build())

            task.addOnSuccessListener { response ->
                val states = response.locationSettingsStates
                if (states.isLocationPresent) {
                    //Do something
                }
            }
            task.addOnFailureListener { e ->
                if (e is ResolvableApiException) {
                    try {
                        // Handle result in onActivityResult()
                        e.startResolutionForResult(it,
                            MainActivity.LOCATION_SETTING_REQUEST)
                    } catch (sendEx: IntentSender.SendIntentException) { }
                }
            }
        }
    }*/

    // Обработка результат ввода факта
    fun okFactDialogClicked(factText: String) {
        try {
            val fact = factText.toDouble()
            val pointValue = viewPointModel!!.currentPoint.value!!
            pointValue.setCountFact(fact)
            // Отметим выполнение точки.
            // Если количество равно 0, то считаем точку выполненной, даже если не сделано фото после
            // Если количество не равно 0, то считаем точку выполненной только при начлии фото после
            if (fact==0.0) {
                pointValue.setDone(true)
                Toast.makeText(activity, "Точка выполнена", Toast.LENGTH_LONG).show()
            }else{
                val valueBefore = pointValue.getDone()
                pointValue.setDone(viewPointModel!!.fileAfterIsDone.value!!)
                if (valueBefore!=pointValue.getDone()){
                    if (valueBefore) {
                        Toast.makeText(
                            activity,
                            "Снято выполнение с точки, для установки выполнения сделайте фото после",
                            Toast.LENGTH_LONG
                        ).show()
                        //TODO Заменить Toast на окно с диалогом, продумать текст
                    }else{
                        Toast.makeText(activity, "Точка выполнена", Toast.LENGTH_LONG).show()
                    }
                }
            }
            pointValue.setCountOverFromPlanAndFact()
            pointValue.setTimestamp(Date())
            viewPointModel!!.getRepository().updatePointAsync(pointValue)
            viewPointModel!!.currentPoint.value = pointValue

        }catch (e: Exception){
            Toast.makeText(activity, "Число введено неправильно", Toast.LENGTH_LONG).show()
        }

    }

    override fun onResume() {
        super.onResume()
        startLocationUpdates()
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationUpdatesCallback,
            Looper.getMainLooper()
        )
    }

    override fun onPause() {
        super.onPause()
        fusedLocationClient.removeLocationUpdates(locationUpdatesCallback)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            101 -> {
                if ((grantResults.isNotEmpty() &&
                            grantResults[0] == PackageManager.PERMISSION_GRANTED)
                ) {
                    // Permission is granted. Continue the action or workflow
                    // in your app.
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Требуется предоставить права на определение местоположения, работа с фотографиями не возможна",
                        Toast.LENGTH_LONG
                    ).show()
                    if (findNavController().popBackStack()) {

                    } else {
                        findNavController().navigate(R.id.start_frame_screen)
                    }

                }
                return
            }
            else -> {

            }
        }
    }
}


