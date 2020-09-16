package com.example.ekotransservice_routemanager



import android.content.Context
import android.os.Build
import android.transition.ChangeBounds
import android.transition.TransitionManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewOutlineProvider.PADDED_BOUNDS
import android.view.animation.Animation
import android.view.animation.AnticipateOvershootInterpolator
import android.view.animation.Transformation
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
        val itemView : View = mLayout.inflate(R.layout.recycleview_item,parent,false)
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
        val point : Point = pointList!![position]
        holder.pointItemView.text = point.getName()
        holder.contCountType.text = point.getContType()
        holder.contCountView.text = point.getContCount().toString()
        holder.itemView.clipToOutline = true

        holder.pointPosition = position
        bind(holder)
        //при нажатии определяется пока только точка

        holder.itemView.setOnClickListener {
            point.setDone(!point.getDone())
            bind(holder)
        }
        holder.itemView.findViewById<Button>(R.id.doneButton).setOnClickListener {
            if (point.getDone()) {
                val bundle = bundleOf("point" to point, "canDone" to true)
                holder.itemView.findNavController()
                    .navigate(R.id.action_route_list_to_point_action, bundle)
            }
        }

        holder.itemView.findViewById<Button>(R.id.cannotDoneButton).setOnClickListener {
            if(point.getDone()) {
                val bundle = bundleOf("point" to point, "canDone" to false)
                holder.itemView.findNavController()
                    .navigate(R.id.action_route_list_to_point_action, bundle)
            }
        }

    }else{
        holder.pointItemView.text = "no points"
    }

    private fun bind (holder: PointViewHolder) = if(pointList!![holder.pointPosition].getDone()) {
        val animateView = AnimateView(holder.buttonsView,holder.itemView.context)
        animateView.showHeight()
        /*holder.buttonsView.measure(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT)
        val actualHeight = holder.buttonsView.measuredHeight
        holder.buttonsView.layoutParams.height = 1
        holder.buttonsView.visibility = View.VISIBLE
        class animateViewOpen ():Animation(){
            override fun applyTransformation(interpolatedTime: Float, t: Transformation?) {
                super.applyTransformation(interpolatedTime, t)
                //

                if(interpolatedTime == 1.0F) {
                    holder.buttonsView.layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
                }else if(interpolatedTime > 0.0F){
                    holder.buttonsView.layoutParams.height = (actualHeight * interpolatedTime).toInt()
                }

                holder.buttonsView.requestLayout()

            }

            override fun willChangeBounds(): Boolean {
                return true
            }
        }

        val animationOpen = animateViewOpen()
        animationOpen.duration = 100//(actualHeight / holder.buttonsView.context.resources.displayMetrics.density).toLong()
        holder.itemView.startAnimation(animationOpen)*/
    }else{
        val animateView = AnimateView(holder.buttonsView,holder.itemView.context)
        animateView.hideHeight()
        /*class animateViewClose ():Animation(){
            override fun applyTransformation(interpolatedTime: Float, t: Transformation?) {
                super.applyTransformation(interpolatedTime, t)

                if(interpolatedTime == 1.0F) {
                    holder.buttonsView.visibility = View.GONE
                }
                holder.buttonsView.layoutParams.height = -1 * ViewGroup.LayoutParams.WRAP_CONTENT
                holder.buttonsView.requestLayout()

            }
        }
        val animationClose = animateViewClose()
        animationClose.duration = 100
        holder.buttonsView.startAnimation(animationClose)*/
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