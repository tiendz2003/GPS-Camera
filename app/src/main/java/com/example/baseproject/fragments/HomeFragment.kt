package com.example.baseproject.fragments

import android.content.Intent
import android.util.Log
import com.example.baseproject.bases.BaseFragment
import com.example.baseproject.databinding.FragmentHomeBinding
import com.example.baseproject.models.ThemeTemplateModel
import com.example.baseproject.presentation.custom.HorizontalSpaceItemDecoration
import com.example.baseproject.presentation.hometab.activities.TemplatesActivity
import com.example.baseproject.presentation.hometab.adapter.ThemeTemplateAdapter
import com.example.baseproject.utils.dpToPx


class HomeFragment : BaseFragment<FragmentHomeBinding>(FragmentHomeBinding::inflate) {
    private lateinit var adapter: ThemeTemplateAdapter
    val listTheme = ThemeTemplateModel.getTemplate()
    override fun initData() {
    }

    override fun initView() {
        Log.d("RecyclerView", "List size: ${listTheme.size}")
        adapter = ThemeTemplateAdapter {
            Log.d("RecyclerView", "Binding item: ${it.id}")
        }
        binding.rcvTheme.adapter = adapter
        binding.rcvTheme.addItemDecoration(
            HorizontalSpaceItemDecoration(16.dpToPx(requireContext()),4.dpToPx(requireContext()))
        )
        binding.rcvTheme.setHasFixedSize(true)
        adapter.submitList(listTheme)

    }

    override fun initActionView() {
        binding.viewAllText.setOnClickListener {
            val intent = Intent(requireContext(), TemplatesActivity::class.java)
            startActivity(intent)
        }
    }


}