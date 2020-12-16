package com.example.ekotransservice_routemanager

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import cat.ereza.customactivityoncrash.CustomActivityOnCrash
import com.google.android.material.button.MaterialButton

class ErrorActivity : AppCompatActivity() {

    var mSavedInstanceState : Bundle? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.error_fragment)
        mSavedInstanceState = savedInstanceState
        val config = CustomActivityOnCrash.getConfigFromIntent(intent)

        if(config == null){
            finish()
            return
        }
        Log.e(MainActivity.TAG,CustomActivityOnCrash.getStackTraceFromIntent(intent))
        val restartButton = findViewById<MaterialButton>(R.id.restartApp)
        if (config.isShowRestartButton && config.restartActivityClass != null){
            restartButton.text = "Перезагрузить"
            restartButton.setOnClickListener {
                CustomActivityOnCrash.restartApplication(
                    this,
                    config
                )
            }
        } else {
            restartButton.text = "Закрыть приложение"
            restartButton.setOnClickListener {
                CustomActivityOnCrash.closeApplication(
                    this,config
                )
            }
        }
    }
}