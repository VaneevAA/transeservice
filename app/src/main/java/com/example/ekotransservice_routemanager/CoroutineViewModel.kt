package com.example.ekotransservice_routemanager

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class CoroutineViewModel(val activity: MainActivity,val coroutineWork : suspend () -> Unit, val afterWork : () -> Unit) : ViewModel() {
    private val workDoneCheck : MutableLiveData<Boolean> = MutableLiveData(false)

    init{
        workDoneCheck.removeObservers(activity)
        workDoneCheck.observe(activity, Observer {if(it){ afterWork() }})
    }

    fun startWork (){
        viewModelScope.launch {
            coroutineWork()
            workDoneCheck.value = true
        }

    }

}