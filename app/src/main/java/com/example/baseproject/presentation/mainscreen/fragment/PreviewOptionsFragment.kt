package com.example.baseproject.presentation.mainscreen.fragment


import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.baseproject.bases.BaseFragment
import com.example.baseproject.data.models.ThemeTemplateModel
import com.example.baseproject.databinding.FragmentPreviewOptionsBinding
import com.example.baseproject.presentation.custom.HorizontalSpaceItemDecoration
import com.example.baseproject.presentation.hometab.adapter.PreviewOptionsAdapter
import com.example.baseproject.presentation.viewmodel.PreviewShareViewModel
import com.example.baseproject.utils.dpToPx
import com.example.baseproject.utils.scrollToCenter
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.activityViewModel


class PreviewOptionsFragment : BaseFragment<FragmentPreviewOptionsBinding>(
    FragmentPreviewOptionsBinding::inflate
) {

    private var previewOptionsAdapter: PreviewOptionsAdapter? = null
    private val previewViewModel: PreviewShareViewModel by activityViewModel()
    private var selectedTemplate: ThemeTemplateModel? = null
    override fun initData() {

    }

    override fun initView() {
        setupRecyclerView()
        observeViewModel()
    }

    override fun initActionView() {

    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                previewViewModel.selectedTemplateId.collect { selectedTemplateId ->
                    selectedTemplateId?.let {
                        selectedTemplate = ThemeTemplateModel.getThemeById(it)
                        val selectedPosition =
                            ThemeTemplateModel.getTemplate().indexOf(selectedTemplate)
                        previewOptionsAdapter?.updateSelection(selectedPosition)
                        binding.rvTemplates.post {
                            binding.rvTemplates.scrollToCenter(selectedPosition)
                        }
                    }

                }
            }
        }
    }

    private fun setupRecyclerView() {
        previewOptionsAdapter = PreviewOptionsAdapter { position, selectedTheme ->
            previewViewModel.setSelectedTemplate(selectedTheme.id)
        }
        binding.rvTemplates.apply {
            setHasFixedSize(true)
            adapter = previewOptionsAdapter
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            addItemDecoration(
                HorizontalSpaceItemDecoration(
                    16.dpToPx(requireContext()),
                    4.dpToPx(requireContext())
                )
            )
        }
        val themeList = ThemeTemplateModel.getTemplate()
        previewOptionsAdapter?.submitList(themeList) {

        }
    }

}