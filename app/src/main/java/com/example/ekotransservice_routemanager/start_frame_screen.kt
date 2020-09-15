package com.example.ekotransservice_routemanager

import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

class start_frame_screen : Fragment() {

    companion object {
        fun newInstance() = start_frame_screen()
    }

    private lateinit var viewModel: StartFrameScreenViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.start_frame_screen_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(StartFrameScreenViewModel::class.java)
        // TODO: Use the ViewModel
    }

}