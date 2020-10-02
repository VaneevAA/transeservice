package com.example.ekotransservice_routemanager

import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.content.Context
import android.transition.AutoTransition
import android.transition.TransitionManager
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.animation.doOnEnd
import androidx.core.animation.doOnStart
import androidx.core.view.children

class AnimateView (var view : View, var context : Context, val animate : Boolean){

    fun hideHeight (){
        if(view is ConstraintLayout) {
            /*val set = ConstraintSet()
            set.clone(view as ConstraintLayout)
            set.connect(view.id, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP)
            setHeightAndVisibility(set,view as ConstraintLayout,0,View.GONE)

            if(animate) {
                val autoTransition = AutoTransition()
                autoTransition.duration = 300;
                TransitionManager.beginDelayedTransition(view as ConstraintLayout, autoTransition)
            }
            set.applyTo(view as ConstraintLayout)*/

            val tY = view.translationY
            val goneY = ((view.top - (view.parent as ViewGroup).height) / 3).toFloat()
            view.translationY = goneY
            val animator = ObjectAnimator.ofPropertyValuesHolder(
                view,
                PropertyValuesHolder.ofFloat(View.TRANSLATION_Y,tY,goneY),
                PropertyValuesHolder.ofFloat(View.SCALE_Y,0F)

            ).apply {
               doOnEnd { view.visibility = View.GONE }
            }
            animator.start()

        }
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

        if(view is ConstraintLayout) {
            /*val set = ConstraintSet()
            set.clone(view as ConstraintLayout)

            view.visibility = ViewGroup.VISIBLE
            setHeightAndVisibility(set,view as ConstraintLayout,ConstraintSet.WRAP_CONTENT,View.VISIBLE)
            if(animate) {
                val autoTransition = AutoTransition()
                autoTransition.duration = 300;
                TransitionManager.beginDelayedTransition(view as ConstraintLayout, autoTransition)
            }
            set.applyTo(view as ConstraintLayout)*/

            val tY = view.translationY
            val goneY = ((view.top - (view.parent as ViewGroup).height ) / 3 ).toFloat()
            view.translationY = goneY
            val animator = ObjectAnimator.ofPropertyValuesHolder(
                view,
                PropertyValuesHolder.ofFloat(View.TRANSLATION_Y,tY,goneY),
                PropertyValuesHolder.ofFloat(View.SCALE_Y,1F)

            ).apply {
                doOnStart { view.visibility = View.VISIBLE }
            }
            animator.start()
        }/*else{
            val show = AnimationUtils.loadAnimation(context,R.anim.hide_height)
            view.startAnimation(show)
            view.visibility = ViewGroup.VISIBLE
        }*/
    }

    fun rotate (){
        val rotate = AnimationUtils.loadAnimation(this.context,R.anim.routate_pict)
        view.startAnimation(rotate)
    }

    fun rotateBack (){
        val rotate = AnimationUtils.loadAnimation(this.context,R.anim.rotate_pict_back)
        view.startAnimation(rotate)
    }

    private fun setHeightAndVisibility(set:ConstraintSet, changeView: ConstraintLayout, height:Int, visibility:Int){
        for (child in changeView.children){
            if(child is ConstraintLayout ){
                setHeightAndVisibility(set,child,height,visibility)
            }
            set.constrainHeight(child.id,height)
            set.setVisibility(child.id,visibility)

        }
    }
}