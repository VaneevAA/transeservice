package com.example.ekotransservice_routemanager.ViewIssues.StartScreen

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.*
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.example.ekotransservice_routemanager.AnimateView
import com.example.ekotransservice_routemanager.DataClasses.Route
import com.example.ekotransservice_routemanager.DataClasses.Vehicle
import com.example.ekotransservice_routemanager.MainActivity
import com.example.ekotransservice_routemanager.R
import java.text.SimpleDateFormat
import java.util.*

class start_frame_screen : Fragment() {
    var closedRoute : Boolean = false

    companion object {
        fun newInstance() = start_frame_screen()
    }

    private lateinit var viewScreen: StartFrameScreenViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        //Объявление основных значений
        val mainView = inflater.inflate(R.layout.start_frame_screen_fragment, container, false)
        val closeView : View = mainView.findViewById(R.id.layoutToCloseRoute)
        val vehicleView: View = mainView.findViewById(R.id.vehicleLayout)
        val imageButton : ImageButton = mainView.findViewById(R.id.imageButton)
        viewScreen = ViewModelProvider(this.requireActivity(),
            StartFrameScreenViewModel.StartFrameScreenModelFactory(requireActivity() as MainActivity)
        )
            .get(StartFrameScreenViewModel::class.java)

        //Получение машины
        //vehicleUpdate(viewScreen.vehicle.value,mainView)
        //Отслеживание изменения маршрута
        viewScreen.routeLiveData.removeObservers(requireActivity())
        viewScreen.routeLiveData.observe(requireActivity(), Observer {
            routeUpdate(it,mainView)
        })
        //Отслеживание изменения машины
        viewScreen.vehicle.removeObservers(requireActivity())
        viewScreen.vehicle.observe(requireActivity(), Observer {
            vehicleUpdate(it,mainView)
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
            showVehiclePrefernces(mainView)
        }

        //Кнопка обновления маршрута
        imageButton.setOnClickListener {
            viewScreen.onRefresh(true)
        }

        mainView.findViewById<View>(R.id.routeInfo).setOnClickListener {
            if(viewScreen.routeLiveData.value != null){
                findNavController().navigate(R.id.route_list)
            }

        }

        //всё сворачиваем для старта
        showHideRouteLiveData(viewScreen.routeLiveData.value,false,mainView)
        closedRoute = false
        showHideCloseRoute(mainView)

        return mainView
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

    }

    private fun showHideCloseRoute(mainView : View){
        val toCloseView = mainView.findViewById<View>(R.id.closeLayout)
        if(!closedRoute){
            val animateView = this.context?.let { it1 -> AnimateView(toCloseView, it1,true) }
            animateView!!.hideHeight()
        }else{
            val animateView = this.context?.let { it1 -> AnimateView(toCloseView, it1,true) }
            animateView!!.showHeight()
        }


        val imageRoutate = mainView.findViewById<View>(R.id.imageOpenCloseRoute)

        if(!closedRoute){
            val animateView = this.context?.let { it1 -> AnimateView(imageRoutate, it1,true) }
            animateView!!.rotate()
        }else{
            val animateView = this.context?.let { it1 -> AnimateView(imageRoutate, it1,true) }
            animateView!!.rotateBack()
        }

        closedRoute = !closedRoute
    }

    private fun showVehiclePrefernces(mainView: View) {
       mainView.findNavController().navigate(R.id.vehicle_screen)
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

    private fun showHideRouteLiveData (route: Route?, animate : Boolean, mainView : View){
        val routeGroup  = mainView.findViewById<View>(R.id.routeGroup)
        val imageButton : ImageButton = mainView.findViewById(R.id.imageButton)
        val atAllCount : TextView = mainView.findViewById(R.id.atAllCount)
        val doneCount : TextView = mainView.findViewById(R.id.doneCount)
        val dateView = mainView.findViewById<TextView>(R.id.dateOfRoute)


        if(route == null){
            val animation = AnimateView(routeGroup,requireContext(),animate)
            animation.hideHeight()
            imageButton.setImageResource(R.drawable.ic_baseline_add_24)
            atAllCount!!.text = "0"
            doneCount!!.text = "0"
            dateView.text = SimpleDateFormat("dd.MM.yyyy").format(Date())
        }else{
            val animation = AnimateView(routeGroup,requireContext(),animate)
            animation.showHeight()
            imageButton.setImageResource(R.drawable.ic_baseline_replay_24)
            atAllCount!!.text = route!!.getCountPoint().toString()
            doneCount!!.text = route!!.getCountPointDone().toString()
            dateView.text = SimpleDateFormat("dd.MM.yyyy").format(route.getRouteDate())
        }
    }

    private fun routeUpdate(route : Route?,mainView: View){
        showHideRouteLiveData(route,true,mainView)

    }

    private fun vehicleUpdate (vehicle : Vehicle?,mainView: View){
        val vehicleView = mainView.findViewById<TextView>(R.id.vehicleNumber)
        if(vehicle == null){
            vehicleView.text = ""
        }else{
            vehicleView.text = vehicle!!.getNumber()
        }

    }
}



