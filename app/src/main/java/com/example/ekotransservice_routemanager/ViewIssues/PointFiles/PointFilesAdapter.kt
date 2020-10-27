package com.example.ekotransservice_routemanager.ViewIssues.PointFiles


import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
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
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.RecyclerView
import com.example.ekotransservice_routemanager.DataClasses.PhotoOrder
import com.example.ekotransservice_routemanager.MainActivity
import com.example.ekotransservice_routemanager.R
import java.io.*
import com.example.ekotransservice_routemanager.DataClasses.PointFile
import kotlin.collections.mutableListOf as mutableListOf


class PointFilesAdapter(val context: Context) : RecyclerView.Adapter<PointFilesAdapter.PointFilesHolder>() {

    class PointFilesHolder(itemView: View) : RecyclerView.ViewHolder(
        itemView
    ) {
        val pointOrderText : TextView = itemView.findViewById(R.id.fileStatus)
        val pointFile : ImageView = itemView.findViewById(R.id.pointFile)
        val listElement : ConstraintLayout = itemView.findViewById(R.id.listElement)
        var isSelected = false
    }

    private var mLayout : LayoutInflater = LayoutInflater.from(context)
    private var pointFilesList : MutableList<PointFile>? = null
    val selectedViewModel = SelectedViewModel(context as MainActivity)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PointFilesHolder {
        val itemView : View = mLayout.inflate(R.layout.point_file_item, parent, false)
        return PointFilesHolder(itemView)
    }

    @SuppressLint("UseCompatLoadingForDrawables")
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

            val imageBytes = getByteArray(pointFile.filePath)
            val mBitmap = BitmapFactory.decodeByteArray(
                imageBytes,
                0,
                imageBytes.size
            )
            holder.pointFile.setImageBitmap(Bitmap.createScaledBitmap(mBitmap, 300, 300, false))
        }catch (e: Exception){
            Toast.makeText(context, "Ошибка сжатия файла: $e", Toast.LENGTH_LONG).show()
            holder.pointFile.setImageURI(Uri.parse(pointFile.filePath))
        }

        holder.listElement.setOnClickListener {

            val openImage = Intent(Intent.ACTION_VIEW).apply {
                putExtra(Intent.EXTRA_STREAM,Uri.parse(pointFile.filePath))
                type = "image/*"
                flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            }

            val shareIntent = Intent.createChooser(openImage,"Выберете приложение для отображения")
            //openImage.setDataAndType(Uri.parse(pointFile.filePath), "image/*")
            //openImage.flags = Intent.FLAG_ACTIVITY_NEW_TASK

            try {
                context.startActivity(shareIntent)
            }catch (e: Exception){
                Toast.makeText(
                    context,
                    "Нет приложения для отображения данного типа файла",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        holder.listElement.isLongClickable = true
        holder.listElement.setOnLongClickListener {
            holder.isSelected = !holder.isSelected

            if(holder.isSelected){
                holder.listElement.background = context.getDrawable(R.drawable.pictures_back)
                selectedViewModel.addFile(pointFile)

            }else{
                holder.listElement.background = context.getDrawable(R.drawable.point_back)
                selectedViewModel.removeFile(pointFile)
            }

            return@setOnLongClickListener true
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

    private fun getByteArray(filePath: String) : ByteArray{
        val file = File(filePath)
        val bytes = ByteArray(file.length().toInt())
        try {
            val buf = BufferedInputStream(FileInputStream(file))
            buf.read(bytes, 0, bytes.size)
            buf.close()
        } catch (e: FileNotFoundException) {
            // TODO Auto-generated catch block
            e.printStackTrace()
        } catch (e: IOException) {
            // TODO Auto-generated catch block
            e.printStackTrace()
        }
        return bytes
    }

    class SelectedViewModel (val activity: MainActivity) : ViewModel() {

        var selectedList : MutableList<PointFile> = mutableListOf()
        var selectedListFilled : MutableLiveData<Boolean> = MutableLiveData(false)



        fun getList () : MutableLiveData<Boolean>{
            return selectedListFilled

        }

        fun addFile (pointFile: PointFile){

            selectedList.add(pointFile)
            selectedListFilled.value = getSize()
        }

        fun removeFile(pointFile: PointFile){
            selectedList.remove(pointFile)
            selectedListFilled.value = getSize()
        }

        fun getSize() : Boolean = selectedList.size > 0



    }
}

