package com.example.ekotransservice_routemanager

import android.content.Context
import android.transition.AutoTransition
import android.transition.TransitionManager
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.children

class AnimateView (var view : View, var context: Context){

    fun hideHeight (){
        val set = ConstraintSet()
        set.clone(view as ConstraintLayout)
        set.connect(view.id,ConstraintSet.TOP,ConstraintSet.PARENT_ID,ConstraintSet.TOP)
        for (child in (view as ConstraintLayout).children){
            set.constrainHeight(child.id,0)
            set.setVisibility(child.id,View.GONE)
        }

        //
        val autoTransition = AutoTransition()
        autoTransition.duration = 300;
        TransitionManager.beginDelayedTransition(view as ConstraintLayout,autoTransition)
        set.applyTo(view as ConstraintLayout)
        /*val hide = AnimationUtils.loadAnimation(context,R.anim.hide_height)
        view.startAnimation(hide)
        hide.setAnimationListener(object  : Animation.AnimationListener{
            override fun onAnimationStart(animation: Animation?) {

            }

            override fun onAnimationEnd(animation: Animation?) {
                view.visibility = View.GONE
            }

            override fun onAnimationRepeat(animation: Animation?) {

            }

        })*/
    }

    fun showHeight(){
        /*val show = AnimationUtils.loadAnimation(context,R.anim.hide_height)
        view.startAnimation(show)
        view.visibility = ViewGroup.VISIBLE*/

        val set = ConstraintSet()
        set.clone(view as ConstraintLayout)

        view.visibility = ViewGroup.VISIBLE
        for (child in (view as ConstraintLayout).children){
            set.constrainHeight(child.id,ConstraintSet.WRAP_CONTENT)
            set.setVisibility(child.id,View.VISIBLE)
        }
        val autoTransition = AutoTransition()
        autoTransition.duration = 300;
        TransitionManager.beginDelayedTransition(view as ConstraintLayout,autoTransition)
        set.applyTo(view as ConstraintLayout)

    }

    fun rotate (){
        val rotate = AnimationUtils.loadAnimation(this.context,R.anim.routate_pict)
        view.startAnimation(rotate) 
    }

    fun rotateBack (){
        val rotate = AnimationUtils.loadAnimation(this.context,R.anim.rotate_pict_back)
        view.startAnimation(rotate)
    }
}