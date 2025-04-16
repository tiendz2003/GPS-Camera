package com.example.baseproject.fragments

import android.content.Intent
import android.util.Log
import com.example.baseproject.bases.BaseFragment
import com.example.baseproject.databinding.FragmentHomeBinding
import com.example.baseproject.data.models.ThemeTemplateModel
import com.example.baseproject.presentation.hometab.activities.EditAlbumLibraryActivity
import com.example.baseproject.presentation.hometab.activities.MediaSavedActivity
import com.example.baseproject.presentation.hometab.activities.PreviewTemplateActivity
import com.example.baseproject.presentation.hometab.activities.TemplatesActivity
import com.example.baseproject.presentation.hometab.adapter.ThemeTemplateAdapter
import com.example.baseproject.utils.setupHorizontal


class HomeFragment : BaseFragment<FragmentHomeBinding>(FragmentHomeBinding::inflate) {
    private lateinit var adapter: ThemeTemplateAdapter
    val listTheme = ThemeTemplateModel.getTemplate()
    override fun initData() {
    }

    override fun initView() {
        Log.d("RecyclerView", "List size: ${listTheme.size}")
        adapter = ThemeTemplateAdapter {
            Log.d("RecyclerView", "Binding item: ${it.id}")
            navToPreview(it)
        }
        adapter.submitList(listTheme)
        binding.rcvTheme.setupHorizontal(adapter)
    }

    override fun initActionView() {
        binding.viewAllText.setOnClickListener {
            val intent = Intent(requireContext(), TemplatesActivity::class.java)
            startActivity(intent)
        }
        binding.editPhotoCard.setOnClickListener {
            val intent = Intent(requireContext(), EditAlbumLibraryActivity::class.java)
            startActivity(intent)
        }
        binding.savedImageCard.setOnClickListener {
            startActivity(MediaSavedActivity.getIntent(requireContext(),false))
        }
        binding.savedVideoCard.setOnClickListener {
            startActivity(MediaSavedActivity.getIntent(requireContext(), true))
        }
    }
    fun navToPreview(selectedTemplate: ThemeTemplateModel) {
        val themeType = selectedTemplate.type
        val filterList = ThemeTemplateModel.getTemplate().filter { it.type == themeType } as ArrayList<ThemeTemplateModel>
        val intent = PreviewTemplateActivity.getIntent(requireContext(), selectedTemplate, filterList,themeType)
        startActivity(intent)
    }

}