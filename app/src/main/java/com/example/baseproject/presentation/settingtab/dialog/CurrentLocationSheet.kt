package com.example.baseproject.presentation.settingtab.dialog

import android.annotation.SuppressLint
import android.app.Dialog
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import com.example.baseproject.R
import com.example.baseproject.databinding.BottomSheetAddressBinding
import com.example.baseproject.utils.formatToDate
import com.example.baseproject.utils.formatToTime
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import java.util.Date

class CurrentLocationSheet(
    private var location: Location,
    private var address: String,
): BottomSheetDialogFragment() {
    private var _binding: BottomSheetAddressBinding? = null
    private val binding get() = _binding!!

    override fun getTheme(): Int {
        return R.style.BottomSheetDialogTheme
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        dialog.setOnShowListener { dialogInterface ->
            val sheet = (dialogInterface as BottomSheetDialog)
                .findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)

            sheet?.let {
                val behavior = BottomSheetBehavior.from(it)
                behavior.isHideable = false
                behavior.state = BottomSheetBehavior.STATE_EXPANDED
                behavior.skipCollapsed = true
            }

            dialog.window?.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
            dialog.window?.addFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
            dialog.window?.setBackgroundDrawable(null)
        }
        return dialog
    }



    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        isCancelable = false
        _binding = BottomSheetAddressBinding.inflate(inflater, container, false)
        return binding.root
    }
    fun updateLocationInfo(newLocation: Location, newAddress: String) {
        this.location = newLocation
        this.address = newAddress

        // view co roi -> updateUI()
        if (_binding != null) {
            updateUI()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updateUI() {
        val now = Date()
        with(binding) {
            tvLocationTitle.text = address
            tvFullAddress.text = address
            tvLatitude.text = "${Location.convert(location.latitude, Location.FORMAT_MINUTES).replace(":", "°").replace(".", "'")}\"N"
            tvLongitude.text = "${Location.convert(location.longitude, Location.FORMAT_MINUTES).replace(":", "°").replace(".", "'")}\"E"
            tvTime.text = now.formatToTime()
            tvDate.text = now.formatToDate()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        updateUI()
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}