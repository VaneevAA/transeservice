package com.example.ekotransservice_routemanager.ViewIssues.RouteList

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.example.ekotransservice_routemanager.MainActivity
import com.example.ekotransservice_routemanager.R
import com.google.android.material.transition.Hold
import com.google.android.material.transition.MaterialElevationScale
import com.example.ekotransservice_routemanager.DataClasses.Point as Point

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
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

        (recycleView!!.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = true
        val adapter = PointListAdapter(view.context)
        recycleView!!.adapter = adapter
        recycleView!!.layoutManager = LinearLayoutManager(view.context)

        (requireActivity() as MainActivity).mSwipeRefreshLayout!!.touchscreenBlocksFocus = true
        (requireActivity() as MainActivity).mSwipeRefreshLayout!!.isRefreshing = true
        setNewViewModel()
        (requireActivity() as MainActivity).mSwipeRefreshLayout!!.isRefreshing = false
        return view
    }

    override fun onResume() {
        super.onResume()
        if((requireActivity() as MainActivity).refreshPointList){
            mViewList = ViewPointList(requireActivity().application,
                requireActivity() as MainActivity
            )
            setNewViewModel()
            (requireActivity() as MainActivity).refreshPointList = false
        }
    }

    private fun setNewViewModel(){
        mViewList = ViewModelProvider(this.requireActivity(),
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
        val observer = Observer<MutableList<Point>> {
                (pointList) -> ((recycleView?.adapter as PointListAdapter)
            .setList(mViewList!!.getList()))
        }
        mViewList!!.getList().removeObservers(requireActivity())
        mViewList!!.getList().observe(requireActivity(), observer)

    }
}