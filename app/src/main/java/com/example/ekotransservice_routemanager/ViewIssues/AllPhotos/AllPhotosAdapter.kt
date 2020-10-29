package com.example.ekotransservice_routemanager.ViewIssues.AllPhotos

import android.annotation.SuppressLint
import android.app.ActionBar
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
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
import androidx.lifecycle.ViewModel
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
        var pointPosition : Int = 0
    }
    private var mLayout : LayoutInflater = LayoutInflater.from(context)
    private var pointList : MutableList<Point>? = null
    val viewModelIsSelected = AllPhotoSelected(context as MainActivity)

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

        //установка размеров элементов формы (-2 - wrap content)
        val recycleView : RecyclerView = holder.itemView.findViewById(R.id.recyclerview)
        val photoFilesFragment = holder.itemView.findViewById<View>(R.id.photoFilesFragment)
        val pointFilesParent = holder.itemView.findViewById<View>(R.id.pointFilesParent)
        val listOfPointFiles = holder.itemView.findViewById<View>(R.id.listOfPointFiles)
        photoFilesFragment.layoutParams.height = -2
        pointFilesParent.layoutParams.height = -2
        recycleView.layoutParams.height = -2
        listOfPointFiles.layoutParams.height = -2
        //установка количества фото на ширену экрана
        val graphicPoint = android.graphics.Point()
        (activity.getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay.getSize(graphicPoint)
        val countOfImages = (graphicPoint.x / 300).toInt()
        recycleView.layoutManager = GridLayoutManager(context,countOfImages)

        recycleView.isVerticalScrollBarEnabled = false
        recycleView.isNestedScrollingEnabled = false
        val adapter = pointList?.get(position)?.let {
            PointFilesAdapter(holder.itemView.context,
                it
            )
        }
        recycleView.adapter = adapter

        val observer = Observer<MutableList<PointFile>> {
                (pointFile) -> (recycleView.adapter as PointFilesAdapter).setList(viewModel!!.getList())
        }

        viewModel?.files?.removeObservers(activity)
        viewModel?.files?.observe(activity,observer)

        viewModel?.loadDataFromDB()
        holder.itemView.findViewById<TextView>(R.id.pointNameText).text = pointList?.get(position)!!.getAddressName()

        holder.pointPosition = position
        viewModelIsSelected.viewModelList.add((recycleView.adapter as PointFilesAdapter).selectedViewModel)
        val selectObserver = Observer<Boolean> {
            viewModelIsSelected.setSelected()
        }

        (recycleView.adapter as PointFilesAdapter).selectedViewModel.selectedListFilled
            .observe(context as MainActivity,selectObserver)

    }

    fun setList(points : MutableLiveData<MutableList<Point>>){
        this.pointList = points.value?.size?.let { MutableList(it) { i: Int -> points.value!![i] } }
        notifyDataSetChanged()
    }

    class AllPhotoSelected (val activity: MainActivity) : ViewModel(){
        var viewModelList : MutableList<PointFilesAdapter.SelectedViewModel> = mutableListOf()
        var hasSelected : MutableLiveData<Boolean> = MutableLiveData(false)

        fun setSelected (){
            for(viewModel in viewModelList){
                if (viewModel.selectedListFilled.value == true) {
                    hasSelected.value = true
                    return
                }

            }
            hasSelected.value = false
        }

        fun getOnClickListener () : View.OnClickListener{
            return View.OnClickListener {
                val imageUris : ArrayList<Uri> = arrayListOf()
                for (viewModel in viewModelList){
                    for (pointFile in viewModel.selectedList){
                        imageUris.add(Uri.parse(pointFile.filePath))
                    }
                }

                val shareIntent = Intent().apply {
                    action = Intent.ACTION_SEND_MULTIPLE
                    putParcelableArrayListExtra(Intent.EXTRA_STREAM,imageUris)
                    type = "image/*"
                }

                activity.startActivity(Intent.createChooser(shareIntent,"Отправка фото"))

            }
        }
    }
}