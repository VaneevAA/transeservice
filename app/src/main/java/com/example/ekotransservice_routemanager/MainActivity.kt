package com.example.ekotransservice_routemanager


import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Layout
import android.view.View
import android.widget.ProgressBar
import androidx.constraintlayout.widget.ConstraintSet
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout


class MainActivity : AppCompatActivity() {
    var mSwipeRefreshLayout : SwipeRefreshLayout? = null
    private var mViewList : ViewPointList? = null

   override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
       mSwipeRefreshLayout = findViewById<View>(R.id.thinking) as SwipeRefreshLayout
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

