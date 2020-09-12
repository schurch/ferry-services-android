package com.stefanchurch.ferryservices

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil.setContentView
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import com.stefanchurch.ferryservices.databinding.MainActivityBinding

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView<MainActivityBinding>(this, R.layout.main_activity)

        val navController = (supportFragmentManager.findFragmentById(R.id.nav_host) as NavHostFragment).navController
        val appBarConfiguration = AppBarConfiguration(navController.graph)
        NavigationUI.setupWithNavController(findViewById(R.id.toolbar), navController, appBarConfiguration)
    }

}