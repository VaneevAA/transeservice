package com.example.ekotransservice_routemanager.ViewIssues.RouteList

import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.SwitchCompat
import androidx.core.os.bundleOf
import androidx.core.view.children
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.example.ekotransservice_routemanager.DataClasses.Point
import com.example.ekotransservice_routemanager.MainActivity
import com.example.ekotransservice_routemanager.R
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.transition.MaterialElevationScale

private const val ARG_PARAM1 = "point"
private const val ARG_PARAM2 = "canDone"

/**
 * A simple [Fragment] subclass.
 * Use the [route_list.newInstance] factory method to
 * create an instance of this fragment.
 */
class route_list : Fragment() {
    private var mViewList : ViewPointList? = null
    private var recycleView : RecyclerView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        exitTransition = MaterialElevationScale(false).setDuration(1000L)
        reenterTransition = MaterialElevationScale(true).setDuration(1000L)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        val view : View = inflater.inflate(R.layout.fragment_route_list, container, false)
        recycleView = view.findViewById(R.id.recyclerview)

        //bottom sheet
        val bts = view.findViewById<View>(R.id.bottomSheetRoute)
        val standartBehavior = BottomSheetBehavior.from(bts)
        val currentPointNameText : TextView = bts.findViewById(R.id.currentPointNameText)


        (recycleView!!.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = true
        val adapter = PointListAdapter(view.context)
        recycleView!!.adapter = adapter
        recycleView!!.layoutManager = LinearLayoutManager(view.context)
        setNewViewModel()
        (requireActivity() as MainActivity).mSwipeRefreshLayout!!.touchscreenBlocksFocus = true
        (requireActivity() as MainActivity).mSwipeRefreshLayout!!.isRefreshing = true

        (requireActivity() as MainActivity).mSwipeRefreshLayout!!.isRefreshing = false

        //current point observe

        (recycleView!!.adapter as PointListAdapter).mCurrentPointViewModel
            .currentPoint.removeObservers(viewLifecycleOwner)
        (recycleView!!.adapter as PointListAdapter).mCurrentPointViewModel
            .bottomSheetOpen.removeObservers(viewLifecycleOwner)

        val currentPointObserver = Observer<Point> {
            if(it != null){
                currentPointNameText.text = it.getAddressName()
                setButtonVisibility(it,bts.findViewById(R.id.canDoneLayout))
                view.refreshDrawableState()
           // }else{
           //     currentPointNameText.text = ""
            }

        }
        (recycleView!!.adapter as PointListAdapter).mCurrentPointViewModel
            .currentPoint.observe(viewLifecycleOwner, currentPointObserver)

        val curPoint = mViewList!!.getList().value?.get(0)
        if (curPoint != null) {
            (recycleView!!.adapter as PointListAdapter).mCurrentPointViewModel.setCurrentPoint(
                curPoint
            )
        }

        val openCloseBottomSheetObserver = Observer<Boolean> {
            if(it){
                standartBehavior.state = BottomSheetBehavior.STATE_EXPANDED
            } else {
                standartBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            }
        }
        (recycleView!!.adapter as PointListAdapter).mCurrentPointViewModel
            .bottomSheetOpen.observe(viewLifecycleOwner, openCloseBottomSheetObserver)

        //bottom sheet click listeners
        val pointDone = bts.findViewById<ImageButton>(R.id.canDoneImageButton)
        pointDone.setOnClickListener {
            if((recycleView!!.adapter as PointListAdapter).mCurrentPointViewModel
                    .currentPoint.value!!.getPolygon()){
                (recycleView!!.adapter as PointListAdapter).mCurrentPointViewModel
                    .currentPoint.value!!.setDone(true)
                mViewList!!.routeRepository.updatePointAsync(
                    (recycleView!!.adapter as PointListAdapter).mCurrentPointViewModel
                    .currentPoint.value!!
                )
                mViewList!!.loadDataFromDB()
            }else{
                val bundle = bundleOf(
                    "point" to (recycleView!!.adapter as PointListAdapter)
                        .mCurrentPointViewModel.currentPoint.value, "canDone" to true
                )
                view.findNavController()
                    .navigate(R.id.action_route_list_to_point_action, bundle)
                standartBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            }

        }

        val pointCantDone = bts.findViewById<ImageButton>(R.id.cannotDoneImageButton)
        pointCantDone.setOnClickListener {
            val bundle = bundleOf(
                "point" to (recycleView!!.adapter as PointListAdapter)
                    .mCurrentPointViewModel.currentPoint.value, "canDone" to false
            )
            view.findNavController()
                .navigate(R.id.action_route_list_to_point_action, bundle)
            standartBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        }

        val photoPoint = bts.findViewById<ImageButton>(R.id.pointPhotos)
        photoPoint.setOnClickListener {
            val bundle = bundleOf(
                "point" to (recycleView!!.adapter as PointListAdapter)
                    .mCurrentPointViewModel.currentPoint.value
            )
            view.findNavController()
                .navigate(R.id.action_route_list_to_pointFiles, bundle)
            standartBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        }

        val home = bts.findViewById<ImageButton>(R.id.home)
        home.setOnClickListener {
            view.findNavController()
                .navigate(R.id.start_frame_screen)
            standartBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        }

        val route = bts.findViewById<ImageButton>(R.id.routeImageButton)
        route.setOnClickListener {
            val curPoint = (recycleView!!.adapter as PointListAdapter).mCurrentPointViewModel.currentPoint.value
            if (curPoint!=null) {
                buildRoute(curPoint,"2gis")
            }else{
                Toast.makeText(activity,"Точка не выбрана",Toast.LENGTH_LONG).show()
            }
        }

        val switch = bts.findViewById<SwitchCompat>(R.id.listSwitcher)
        switch.setOnCheckedChangeListener() { compoundButton: CompoundButton, b: Boolean ->
            mViewList!!.loadFullList = b
            mViewList!!.loadDataFromDB()
            standartBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        }

        val currentPointHeader = bts.findViewById<View>(R.id.upperComponentOfBottomSheet)
        currentPointHeader.setOnClickListener {
            (recycleView!!.adapter as PointListAdapter).mCurrentPointViewModel.setSheetOpposite()
        }


        return view
    }

