package com.example.ekotransservice_routemanager


import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.android.synthetic.main.activity_main.*
import java.lang.Exception


class MainActivity : AppCompatActivity() {

    private var mViewList : ViewPointList? = null

   override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

       val bottomNavigation: BottomNavigationView = bottom_menu
       val navHostFragment = supportFragmentManager.findFragmentById(R.id.my_nav_host_fragment) as NavHostFragment
       val navController = navHostFragment.navController

       bottomNavigation.setOnNavigationItemSelectedListener(BottomNavigationView.OnNavigationItemSelectedListener { menuItem ->
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

}

