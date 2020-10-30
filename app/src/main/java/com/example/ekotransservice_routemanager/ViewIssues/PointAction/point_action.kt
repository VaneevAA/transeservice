package com.example.ekotransservice_routemanager.ViewIssues.PointAction

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.graphics.Bitmap
import android.location.Location
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
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
import com.google.android.material.transition.MaterialContainerTransform
import kotlinx.android.synthetic.main.fragment_point_action.view.*
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.Serializable
import java.text.SimpleDateFormat
import java.util.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [point_action.newInstance] factory method to
 * create an instance of this fragment.
 */
class point_action : Fragment() {

    private var point: Point? = null
    private var canDone: Boolean = true
    private var viewPointModel : ViewPointAction? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment point_action.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(point: Point, canDone: Boolean) =
            point_action().apply {
                arguments = Bundle().apply {
                    putSerializable("point", point as Serializable)
                    putBoolean("canDone", canDone)
                }
            }
    }

    //var fileBefore : File? = null
    //var fileAfter : File? = null
    var currentFile: File? = null
    var currentFileOrder: PhotoOrder = PhotoOrder.DONT_SET
    var currentFilePath: String =""
    private var location: Location? = null

    // Настройки обновления местоположения
    val locationRequest  = LocationRequest.create().apply {
        fastestInterval = 10000
        interval = 10000
        priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        smallestDisplacement = 1.0f
    }

    //
    val locationUpdatesCallback = object : LocationCallback() {
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

    fun getViewModel(): ViewPointAction {
        return viewPointModel!!
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
                );
                // TODO: Обработка результата запроса разрешения
            }

            // Location
            val REQUEST_CHECK_STATE = 12300 // any suitable ID
            val builder = LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest)
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        }
        startLocationUpdates()
    }

    @SuppressLint("ObjectAnimatorBinding")
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
                    Uri.parse("yandexmaps://maps.yandex.ru/?rtext=$startlat,$startlon~$endlat,$endlon&rtt=auto&apikey=14e02c01-44b0-4837-8494-3f947e456472")
                var intent = Intent(Intent.ACTION_VIEW, uri)
                val packageManager: PackageManager = requireContext().packageManager
                val activities: List<ResolveInfo> = packageManager.queryIntentActivities(intent, 0)
                val isIntentSafe: Boolean = activities.size > 0
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
            }
        }
    }

    fun endOfDialog(mainFragment: View){
        fillFragment(mainFragment)
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

        var listOfActions: ArrayList<PointActoins> = if(canDone){
            viewPointModel!!.getPoint().value!!.getPointActionsArray()
        }else{
            viewPointModel!!.getPoint().value!!.getPointActionsCancelArray()
        }

        /*if (viewPointModel!!.fileAfterIsDone.value != null && viewPointModel!!.fileAfterIsDone.value!!) {
            mainFragment.findViewById<ImageView>(R.id.doneTakePhotoAfter).visibility = View.VISIBLE
        } else {
            mainFragment.findViewById<ImageView>(R.id.doneTakePhotoAfter).visibility  = View.INVISIBLE
        }

        if (viewPointModel!!.fileBeforeIsDone.value != null && viewPointModel!!.fileBeforeIsDone.value!!) {
            mainFragment.findViewById<ImageView>(R.id.doneTakePhotoBefore).visibility = View.VISIBLE
        } else {
            mainFragment.findViewById<ImageView>(R.id.doneTakePhotoBefore).visibility  = View.INVISIBLE
        }*/

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
                //takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,pictureUri)
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

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        try {
        if (resultCode == Activity.RESULT_OK) {
            val bitmap = data!!.extras!!.get("data") as Bitmap
            val bos = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos)

            val fos = currentFile!!.outputStream()
            fos.write(bos.toByteArray())
            fos.flush()
            fos.close()

            setGeoTag(currentFile!!)

            if (currentFile != null) {
               viewPointModel!!.saveFile(currentFile!!, point!!, currentFileOrder)
                if (currentFileOrder == PhotoOrder.PHOTO_AFTER && !point!!.getDone()) {
                    point!!.setDone(true)
                    Toast.makeText(requireContext(), "Точка выполнена!", Toast.LENGTH_LONG).show()
                }
            }
            // Фотографию надо делать всегда не зависимо от возможности присвоения геометки
        }} catch (e: java.lang.Exception) {
            Log.e("Ошибка фото", "Ошибка фото $e")
            Toast.makeText(requireContext(), "Ошибка $e", Toast.LENGTH_LONG).show()
        }

    }

    @SuppressLint("MissingPermission")
    private fun setGeoTag(file: File) : Boolean {
        /*fusedLocationClient.lastLocation
            .addOnSuccessListener { location : Location? ->
                val exifInterface = androidx.exifinterface.media.ExifInterface(currentFile!!.absoluteFile)
                exifInterface.setGpsInfo(location)
                exifInterface.saveAttributes()
            }*/
        try {
            return if (location != null && location!!.latitude != 0.0  && location!!.longitude != 0.0 ) {
                val exifInterface = androidx.exifinterface.media.ExifInterface(currentFile!!.absoluteFile)
                exifInterface.setGpsInfo(location)
                exifInterface.saveAttributes()
                true
                /*exifInterface.setAttribute(
                    androidx.exifinterface.media.ExifInterface.TAG_GPS_LATITUDE,
                    Location.convert(location.latitude, Location.FORMAT_SECONDS)
                )
                exifInterface.setAttribute(
                    androidx.exifinterface.media.ExifInterface.TAG_GPS_LONGITUDE,
                    Location.convert(location!!.longitude, Location.FORMAT_SECONDS)
                )
                exifInterface.setAttribute(
                    androidx.exifinterface.media.ExifInterface.TAG_GPS_LATITUDE_REF,
                    if (location.latitude > 0.0) {
                        "N"
                    } else {
                        "S"
                    }
                )
                exifInterface.setAttribute(
                    androidx.exifinterface.media.ExifInterface.TAG_GPS_LONGITUDE_REF,
                    if (location.longitude > 0.0) {
                        "E"
                    } else {
                        "W"
                    }
                )
                exifInterface.latLong
                exifInterface.saveAttributes()*/
            }else{
                false
            }
        }catch (e: java.lang.Exception){
            Toast.makeText(requireContext(), "Ошибка $e", Toast.LENGTH_LONG).show()
            return false
        }

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
                Toast.makeText(activity,"Точка выполнена",Toast.LENGTH_LONG).show()
            }else{
                val valueBefore = pointValue.getDone()
                pointValue.setDone(viewPointModel!!.fileAfterIsDone.value!!)
                if (valueBefore!=pointValue.getDone()){
                    if (valueBefore) {
                        Toast.makeText(activity,"Снято выполнение с точки, для установки выполнения сделайте фото после",Toast.LENGTH_LONG).show()
                        //TODO Заменить Toast на окно с диалогом, продумать текст
                    }else{
                        Toast.makeText(activity,"Точка выполнена",Toast.LENGTH_LONG).show()
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


