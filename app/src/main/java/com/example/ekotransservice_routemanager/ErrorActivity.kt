package com.example.ekotransservice_routemanager

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import cat.ereza.customactivityoncrash.CustomActivityOnCrash
import com.google.android.material.button.MaterialButton

class ErrorActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.error_fragment)

        val config = CustomActivityOnCrash.getConfigFromIntent(intent)

        if(config == null){
            finish()
            return
        }

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