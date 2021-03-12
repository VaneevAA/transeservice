/*
 * Copyright 2020 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.ekotransservice_routemanager.camera

import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import com.example.ekotransservice_routemanager.R
import com.bumptech.glide.Glide
import com.example.ekotransservice_routemanager.DataClasses.PhotoOrder
import com.example.ekotransservice_routemanager.MainActivity
import com.example.ekotransservice_routemanager.ViewIssues.PointAction.ViewPointAction
import com.example.ekotransservice_routemanager.utils.ImageFileProcessing
import com.muslimcompanion.utills.GPSTracker
import kotlinx.android.synthetic.main.fragment_photo_priview.*
import java.io.File
import java.util.*

class PhotoFragment : Fragment() {


    /*override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?) = ImageView(context)*/

    var currentFile: File? = null
    lateinit var viewPointModel: ViewPointAction
    private lateinit var point: com.example.ekotransservice_routemanager.DataClasses.Point
    private var gps: GPSTracker? = null
    private var canDone: Boolean = true
    private var location: Location? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_photo_priview, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val resource = currentFile ?: R.drawable.ic_photo
        val imageView = view.findViewById<ImageView>(R.id.photoPreview)
        Log.i("PhotoPriview onViewCreated","current file: ${currentFile?.absolutePath}")
        var location: Location? = null
        if(gps!!.canGetLocation()){
            location = gps!!.location
        }else
        {
            gps!!.showSettingsAlert()
        }
        if (location == null) {
            viewPointModel!!.geoIsRequired = true
        }
        if (currentFile!=null && location!=null){
            ImageFileProcessing.createResultImageFile(currentFile!!.absolutePath,location!!.latitude,location!!.longitude,point,requireContext())
        }
        Glide.with(requireContext()).load(resource).into(imageView as ImageView)
        view.findViewById<TextView>(R.id.tv_confirm).setOnClickListener {
            if (location == null) {
                Toast.makeText(
                    activity,
                    "Предупреждение, местоположение не определено",
                    Toast.LENGTH_LONG
                ).show()

            }else{
                processImageFile(location!!)
            }
            Navigation.findNavController(requireActivity(), R.id.my_nav_host_fragment)
                .navigate(PhotoFragmentDirections.actionPreviewToPointAction(point,canDone))
        }

        view.findViewById<TextView>(R.id.tv_cancel).setOnClickListener {
            if (currentFile!=null){
                currentFile!!.delete()
            }
            Navigation.findNavController(requireActivity(), R.id.my_nav_host_fragment)
                .popBackStack()

        }

        (requireActivity() as MainActivity).supportActionBar?.hide()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // get the same viewModel as point_action fragment
        val args = arguments ?: return
        currentFile = args.getString(FILE_NAME_KEY)?.let { File(it) }
        Log.i("PhotoPriview onCreate","current file: ${currentFile?.absolutePath}")
        point = args.getSerializable("point") as com.example.ekotransservice_routemanager.DataClasses.Point
        canDone = args.getBoolean("canDone")
        gps = GPSTracker(requireContext())

        viewPointModel = ViewModelProvider(
            this.requireActivity(),
            ViewPointAction.ViewPointsFactory(
                this.requireActivity().application,
                requireActivity() as MainActivity,
                point!!
            )
        ).get(ViewPointAction::class.java)

    }

    override fun onDestroy() {
        super.onDestroy()
        gps!!.stopUsingGPS()
    }

    private fun processImageFile(location: Location){
        if (currentFile != null) {
            ImageFileProcessing.setGeoTag(location,currentFile!!.absolutePath)
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
        }
    }

    companion object {
        private const val FILE_NAME_KEY = "file_name"

        fun create(image: File) = PhotoFragment().apply {
            arguments = Bundle().apply {
                putString(FILE_NAME_KEY, image.absolutePath)
            }
        }
    }
}