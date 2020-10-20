package com.example.ekotransservice_routemanager.ViewIssues.AllPhotos

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ekotransservice_routemanager.DataClasses.Point
import com.example.ekotransservice_routemanager.MainActivity
import com.example.ekotransservice_routemanager.R
import com.example.ekotransservice_routemanager.ViewIssues.RouteList.PointListAdapter

class AllPhotos : Fragment() {

    companion object {
        fun newInstance() = AllPhotos()
    }

    private lateinit var viewModel: AllPhotosViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view : View = inflater.inflate(R.layout.all_photos_fragment, container, false)
        val recycleView : RecyclerView = view.findViewById(R.id.recyclerviewAllFiles)
        viewModel = AllPhotosViewModel(requireActivity() as MainActivity)
        val adapter = AllPhotosAdapter(view.context,requireActivity() as MainActivity,this)
        recycleView.adapter = adapter
        recycleView.layoutManager = LinearLayoutManager(view.context)

        val observer = Observer<MutableList<Point>> {
                (pointList) -> (recycleView.adapter as AllPhotosAdapter).setList(viewModel!!.getList())
        }
        viewModel.allPoints.removeObservers(requireActivity())
        viewModel.allPoints.observe(requireActivity(),observer)
        viewModel.loadDataFromDB()
        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        //viewModel = AllPhotosViewModel(requireActivity() as MainActivity)
        // TODO: Use the ViewModel
    }

}