package com.ssquad.gps.camera.geotag.presentation.settingtab.fragment

import android.graphics.Bitmap
import android.location.Location
import android.util.Log
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapboxMap
import com.mapbox.maps.Style
import com.mapbox.maps.extension.style.layers.properties.generated.IconAnchor
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.PointAnnotation
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationManager
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createPointAnnotationManager
import com.mapbox.search.autocomplete.PlaceAutocomplete
import com.mapbox.search.autocomplete.PlaceAutocompleteOptions
import com.mapbox.search.autocomplete.PlaceAutocompleteSuggestion
import com.ssquad.gps.camera.geotag.R
import com.ssquad.gps.camera.geotag.bases.BaseFragment
import com.ssquad.gps.camera.geotag.databinding.FragmentSearchLocationBinding
import com.ssquad.gps.camera.geotag.presentation.settingtab.activity.MapSettingState
import com.ssquad.gps.camera.geotag.presentation.settingtab.adapter.PlaceAutoCompleteAdapter
import com.ssquad.gps.camera.geotag.presentation.viewmodel.MapSettingViewModel
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

    private val placeAutoComplete by lazy {
        PlaceAutocomplete.create()
    }
    private val viewModel: MapSettingViewModel by activityViewModel()
    private lateinit var pointAnnotationManager: PointAnnotationManager
    private var currentPointAnnotation: PointAnnotation? = null
    private lateinit var mapboxMap: MapboxMap
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<*>
    private var searchJob: Job? = null
    private var placeAutoCompleteAdapter: PlaceAutoCompleteAdapter? = null
    override fun initData() {

    }

    override fun initView() {

        setupMapView()
        setupRecycleView()
        setupSearchUI()
        setupBottomSheet()
        observeViewModel()

    }

    override fun initActionView() {

    }

    private fun setupMapView() {
        // Khởi tạo bản đồ
        mapboxMap = binding.mapView.mapboxMap

        // Tải style bản đồ
        mapboxMap.loadStyle(Style.MAPBOX_STREETS) {
            viewModel.mapSettingState.value.currentLocation?.let { location ->
                val annotationPlugin = binding.mapView.annotations
                pointAnnotationManager = annotationPlugin.createPointAnnotationManager()

                updateMapCamera(location)
            }
        }
    }

    private fun updateMapCamera(location: Location) {
        val cameraPosition = CameraOptions.Builder()
            .center(Point.fromLngLat(location.longitude, location.latitude))
            .zoom(10.0)
            .build()
        mapboxMap.setCamera(cameraPosition)
        updateMarker(
            Point.fromLngLat(location.longitude, location.latitude)
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
            override fun onStateChanged(bottomSheet: android.view.View, newState: Int) {
                if (newState == BottomSheetBehavior.STATE_EXPANDED) {
                    hideSearch()
                    binding.edtSearch.clearFocus()
                }
            }

            override fun onSlide(bottomSheet: android.view.View, slideOffset: Float) {}
        })
    }

    private fun setupRecycleView() {
        placeAutoCompleteAdapter = PlaceAutoCompleteAdapter { suggestion ->
            onPlaceSuggestionSelected(suggestion)
            binding.edtSearch.setText(suggestion.name)
            binding.edtSearch.clearFocus()
            searchJob?.cancel()
            showBottomSheet()
            hideKeyboard()
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
                        delay(300)
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
                val response = placeAutoComplete.suggestions(
                    query = query,
                    options = PlaceAutocompleteOptions(
                        limit = 10
                    ),
                    proximity = viewModel.mapSettingState.value.currentLocation?.let { location ->
                        Point.fromLngLat(location.longitude, location.latitude)
                    } ?: Point.fromLngLat(0.0, 0.0),
                )
                if (response.isValue) {
                    val suggestion = requireNotNull(response.value)
                    if (suggestion.isNotEmpty()) {
                        Log.d("SearchLocationFragment", "Kết quả tìm kiếm: $suggestion")
                        showPlaceSuggestion(suggestion)
                    } else {
                        hideSearch()
                        showBottomSheet()
                    }
                }
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
                val selectionResponse = placeAutoComplete.select(suggestion = suggestion)
                selectionResponse.onValue { result ->
                    binding.progressIndicator.gone()
                    val coordinate = result.coordinate
                    val location = Location("SearchResult").apply {
                        latitude = coordinate.latitude()
                        longitude = coordinate.longitude()
                    }
                    updateMapCamera(location)

                }.onError { e ->
                    Log.e("SearchLocationFragment", "Lỗi khi chọn địa điểm: ${e.message}")
                    showBottomSheet()
                }
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

    private fun updateMarker(point: Point) {
        currentPointAnnotation?.let {
            pointAnnotationManager.delete(it)
        }
        val icon = AppCompatResources.getDrawable(requireContext(), R.drawable.ic_location)
        val bitmap = icon?.let { drawable ->
            val bitmap = Bitmap.createBitmap(
                drawable.intrinsicWidth,
                drawable.intrinsicHeight,
                Bitmap.Config.ARGB_8888
            )
            val canvas = android.graphics.Canvas(bitmap)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)
            bitmap
        } ?: return
        val pointAnnotation = PointAnnotationOptions()
            .withPoint(point)
            .withIconImage(bitmap)
            .withIconAnchor(IconAnchor.BOTTOM)
        currentPointAnnotation = pointAnnotationManager.create(pointAnnotation)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        searchJob?.cancel()
    }
}