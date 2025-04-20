package com.example.baseproject.presentation.settingtab.activity

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.baseproject.R
import com.example.baseproject.bases.BaseActivity
import com.example.baseproject.databinding.ActivityCustomDateTimeBinding
import com.example.baseproject.presentation.settingtab.dialog.CustomDateTimeDialog
import com.example.baseproject.utils.SharePrefManager
import com.example.baseproject.utils.formatToDateTime
import com.example.baseproject.utils.loadImageIcon
import java.util.Date

class CustomDateTimeActivity : BaseActivity<ActivityCustomDateTimeBinding>(
    ActivityCustomDateTimeBinding::inflate
) {
    private var selectedOption = "current"
    private var customDate = ""
    private var customTime = ""
    private var dialog: CustomDateTimeDialog? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
    }

    override fun initData() {
        val savedOption = SharePrefManager.getString("DATE_TIME_OPTION", "current") ?: "current"
        selectedOption = savedOption
        customDate = SharePrefManager.getString("CUSTOM_DATE", "") ?: ""
        customTime = SharePrefManager.getString("CUSTOM_TIME", "") ?: ""
    }

    @SuppressLint("SetTextI18n")
    override fun initView() {
        val now = Date()
        val currentTime = now.formatToDateTime()
        binding.current.text = currentTime

        if (customDate.isNotEmpty() && customTime.isNotEmpty()) {
            binding.custom.text = "$customDate $customTime"
        } else {
            binding.custom.text = getString(R.string.tap_to_set)
        }

        setChecked(selectedOption)
    }

    override fun initActionView() {
        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.btnCheck.setOnClickListener {
            saveSelectedOption()
            finish()
        }

        binding.ctCurrent.setOnClickListener {
            selectedOption = "current"
            setChecked(selectedOption)
        }

        binding.ctCustom.setOnClickListener {
            showCustomDateTimeDialog()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun showCustomDateTimeDialog() {
        dialog = CustomDateTimeDialog(
            this,
            customDate,
            customTime
        ) {
            selectedOption = "custom"
            setChecked(selectedOption)
            customDate = dialog?.currDate ?: ""
            customTime = dialog?.currTime ?: ""
            binding.custom.text = "$customDate $customTime"
        }
        dialog?.setCancelable(false)
        dialog?.showDialog()
    }

    private fun saveSelectedOption() {
        SharePrefManager.putString("DATE_TIME_OPTION", selectedOption)
        if (selectedOption == "custom") {
            SharePrefManager.putString("CUSTOM_DATE", customDate)
            SharePrefManager.putString("CUSTOM_TIME", customTime)
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun setChecked(type: String) {
        when (type) {
            "current" -> {
                binding.cbCheckCurrent.loadImageIcon(R.drawable.ic_selected)
                binding.cbCheckCustom.loadImageIcon(R.drawable.ic_unchecked_language)
            }

            "custom" -> {
                binding.cbCheckCurrent.loadImageIcon(R.drawable.ic_unchecked_language)
                binding.cbCheckCustom.loadImageIcon(R.drawable.ic_selected)
            }
        }
    }
}