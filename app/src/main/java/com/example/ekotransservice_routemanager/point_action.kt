package com.example.ekotransservice_routemanager

import android.os.Bundle

import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.children
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.ekotransservice_routemanager.DataClasses.Point
import com.example.ekotransservice_routemanager.DataClasses.PointActoins
import java.io.Serializable

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [point_action.newInstance] factory method to
 * create an instance of this fragment.
 */
class point_action : Fragment() {
    // TODO: Rename and change types of parameters
    private var point: Point? = null
    private var canDone: Boolean = true
    private var viewPointModel : ViewPointAction? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            point  = it.getSerializable("point") as Point
            canDone = it.getBoolean("canDone")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val mainFragment = inflater.inflate(R.layout.fragment_point_action, container, false)
        fillFragment(mainFragment)

        // Inflate the layout for this fragment

        viewPointModel!!.getPoint().observe(this.viewLifecycleOwner, Observer {
            viewPointModel!!.setPoint(it!!)
            fillFragment(mainFragment)

        })
        return mainFragment
    }

    private fun showButtons(mainFragment:View, listOfActions:ArrayList<PointActoins>){
        for(child in (mainFragment.findViewById<View>(R.id.buttonsToDo) as ConstraintLayout).children){
            child.visibility = View.GONE
        }

        for(action in listOfActions){
            when (action){
                PointActoins.TAKE_PHOTO_BEFORE
                -> mainFragment.findViewById<View>(R.id.layoutTakePhotoBefore).visibility = View.VISIBLE
                PointActoins.TAKE_PHOTO_AFTER
                -> mainFragment.findViewById<View>(R.id.layoutTakePhotoAfter).visibility = View.VISIBLE
                PointActoins.SET_VOLUME
                -> mainFragment.findViewById<View>(R.id.layoutSetCountFact).visibility = View.VISIBLE
            }
        }
    }

    private fun fillFragment(mainFragment: View){
        viewPointModel = activity?.application?.let { ViewPointAction(it,point!!) }

        val addressText = mainFragment.findViewById<TextView>(R.id.pointAdress)
        addressText.text = viewPointModel!!.getPoint().value!!.getAddressName()

        val agentText = mainFragment.findViewById<TextView>(R.id.agentName)
        agentText.text = viewPointModel!!.getPoint().value!!.getAgentName()

        val contNameText = mainFragment.findViewById<TextView>(R.id.containerName)
        contNameText.text = viewPointModel!!.getPoint().value!!.getContainerName()

        val contCountText = mainFragment.findViewById<TextView>(R.id.containerCount)
        contCountText.text = viewPointModel!!.getPoint().value!!.getContCount().toString()

        var listOfActions: ArrayList<PointActoins> = if(canDone){
            viewPointModel!!.getPoint().value!!.getPointActionsArray()
        }else{
            viewPointModel!!.getPoint().value!!.getPointActionsCancelArray()
        }

        showButtons(mainFragment, listOfActions)
    }
    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment point_action.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(point: Point, canDone: Boolean) =
            point_action().apply {
                arguments = Bundle().apply {
                    putSerializable("point", point as Serializable)
                    putBoolean("canDone", canDone)
                }
            }
    }
}

