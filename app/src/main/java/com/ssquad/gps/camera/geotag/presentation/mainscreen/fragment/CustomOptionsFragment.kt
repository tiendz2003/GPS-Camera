package com.ssquad.gps.camera.geotag.presentation.mainscreen.fragment


import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import com.ssquad.gps.camera.geotag.bases.BaseFragment
import com.ssquad.gps.camera.geotag.data.models.CustomTemplateModel
import com.ssquad.gps.camera.geotag.data.models.TemplateState
import com.ssquad.gps.camera.geotag.presentation.hometab.adapter.CustomOptionsAdapter
import com.ssquad.gps.camera.geotag.databinding.FragmentCustomOptionsBinding
import com.ssquad.gps.camera.geotag.presentation.viewmodel.PreviewShareViewModel
import com.ssquad.gps.camera.geotag.utils.CustomTemplateConfig
import com.ssquad.gps.camera.geotag.utils.GridSpaceItemDecoration
import com.ssquad.gps.camera.geotag.utils.dpToPx
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.activityViewModel

class CustomOptionsFragment :
    BaseFragment<FragmentCustomOptionsBinding>(FragmentCustomOptionsBinding::inflate) {

    private var customOptionsAdapter: CustomOptionsAdapter? = null
    private val previewViewModel: PreviewShareViewModel by activityViewModel()
    private var customTemplateList: List<CustomTemplateModel> = mutableListOf<CustomTemplateModel>()

    override fun initData() {
        customTemplateList = CustomTemplateModel.getCustomTemplates(requireContext())
    }

    override fun initView() {
        setupRecyclerView()
        observeViewModel()
    }

    override fun initActionView() {

    }

    private fun setupRecyclerView() {
        customOptionsAdapter = CustomOptionsAdapter { customTemplate, position ->
            customTemplate.isSelected = !customTemplate.isSelected
            customOptionsAdapter?.notifyItemChanged(position)
            val currentState = previewViewModel.previewUiState.value.templateState
            val newState = when (customTemplate.id) {
                CustomTemplateConfig.LOCATION -> currentState.copy(showLocation = customTemplate.isSelected)
                CustomTemplateConfig.LAT_LONG -> currentState.copy(showLatLong = customTemplate.isSelected)

                CustomTemplateConfig.TIME -> currentState.copy(showTime = customTemplate.isSelected)

                CustomTemplateConfig.DATE -> currentState.copy(showDate = customTemplate.isSelected)
                else -> currentState
            }
            previewViewModel.updateTemplateState(newState)
        }

        binding.rvCustomControls.apply {
            layoutManager = GridLayoutManager(requireContext(), 4)
            adapter = customOptionsAdapter
            addItemDecoration(
                GridSpaceItemDecoration(16.dpToPx(requireContext()))
            )
        }

        customOptionsAdapter?.submitList(customTemplateList.toList())
    }
    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                previewViewModel.previewUiState.collect { previewUiState ->
                    updateCustomState(previewUiState.templateState)
                }
            }
        }
    }
    private fun updateCustomState(state: TemplateState) {
        val updatedList = customTemplateList.map { item ->
            val isSelected = when (item.id) {
                CustomTemplateConfig.LOCATION -> state.showLocation
                CustomTemplateConfig.LAT_LONG -> state.showLatLong
                CustomTemplateConfig.TIME -> state.showTime
                CustomTemplateConfig.DATE -> state.showDate
                else -> item.isSelected
            }
            item.copy(isSelected = isSelected)
        }

        customTemplateList = updatedList

        customOptionsAdapter?.submitList(updatedList)
    }
}