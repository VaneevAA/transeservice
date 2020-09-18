package com.example.ekotransservice_routemanager



import android.content.Context
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.annotation.RequiresApi
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
        holder.pointItemView.text = point.getAddressName()
        holder.contCountType.text = point.getContType()
        holder.contCountView.text = point.getContCount().toString()
        holder.itemView.clipToOutline = true

        holder.pointPosition = position
        val animateView = AnimateView(holder.buttonsView,holder.itemView.context,false)
        animateView.hideHeight()
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
        val animateView = AnimateView(holder.buttonsView,holder.itemView.context,true)
        animateView.showHeight()
    }else{
        val animateView = AnimateView(holder.buttonsView,holder.itemView.context,true)
        animateView.hideHeight()
    }




}