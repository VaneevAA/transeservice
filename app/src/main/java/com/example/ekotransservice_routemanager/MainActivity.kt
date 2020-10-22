package com.example.ekotransservice_routemanager


import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.Guideline
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.preference.PreferenceManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.ekotransservice_routemanager.DataBaseInterface.RouteRepository
import com.example.ekotransservice_routemanager.ViewIssues.AnimateView
import com.example.ekotransservice_routemanager.ViewIssues.StartScreen.StartFrameScreenViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.launch


class MainActivity : AppCompatActivity() {

    var mSwipeRefreshLayout: SwipeRefreshLayout? = null
    private var doubleBackClick = false
    lateinit var navController: NavController
    lateinit private var routeRepository: RouteRepository

    private val mPrefsListener =
        OnSharedPreferenceChangeListener { sharedPreferences, key ->
            if (key == "URL_NAME" || key == "URL_PORT" || key == "URL_AUTHPASS" || key == "VEHICLE") {
                routeRepository.setPrefernces()
            }
        }

    @RequiresApi(Build.VERSION_CODES.N)
    @SuppressLint("RestrictedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        routeRepository = RouteRepository.getInstance(applicationContext)

        mSwipeRefreshLayout = findViewById<View>(R.id.thinking) as SwipeRefreshLayout
        val bottomMenu = findViewById<BottomNavigationView>(R.id.bottom_menu)
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.my_nav_host_fragment) as NavHostFragment
        val guideLine = findViewById<Guideline>(R.id.guidelineMain)
        navController = navHostFragment.navController
        bottomMenu.setOnNavigationItemSelectedListener(BottomNavigationView.OnNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.home -> {
                    navController.navigate(R.id.start_frame_screen)
                    return@OnNavigationItemSelectedListener true
                }
                R.id.list -> {
                    navController.navigate(R.id.route_list)
                    return@OnNavigationItemSelectedListener true
                }
                R.id.photos -> {
                    return@OnNavigationItemSelectedListener false
                }
                R.id.settings -> {
                    try {
                        navController.navigate(R.id.settingFragment)
                    } catch (e: Exception) {
                        Log.d("nav error ", "e: $e")
                        return@OnNavigationItemSelectedListener false
                    }

                    return@OnNavigationItemSelectedListener true
                }
            }
            false
        })

        navController.addOnDestinationChangedListener { _, destanation, _ ->
            findViewById<View>(R.id.bottom_menu).visibility = View.VISIBLE

            navController.backStack.removeIf {
                it.destination.id == destanation.id
                        && navController.backStack.last != it && navController.backStack.first != it
            }


            when (destanation.id) {
                R.id.route_list -> {
                    bottomMenu.menu.findItem(R.id.list).isChecked = true
                    val animateView = AnimateView(guideLine, this, true)
                    animateView.showHeight()
                    return@addOnDestinationChangedListener
                }
                R.id.start_frame_screen -> {
                    bottomMenu.menu.findItem(R.id.home).isChecked = true
                    val animateView = AnimateView(guideLine, this, true)
                    animateView.showHeight()
                    return@addOnDestinationChangedListener

                }
                R.id.settingFragment -> {
                    bottomMenu.menu.findItem(R.id.settings).isChecked = true
                    val animateView = AnimateView(guideLine, this, true)
                    animateView.showHeight()
                    return@addOnDestinationChangedListener

                }
                else -> {
                    val animateView = AnimateView(guideLine, this, true)
                    animateView.hideHeight()
                    //bottomMenu.visibility = View.GONE
                }
            }

        }



        /*
        val recycleView : RecyclerView = this.findViewById(R.id.recyclerview)
        val adapter = PointListAdapter(this)
        recycleView.adapter = adapter
        recycleView.layoutManager = LinearLayoutManager(this)
        mViewList = ViewModelProvider(this,ViewPointList.ViewPointsFactory(this.application)).get(ViewPointList::class.java)
        var observer = Observer<MutableList<Point>> {
               (pointList) -> (recycleView.adapter as PointListAdapter).setList(mViewList!!.pointsList)
        }
        mViewList!!.getList().observe(this, observer)
        */

    }

    @SuppressLint("RestrictedApi")
    override fun onBackPressed() {
        if (navController.previousBackStackEntry != null) {
            navController.popBackStack()
            return
        }

        if (doubleBackClick) {
            super.onBackPressed()
            return
        }

        doubleBackClick = true
        Toast.makeText(this, "Два раза нажмите для выхода", Toast.LENGTH_SHORT).show()

        Handler().postDelayed({ doubleBackClick = false }, 2000)
    }

    override fun onStart() {
        super.onStart()
        PreferenceManager.getDefaultSharedPreferences(this)
            .registerOnSharedPreferenceChangeListener(mPrefsListener);
    }

    override fun onStop() {
        super.onStop()
        PreferenceManager.getDefaultSharedPreferences(this)
            .unregisterOnSharedPreferenceChangeListener(mPrefsListener);
    }

    @RequiresApi(Build.VERSION_CODES.P)
    fun endOfTheRoute (viewModel : StartFrameScreenViewModel){

        //создание потока
        val mNotificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(
            "UPLOAD_ROUTE_DATA",
            "upload route data",
            NotificationManager.IMPORTANCE_DEFAULT
        )
        channel.description = "Выгрузка данных и фотографий на сервер"
        mNotificationManager.createNotificationChannel(channel)

        //создание builder'а (что будет отображаться)
        val builder = androidx.core.app.NotificationCompat.Builder(this, channel.id)
           .setContentTitle("Выгрузка данных")
           .setContentText("Выгрузка данных маршрута и фотографий")
           .setSmallIcon(R.drawable.ic_logo_mini)
           .setPriority(androidx.core.app.NotificationCompat.PRIORITY_LOW)

        //вывод уведомления
        NotificationManagerCompat.from(this).apply {
            val notificationId : Int = 100
            builder.setProgress(100, 0, true)
            notify(notificationId, builder.build())

            viewModel.viewModelScope.launch {
                val result = routeRepository.uploadTrackListToServerAsync()
                if (result) {
                    builder.setProgress(0, 0, false)
                    builder.setContentTitle("Выгрузка завершена")

                    notify(notificationId, builder.build())
                } else {
                    builder.setProgress(0, 0, false)
                    builder.setContentTitle("Ошибка выгрузки")

                    notify(notificationId, builder.build())
                }
            }
        }
    }

}

