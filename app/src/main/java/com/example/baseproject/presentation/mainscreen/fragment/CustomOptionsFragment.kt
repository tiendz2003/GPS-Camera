package com.example.baseproject.presentation.mainscreen.fragment


import android.annotation.SuppressLint
import androidx.recyclerview.widget.GridLayoutManager
import com.example.baseproject.bases.BaseFragment
import com.example.baseproject.data.models.CustomTemplateModel
import com.example.baseproject.databinding.FragmentCustomOptionsBinding
import com.example.baseproject.presentation.custom.HorizontalSpaceItemDecoration
import com.example.baseproject.presentation.hometab.adapter.CustomOptionsAdapter
import com.example.baseproject.utils.GridSpaceItemDecoration
import com.example.baseproject.utils.dpToPx

class CustomOptionsFragment : BaseFragment<FragmentCustomOptionsBinding>(FragmentCustomOptionsBinding::inflate) {

    private var customOptionsAdapter: CustomOptionsAdapter? = null

    override fun initData() {

    }

    override fun initView() {
        setupRecyclerView()
    }

    override fun initActionView() {

    }
    @SuppressLint("NotifyDataSetChanged")
    private fun setupRecyclerView() {
        customOptionsAdapter = CustomOptionsAdapter { customTemplate ->

            customTemplate.isSelected = !customTemplate.isSelected
            customOptionsAdapter?.notifyDataSetChanged()

        }

        binding.rvCustomControls.apply {
            layoutManager = GridLayoutManager(requireContext(), 4)
            adapter = customOptionsAdapter
            addItemDecoration(
                GridSpaceItemDecoration(16.dpToPx(requireContext()))
            )
        }

        customOptionsAdapter?.submitList(CustomTemplateModel.getCustomTemplates(requireContext()))
    }
    companion object {
        @JvmStatic
        fun newInstance() = CustomOptionsFragment()
    }
}