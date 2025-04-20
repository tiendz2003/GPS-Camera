package com.example.baseproject.bases

import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.example.baseproject.databinding.ItemDetailTemplateBinding
import com.example.baseproject.databinding.ItemThemeHomeBinding
import com.example.baseproject.data.models.ThemeTemplateModel
import com.example.baseproject.utils.SharePrefManager
import com.example.baseproject.utils.loadImageIcon

sealed class BaseViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    abstract fun bind(item: ThemeTemplateModel)
    class ViewHolder(private val binding: ItemThemeHomeBinding) : BaseViewHolder(binding.root) {
        override fun bind(item: ThemeTemplateModel) {
            binding.imvTheme.loadImageIcon(item.image)
            binding.imvCheck.isVisible = item.isSelected
        }
        companion object{
            fun create(parent: ViewGroup):ViewHolder{
                val binding = ItemThemeHomeBinding.inflate(
                    android.view.LayoutInflater.from(parent.context),
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
            fun create(parent: ViewGroup):DetailViewHolder{
                val binding = ItemDetailTemplateBinding.inflate(
                    android.view.LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                return DetailViewHolder(binding)
            }
        }
    }
}