package com.ssquad.gps.camera.geotag.presentation.mainscreen.bases

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView

import com.ssquad.gps.camera.geotag.data.models.ThemeTemplateModel
import com.ssquad.gps.camera.geotag.databinding.ItemDetailTemplateBinding
import com.ssquad.gps.camera.geotag.databinding.ItemThemeHomeBinding
import com.ssquad.gps.camera.geotag.utils.loadImageIcon

sealed class BaseViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    abstract fun bind(item: ThemeTemplateModel)
    class ViewHolder(private val binding: ItemThemeHomeBinding) : BaseViewHolder(binding.root) {
        override fun bind(item: ThemeTemplateModel) {
            binding.imvTheme.loadImageIcon(item.image)
            binding.imvCheck.isVisible = item.isSelected
        }
        companion object{
            fun create(parent: ViewGroup): ViewHolder {
                val binding = ItemThemeHomeBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                return ViewHolder(binding)
            }
        }
    }
    class DetailViewHolder(private val binding: ItemDetailTemplateBinding) : BaseViewHolder(binding.root) {
        override fun bind(item: ThemeTemplateModel) {
            binding.imvTheme.loadImageIcon(item.image)
            binding.imvCheck.isVisible = item.isSelected
        }
        companion object{
            fun create(parent: ViewGroup): DetailViewHolder {
                val binding = ItemDetailTemplateBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                return DetailViewHolder(binding)
            }
        }
    }
}