    override fun onResume() {
        super.onResume()
        if((requireActivity() as MainActivity).refreshPointList){
            mViewList = ViewPointList(
                requireActivity().application,
                requireActivity() as MainActivity
            )
            setNewViewModel()
            (requireActivity() as MainActivity).refreshPointList = false
        }
    }

    private fun setNewViewModel(){
        mViewList = ViewModelProvider(
            this.requireActivity(),
            ViewPointList.ViewPointsFactory(
                this.requireActivity().application,
                requireActivity() as MainActivity
            )
        )
            .get(ViewPointList::class.java)
        /*if(mViewList!!.getList().value == null){
            activity?.onBackPressed()
            return
        }*/
        val observer = Observer<MutableList<Point>> { (pointList) -> ((recycleView?.adapter as PointListAdapter)
            .setList(mViewList!!.getList()))
        }
        mViewList!!.getList().removeObservers(requireActivity())
        mViewList!!.getList().observe(requireActivity(), observer)
        mViewList!!.loadDataFromDB()

    }

    private fun buildRoute(point: Point, naviApp: String) {
        //if (location != null && location!!.latitude != 0.0 && location!!.longitude != 0.0) {
        // val startlat = location!!.latitude
        // val startlon = location!!.longitude
        val endlat = point.getAddressLat()
        val endlon = point.getAddressLon()
        if (endlat == 0.0 || endlon == 0.0) {
            Toast.makeText(
                activity,
                "Отмена.Для данной точки не заданы координаты",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        var uriString = ""
        var packageName = ""

        when (naviApp) {
            "yandex" -> {
                uriString = "yandexnavi://build_route_on_map?lat_to=$endlat&lon_to=$endlon"
                packageName = "ru.yandex.yandexnavi"
            }
            "2gis" -> {
                uriString = "dgis://2gis.ru/routeSearch/rsType/car/to/$endlon,$endlat"
                packageName = "ru.dublgis.dgismobile"
            }
        }

        if (uriString.isEmpty() || packageName.isEmpty()) {
            Toast.makeText(activity,"Отмена. Неизвестное приложение навигации", Toast.LENGTH_LONG).show()
            return
        }

        val uri = Uri.parse(uriString)
        var intent = Intent(Intent.ACTION_VIEW, uri)
        intent.setPackage(packageName)
        val packageManager: PackageManager = requireContext().packageManager
        val activities: List<ResolveInfo> = packageManager.queryIntentActivities(intent, 0)
        val isIntentSafe: Boolean = activities.isNotEmpty()
        if (isIntentSafe) {
            startActivity(intent)
        } else {
            // Открываем страницу приложения Яндекс.Карты в Google Play.
            intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse("market://details?id=$packageName")
            startActivity(intent)
        }

        /*} else {
            Toast.makeText(
                requireContext(),
                "Текущее местоположение не определено",
                Toast.LENGTH_LONG
            ).show()
        }*/

    }

    private fun setButtonVisibility (point : Point, parentView : ViewGroup){
        for (child in parentView.children){
            when(child.id){
                R.id.cannotDoneImageButton -> child.visibility = if (point.getPolygon()){
                    ViewGroup.GONE
                }else{
                    ViewGroup.VISIBLE
                }
                R.id.pointPhotos -> child.visibility = if (point.getPolygon()){
                    ViewGroup.GONE
                }else{
                    ViewGroup.VISIBLE
                }
            }

        }
    }
}