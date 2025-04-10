package com.example.baseproject.presentation.mainscreen.fragment


import androidx.recyclerview.widget.LinearLayoutManager
import com.example.baseproject.bases.BaseFragment
import com.example.baseproject.data.models.ThemeTemplateModel
import com.example.baseproject.databinding.FragmentPreviewOptionsBinding
import com.example.baseproject.presentation.custom.HorizontalSpaceItemDecoration
import com.example.baseproject.presentation.hometab.adapter.PreviewOptionsAdapter
import com.example.baseproject.utils.dpToPx


class PreviewOptionsFragment : BaseFragment<FragmentPreviewOptionsBinding>(
    FragmentPreviewOptionsBinding::inflate
) {
    private var previewOptionsAdapter: PreviewOptionsAdapter? = null

    override fun initData() {

    }

    override fun initView() {
        setupRecyclerView()
    }

    override fun initActionView() {

    }
    private fun setupRecyclerView() {
        previewOptionsAdapter = PreviewOptionsAdapter(){position,selectedTheme->
        }
        binding.rvTemplates.apply {
            setHasFixedSize(true)
            adapter = previewOptionsAdapter
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            addItemDecoration(
                HorizontalSpaceItemDecoration(16.dpToPx(requireContext()),4.dpToPx(requireContext()))
            )
        }
        previewOptionsAdapter?.submitList(ThemeTemplateModel.getTemplate())
    }
    companion object {
        @JvmStatic
        fun newInstance() =
            PreviewOptionsFragment()
    }
}