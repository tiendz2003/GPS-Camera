package com.ssquad.gps.camera.geotag.presentation.settingtab.dialog

import android.annotation.SuppressLint
import android.app.Dialog
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.ssquad.gps.camera.geotag.utils.formatToDate
import com.ssquad.gps.camera.geotag.utils.formatToTime
import com.ssquad.gps.camera.geotag.R
import com.ssquad.gps.camera.geotag.databinding.BottomSheetAddressBinding
import java.util.Date

class CurrentLocationSheet : Fragment() {

    private var _binding: BottomSheetAddressBinding? = null
    private val binding get() = _binding!!

    private var location: Location? = null
    private var address: String = ""
    private var isLoading: Boolean = false

    companion object {
        fun newInstance(location: Location, address: String, isLoading: Boolean = false): CurrentLocationSheet {
            val fragment = CurrentLocationSheet()
            val args = Bundle().apply {
                putDouble("lat", location.latitude)
                putDouble("lng", location.longitude)
                putString("address", address)
                putBoolean("loading", isLoading)
            }
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            location = Location("").apply {
                latitude = it.getDouble("lat")
                longitude = it.getDouble("lng")
            }
            address = it.getString("address", "")
            isLoading = it.getBoolean("loading", false)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = BottomSheetAddressBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        updateUI()
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
                tvLocationTitle.text = if (isLoading) getString(R.string.loading_address) else address
                tvFullAddress.text = address
                tvLatitude.text = "${Location.convert(loc.latitude, Location.FORMAT_MINUTES).replace(":", "°").replace(".", "'")}\"N"
                tvLongitude.text = "${Location.convert(loc.longitude, Location.FORMAT_MINUTES).replace(":", "°").replace(".", "'")}\"E"
                tvTime.text = now.formatToTime()
                tvDate.text = now.formatToDate()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
