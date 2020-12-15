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
import android.widget.Button
import android.widget.ListView
import android.widget.Toast
import androidx.preference.*
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.*

class SettingsFragment : PreferenceFragmentCompat() {

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
        /*for( child in (view as ViewGroup).children){
            //child.background = requireActivity().getDrawable(R.drawable.point_back)
            if (child is ViewGroup){
                for (grandChild in (child as ViewGroup).children){
                    grandChild.background = requireActivity().getDrawable(R.drawable.point_back)
                }
            }
        }*/

        return view
    }





        fun createLogFile (activity: MainActivity) : File? {
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

        fun setLogInFile (file: File){
            val command = "logcat -v threadtime *:* -f " + file.absoluteFile
            val progress = Runtime.getRuntime().exec(command)

        }

        fun sendLog (activity: MainActivity){
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

}

