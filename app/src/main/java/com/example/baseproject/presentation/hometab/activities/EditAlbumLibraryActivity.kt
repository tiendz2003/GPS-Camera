package com.example.baseproject.presentation.hometab.activities

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.baseproject.R
import com.example.baseproject.bases.BaseActivity
import com.example.baseproject.databinding.ActivityAlbumLibraryBinding

class EditAlbumLibraryActivity : BaseActivity<ActivityAlbumLibraryBinding>(
    ActivityAlbumLibraryBinding::inflate
) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

    }

    override fun initData() {
    }

    override fun initView() {

    }

    override fun initActionView() {

    }
}