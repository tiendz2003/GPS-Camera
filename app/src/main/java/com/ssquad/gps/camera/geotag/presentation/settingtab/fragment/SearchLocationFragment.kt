package com.ssquad.gps.camera.geotag.presentation.settingtab.fragment

import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.mapbox.maps.Style
import com.mapbox.search.autocomplete.PlaceAutocompleteSuggestion
import com.ssquad.gps.camera.geotag.R
import com.ssquad.gps.camera.geotag.presentation.mainscreen.bases.BaseFragment
import com.ssquad.gps.camera.geotag.databinding.FragmentSearchLocationBinding
import com.ssquad.gps.camera.geotag.presentation.settingtab.activity.MapSettingState
import com.ssquad.gps.camera.geotag.presentation.settingtab.adapter.PlaceAutoCompleteAdapter
import com.ssquad.gps.camera.geotag.presentation.viewmodel.MapSettingViewModel
import com.ssquad.gps.camera.geotag.service.MapboxManager
import com.ssquad.gps.camera.geotag.utils.gone
import com.ssquad.gps.camera.geotag.utils.setOnDebounceClickListener
import com.ssquad.gps.camera.geotag.utils.visible
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale


class SearchLocationFragment :
    BaseFragment<FragmentSearchLocationBinding>(FragmentSearchLocationBinding::inflate) {
    private val viewModel: MapSettingViewModel by activityViewModel()
    private lateinit var mapManager:MapboxManager
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<*>
    private var searchJob: Job? = null
    private var placeAutoCompleteAdapter: PlaceAutoCompleteAdapter? = null
    override fun initData() {

    }

    override fun initView() {
        mapManager = MapboxManager(requireContext())
        setupMapView()
        setupRecycleView()
        setupSearchUI()
        setupBottomSheet()
        observeViewModel()

    }

    override fun initActionView() {

    }

    private fun setupMapView() {
        mapManager.initializeMap(
            mapView = binding.mapView,
            mapStyle = Style.SATELLITE_STREETS,
            onMapReady = {mapboxMap->
                viewModel.mapSettingState.value.currentLocation?.let { location ->
                    mapManager.moveCameraToLocation(location)
                }
            }
        )

    }


    private fun updateUI(state: MapSettingState) {
        // Cập nhật trạng thái loading
        binding.progressIndicator.isVisible = state.isLoading

        // Cập nhật thông tin địa chỉ trong bottom sheet
        binding.tvLocationTitle.text = state.currentAddress ?: getString(R.string.unknow_location)
        binding.tvDetailedAddress.text = state.currentAddress ?: getString(R.string.unknow_location)

        // Cập nhật thông tin tọa độ
        state.currentLocation?.let { location ->
            binding.tvLatitude.text = String.format(Locale.getDefault(), "%.5f", location.latitude)
            binding.tvLongitude.text =
                String.format(Locale.getDefault(), "%.5f", location.longitude)
        }
        updateDateTime()
    }

    private fun setupBottomSheet() {
        bottomSheetBehavior = BottomSheetBehavior.from(binding.bottomSheet)
        bottomSheetBehavior.peekHeight =
            resources.getDimensionPixelSize(R.dimen.bottom_sheet_peek_height)
        bottomSheetBehavior.addBottomSheetCallback(object :
            BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if (newState == BottomSheetBehavior.STATE_EXPANDED) {
                    hideSearch()
                    binding.edtSearch.clearFocus()
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {}
        })
    }

    private fun setupRecycleView() {
        placeAutoCompleteAdapter = PlaceAutoCompleteAdapter { suggestion ->
            onPlaceSuggestionSelected(suggestion)
            binding.edtSearch.setText(suggestion.name)
            binding.edtSearch.clearFocus()
            binding.tvLocationTitle.text = suggestion.name
            binding.tvDetailedAddress.text = suggestion.formattedAddress
            suggestion.coordinate?.let { coordinate ->
                binding.tvLatitude.text = String.format(
                    Locale.getDefault(),
                    "%.5f",
                    coordinate.latitude()
                )
                binding.tvLongitude.text = String.format(
                    Locale.getDefault(),
                    "%.5f",
                    coordinate.longitude()
                )
            }
        }
        binding.recyclerView.adapter = placeAutoCompleteAdapter
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.gone()
    }

    private fun setupSearchUI() {
        with(binding) {
            edtSearch.doAfterTextChanged { text ->
                val query = text.toString().trim()
                if (query.length > 2) {
                    hideBottomSheet()
                    searchJob?.cancel()
                    searchJob = lifecycleScope.launch {
                        delay(500) // Thời gian trễ 500ms trước khi thực hiện tìm kiếm
                        performSearch(query)
                    }
                } else if (query.isEmpty()) {
                    hideSearch()
                    showBottomSheet()
                }
            }
            btnSearch.setOnDebounceClickListener {
                val query = binding.edtSearch.text.toString().trim()
                if (query.isNotEmpty()) {
                    hideBottomSheet()
                    performSearch(query)
                } else {
                    showBottomSheet()
                    hideSearch()
                }
            }
            edtSearch.setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    val query = binding.edtSearch.text.toString().trim()
                    if (query.isNotEmpty()) {
                        hideBottomSheet()
                        performSearch(query)
                        // Ẩn bàn phím
                        hideKeyboard()
                    }
                    return@setOnEditorActionListener true
                }
                false
            }
            edtSearch.setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    hideBottomSheet()
                }
            }
        }
    }

    private fun performSearch(query: String) {
        binding.progressIndicator.visible()
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                mapManager.searchPlaces(
                    query = query,
                    proximityLocation = viewModel.mapSettingState.value.currentLocation,
                    onSearchStarted = {
                        binding.progressIndicator.visible()
                    },
                    onSearchResult = {suggestion->
                        binding.progressIndicator.gone()
                        if(suggestion.isNotEmpty()){
                            showPlaceSuggestion(suggestion)
                        }else{
                            hideSearch()
                            showBottomSheet()
                        }
                    }
                )
            }
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.mapSettingState.collect { state ->
                    updateUI(state)
                }
            }
        }
    }

    private fun showPlaceSuggestion(suggestions: List<PlaceAutocompleteSuggestion>) {
        placeAutoCompleteAdapter?.submitList(suggestions)
        binding.recyclerView.visible()
    }

    private fun hideSearch() {
        binding.recyclerView.gone()

    }

    private fun hideBottomSheet() {
        // Ẩn BottomSheet bằng cách đặt state là HIDDEN
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
    }

    private fun showBottomSheet() {
        // Hiện BottomSheet bằng cách đặt state là COLLAPSED
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
    }

    private fun onPlaceSuggestionSelected(suggestion: PlaceAutocompleteSuggestion) {
        binding.progressIndicator.visible()
        hideSearch()
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                mapManager.selectPlace(
                    suggestion = suggestion,
                    onSuccess = {location->
                        binding.progressIndicator.gone()
                        mapManager.moveCameraToLocation(location)
                        viewModel.updateSelectedLocation(location)
                        searchJob?.cancel()
                        showBottomSheet()
                        hideKeyboard()
                    },
                    onError = {
                        binding.progressIndicator.gone()
                        Log.e("SearchLocationFragment", "Lỗi khi chọn địa điểm: ${it.message}")
                        showBottomSheet()
                    }
                )
            }
        }
    }

    private fun hideKeyboard() {
        val imm = requireActivity().getSystemService(InputMethodManager::class.java)
        imm?.hideSoftInputFromWindow(binding.edtSearch.windowToken, 0)
    }

    private fun updateDateTime() {
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("EEE, dd MMM yyyy", Locale.getDefault())
        val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())

        binding.tvDate.text = dateFormat.format(calendar.time)
        binding.tvTime.text = timeFormat.format(calendar.time)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        searchJob?.cancel()
        mapManager.cleanUp()
    }
}