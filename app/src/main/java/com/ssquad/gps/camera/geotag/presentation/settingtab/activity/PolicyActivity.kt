package com.ssquad.gps.camera.geotag.presentation.settingtab.activity

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.ssquad.gps.camera.geotag.BuildConfig
import com.ssquad.gps.camera.geotag.R
import com.ssquad.gps.camera.geotag.bases.BaseActivity
import com.ssquad.gps.camera.geotag.databinding.ActivityPolicyBinding
import androidx.core.net.toUri

private const val URL = "https://edeo-hk.com/privacy-policy.html?app=GPS%20Camera:%20Geotag%20Photos&store=EDEO%20TECH%20LIMITED"
class PolicyActivity : BaseActivity<ActivityPolicyBinding>(ActivityPolicyBinding::inflate) {
    override fun initData() {
    }

    @SuppressLint("SetTextI18n")
    override fun initView() {
        binding.tvVersion.text = getString(R.string.version) + " " + BuildConfig.VERSION_NAME
    }

    override fun initActionView() {
        with(binding) {

            imgBack.setOnClickListener {
                finish()
            }

            tvTerm.setOnClickListener {
                openUrl()
            }
        }
    }
    private fun openUrl() {
        val intent = Intent(Intent.ACTION_VIEW, URL.toUri())
        intent.addCategory(Intent.CATEGORY_BROWSABLE)
        try {
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Log.d("", e.toString())
        }
    }
}