package com.example.ekotransservice_routemanager

import android.Manifest
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Context.LOCATION_SERVICE
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.location.Criteria
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.location.LocationManager.NETWORK_PROVIDER
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.content.FileProvider
import androidx.core.view.children
import androidx.exifinterface.media.ExifInterface
import androidx.lifecycle.Observer
import com.example.ekotransservice_routemanager.DataClasses.PhotoOrder
import com.example.ekotransservice_routemanager.DataClasses.Point
import com.example.ekotransservice_routemanager.DataClasses.PointActoins
import com.example.ekotransservice_routemanager.DataClasses.PointFile
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.tasks.RuntimeExecutionException
import kotlinx.android.synthetic.main.fragment_point_action.*
import java.io.File
import java.io.Serializable
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

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
    // TODO: Rename and change types of parameters
    private var point: Point? = null
    private var canDone: Boolean = true
    private var viewPointModel : ViewPointAction? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            point  = it.getSerializable("point") as Point
            canDone = it.getBoolean("canDone")

            fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

            // Create persistent LocationManager reference
            mLocationManager = requireContext().getSystemService(LOCATION_SERVICE) as LocationManager?

            val REQUEST_CHECK_STATE = 12300 // any suitable ID
            val builder = LocationSettingsRequest.Builder()
                .addLocationRequest(reqSetting)

            val client = LocationServices.getSettingsClient(requireActivity())
            client.checkLocationSettings(builder.build()).addOnCompleteListener { task ->
                try {
                    val state: LocationSettingsStates = task.result.locationSettingsStates
                    /*Log.e("LOG", "LocationSettings: \n" +
                            " GPS present: ${state.isGpsPresent} \n" +
                            " GPS usable: ${state.isGpsUsable} \n" +
                            " Location present: " +
                            "${state.isLocationPresent} \n" +
                            " Location usable: " +
                            "${state.isLocationUsable} \n" +
                            " Network Location present: " +
                            "${state.isNetworkLocationPresent} \n" +
                            " Network Location usable: " +
                            "${state.isNetworkLocationUsable} \n"
                    )*/
                } catch (e: RuntimeExecutionException) {
                    if (e.cause is ResolvableApiException)
                        (e.cause as ResolvableApiException).startResolutionForResult(requireActivity(),
                            REQUEST_CHECK_STATE)
                }
            }
        }
    }

    @SuppressLint("ObjectAnimatorBinding")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val mainFragment = inflater.inflate(R.layout.fragment_point_action, container, false)
        fillFragment(mainFragment)

        // Inflate the layout for this fragment

        viewPointModel!!.getPoint().observe(this.viewLifecycleOwner, Observer {
            viewPointModel!!.setPoint(it!!)
            fillFragment(mainFragment)

        })


        mainFragment.findViewById<Button>(R.id.takePhotoBefore)!!.setOnClickListener {
            //if(fileBefore == null){
                currentFileOrder = PhotoOrder.PHOTO_BEFORE
                takePicture()
                fileBefore = currentFile
            //}else{
            //    Toast.makeText(requireContext(),"Фото уже есть",Toast.LENGTH_LONG)
            //}
        }
        mainFragment.findViewById<Button>(R.id.takePhotoAfter).setOnClickListener {
            if(fileBefore != null && point!!.getContCount() != 0){
                Toast.makeText(requireContext(),"Предыдущие действия не выполнены",Toast.LENGTH_LONG).show()
            }else if(fileAfter == null){
                currentFileOrder = PhotoOrder.PHOTO_AFTER
                takePicture()
                fileAfter = currentFile
            }else{
                Toast.makeText(requireContext(),"Фото уже есть",Toast.LENGTH_LONG).show()
            }
        }

        mainFragment.findViewById<Button>(R.id.setCountFact).setOnClickListener {
            /*val progressBar = ((requireActivity() as MainActivity).getProgressBar())
            val animator = ObjectAnimator.ofInt(progressBar as ProgressBar,"Await",1)
            animator.start()*/
        }

        return mainFragment
    }

    private fun showButtons(mainFragment:View, listOfActions:ArrayList<PointActoins>){
        for(child in (mainFragment.findViewById<View>(R.id.buttonsToDo) as ConstraintLayout).children){
            child.visibility = View.GONE
        }

        for(action in listOfActions){
            when (action){
                PointActoins.TAKE_PHOTO_BEFORE
                -> mainFragment.findViewById<View>(R.id.layoutTakePhotoBefore).visibility = View.VISIBLE
                PointActoins.TAKE_PHOTO_AFTER
                -> mainFragment.findViewById<View>(R.id.layoutTakePhotoAfter).visibility = View.VISIBLE
                PointActoins.SET_VOLUME
                -> mainFragment.findViewById<View>(R.id.layoutSetCountFact).visibility = View.VISIBLE
            }
        }
    }

    private fun fillFragment(mainFragment: View){
        viewPointModel = activity?.application?.let { ViewPointAction(it,point!!) }

        val addressText = mainFragment.findViewById<TextView>(R.id.pointAdress)
        addressText.text = viewPointModel!!.getPoint().value!!.getAddressName()

        val agentText = mainFragment.findViewById<TextView>(R.id.agentName)
        agentText.text = viewPointModel!!.getPoint().value!!.getAgentName()

        val contNameText = mainFragment.findViewById<TextView>(R.id.containerName)
        contNameText.text = viewPointModel!!.getPoint().value!!.getContainerName()

        val contCountText = mainFragment.findViewById<TextView>(R.id.containerCount)
        contCountText.text = viewPointModel!!.getPoint().value!!.getContCount().toString()

        var listOfActions: ArrayList<PointActoins> = if(canDone){
            viewPointModel!!.getPoint().value!!.getPointActionsArray()
        }else{
            viewPointModel!!.getPoint().value!!.getPointActionsCancelArray()
        }
        //TODO is there already files
        showButtons(mainFragment, listOfActions)
    }

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

    var fileBefore : File? = null
    var fileAfter : File? = null
    var mLocationManager : LocationManager? = null
    var currentFile: File? = null
    var currentFileOrder: PhotoOrder = PhotoOrder.DONT_SET

    val reqSetting = LocationRequest.create().apply {
        fastestInterval = 10000
        interval = 10000
        priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        smallestDisplacement = 1.0f
    }

    val locationUpdates = object : LocationCallback() {
        override fun onLocationResult(lr: LocationResult) {
            try {
                val exifInterface = androidx.exifinterface.media.ExifInterface(currentFile!!.absoluteFile)
                val location = lr.locations.last()
                exifInterface.setGpsInfo(location)
                exifInterface.saveAttributes()
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

    private fun takePicture(){
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(requireActivity(),arrayOf(Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION), 101);
            // TODO: Обработка результата запроса разрешения
        }
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also {
            takePictureIntent -> takePictureIntent.resolveActivity(requireActivity().packageManager)?.also {
            val pictureFile = createFile()
            pictureFile?.also {
                // val pictureUri = it.toURI()
                val pictureUri: Uri = FileProvider.getUriForFile(
                    requireContext(),
                    "com.example.ekotransservice_routemanager.fileprovider",
                    it
                )
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,pictureUri)
                startActivityForResult(takePictureIntent,1)
            }
        }
        }
    }

    private fun createFile() : File?{
        val timeCreated = SimpleDateFormat("yyyyMMddHHmmss").format(Date())
        val storage = requireActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        try {
            currentFile = File.createTempFile("${point!!.getLineUID()}_($timeCreated)_${currentFileOrder.string}",".jpg",storage)
            //    .apply { currentFilePath = absolutePath }
            return currentFile
        }catch (e : Exception){
            Toast.makeText(requireContext(),"Неудалось записать файл",Toast.LENGTH_LONG).show()
            //TODO create exception behavior
        }

        return null
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if(!setGeoTag(currentFile!!)){
                currentFile = null
            }else{
                this.doneTakePhotoBefore.visibility = View.VISIBLE
            }
        }

    }

    @SuppressLint("MissingPermission")
    private fun setGeoTag(file : File) : Boolean {
        fusedLocationClient?.requestLocationUpdates(reqSetting,
            locationUpdates,
            null /* Looper */)
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

    override fun onPause() {
        super.onPause()
        fusedLocationClient?.removeLocationUpdates(locationUpdates)
    }
}

