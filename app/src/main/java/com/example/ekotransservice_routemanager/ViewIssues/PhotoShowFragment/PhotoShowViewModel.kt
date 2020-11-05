package com.example.ekotransservice_routemanager.ViewIssues.PhotoShowFragment

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ekotransservice_routemanager.DataBaseInterface.RouteRepository
import com.example.ekotransservice_routemanager.DataClasses.Point
import com.example.ekotransservice_routemanager.DataClasses.PointFile
import com.example.ekotransservice_routemanager.MainActivity
import kotlinx.coroutines.launch

class PhotoShowViewModel (pointFile: PointFile,val point : Point?,val activity : MainActivity) : ViewModel() {
    val mData : MutableLiveData<PointFile> = MutableLiveData(pointFile)
    val routeRepository = RouteRepository.getInstance(activity.applicationContext)
    val photoList : MutableLiveData<MutableList<PointFile>> = MutableLiveData(mutableListOf())
    var currentIndex : Int = 0

    fun loadPhotos (){
        if(point == null){

        }else{
            viewModelScope.launch {
                val resultBefore = routeRepository.getFilesFromDBAsync(point!!)
                if (resultBefore != null) {
                    currentIndex = 0
                    for (pointFile in resultBefore){
                        if (pointFile.id == mData.value!!.id){
                            currentIndex = resultBefore.indexOf(pointFile)
                            break
                        }
                    }
                    photoList.value = resultBefore
                }

                activity.mSwipeRefreshLayout!!.isRefreshing = false
            }
        }
    }

}