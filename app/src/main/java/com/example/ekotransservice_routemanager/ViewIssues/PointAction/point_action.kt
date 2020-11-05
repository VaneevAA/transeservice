package com.example.ekotransservice_routemanager.ViewIssues.PointAction

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Looper
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
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
import java.io.File
import java.io.Serializable
import java.text.SimpleDateFormat
import java.util.*


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
                if (location!=null && viewPointModel!!.geoIsRequired) {
                    viewPointModel!!.setPointFilesGeodata(location!!)
                }
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


        viewPointModel!!.setViewData(point!!, canDone)

        mainFragment.findViewById<Button>(R.id.takePhotoBefore)!!.setOnClickListener {
            takePicture(
                if (canDone) {
                    PhotoOrder.PHOTO_BEFORE
                } else {
                    PhotoOrder.PHOTO_CANTDONE
                }
            )
        }
        mainFragment.findViewById<Button>(R.id.takePhotoAfter).setOnClickListener {
            if (viewPointModel!!.fileBeforeIsDone.value!!
                && viewPointModel!!.currentPoint.value!!.getCountFact() != -1.0
            ) {
                takePicture(PhotoOrder.PHOTO_AFTER)
            } else {
                Toast.makeText(
                    requireContext(),
                    "Предыдущие действия не выполнены",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
        mainFragment.findViewById<Button>(R.id.setCountFact).setOnClickListener {
            if (viewPointModel!!.fileBeforeIsDone.value!!) {
                val dialog = FactDialog(
                    requireParentFragment(),
                    viewPointModel!!.currentPoint,
                    this,
                    mainFragment
                )
                dialog.show(requireActivity().supportFragmentManager, "factDialog")
            } else {
                Toast.makeText(requireContext(), "Нет фото до", Toast.LENGTH_LONG).show()
            }

        }

        mainFragment.findViewById<ImageView>(R.id.doneTakePhotoBefore).setOnClickListener {
            if (viewPointModel!!.fileBeforeIsDone.value!!) {
                val bundle = bundleOf("point" to point!!)
                (requireActivity() as MainActivity).navController.navigate(
                    R.id.pointFiles,
                    bundle
                )
            }
        }

        mainFragment.findViewById<ImageView>(R.id.doneTakePhotoAfter).setOnClickListener {
            if (viewPointModel!!.fileBeforeIsDone.value!!) {
                val bundle = bundleOf("point" to point!!)
                (requireActivity() as MainActivity).navController.navigate(
                    R.id.pointFiles,
                    bundle
                )
            }
        }

        if (!canDone) {
            fillCannotDone(mainFragment)
        }
        //fillFragment(mainFragment)
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


    private fun getReasonArray(): MutableList<String> {
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
        return reasonArray
    }

    private fun fillCannotDone(mainFragment: View){
        val reasonArray = getReasonArray()
        /*mutableListOf<String>(
        NO_GARBEGE,
        CARS_ON_POINT,
        ROAD_REPAER,
        DOORS_CLOSED,
        CLIENT_DENIAL,
        NO_EQUIPMENT,
        EQUIPMENT_LOCKED,
        OTHER
    )*/
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
                point!!.setReasonComment(item)
                viewPointModel!!.getRepository().updatePointAsync(point!!)
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
            point!!.setReasonComment(commentText)
            viewPointModel!!.getRepository().updatePointAsync(point!!)
            // здесь можно писать в точку
        }

        //Установим текущие значения значением полученным из точки
        val reasonComment: String = viewPointModel!!.getPoint().value!!.getReasonComment()
        if (reasonComment!="") {
            if (reasonArray.contains(reasonComment)) {
                val spinnerPosition: Int =
                    (spinner.adapter as ArrayAdapter<String>).getPosition(reasonComment)
                spinner.setSelection(spinnerPosition)
            } else {
                val spinnerPosition: Int =
                    (spinner.adapter as ArrayAdapter<String>).getPosition(OTHER)
                spinner.setSelection(spinnerPosition)
                comment.setText(reasonComment)
            }
        }
    }

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
        val storage = requireActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val fileName = generateFileName(point!!)
        try {
            currentFile = File.createTempFile(
                fileName,
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

    private fun generateFileName(point: Point): String {
        val timeCreated = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        return "${point.getRouteName()}__{$timeCreated}__${point.getAddressName()}_${currentFileOrder.string}"
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

                if (location == null) {
                    Toast.makeText(
                        activity,
                        "Предупреждение, местоположение не определено",
                        Toast.LENGTH_LONG
                    ).show()
                    /*fusedLocationClient.lastLocation
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
                    }*/
                    //return
                }

                if (currentFile != null) {
                    setGeoTag()
                    val pointFile = viewPointModel!!.saveFile(
                        currentFile!!,
                        point!!,
                        currentFileOrder
                    )
                    if (currentFileOrder == PhotoOrder.PHOTO_AFTER && !point!!.getDone()) {
                        point!!.setDone(true)
                        point!!.setTimestamp(Date())
                        viewPointModel!!.getRepository().updatePointAsync(point!!)
                        Toast.makeText(requireContext(), "Точка выполнена!", Toast.LENGTH_LONG)
                            .show()
                    }

                    if (location != null || (pointFile.lat != 0.0 && pointFile.lon != 0.0)) {
                        viewPointModel!!.setDataInfoOnFile(pointFile, location)
                    } else {
                        viewPointModel!!.geoIsRequired = true
                    }
                }

            }
        /*} catch (e: java.lang.Exception) {
            Log.e("Ошибка фото", "Ошибка фото $e")
            Toast.makeText(requireContext(), "Ошибка $e", Toast.LENGTH_LONG).show()
        }*/

    }


    @SuppressLint("MissingPermission")
    private fun setGeoTag() : Boolean {

        val exifInterface =
            ExifInterface(currentFile!!.absoluteFile)
        exifInterface.setGpsInfo(location)
        exifInterface.saveAttributes()
        return true
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


