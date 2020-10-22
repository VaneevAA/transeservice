package com.example.ekotransservice_routemanager.ViewIssues.AllPhotos

import android.annotation.SuppressLint
import android.app.ActionBar
import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ekotransservice_routemanager.DataClasses.Point
import com.example.ekotransservice_routemanager.DataClasses.PointFile
import com.example.ekotransservice_routemanager.MainActivity
import com.example.ekotransservice_routemanager.R
import com.example.ekotransservice_routemanager.ViewIssues.PointFiles.PointFiles
import com.example.ekotransservice_routemanager.ViewIssues.PointFiles.PointFilesAdapter
import com.example.ekotransservice_routemanager.ViewIssues.PointFiles.PointFilesViewModel


class AllPhotosAdapter(val context: Context, val activity: MainActivity, val parentFragment : AllPhotos) : RecyclerView.Adapter<AllPhotosAdapter.PointPhotosViewHolder>() {

    class PointPhotosViewHolder( itemView: View, val viewGroup : ViewGroup) : RecyclerView.ViewHolder(
        itemView
    ) {
        var currentItem : View? = null
        var pointPosition : Int = 0
    }
    private var mLayout : LayoutInflater = LayoutInflater.from(context)
    private var pointList : MutableList<Point>? = null


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): PointPhotosViewHolder {

        val itemView : View = mLayout.inflate(R.layout.point_files_fragment,parent,false)
        return PointPhotosViewHolder(itemView,parent)
    }

    override fun getItemCount(): Int {
        return if(pointList == null){
            0
        }else{
            pointList!!.size
        }
    }

    @SuppressLint("ResourceType")
    override fun onBindViewHolder(holder: PointPhotosViewHolder, position: Int) {
        val viewModel = pointList?.get(position)?.let { PointFilesViewModel(activity, it) }
        val recycleView : RecyclerView = holder.itemView.findViewById(R.id.recyclerview)
        val graphicPoint = android.graphics.Point()
        (activity.getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay.getSize(graphicPoint)

        recycleView.layoutParams.height = -2
        val countOfImages = (graphicPoint.x / 300).toInt()
        recycleView.layoutManager = GridLayoutManager(context,countOfImages)
        recycleView.isVerticalScrollBarEnabled = false
        recycleView.isNestedScrollingEnabled = false
        val adapter = PointFilesAdapter(holder.itemView.context)
        recycleView.adapter = adapter

        val observer = Observer<MutableList<PointFile>> {
                (pointFile) -> (recycleView.adapter as PointFilesAdapter).setList(viewModel!!.getList())
        }

        viewModel?.files?.removeObservers(activity)
        viewModel?.files?.observe(activity,observer)

        viewModel?.loadDataFromDB()
        holder.itemView.findViewById<TextView>(R.id.pointNameText).text = pointList?.get(position)!!.getAddressName()

        holder.pointPosition = position

    }

    fun setList(points : MutableLiveData<MutableList<Point>>){
        this.pointList = points.value?.size?.let { MutableList(it) { i: Int -> points.value!![i] } }
        notifyDataSetChanged()
    }
}