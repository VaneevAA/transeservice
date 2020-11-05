package com.example.ekotransservice_routemanager.ViewIssues.RouteList



import android.content.Context
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.os.bundleOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.navigation.findNavController
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.recyclerview.widget.RecyclerView
import com.example.ekotransservice_routemanager.ViewIssues.AnimateView
import com.example.ekotransservice_routemanager.DataClasses.Point
import com.example.ekotransservice_routemanager.MainActivity
import com.example.ekotransservice_routemanager.R


class PointListAdapter(context : Context) : RecyclerView.Adapter<PointListAdapter.PointViewHolder>() {

    class PointViewHolder( itemView: View) : RecyclerView.ViewHolder(
        itemView
    ) {
        var pointItemView : TextView = itemView.findViewById(R.id.nameTextView)
        var contCountView : TextView = itemView.findViewById(R.id.contCountTextView)
        var contCountType : TextView = itemView.findViewById(R.id.contTypeTextView)
        var buttonsView : View = itemView.findViewById(R.id.laynerButtons)
        var pointPosition : Int = 0
        var viewClosed = true
    }

    private var mLayout : LayoutInflater = LayoutInflater.from(context)
    private var pointList : MutableList<Point>? = null
    var mCurrentPointViewModel : viewModelCurrentPoint = viewModelCurrentPoint(null)

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


    @RequiresApi(Build.VERSION_CODES.M)
    override fun onBindViewHolder(holder: PointViewHolder, position: Int) = if (pointList != null){
        val point : Point = pointList!![position]
        if(mCurrentPointViewModel.currentPoint.value == null ){
            mCurrentPointViewModel.currentPoint.value = point
        }

        val doneImage = holder.itemView.findViewById<ImageView>(R.id.doneImage)
        doneImage.visibility = ViewGroup.VISIBLE
        if(!point.getReasonComment().equals("")){
            doneImage.setImageResource(R.drawable.ic_baseline_block_24_small)
        }else if (!point.getDone()){
            doneImage.visibility = ViewGroup.GONE
        }else{
            doneImage.setImageResource(R.drawable.ic_baseline_check_24_small)
        }

        holder.pointItemView.text = "${point.getRowNumber()}. ${point.getAddressName()}"
        holder.contCountType.text = point.getContType()
        holder.contCountView.text = point.getContCount().toString()
        holder.itemView.clipToOutline = true

        holder.pointPosition = position
        //val animateView = AnimateView(holder.buttonsView,holder.itemView.context,false)
        //animateView.hideHeight()
        //при нажатии определяется пока только точка

        holder.itemView.setOnClickListener {
            /*holder.viewClosed = !holder.viewClosed
            bind(holder)*/
            mCurrentPointViewModel.setCurrentPoint(point)
        }
        /*holder.itemView.findViewById<Button>(R.id.doneButton).setOnClickListener {

            val bundle = bundleOf("point" to point, "canDone" to true)
            holder.itemView.findNavController()
                .navigate(R.id.action_route_list_to_point_action, bundle)

        }

        holder.itemView.findViewById<Button>(R.id.cannotDoneButton).setOnClickListener {

            val bundle = bundleOf("point" to point, "canDone" to false)

            holder.itemView.findNavController()
                .navigate(R.id.action_route_list_to_point_action, bundle)

        }*/
        //image done


    }else{
        holder.pointItemView.text = "no points"
    }

    private fun bind (holder: PointViewHolder) = if(!holder.viewClosed) {
        val animateView = AnimateView(holder.buttonsView,holder.itemView.context,true)
        animateView.showHeight()
    }else{
        val animateView = AnimateView(holder.buttonsView,holder.itemView.context,true)
        animateView.hideHeight()
    }

    fun setList(pointList: LiveData<MutableList<Point>>){
        this.pointList = pointList.value
        notifyDataSetChanged()
    }

    class viewModelCurrentPoint (startPoint : Point?) : ViewModel() {
        val currentPoint : MutableLiveData<Point> = MutableLiveData(startPoint)
        val bottomSheetOpen : MutableLiveData<Boolean> = MutableLiveData(false)


        fun setCurrentPoint (point : Point){
            currentPoint.value = point
            bottomSheetOpen.value = true
        }

        fun setSheetClose(){
            bottomSheetOpen.value = false
        }

        fun setSheetOpen(){
            bottomSheetOpen.value = true
        }

        fun setSheetOpposite(){
            bottomSheetOpen.value = !bottomSheetOpen.value!!
        }
    }


}