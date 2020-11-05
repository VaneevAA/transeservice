package com.example.ekotransservice_routemanager.ViewIssues.PhotoShowFragment

import android.net.Uri
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.ekotransservice_routemanager.DataClasses.PhotoOrder
import com.example.ekotransservice_routemanager.MainActivity
import com.example.ekotransservice_routemanager.R
import com.google.android.material.slider.Slider

class PhotoSlideAdapter(val activity : MainActivity, val mViewModel : PhotoShowViewModel) : RecyclerView.Adapter<PhotoSlideAdapter.ImageHolder>() {

    class ImageHolder (itemView: View) : RecyclerView.ViewHolder(
        itemView
    ) {
        val imageView : ImageView = itemView.findViewById(R.id.photoImage)
        val description : TextView = itemView.findViewById(R.id.description)

    }

    init {
       setHasStableIds(true)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageHolder {
        return ImageHolder(LayoutInflater.from(activity)
            .inflate(R.layout.photo_show_item,parent,false))
    }

    override fun onBindViewHolder(holder: ImageHolder, position: Int) {
        //mViewModel.setNextPhoto(position)
        val currentFile = mViewModel.photoList.value?.get(position)
        holder.imageView.setImageURI(Uri.parse(currentFile!!.filePath))
        holder.description.text = when (currentFile.photoOrder) {
            PhotoOrder.PHOTO_BEFORE -> {
                "До вывоза"
            }
            PhotoOrder.PHOTO_AFTER -> {
                "После вывоза"
            }
            else -> {
                ""
            }
        }

    }

    override fun getItemCount(): Int {
        return mViewModel.photoList.value!!.size
    }



}