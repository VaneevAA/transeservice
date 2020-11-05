package com.example.ekotransservice_routemanager.ViewIssues.RouteList

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.*
import com.example.ekotransservice_routemanager.DataBaseInterface.RouteRepository
import com.example.ekotransservice_routemanager.DataClasses.Point
import com.example.ekotransservice_routemanager.MainActivity
import kotlinx.coroutines.launch
import java.lang.IllegalArgumentException

class ViewPointList(application: Application, val activity: MainActivity):AndroidViewModel(application) {
    var pointsList = MutableLiveData<MutableList<Point>>()
    var loadFullList : Boolean = false
    /*private val result : LiveData<MutableList<Point>> = liveData {
        activity.mSwipeRefreshLayout!!.isRefreshing = true
        emit(loadDataFromDB())
        activity.mSwipeRefreshLayout!!.isRefreshing = false
    }*/

    val routeRepository: RouteRepository = RouteRepository.getInstance(application.applicationContext)

    class ViewPointsFactory(private val application: Application, private  val activity: MainActivity):ViewModelProvider.Factory{
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if(modelClass.isAssignableFrom(ViewPointList::class.java)){
                return ViewPointList(application,activity) as T
            }
            throw IllegalArgumentException("Unknown class")
        }

    }

    fun loadDataFromDB(){

        /*val trackList = viewModelScope.async {routeRepository.getPointList(false)}
        return trackList.await() ?: mutableListOf()*/
        viewModelScope.launch {
            try {
                val trackList = routeRepository.getPointList(false,!loadFullList)
                pointsList.value =  trackList
            }catch (e:Exception){
                Toast.makeText(activity,"Маршрут ещё не загружен",Toast.LENGTH_LONG).show()
                activity.onBackPressed()
            }

        }

    }

    fun getList () : MutableLiveData<MutableList<Point>> {
        return pointsList
    }


}