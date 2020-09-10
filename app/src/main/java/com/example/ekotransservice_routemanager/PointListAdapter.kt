package com.example.ekotransservice_routemanager


import android.content.Context
import android.os.Build
import android.transition.ChangeBounds
import android.transition.TransitionManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnticipateOvershootInterpolator
import android.widget.Button
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.os.bundleOf
import androidx.lifecycle.MutableLiveData
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.ekotransservice_routemanager.DataClasses.Point
import com.example.ekotransservice_routemanager.DataClasses.PointStatuses


class PointListAdapter(context : Context) : RecyclerView.Adapter<PointListAdapter.PointViewHolder>() {

    class PointViewHolder( itemView: View) : RecyclerView.ViewHolder(
        itemView
    ) {
        var pointItemView : TextView = itemView.findViewById(R.id.nameTextView)
        var contCountView : TextView = itemView.findViewById(R.id.contCountTextView)
        var contCountType : TextView = itemView.findViewById(R.id.contTypeTextView)
        var buttonsView : View = itemView.findViewById(R.id.laynerButtons)
        var pointPosition : Int = 0
    }

    private var mLayout : LayoutInflater = LayoutInflater.from(context)
    var pointList : MutableList<Point>? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PointViewHolder {
        var itemView : View = mLayout.inflate(R.layout.recycleview_item_closed,parent,false)
        return PointViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        if (pointList != null){
            return pointList!!.size
        }
        return 0
    }

    fun setList(pointList: MutableLiveData<MutableList<Point>>){
        this.pointList = pointList.value
        notifyDataSetChanged()
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onBindViewHolder(holder: PointViewHolder, position: Int) = if (pointList != null){
        var point : Point = pointList!![position]
        holder.pointItemView.text = point.getName()
        holder.contCountType.text = point.getContType()
        holder.contCountView.text = point.getContCount().toString()
        holder.buttonsView.layoutParams.height = 0
        when (point.getStatus()){
            PointStatuses.CANNOT_DONE -> holder.itemView.setBackgroundResource(R.drawable.point_back_cannot_done)
            PointStatuses.DONE        -> holder.itemView.setBackgroundResource(R.drawable.point_back_done)
            else                      -> holder.itemView.setBackgroundResource(R.drawable.point_back)
        }
        holder.pointPosition = position

        //при нажатии определяется пока только точка

        holder.itemView.setOnClickListener {
            if(it.isActivated){
                hideButtons(holder)
                it.isActivated = !it.isActivated
                point.setDone(false)
                it!!.animate().start()

            }else {

                it.isActivated = !it.isActivated
                pointList!![holder.pointPosition].setDone(true)
                showButtons(holder)
                it!!.animate().start()
            }


        }
        holder.itemView.findViewById<Button>(R.id.doneButton).setOnClickListener {
            if (point.getDone()) {
                var bundle = bundleOf("point" to point, "canDone" to true)
                holder.itemView.findNavController()
                    .navigate(R.id.action_route_list_to_point_action, bundle)
            }
        }

        holder.itemView.findViewById<Button>(R.id.cannotDoneButton).setOnClickListener {
            if(point.getDone()) {
                var bundle = bundleOf("point" to point, "canDone" to false)
                holder.itemView.findNavController()
                    .navigate(R.id.action_route_list_to_point_action, bundle)
            }
        }

    }else{
        holder.pointItemView.text = "no points"
    }

    private fun showButtons (holder : PointViewHolder){
        val constraintSet = ConstraintSet()
        constraintSet.clone(holder.itemView.context,R.layout.recycleview_item)

        val transition = ChangeBounds()
        transition.interpolator = AnticipateOvershootInterpolator(1.0F)
        transition.duration = 1200

        TransitionManager.beginDelayedTransition(holder.itemView as ViewGroup?,transition)
        constraintSet.applyTo(holder.itemView as ConstraintLayout?)
    }

    private fun hideButtons(holder: PointViewHolder){
        val constraintSet = ConstraintSet()
        constraintSet.clone(holder.itemView.context,R.layout.recycleview_item_closed)

        val transition = ChangeBounds()
        transition.interpolator = AnticipateOvershootInterpolator(1.0F)
        transition.duration = 1200

        TransitionManager.beginDelayedTransition(holder.itemView as ViewGroup?,transition)
        constraintSet.applyTo(holder.itemView as ConstraintLayout?)
    }
}