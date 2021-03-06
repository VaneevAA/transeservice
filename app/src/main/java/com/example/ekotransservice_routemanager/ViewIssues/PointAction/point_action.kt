package com.example.ekotransservice_routemanager.ViewIssues.PointAction

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
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
import android.view.WindowManager
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
import androidx.lifecycle.viewModelScope
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import androidx.work.Operation
import com.example.ekotransservice_routemanager.CoroutineViewModel
import com.example.ekotransservice_routemanager.DataClasses.PhotoOrder
import com.example.ekotransservice_routemanager.DataClasses.Point
import com.example.ekotransservice_routemanager.DataClasses.PointActoins
import com.example.ekotransservice_routemanager.MainActivity
import com.example.ekotransservice_routemanager.R
import com.example.ekotransservice_routemanager.camera.CameraFragmentDirections
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.GooglePlayServicesUtil
import com.google.android.gms.common.GooglePlayServicesUtilLight
import com.google.android.gms.location.*
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.transition.MaterialContainerTransform
import com.muslimcompanion.utills.GPSTracker
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
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
    private lateinit var fusedLocationClient: FusedLocationProviderClient // location client if google play services is AVAILABLE
    private var gps: GPSTracker? = null // location client if google play services is UNAVAILABLE

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
    //private var currentFileOrder: PhotoOrder = PhotoOrder.DONT_SET
    private var currentFilePath: String =""
    private var location: Location? = null
    //private var reasonComment: String = ""
    private var googlePlayServicesAvailable: Boolean = false

    //region Location
    // Настройки обновления местоположения
    private val locationRequest  = LocationRequest.create().apply {
        fastestInterval = 2000
        interval = 2000
        priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        smallestDisplacement = 1.0f
    }

    private val locationUpdatesCallback = object : LocationCallback() {
        override fun onLocationResult(lr: LocationResult) {
            try {
                location = lr.locations.last()
                changeGeoTagIfNeeded()
            } catch (e: java.lang.Exception) {
                Toast.makeText(
                    requireContext(),
                    "Присвоить координаты не получилось",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun changeGeoTagIfNeeded() {
        if (!googlePlayServicesAvailable && gps!=null) {
            location = gps!!.location
        }
        if (viewPointModel != null) {
            if (location != null && viewPointModel!!.geoIsRequired) {
                viewPointModel!!.setPointFilesGeodata(location!!)
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationUpdatesCallback,
            Looper.getMainLooper()
        )
    }
    //endregion

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedElementEnterTransition = MaterialContainerTransform()
        arguments?.let {
            point  = it.getSerializable("point") as Point
            canDone = it.getBoolean("canDone")

            googlePlayServicesAvailable = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context) == ConnectionResult.SUCCESS
            if (googlePlayServicesAvailable) {
                fusedLocationClient =
                    LocationServices.getFusedLocationProviderClient(requireActivity())
            }
        }

        if (googlePlayServicesAvailable) {
            startLocationUpdates()
        }else{
            gps = GPSTracker(requireContext())
            if(gps!!.canGetLocation()){
                location = gps!!.location
            }else
            {
                gps!!.showSettingsAlert()
            }
        }

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
        val buttonEnd = mainFragment.findViewById<Button>(R.id.pointDone)
        buttonEnd.setOnClickListener {
            if(point!!.getDone() || point!!.getReasonComment() != ""){
                requireActivity().onBackPressed()
            } else {
                Toast.makeText(requireContext(),"Точка не может считаться выполненной",Toast.LENGTH_LONG).show()
            }

        }
        //TODO set comment
        (requireActivity() as MainActivity).supportActionBar?.show()
        return mainFragment
    }

    override fun onDestroy() {
        super.onDestroy()
        if (googlePlayServicesAvailable) {
            fusedLocationClient.removeLocationUpdates(locationUpdatesCallback)
        }else{
            gps!!.stopUsingGPS()
        }
        changeGeoTagIfNeeded()
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

    private fun getReasonArray(): MutableList<String> {
        return mutableListOf(
            NO_GARBEGE,
            CARS_ON_POINT,
            ROAD_REPAER,
            DOORS_CLOSED,
            CLIENT_DENIAL,
            NO_EQUIPMENT,
            EQUIPMENT_LOCKED,
            OTHER
        )
    }

    private fun fillCannotDone(mainFragment: View){
        val reasonArray = getReasonArray()

        val spinner = mainFragment.findViewById<Spinner>(R.id.reasonSpinner)
        val arrayAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            reasonArray
        )
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        val comment = mainFragment.findViewById<TextInputEditText>(R.id.reasonInput)
        arrayAdapter.setNotifyOnChange(true)
        spinner.adapter = arrayAdapter
        viewPointModel!!.reasonComment = comment.text.toString()
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
                viewPointModel!!.reasonComment = item
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

        val commentText = mainFragment.findViewById<TextView>(R.id.commentText)
        commentText.text =  viewPointModel!!.getPoint().value!!.getComment()

        //phone number stuff
        val callButton = mainFragment.findViewById<ImageButton>(R.id.call)
        if (viewPointModel!!.getPhoneNumber() == ""){
            callButton.visibility = View.GONE
        } else {
            callButton.visibility = View.VISIBLE
        }
        callButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:" + viewPointModel!!.getPhoneNumber())
            }
            if (intent.resolveActivity(requireActivity().packageManager) != null){
                startActivity(intent)
            }
        }

        showButtons(mainFragment, listOfActions)
    }

    // Обработка результат ввода факта
    fun okFactDialogClicked(fact : Double) {
        try {
            //val fact = factText.toDouble()
            val pointValue = viewPointModel!!.currentPoint.value!!
            pointValue.setCountFact(fact)
            // Отметим выполнение точки.
            // Если количество равно 0, то считаем точку выполненной, даже если не сделано фото после
            // Если количество не равно 0, то считаем точку выполненной только при начлии фото после
            if (fact == 0.0) {
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
        changeGeoTagIfNeeded()
    }

    private fun takePicture(fileOrder: PhotoOrder){
        viewPointModel!!.currentFileOrder = fileOrder
        Navigation.findNavController(requireActivity(), R.id.my_nav_host_fragment).navigate(
            point_actionDirections.actionPointActionToCameraFragment(point!!,viewPointModel!!.currentFileOrder,canDone)
        )

        /*Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent -> takePictureIntent.resolveActivity(
            requireActivity().packageManager
        )?.also {
            val pictureFile = createFile()
            pictureFile?.also {
                val pictureUri: Uri = FileProvider.getUriForFile(
                    requireContext(),
                    "com.example.ekotransservice_routemanager.fileprovider",
                    it
                )
                //pictureFile.delete()
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, pictureUri)

                startActivityForResult(takePictureIntent, 1)
            }
        }
        }*/
    }

    /*@SuppressLint("SimpleDateFormat")
    private fun createFile() : File?{
        val storage = requireActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val fileName = generateFileName(point!!)
        try {
            currentFile = File(storage,"$fileName.jpg")
           /*currentFile = File.createTempFile(
                fileName,
                ".jpg",
                storage
            )*/
                .apply { currentFilePath = absolutePath }
            return currentFile
        }catch (e: Exception){
            Toast.makeText(requireContext(), "Неудалось записать файл", Toast.LENGTH_LONG).show()
        }
        return null
    }

    private fun generateFileName(point: Point): String {
        val timeCreated = SimpleDateFormat("yyyyMMdd_HHmmss",Locale("RU")).format(Date())
        var addressName = point.getAddressName().replace("/","")
        addressName = addressName.replace("\\" , "")
        addressName = addressName.replace(":" , "")
        addressName = addressName.replace("(" , "")
        addressName = addressName.replace(")" , "")
        addressName = addressName.replace("\"" , "")
        return "${point.getRouteName()}__{$timeCreated}__${addressName}_${currentFileOrder.string}"
    }

    //TODO Refactor code CameraIntent
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

                    /*val viewModel = CoroutineViewModel(this as MainActivity,{
                        delay(5000)
                    },{
                        if (currentFile==null || currentFile?.length() == 0L) {
                            Toast.makeText(
                                activity,
                                "Ошибка работы камеры, вернулся пустой файл",
                                Toast.LENGTH_LONG
                            ).show()
                            return@CoroutineViewModel
                        }else{
                            processImageFile()
                        }
                    })
                    viewModel.startWork()*/

                }else{
                    processImageFile()
                }

            }
    }

    private fun processImageFile(){
        if (location == null) {
            Toast.makeText(
                activity,
                "Предупреждение, местоположение не определено",
                Toast.LENGTH_LONG
            ).show()

        }

        if (currentFile != null) {
            setGeoTag()
            val pointFile = viewPointModel!!.saveFile(
                currentFile!!,
                point!!,
                viewPointModel!!.currentFileOrder
            )
            if (viewPointModel!!.currentFileOrder == PhotoOrder.PHOTO_AFTER && !point!!.getDone()) {
                point!!.setDone(true)
                point!!.setTimestamp(Date())
                viewPointModel!!.getRepository().updatePointAsync(point!!)
                Toast.makeText(requireContext(), "Точка выполнена!", Toast.LENGTH_LONG)
                    .show()
            }

            if (viewPointModel!!.currentFileOrder == PhotoOrder.PHOTO_CANTDONE) {
                if (point!!.getReasonComment().isEmpty()) {
                    point!!.setReasonComment(viewPointModel!!.reasonComment)
                }
                point!!.setTimestamp(Date())
                viewPointModel!!.getRepository().updatePointAsync(point!!)
            }
            if (location != null || (pointFile.lat != 0.0 && pointFile.lon != 0.0)) {
                viewPointModel!!.setDataInfoOnFile(pointFile, location)
            } else {
                viewPointModel!!.geoIsRequired = true
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun setGeoTag() : Boolean {
        val exifInterface =
            ExifInterface(currentFile!!.absoluteFile)
        exifInterface.setGpsInfo(location)
        exifInterface.saveAttributes()
        return true
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
    }*/
}


