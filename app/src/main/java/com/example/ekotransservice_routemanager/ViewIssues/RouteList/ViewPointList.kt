package com.example.ekotransservice_routemanager.ViewIssues.RouteList

import android.app.Application
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.*
import com.example.ekotransservice_routemanager.DataBaseInterface.RouteRepository
import com.example.ekotransservice_routemanager.DataClasses.Point
import com.example.ekotransservice_routemanager.MainActivity
import com.example.ekotransservice_routemanager.MainActivity.Companion.TAG
import kotlinx.coroutines.launch
import java.lang.IllegalArgumentException

class ViewPointList(application: Application, val activity: MainActivity):AndroidViewModel(application) {

    private val query = MutableLiveData("")
    var pointsList = MutableLiveData<MutableList<Point>>()
    val mediatorResult = MediatorLiveData<MutableList<Point>>()
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

    fun removeSources(){
        mediatorResult.removeSource(pointsList)
        mediatorResult.removeSource(query)
    }

    fun addSources(){
        val filterF = {
            val queryStr = query.value!!
            val points: MutableList<Point> =
                if (pointsList.value == null) mutableListOf() else pointsList.value!!
            if (points.isNotEmpty()) {
                mediatorResult.value = if (queryStr.isEmpty()) points
                else points.filter {
                    it.getAddressName().contains(queryStr, true)
                }.toMutableList()
            }
        }

        mediatorResult.addSource(pointsList) { filterF.invoke() }
        mediatorResult.addSource(query) { filterF.invoke() }
    }


    fun handleSearchQuery(text: String) {
        query.value = text
    }


}