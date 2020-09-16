package com.example.electroniktrack_tko

import android.R
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NavUtils

class SettingsActivity : AppCompatActivity() {
    var settingsFragment = SettingFragment()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(com.example.ekotransservice_routemanager.R.layout.activity_settings)
        val actionBar = this.supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)
        val preferenceManager = settingsFragment.preferenceManager
        supportFragmentManager.beginTransaction().replace(com.example.ekotransservice_routemanager.R.id.fl_main_settings, settingsFragment)
            .commit()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == R.id.home) {
            NavUtils.navigateUpFromSameTask(this)
        }
        return super.onOptionsItemSelected(item)
    }
}