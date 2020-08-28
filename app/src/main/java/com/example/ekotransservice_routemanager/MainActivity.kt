package com.example.ekotransservice_routemanager


import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.dynamicanimation.animation.DynamicAnimation
import androidx.dynamicanimation.animation.FlingAnimation
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ekotransservice_routemanager.DataClasses.Point


class MainActivity : AppCompatActivity() {

    private var mViewList : ViewPointList? = null

   override fun onCreate(savedInstanceState: Bundle?) {
       super.onCreate(savedInstanceState)
       setContentView(R.layout.activity_main)

       val recycleView : RecyclerView = findViewById(R.id.recyclerview)
       val adapter = PointListAdapter(this)
       recycleView.adapter = adapter
       recycleView.layoutManager = LinearLayoutManager(this)
       mViewList = ViewModelProvider(this,ViewPointList.ViewPointsFactory(this.application)).get(ViewPointList::class.java)
       var observer = Observer<MutableList<Point>> {
               (pointList) -> (recycleView.adapter as PointListAdapter).setList(mViewList!!.pointsList)
       }
       mViewList!!.getList().observe(this, observer)

   }

}

