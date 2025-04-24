package com.ssquad.gps.camera.geotag.presentation.settingtab.dialog

import android.annotation.SuppressLint
import android.app.Dialog
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import com.ssquad.gps.camera.geotag.utils.formatToDate
import com.ssquad.gps.camera.geotag.utils.formatToTime
import com.google.android.material.R
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.ssquad.gps.camera.geotag.R.*
import com.ssquad.gps.camera.geotag.databinding.BottomSheetAddressBinding
import java.util.Date

class CurrentLocationSheet(): BottomSheetDialogFragment() {
    private var _binding: BottomSheetAddressBinding? = null
    private val binding get() = _binding!!
    private var location: Location? = null
    private var address: String = ""
    private var isLoading: Boolean = false

    companion object {
        private const val ARG_LOCATION_LAT = "arg_location_lat"
        private const val ARG_LOCATION_LONG = "arg_location_long"
        private const val ARG_ADDRESS = "arg_address"
        private const val ARG_IS_LOADING = "arg_is_loading"

        fun newInstance(location: Location, address: String, isLoading: Boolean = false): CurrentLocationSheet {
            val fragment = CurrentLocationSheet()
            val args = Bundle().apply {
                putDouble(ARG_LOCATION_LAT, location.latitude)
                putDouble(ARG_LOCATION_LONG, location.longitude)
                putString(ARG_ADDRESS, address)
                putBoolean(ARG_IS_LOADING, isLoading)
            }
            fragment.arguments = args
            return fragment
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let { args ->
            val lat = args.getDouble(ARG_LOCATION_LAT)
            val lng = args.getDouble(ARG_LOCATION_LONG)
            location = Location("").apply {
                latitude = lat
                longitude = lng
            }
            address = args.getString(ARG_ADDRESS, "")
            isLoading = args.getBoolean(ARG_IS_LOADING, false)
        }
    }
    override fun getTheme(): Int {
        return style.BottomSheetDialogTheme
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        dialog.setOnShowListener { dialogInterface ->
            val sheet = (dialogInterface as BottomSheetDialog)
                .findViewById<View>(R.id.design_bottom_sheet)

            sheet?.let {
                val behavior = BottomSheetBehavior.from(it)
                behavior.isHideable = true
                behavior.state = BottomSheetBehavior.STATE_EXPANDED
                behavior.skipCollapsed = true
            }

            dialog.window?.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
            dialog.window?.setBackgroundDrawable(null)
            dialog.setCanceledOnTouchOutside(true)

        }
        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        isCancelable = true
        _binding = BottomSheetAddressBinding.inflate(inflater, container, false)
        return binding.root
    }
    fun updateLocationInfo(newLocation: Location, newAddress: String, loading: Boolean = false) {
        this.location = newLocation
        this.address = newAddress
        this.isLoading = loading

        if (_binding != null) {
            updateUI()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updateUI() {
        location?.let { loc ->
            val now = Date()
            with(binding) {
                if (isLoading) {
                    tvLocationTitle.text = getString(string.loading_address)
                } else {
                    tvLocationTitle.text = address
                    tvFullAddress.text = address
                    tvLatitude.text = "${Location.convert(loc.latitude, Location.FORMAT_MINUTES).replace(":", "°").replace(".", "'")}\"N"
                    tvLongitude.text = "${Location.convert(loc.longitude, Location.FORMAT_MINUTES).replace(":", "°").replace(".", "'")}\"E"
                    tvTime.text = now.formatToTime()
                    tvDate.text = now.formatToDate()
                }
            }
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