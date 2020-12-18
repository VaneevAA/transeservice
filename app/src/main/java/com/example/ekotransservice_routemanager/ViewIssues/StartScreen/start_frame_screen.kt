package com.example.ekotransservice_routemanager.ViewIssues.StartScreen

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageInstaller
import android.content.pm.PackageInstaller.SessionParams
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.*
import androidx.lifecycle.Observer
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.findNavController
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import com.example.ekotransservice_routemanager.BuildConfig
import com.example.ekotransservice_routemanager.DataClasses.Route
import com.example.ekotransservice_routemanager.DataClasses.Vehicle
import com.example.ekotransservice_routemanager.MainActivity
import com.example.ekotransservice_routemanager.ViewIssues.AnimateView
import com.google.android.material.transition.Hold
import com.google.android.material.transition.MaterialContainerTransform
import kotlinx.android.synthetic.main.start_frame_screen_fragment.*
import kotlinx.android.synthetic.main.start_frame_screen_fragment.view.*
import kotlinx.coroutines.*
import java.io.File
import java.io.FileInputStream
import java.io.OutputStream
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*

class start_frame_screen : Fragment() {
    var closedRoute : Boolean = false

    companion object {
        fun newInstance() = start_frame_screen()
    }

    private lateinit var viewScreen: StartFrameScreenViewModel

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        //Объявление основных значений
        val mainView = inflater.inflate(
            com.example.ekotransservice_routemanager.R.layout.start_frame_screen_fragment,
            container,
            false
        )
        val closeView : View = mainView.findViewById(com.example.ekotransservice_routemanager.R.id.layoutToCloseRoute)
        val vehicleView: View = mainView.findViewById(com.example.ekotransservice_routemanager.R.id.vehicleLayout)
        val imageButton : ImageButton = mainView.findViewById(com.example.ekotransservice_routemanager.R.id.imageButton)
        viewScreen = ViewModelProvider(
            this.requireActivity(),
            StartFrameScreenViewModel.StartFrameScreenModelFactory(requireActivity() as MainActivity)
        )
            .get(StartFrameScreenViewModel::class.java)

        //Получение машины
        //vehicleUpdate(viewScreen.vehicle.value,mainView)
        //Отслеживание изменения маршрута
        viewScreen.routeLiveData.removeObservers(requireActivity())
        viewScreen.routeLiveData.observe(requireActivity(), Observer {
            routeUpdate(it, mainView)
        })

        //Отслеживание ошибок
        viewScreen.errorLiveData.removeObservers(requireActivity())
        viewScreen.errorLiveData.observe(requireActivity(), Observer {
            if (it) {
                (requireActivity() as MainActivity).errorCheck(viewScreen.routeRepository)
                viewScreen.errorLiveData.value = false
            }

        })
        //Отслеживание изменения машины
        viewScreen.vehicle.removeObservers(requireActivity())
        viewScreen.vehicle.observe(requireActivity(), Observer {
            vehicleUpdate(it, mainView)
        })


        //Событие обновления
        (requireActivity() as MainActivity).mSwipeRefreshLayout!!.setOnRefreshListener {

            viewScreen.onRefresh()

        }

        viewScreen.onRefresh()

        //Сворачивание/разворачивание маршрута
        closeView.setOnClickListener {
            showHideCloseRoute(mainView)
        }

        //Установка машины
        vehicleView.setOnClickListener{
            showVehiclePrefernces(mainView, it)
        }

        //Кнопка обновления маршрута
        imageButton.setOnClickListener {
            viewScreen.onRefresh(true)
        }

        mainView.findViewById<View>(com.example.ekotransservice_routemanager.R.id.routeInfo).setOnClickListener {
            if(viewScreen.routeLiveData.value != null){
                findNavController().navigate(com.example.ekotransservice_routemanager.R.id.action_start_frame_screen_to_route_list)
            }

        }

        mainView.finishRoute.setOnClickListener {
            //viewScreen.finishRoute()
            (activity as MainActivity).endOfTheRoute(viewScreen)
        }

        mainView.findViewById<View>(com.example.ekotransservice_routemanager.R.id.photoLayout).setOnClickListener {
            if(viewScreen.routeLiveData.value != null){
                findNavController().navigate(com.example.ekotransservice_routemanager.R.id.action_start_frame_screen_to_allPhotos)
            }
        }

        //всё сворачиваем для старта
        showHideRouteLiveData(viewScreen.routeLiveData.value, false, mainView)
        closedRoute = false
        showHideCloseRoute(mainView)

