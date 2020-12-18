package com.example.ekotransservice_routemanager

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class CoroutineViewModel(val activity: MainActivity,
                         val coroutineWork : suspend () -> Unit,
                         val afterWork : () -> Unit) : ViewModel() {
    private val workDoneCheck : MutableLiveData<Boolean> = MutableLiveData(false)

    init{
        workDoneCheck.removeObservers(activity)
        workDoneCheck.observe(activity, {
            if(it) {
                afterWork()
                workDoneCheck.value = false
            }

        })
    }

    fun startWork (){
        viewModelScope.launch {
            try {
                coroutineWork()
            } catch (e: Exception){
                //log
                Log.e(MainActivity.TAG,"" + this@CoroutineViewModel::class.java + "Exception in work ",e)
            }

            workDoneCheck.value = true
        }

    }

}