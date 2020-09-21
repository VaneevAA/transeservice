package com.example.ekotransservice_routemanager

import android.Manifest
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Criteria
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
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
import androidx.core.view.children
import androidx.lifecycle.Observer
import com.example.ekotransservice_routemanager.DataClasses.Point
import com.example.ekotransservice_routemanager.DataClasses.PointActoins
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            point  = it.getSerializable("point") as Point
            canDone = it.getBoolean("canDone")
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
            if(fileBefore == null){
                takePicture(true)
            }else{
                Toast.makeText(requireContext(),"Фото уже есть",Toast.LENGTH_LONG)
            }
        }
        mainFragment.findViewById<Button>(R.id.takePhotoAfter).setOnClickListener {
            if(fileBefore != null && point!!.getContCount() != 0){
                Toast.makeText(requireContext(),"Предыдущие действия не выполнены",Toast.LENGTH_LONG)
            }else if(fileAfter == null){
                takePicture(true)
            }else{
                Toast.makeText(requireContext(),"Фото уже есть",Toast.LENGTH_LONG)
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
    var mLocation : Location? = null
    var mLocationManager : LocationManager? = null
    private val mLocationListener = object : LocationListener {
        override fun onLocationChanged(location: Location?) {
            if(location!!.latitude != 0.0 && location!!.longitude != 0.0){
                mLocation = location
                mLocationManager!!.removeUpdates(this)


            }
        }

        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {

        }

        override fun onProviderEnabled(provider: String?) {

        }

        override fun onProviderDisabled(provider: String?) {

        }

    }

    private fun takePicture(before : Boolean){
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also {
            takePictureIntent -> takePictureIntent.resolveActivity(requireActivity().packageManager)?.also {
            val pictureFile = createFile(before)
            pictureFile?.also {
                val pictureUri = it.toURI()
                    //FileProvider.getUriForFile(this.requireContext(),
                    //"com.example.android.fileprovider",
                    //it)
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,pictureUri)
                startActivityForResult(takePictureIntent,if (before){
                    1
                }else{
                    2
                })
            }
        }
        }
    }

    private fun createFile(before : Boolean) : File?{
        val timeCreated = SimpleDateFormat("yyyyMMddHHmmss").format(Date())
        val storage = requireActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val postfix = if(before){
            "before"
        }else{
            "after"
        }
        try {
            return if(before){
                fileBefore = File.createTempFile("${point!!.getLineUID()}_($timeCreated)_$postfix",".jpg",storage)
                fileBefore
            }else{
                fileAfter = File.createTempFile("${point!!.getLineUID()}_($timeCreated)_$postfix",".jpg",storage)
                fileAfter
            }

        }catch (e : Exception){
            Toast.makeText(requireContext(),"Неудалось записать файл",Toast.LENGTH_LONG)
            //TODO create exception behavior
        }

        return null
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == 1){
            val criteria = Criteria()
            val provider = mLocationManager!!.getBestProvider(criteria,true)

            if (ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                if(requestCode == 1){
                    fileBefore = null
                }else{
                    fileAfter = null
                }
                return
            }
            mLocationManager = this.requireActivity().getSystemService(Context.LOCATION_SERVICE) as LocationManager
            mLocationManager!!.requestLocationUpdates(provider,1000,0.0f,mLocationListener)

            if(mLocation != null && mLocation!!.longitude != 0.0 && mLocation!!.latitude != 0.0){
                if(requestCode == 1){
                    if(!setGeoTag(fileBefore!!)){
                        fileBefore = null
                    }else{
                       this.doneTakePhotoBefore.visibility = View.VISIBLE
                    }
                }else{
                    if(!setGeoTag(fileAfter!!)){
                        fileAfter = null
                    }else{
                        this.doneTakePhotoAfter.visibility = View.VISIBLE
                    }
                }
            }
        }


    }



    private fun setGeoTag(file : File) : Boolean{
        try {
            val exifInterface = androidx.exifinterface.media.ExifInterface(file.absoluteFile)

            exifInterface.setAttribute(androidx.exifinterface.media.ExifInterface.TAG_GPS_LATITUDE,
                Location.convert(mLocation!!.latitude,Location.FORMAT_SECONDS))
            exifInterface.setAttribute(androidx.exifinterface.media.ExifInterface.TAG_GPS_LONGITUDE,
                Location.convert(mLocation!!.longitude,Location.FORMAT_SECONDS))
            exifInterface.setAttribute(androidx.exifinterface.media.ExifInterface.TAG_GPS_LATITUDE_REF,
                if(mLocation!!.latitude > 0.0){
                    "N"
                }else{
                    "S"
                })
            exifInterface.setAttribute(androidx.exifinterface.media.ExifInterface.TAG_GPS_LONGITUDE_REF,
                if(mLocation!!.longitude > 0.0){
                    "E"
                }else{
                    "W"
                })

            exifInterface.saveAttributes()
        }catch (e : java.lang.Exception){
            Toast.makeText(requireContext(),"Присвоить координаты не получилось",Toast.LENGTH_SHORT)
            //TODO create exception behavior
            return false
        }
        return true
    }
}

