package com.example.ekotransservice_routemanager

import android.animation.Animator
import android.animation.StateListAnimator
import android.animation.ValueAnimator
import android.graphics.drawable.Drawable
import android.opengl.Visibility
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.RotateAnimation
import androidx.core.animation.addListener

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

        closeView.setOnClickListener {
            showHideCloseRoute(mainView)
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

}



