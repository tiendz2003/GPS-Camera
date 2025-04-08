package com.example.baseproject.presentation.hometab.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.core.content.ContextCompat
import com.example.baseproject.bases.BaseActivity
import com.example.baseproject.databinding.ActivityMainBinding
import androidx.core.view.get
import androidx.fragment.app.Fragment
import com.example.baseproject.R
import com.example.baseproject.fragments.HomeFragment
import com.example.baseproject.fragments.SettingsFragment
import com.example.baseproject.presentation.mainscreen.activity.CameraActivity
import com.example.baseproject.utils.dpToPx
import com.example.baseproject.utils.updateCornerSize
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.ShapeAppearanceModel

class MainActivity : BaseActivity<ActivityMainBinding>(ActivityMainBinding::inflate) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        if(savedInstanceState == null){
            supportFragmentManager.beginTransaction()
                .add(R.id.fragmentContainer, HomeFragment())
                .commit()
        }
    }
    override fun initData() {

    }

    override fun initView() {

        binding.bottomNavigationView.background = null
        binding.bottomNavigationView.menu[1].isEnabled = false

        val materialShapeDrawable = binding.bottomAppBar.background as MaterialShapeDrawable
        materialShapeDrawable.updateCornerSize(this)

    }

    override fun initActionView() {
        setupBottomNavigation()
        setupFab()

    }
    private fun setupBottomNavigation (){
        binding.bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> replaceFragment(HomeFragment())
                R.id.nav_setting -> replaceFragment(SettingsFragment())
            }
            true
        }
    }
    private fun setupFab(){
        binding.fab.setOnClickListener {
            startActivity(Intent(this, CameraActivity::class.java))
        }
    }
    private fun replaceFragment(fragment: Fragment){
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.fragmentContainer, fragment).commit()
    }
}
