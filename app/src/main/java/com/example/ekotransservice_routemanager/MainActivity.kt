package com.example.ekotransservice_routemanager


import android.annotation.SuppressLint
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.constraintlayout.widget.Guideline
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.example.ekotransservice_routemanager.ViewIssues.AnimateView
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.lang.Exception


class MainActivity : AppCompatActivity() {
    var mSwipeRefreshLayout : SwipeRefreshLayout? = null
    private var doubleBackClick = false
    lateinit var navController : NavController
   @RequiresApi(Build.VERSION_CODES.N)
   @SuppressLint("RestrictedApi")
   override fun onCreate(savedInstanceState: Bundle?) {
       super.onCreate(savedInstanceState)
       setContentView(R.layout.activity_main)
       mSwipeRefreshLayout = findViewById<View>(R.id.thinking) as SwipeRefreshLayout
       val bottomMenu = findViewById<BottomNavigationView>(R.id.bottom_menu)

       val navHostFragment = supportFragmentManager.findFragmentById(R.id.my_nav_host_fragment) as NavHostFragment
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
        navController.addOnDestinationChangedListener{_,destanation, _ ->
            findViewById<View>(R.id.bottom_menu).visibility = View.VISIBLE

            navController.backStack.removeIf {
                it.destination.id == destanation.id
                        && navController.backStack.last != it && navController.backStack.first != it}


            when(destanation.id){
                R.id.route_list ->{
                    bottomMenu.menu.findItem(R.id.list).isChecked = true
                    val animateView = AnimateView(guideLine,this,true)
                    animateView.showHeight()
                    return@addOnDestinationChangedListener
                }
                R.id.start_frame_screen -> {
                    bottomMenu.menu.findItem(R.id.home).isChecked = true
                    val animateView = AnimateView(guideLine,this,true)
                    animateView.showHeight()
                    return@addOnDestinationChangedListener

                }
                R.id.settingFragment -> {
                    bottomMenu.menu.findItem(R.id.settings).isChecked = true
                    val animateView = AnimateView(guideLine,this,true)
                    animateView.showHeight()
                    return@addOnDestinationChangedListener

                }
                else -> {
                    val animateView = AnimateView(guideLine,this,true)
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
        if(navController.previousBackStackEntry != null){
            navController.popBackStack()
            return
        }

        if(doubleBackClick){
            super.onBackPressed()
            return
        }

        doubleBackClick = true
        Toast.makeText(this,"Два раза нажмите для выхода",Toast.LENGTH_SHORT).show()

        Handler().postDelayed({ doubleBackClick = false }, 2000)
    }


}

