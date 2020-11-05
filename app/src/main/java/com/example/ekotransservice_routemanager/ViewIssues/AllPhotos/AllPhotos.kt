package com.example.ekotransservice_routemanager.ViewIssues.AllPhotos

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
import com.example.ekotransservice_routemanager.ViewIssues.AnimateView

class AllPhotos : Fragment() {

    var sendFilesView : View? = null
    var sendFilesShow : Boolean = false

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

        sendFilesView = view.findViewById(R.id.bottomSender)
        sendFilesShow = false

        val selectObserver = Observer<Boolean> {
            showHideFileSend(it)
        }

        (recycleView.adapter as AllPhotosAdapter).viewModelIsSelected.hasSelected
            .observe(requireActivity(),selectObserver)

        sendFilesView!!.setOnClickListener((recycleView.adapter as AllPhotosAdapter)
            .viewModelIsSelected.getOnClickListener())
        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        //viewModel = AllPhotosViewModel(requireActivity() as MainActivity)
        // TODO: Use the ViewModel

    }

    private fun showHideFileSend (show : Boolean){
        val animator =  AnimateView(sendFilesView!!,context as MainActivity,true)
        if(show && !sendFilesShow){
            animator.showHeight()
            sendFilesShow = true

        }else if(!show && sendFilesShow){
            animator.hideHeight()
            sendFilesShow = false
        }
    }

}