        return mainView
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

    }

    private fun showHideCloseRoute(mainView: View){
        val toCloseView = mainView.findViewById<View>(com.example.ekotransservice_routemanager.R.id.closeLayout)
        if(!closedRoute){
            val animateView = this.context?.let { it1 -> AnimateView(toCloseView, it1, true) }
            animateView!!.hideHeight()
        }else{
            val animateView = this.context?.let { it1 -> AnimateView(toCloseView, it1, true) }
            animateView!!.showHeight()
        }


        val imageRoutate = mainView.findViewById<View>(com.example.ekotransservice_routemanager.R.id.imageOpenCloseRoute)

        if(!closedRoute){
            val animateView = this.context?.let { it1 -> AnimateView(imageRoutate, it1, true) }
            animateView!!.rotate()
        }else{
            val animateView = this.context?.let { it1 -> AnimateView(imageRoutate, it1, true) }
            animateView!!.rotateBack()
        }

        closedRoute = !closedRoute
    }

    private fun showVehiclePrefernces(mainView: View, view: View) {
        val extra = FragmentNavigatorExtras(view to "vehicle")
        mainView.findNavController().navigate(
            com.example.ekotransservice_routemanager.R.id.action_start_frame_screen_to_vehicle_screen,
            null,
            null,
            extra
        )
    }

    /*private fun getCurrentRoute(){

        val routeRepository = RouteRepository(requireActivity().application)


        GlobalScope.launch {

            currentRoute =
                withContext(Dispatchers.Default) { routeRepository.getCurrentRoute() }
            (requireActivity() as MainActivity).mSwipeRefreshLayout!!.isRefreshing = false
        }



    }*/

    /*private fun showHideRoute (animate : Boolean, mainView : View){
        val routeGroup  = mainView.findViewById<View>(R.id.routeGroup)
        val imageButton : ImageButton = mainView.findViewById(R.id.imageButton)
        val atAllCount : TextView = mainView.findViewById(R.id.atAllCount)
        val doneCount : TextView = mainView.findViewById(R.id.doneCount)

        if(currentRoute == null){
            val animation = AnimateView(routeGroup,requireContext(),animate)
            animation.hideHeight()
            imageButton.setImageResource(R.drawable.ic_baseline_add_24)
            atAllCount!!.text = "0"
            doneCount!!.text = "0"

        }else{
            val animation = AnimateView(routeGroup,requireContext(),animate)
            animation.showHeight()
            imageButton.setImageResource( R.drawable.ic_baseline_replay_24)
            atAllCount!!.text = currentRoute!!.getCountPoint().toString()
            doneCount!!.text = currentRoute!!.getCountPointDone().toString()

        }
    }*/

    @SuppressLint("SimpleDateFormat")
    private fun showHideRouteLiveData(route: Route?, animate: Boolean, mainView: View){
        val routeGroup  = mainView.findViewById<View>(com.example.ekotransservice_routemanager.R.id.routeGroup)
        val imageButton : ImageButton = mainView.findViewById(com.example.ekotransservice_routemanager.R.id.imageButton)
        val atAllCount : TextView = mainView.findViewById(com.example.ekotransservice_routemanager.R.id.atAllCount)
        val doneCount : TextView = mainView.findViewById(com.example.ekotransservice_routemanager.R.id.doneCount)
        val dateView = mainView.findViewById<TextView>(com.example.ekotransservice_routemanager.R.id.dateOfRoute)


        if(route == null){
            val animation = AnimateView(routeGroup, requireContext(), animate)
            animation.hideHeight()
            imageButton.setImageResource(com.example.ekotransservice_routemanager.R.drawable.ic_baseline_add_24)
            atAllCount.text = "0"
            doneCount.text = "0"
            dateView.text = SimpleDateFormat("dd.MM.yyyy").format(Date())
        }else{
            val animation = AnimateView(routeGroup, requireContext(), animate)
            animation.showHeight()
            imageButton.setImageResource(com.example.ekotransservice_routemanager.R.drawable.ic_baseline_replay_24)
            atAllCount.text = route.getCountPoint().toString()
            doneCount.text = route.getCountPointDone().toString()
            dateView.text = SimpleDateFormat("dd.MM.yyyy").format(route.getRouteDate())
        }
    }

    private fun routeUpdate(route: Route?, mainView: View){
        showHideRouteLiveData(route, true, mainView)

    }

    private fun vehicleUpdate(vehicle: Vehicle?, mainView: View){
        val vehicleView = mainView.findViewById<TextView>(com.example.ekotransservice_routemanager.R.id.vehicleNumber)
        if(vehicle == null){
            vehicleView.text = ""
        }else{
            vehicleView.text = vehicle.getNumber()
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedElementEnterTransition = MaterialContainerTransform()
        exitTransition = Hold()
    }

    private fun downloadAppFile(){
        viewScreen.loadApk()
    }

    private fun openApkFile(){
        val intent = Intent(Intent.ACTION_VIEW)
        if (viewScreen.fileApk.value!!.length() == 0L) {
            return
        }
        val uri = FileProvider.getUriForFile(
            requireContext(), BuildConfig.APPLICATION_ID + ".fileprovider",
            viewScreen.fileApk.value!!
        )
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        intent.setDataAndType(uri, "application/vnd.android.package-archive")
        intent.putExtra(Intent.EXTRA_NOT_UNKNOWN_SOURCE, true)

        startActivity(intent)
    }

}



