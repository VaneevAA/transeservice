package com.example.ekotransservice_routemanager

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import cat.ereza.customactivityoncrash.CustomActivityOnCrash
import com.google.android.material.button.MaterialButton
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

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
        val error = CustomActivityOnCrash.getStackTraceFromIntent(intent)
        if(error != null){
            Log.e(MainActivity.TAG,error)
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

        val logButton = findViewById<Button>(R.id.sendLogFile)
        logButton.setOnLongClickListener {

            sendLog()

            return@setOnLongClickListener true
        }

    }

    private fun createLogFile () : File? {
        val storage = this.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val fileName = "log" + SimpleDateFormat("yyyyMMdd_HHmmss", Locale("RU"))
            .format(Date())

        try {

            return File.createTempFile(
                fileName,
                ".txt",
                storage
            )
        }catch (e: Exception){
            Toast.makeText(this, "Неудалось записать файл", Toast.LENGTH_LONG).show()
        }
        return null
    }

    private fun setLogInFile (file: File){
        val command = "logcat " + MainActivity.TAG + ":* -f " + file.absoluteFile
        Runtime.getRuntime().exec(command)

    }

    private fun sendLog (){
        val file = createLogFile()
        if(file != null){
            setLogInFile(file)
            val imageUris : ArrayList<Uri> = arrayListOf()
            imageUris.add(Uri.parse(file.absolutePath))

            val shareIntent = Intent().apply {
                action = Intent.ACTION_SEND_MULTIPLE
                putParcelableArrayListExtra(Intent.EXTRA_STREAM,imageUris)
                type = "*/*"
            }

            this.startActivity(Intent.createChooser(shareIntent,"Отправка лога"))
        }
    }
}