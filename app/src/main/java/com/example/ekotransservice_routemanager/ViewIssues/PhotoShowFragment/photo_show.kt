package com.example.ekotransservice_routemanager.ViewIssues.PhotoShowFragment

import android.net.Uri
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.lifecycle.Observer
import androidx.viewpager2.widget.ViewPager2
import com.example.ekotransservice_routemanager.DataClasses.Point
import com.example.ekotransservice_routemanager.DataClasses.PointFile
import com.example.ekotransservice_routemanager.MainActivity
import com.example.ekotransservice_routemanager.R

class photo_show : Fragment() {

    companion object {
        fun newInstance() = photo_show()
    }

    private lateinit var viewModel: PhotoShowViewModel
    private lateinit var imageView : ViewPager2
    lateinit var pointFile: PointFile
    var point : Point? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.photo_show_fragment, container, false)
        (requireActivity() as MainActivity).mSwipeRefreshLayout!!.isRefreshing =false
        (requireActivity() as MainActivity).mSwipeRefreshLayout!!.isEnabled =false
        imageView = view.findViewById(R.id.photoImageView)

        viewModel = PhotoShowViewModel(pointFile,point,requireActivity() as MainActivity)
        viewModel.loadPhotos()
        val adapter = PhotoSlideAdapter(requireActivity() as MainActivity,viewModel)
        val observer = Observer<MutableList<PointFile>> {
            imageView.adapter = adapter
        }
        viewModel.photoList.observe(requireActivity(),observer)
        imageView.setPageTransformer(ZoomOutPageTransformer())

        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        //viewModel = ViewModelProvider(this).get(PhotoShowViewModel::class.java)
        // TODO: Use the ViewModel
    }

    class ZoomOutPageTransformer : ViewPager2.PageTransformer {

        override fun transformPage(page: View, position: Float) {
            page.apply {
                val pageWidth = width
                val pageHeight = height
                when {
                    position < -1 -> { // [-Infinity,-1)
                        // This page is way off-screen to the left.
                        alpha = 0f
                    }
                    position <= 1 -> { // [-1,1]
                        // Modify the default slide transition to shrink the page as well
                        val scaleFactor = Math.max(Companion.MIN_SCALE, 1 - Math.abs(position))
                        val vertMargin = pageHeight * (1 - scaleFactor) / 2
                        val horzMargin = pageWidth * (1 - scaleFactor) / 2
                        translationX = if (position < 0) {
                            horzMargin - vertMargin / 2
                        } else {
                            horzMargin + vertMargin / 2
                        }

                        // Scale the page down (between MIN_SCALE and 1)
                        scaleX = scaleFactor
                        scaleY = scaleFactor

                        // Fade the page relative to its size.
                        alpha = (Companion.MIN_ALPHA +
                                (((scaleFactor - Companion.MIN_SCALE) / (1 - Companion.MIN_SCALE)) * (1 - Companion.MIN_ALPHA)))
                    }
                    else -> { // (1,+Infinity]
                        // This page is way off-screen to the right.
                        alpha = 0f
                    }
                }
            }

        }

        companion object {
            private const val MIN_SCALE = 0.85f
            private const val MIN_ALPHA = 0.5f
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pointFile = requireArguments().get("pointFileValue") as PointFile
        point = requireArguments().get("point") as Point
    }


}