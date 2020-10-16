package com.example.ekotransservice_routemanager.ViewIssues.PointFiles

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.RecyclerView
import com.example.ekotransservice_routemanager.DataClasses.PhotoOrder
import com.example.ekotransservice_routemanager.DataClasses.PointFile
import com.example.ekotransservice_routemanager.R

class PointFilesAdapter(val context: Context) : RecyclerView.Adapter<PointFilesAdapter.PointFilesHolder>() {

    class PointFilesHolder(itemView: View) : RecyclerView.ViewHolder(
        itemView
    ) {
        val pointOrderText : TextView = itemView.findViewById(R.id.fileStatus)
        val pointFile : ImageView = itemView.findViewById(R.id.pointFile)
        val listElement : ConstraintLayout = itemView.findViewById(R.id.listElement)
    }

    private var mLayout : LayoutInflater = LayoutInflater.from(context)
    private var pointFilesList : MutableList<PointFile>? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PointFilesHolder {
        val itemView : View = mLayout.inflate(R.layout.point_file_item, parent, false)
        return PointFilesHolder(itemView)
    }

    override fun onBindViewHolder(holder: PointFilesHolder, position: Int) {
        val pointFile = pointFilesList?.get(position)

        holder.pointOrderText.text = when (pointFile!!.photoOrder) {
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
        try {

            val imageBytes = Base64.decode(pointFile.filePath, Base64.DEFAULT or Base64.NO_WRAP)
            val mBitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
            holder.pointFile.setImageBitmap(Bitmap.createScaledBitmap(mBitmap, 50, 50, false))
        }catch (e: Exception){
            Toast.makeText(context, "Ошибка сжатия файла: $e", Toast.LENGTH_LONG).show()
            holder.pointFile.setImageURI(Uri.parse(pointFile.filePath))
        }

        holder.listElement.setOnClickListener {
            val openImage = Intent(Intent.ACTION_VIEW)
            openImage.setDataAndType(Uri.parse(pointFile.filePath), "image/*")
            openImage.flags = Intent.FLAG_ACTIVITY_NEW_TASK

            try {
                context.startActivity(openImage)
            }catch (e: Exception){
                Toast.makeText(
                    context,
                    "Нет приложения для отображения данного типа файла",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
        //holder.pointFile.setImageURI(Uri.parse(pointFile.filePath))

    }

    override fun getItemCount(): Int {
        return if (pointFilesList != null){
            pointFilesList!!.size
        }else{
            0
        }
    }

    fun setList(pointList: MutableLiveData<MutableList<PointFile>>){
        this.pointFilesList = pointList.value
        notifyDataSetChanged()
    }
}

