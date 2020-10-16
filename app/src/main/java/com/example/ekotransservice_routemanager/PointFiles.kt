package com.example.ekotransservice_routemanager

import android.content.Context
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ekotransservice_routemanager.DataClasses.Point
import com.example.ekotransservice_routemanager.DataClasses.PointFile

class PointFiles : Fragment() {

    var point : Point? = null

    companion object {
        fun newInstance() = PointFiles()
    }

    private lateinit var viewModel: PointFilesViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.point_files_fragment, container, false)
        val recycleView : RecyclerView = view.findViewById(R.id.recyclerview)
        val graphicPoint = android.graphics.Point()
        (requireActivity().getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay.getSize(graphicPoint)


        val countOfImages = (graphicPoint.x / 400).toInt()
        recycleView.layoutManager = GridLayoutManager(requireContext(),countOfImages)
        val adapter = PointFilesAdapter(view.context)
        recycleView.adapter = adapter

        (requireActivity() as MainActivity).mSwipeRefreshLayout!!.touchscreenBlocksFocus = true
        (requireActivity() as MainActivity).mSwipeRefreshLayout!!.isRefreshing = true

        val observer = Observer<MutableList<PointFile>> {
                (pointFile) -> (recycleView.adapter as PointFilesAdapter).setList(viewModel!!.getList())
        }

        viewModel.files.removeObservers(requireActivity())
        viewModel.files.observe(requireActivity(),observer)

        viewModel.loadDataFromDB()
        view.findViewById<TextView>(R.id.pointNameText).text = point!!.getAddressName()
        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        //viewModel = ViewModelProvider(this).get(PointFilesViewModel::class.java)
        // TODO: Use the ViewModel
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            point = it.getSerializable("point") as Point

            viewModel = PointFilesViewModel(requireActivity() as MainActivity, point!!)
        }
    }
}