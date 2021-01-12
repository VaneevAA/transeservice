package com.example.ekotransservice_routemanager

import android.annotation.SuppressLint
import android.app.Activity
import android.app.DownloadManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageInstaller
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.text.InputType
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.preference.*
import com.example.ekotransservice_routemanager.DataBaseInterface.RouteRepository
import kotlinx.coroutines.*
import java.io.File
import java.io.FileInputStream
import java.io.OutputStream
import java.net.URL
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
            val sendLog = sendLog(requireActivity() as MainActivity)
            sendLog.sendLogInFile()
            return@OnPreferenceClickListener true
        }

        val update = findPreference<Preference>(getString(R.string.update))

        update?.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            updateAppFromApk(requireActivity() as MainActivity)
            return@OnPreferenceClickListener true
        }

        val clearCache = findPreference<Preference>(getString(R.string.clearCache))

        clearCache?.onPreferenceClickListener = Preference.OnPreferenceClickListener {
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

        val view = super.onCreateView(inflater, container, savedInstanceState)
        view?.background = requireActivity().getDrawable(R.drawable.pictures_back)
        routeRepository = RouteRepository(requireContext())

        return view
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

    private fun updateAppFromApk(activity: MainActivity) {
        activity.mSwipeRefreshLayout?.isRefreshing = true
        requireActivity().window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
            ,WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        activity.backPressedBlock = true
        val dir = requireContext().getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
        val fileName = "apk_release.apk"
        val viewModel = CoroutineViewModel(activity,{
            routeRepository.loadApkAsync(dir!!,fileName)
        },{
            openApkFile(dir!!,fileName)
            requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
            activity.mSwipeRefreshLayout?.isRefreshing = false
            activity.backPressedBlock = false
        })
        viewModel.startWork()
    }

    private fun openApkFile(dir:File, fileName: String){
        val intent = Intent(Intent.ACTION_VIEW)
        val fileApk = File("${dir.absolutePath}/$fileName")
        if (fileApk.length() == 0L) {
            return
        }
        val uri = FileProvider.getUriForFile(
            requireContext(), BuildConfig.APPLICATION_ID + ".fileprovider",
            fileApk
        )
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        intent.setDataAndType(uri, "application/vnd.android.package-archive")
        intent.putExtra(Intent.EXTRA_NOT_UNKNOWN_SOURCE, true)
        startActivity(intent)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {

        }
    }

    //region downloadManger Оставить для примера, возможно переделат в дальнейшем
    /*private var downloadReference: Long = 0
    private lateinit var downloadManager: DownloadManager
    private var uri: Uri? = null

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            if (action == DownloadManager.ACTION_DOWNLOAD_COMPLETE) {
                val downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
                if (downloadId != downloadReference) {
                    context.unregisterReceiver(this)
                    return
                }
                Log.d(
                    "Download APK",
                    "" + this::class.java + " BroadcastReceiver onReceive "
                )

                val query = DownloadManager.Query()
                query.setFilterById(downloadReference)
                val cursor = downloadManager.query(query)
                cursor?.let {
                    if (cursor.moveToFirst()) {
                        val columnIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
                        if (DownloadManager.STATUS_SUCCESSFUL == cursor.getInt(columnIndex)) {
                            var localFile = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI))
                            Toast.makeText(context,"Загружен$localFile", Toast.LENGTH_LONG).show()

                            val install = Intent(Intent.ACTION_VIEW)
                            install.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                            install.setDataAndType(
                                Uri.parse(localFile),
                                downloadManager!!.getMimeTypeForDownloadedFile(downloadId)
                            )
                            Log.d(
                                "Download APK",
                                "" + this::class.java + " BroadcastReceiver onReceive $install.type ${install.data.toString()}"
                            )

                            if (localFile.contains("file:///")) {
                                localFile = localFile.removePrefix("file:///").substringBeforeLast(File.separator)
                            }


                            startActivity(install)
                        } else if (DownloadManager.STATUS_FAILED == cursor.getInt(columnIndex)) {
                            val message = "Download error ${cursor.getString(cursor.getColumnIndex(
                                DownloadManager.COLUMN_REASON))}"
                            Toast.makeText(context,message, Toast.LENGTH_LONG).show()
                        }
                    }
                    cursor.close()
                }



                context.unregisterReceiver(this)

            }
        }
    }

    private fun installApk(){
        /*val packageManger = requireContext().packageManager
        val packageInstaller = packageManger.packageInstaller
        val params = PackageInstaller.SessionParams(
            PackageInstaller.SessionParams.MODE_FULL_INSTALL
        )
        val packageName = Context::getPackageName.name
        params.setAppPackageName(packageName)
        var session: PackageInstaller.Session? = null
        try {
            val sessionId = packageInstaller.createSession(params)
            session = packageInstaller.openSession(sessionId)
            val out: OutputStream = session.openWrite(packageName, 0, -1)

            val buffer = ByteArray(1024)
            var length: Int
            var count = 0

            val apkStream = FileInputStream(viewScreen.fileApk.value)
            while (apkStream.read(buffer).also { length = it } != -1) {
                out.write(buffer, 0, length)
                count += length
            }
            //out.write(apkStream.readBytes())
            session.fsync(out)
            out.close()
            val intent = Intent(Intent.ACTION_PACKAGE_ADDED)
            session.commit(
                PendingIntent.getBroadcast(
                    context, sessionId,
                    intent, PendingIntent.FLAG_UPDATE_CURRENT
                ).intentSender
            )
        } finally {
            session?.close()
        }*/
    }

    private fun downloadAndInstallApk() {

        var destination: String =
            context?.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)!!.absolutePath + "/"
        val fileName = "apk_release.apk"
        destination += fileName
        uri = Uri.parse("file://$destination")

        //val uri = Uri.parse("file://" + BuildConfig.APPLICATION_ID + "/Download/apk_release.apk")
        /*val uri = FileProvider.getUriForFile(
            requireContext(), BuildConfig.APPLICATION_ID + ".fileprovider",
            //File("Download/apk_release.apk")
            viewScreen.fileApk.value!!
        )*/

        //Delete update file if exists
        val file = File(destination)
        if (file.exists()) //file.delete() - test this, I think sometimes it doesnt work
            file.delete()

        val url = URL("https", "188.234.242.63", 444, "/apk/app-release.apk")

        downloadManager = requireContext().getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

        val request = DownloadManager.Request(Uri.parse(url.toString()))
        request.apply {
            setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE or DownloadManager.Request.NETWORK_WIFI)
            //setAllowedOverRoaming(true)
            setTitle(fileName)
            setDescription("Downloading $fileName")
            setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            //setDestinationUri(uri)
            //request.setDestinationUri(Uri.fromFile(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)))
            setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
            requireContext().registerReceiver(receiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
            downloadReference = downloadManager.enqueue(this)
        }

        Log.d("d","d")

        /*request.setDescription("Electronic route manager")
        request.setTitle(Context::getPackageName.name)
        //set destination
        request.setDestinationUri(uri)
        // get download service and enqueue file
        val manager = requireContext().getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager?
        val downloadId = manager!!.enqueue(request)

        //set BroadcastReceiver to install app when .apk is downloaded
        val onComplete: BroadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(ctxt: Context?, intent: Intent?) {
                Log.d(
                    "Download APK",
                    "" + this::class.java + " BroadcastReceiver onReceive "
                )
                val install = Intent(Intent.ACTION_VIEW)
                install.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                install.setDataAndType(
                    uri,
                    manager!!.getMimeTypeForDownloadedFile(downloadId)
                )
                Log.d(
                    "Download APK",
                    "" + this::class.java + " BroadcastReceiver onReceive $install.type ${install.data.toString()}"
                )
                startActivity(install)
                LocalBroadcastManager.getInstance(requireContext())
                    .unregisterReceiver(this)
            }
        }

        LocalBroadcastManager.getInstance(requireContext())
            .registerReceiver(onComplete, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))*/
    }
    */
    //endregion

}



