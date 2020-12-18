package com.example.ekotransservice_routemanager

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.preference.*
import com.example.ekotransservice_routemanager.DataBaseInterface.RouteRepository
import kotlinx.coroutines.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class SettingsFragment : PreferenceFragmentCompat() {

    lateinit var routeRepository: RouteRepository

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)

        val portPreference: EditTextPreference? = findPreference("URL_PORT")

        portPreference?.setOnBindEditTextListener { editText ->
            editText.inputType = InputType.TYPE_CLASS_NUMBER
        }

        val urlPreference: EditTextPreference? = findPreference("URL_NAME")

        urlPreference?.setOnBindEditTextListener { editText ->
            editText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_URI
        }

        val passwordPreference: EditTextPreference? = findPreference("URL_AUTHPASS")

        passwordPreference?.setOnBindEditTextListener { editText ->
            editText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        }

        val datePreference: EditTextPreference? = findPreference("DATE")
        datePreference?.setOnBindEditTextListener { editText ->
            editText.inputType = InputType.TYPE_DATETIME_VARIATION_DATE
        }

        val sendLog = findPreference<Preference>(getString(R.string.sendLog))

        sendLog?.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            sendLog(requireActivity() as MainActivity)
            return@OnPreferenceClickListener true
        }

        val update = findPreference<Preference>(getString(R.string.update))

        update?.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            //your update method
            return@OnPreferenceClickListener true
        }

        val clearCache = findPreference<Preference>(getString(R.string.clearCache))

        clearCache?.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            //your clear method
            prepareClearCache(requireActivity() as MainActivity)

            return@OnPreferenceClickListener true
        }



    }

    @SuppressLint("UseCompatLoadingForDrawables")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        //requireContext().getTheme().applyStyle(R.style.PreferenceScreen, true);
        val view = super.onCreateView(inflater, container, savedInstanceState)
        view?.background = requireActivity().getDrawable(R.drawable.pictures_back)
        routeRepository = RouteRepository(requireContext())

        return view
    }

    private fun createLogFile (activity: MainActivity) : File? {
        val storage = activity.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val fileName = "log" + SimpleDateFormat("yyyyMMdd_HHmmss", Locale("RU"))
            .format(Date())

        try {
            val currentFile = File.createTempFile(
                fileName,
                ".txt",
                storage
            )

            return currentFile
        }catch (e: Exception){
            Toast.makeText(activity, "Неудалось записать файл", Toast.LENGTH_LONG).show()
        }
        return null
    }

    private fun setLogInFile (file: File){
        val command = "logcat " + MainActivity.TAG + ":* -f " + file.absoluteFile
        val progress = Runtime.getRuntime().exec(command)

    }

    private fun sendLog (activity: MainActivity){
        val file = createLogFile(activity)
        if(file != null){
            setLogInFile(file)
            val imageUris : ArrayList<Uri> = arrayListOf()
            imageUris.add(Uri.parse(file.absolutePath))

            val shareIntent = Intent().apply {
                action = Intent.ACTION_SEND_MULTIPLE
                putParcelableArrayListExtra(Intent.EXTRA_STREAM,imageUris)
                type = "*/*"
            }

            activity.startActivity(Intent.createChooser(shareIntent,"Отправка лога"))
        }
    }

    private fun clearDir (dir : File,notDeleteFiles : ArrayList<String>){
        if(dir.isDirectory){
            val children = dir.list()
            if(children != null){
                for(itemFile in children) {
                    val path = dir.absolutePath + "/" + itemFile
                    if (notDeleteFiles.contains(path)) {
                        continue
                    }
                    val file = File(path)
                    if (file.isDirectory) {
                        clearDir(file, notDeleteFiles)
                    } else {
                        file.delete()
                        if(file.exists()){
                            file.canonicalFile.delete()
                            if (file.exists()){
                                context?.deleteFile(file.path)
                            }
                        }
                    }
                }   
            }
        }
    }
    

    private fun prepareClearCache(activity: MainActivity){
        activity.mSwipeRefreshLayout?.isRefreshing = true
        requireActivity().window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                                            ,WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        activity.backPressedBlock = true
        val viewModel = CoroutineViewModel(activity,{
            val storage = activity.getExternalFilesDir(Environment.DIRECTORY_PICTURES) ?: return@CoroutineViewModel
            val points = routeRepository.getPointsWithFilesAsync()
            val notDeleteFiles = ArrayList<String>()
            if (points != null) {
                for (point in points){
                    val files = routeRepository.getFilesFromDBAsync(point)
                    if(files != null){
                        for (file in files){
                            notDeleteFiles.add(file.filePath)
                        }
                    }
                }
            }
            delay(5000)
            clearDir(storage,notDeleteFiles)
        },{
            requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
            activity.mSwipeRefreshLayout?.isRefreshing = false
            activity.backPressedBlock = false
        })
        viewModel.startWork()

    }


}



