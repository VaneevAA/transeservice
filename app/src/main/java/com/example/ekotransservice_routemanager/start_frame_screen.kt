package com.example.ekotransservice_routemanager

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.findNavController
import com.example.ekotransservice_routemanager.DataBaseInterface.RouteRepository
import com.example.ekotransservice_routemanager.DataClasses.Route
import kotlinx.android.synthetic.main.start_frame_screen_fragment.*
import kotlinx.coroutines.*
import java.util.*

class start_frame_screen : Fragment() {
    var closedRoute : Boolean = false
    var currentRoute : Route? = null
    companion object {
        fun newInstance() = start_frame_screen()
    }

    private lateinit var viewModel: StartFrameScreenViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val mainView = inflater.inflate(R.layout.start_frame_screen_fragment, container, false)
        val closeView : View = mainView.findViewById(R.id.layoutToCloseRoute)
        val vehicleView: View = mainView.findViewById(R.id.vehicleLayout)

        val imageButton : ImageButton = mainView.findViewById(R.id.imageButton)
        (requireActivity() as MainActivity).mSwipeRefreshLayout!!.setOnRefreshListener {
            getCurrentRoute()
            showHideRoute(true,mainView)

        }

        getCurrentRoute()
        showHideRoute(true,mainView)


        closeView.setOnClickListener {
            showHideCloseRoute(mainView)
        }

        vehicleView.setOnClickListener{
            showVehiclePrefernces(mainView)
        }

        imageButton.setOnClickListener {
            (requireActivity() as MainActivity).mSwipeRefreshLayout!!.isRefreshing = true
            getCurrentRoute()
            showHideRoute(true,mainView)

        }
        val animateView = this.context?.let { it1 -> AnimateView(mainView.findViewById<View>(R.id.closeLayout), it1,false) }
        animateView!!.hideHeight()
        return mainView
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(StartFrameScreenViewModel::class.java)
        // TODO: Use the ViewModel
    }

    fun showHideCloseRoute(mainView : View){
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

    private fun getCurrentRoute(){

        val routeRepository = RouteRepository(requireActivity().application)


        GlobalScope.launch {
           //delay(2000)
           (requireActivity() as MainActivity).mSwipeRefreshLayout!!.isRefreshing = false
                currentRoute =
                withContext(Dispatchers.Default) { routeRepository.getCurrentRoute() }

        }



    }

    private fun showHideRoute (animate : Boolean, mainView : View){
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
    }

}



