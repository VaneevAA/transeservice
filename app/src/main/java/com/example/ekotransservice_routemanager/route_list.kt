package com.example.ekotransservice_routemanager

import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import kotlinx.android.synthetic.main.fragment_route_list.*
import java.lang.Exception
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        val view : View = inflater.inflate(R.layout.fragment_route_list, container, false)
        val recycleView : RecyclerView = view.findViewById(R.id.recyclerview)
        (recycleView.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = true
        val adapter = PointListAdapter(view.context)
        recycleView.adapter = adapter
        recycleView.layoutManager = LinearLayoutManager(view.context)
        mViewList = ViewModelProvider(this.requireActivity(),ViewPointList.ViewPointsFactory(this.requireActivity().application)).get(ViewPointList::class.java)
        val observer = Observer<MutableList<Point>> {
                (pointList) -> (recycleView.adapter as PointListAdapter).setList(mViewList!!.pointsList)
        }
        mViewList!!.getList().observe(this.requireActivity(), observer)

        return view
    }

    /*companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment route_list.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(point: Point, canDone: Boolean) =
            route_list().apply {
                arguments = Bundle().apply {
                    putParcelable("point", point as Parcelable)
                    putBoolean("canDone", canDone)
                }
            }
    }*/
}