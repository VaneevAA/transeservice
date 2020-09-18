package com.example.ekotransservice_routemanager

import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.findNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.android.synthetic.main.start_frame_screen_fragment.*
import kotlinx.android.synthetic.main.start_frame_screen_fragment.view.*

class start_frame_screen : Fragment() {
    var closedRoute : Boolean = false
    companion object {
        fun newInstance() = start_frame_screen()
    }

    private lateinit var viewModel: StartFrameScreenViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val mainView = inflater.inflate(R.layout.start_frame_screen_fragment, container, false)
        val closeView : View = mainView.findViewById<View>(R.id.layoutToCloseRoute)
        val vehicleView: View = mainView.findViewById(R.id.vehicleLayout)

        closeView.setOnClickListener {
            showHideCloseRoute(mainView)
        }

        vehicleView.setOnClickListener{
            showVehiclePrefernces(mainView)
        }

        showHideCloseRoute(mainView)
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
            val animateView = this.context?.let { it1 -> AnimateView(toCloseView, it1) }
            animateView!!.hideHeight()
        }else{
            val animateView = this.context?.let { it1 -> AnimateView(toCloseView, it1) }
            animateView!!.showHeight()
        }


        val imageRoutate = mainView.findViewById<View>(R.id.imageOpenCloseRoute)

        if(!closedRoute){
            val animateView = this.context?.let { it1 -> AnimateView(imageRoutate, it1) }
            animateView!!.rotate()
        }else{
            val animateView = this.context?.let { it1 -> AnimateView(imageRoutate, it1) }
            animateView!!.rotateBack()
        }

        closedRoute = !closedRoute
    }

    private fun showVehiclePrefernces(mainView: View) {
       mainView.findNavController().navigate(R.id.vehicle_screen)
    }

}



