package com.example.baseproject.presentation.hometab.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.baseproject.R
import com.example.baseproject.databinding.ItemThemeOptionBinding
import com.example.baseproject.data.models.ThemeTemplateModel
import com.example.baseproject.utils.gone
import com.example.baseproject.utils.loadImageIcon
import com.example.baseproject.utils.visible


class PreviewOptionsAdapter(
    private val onThemeOptionSelected: (Int, ThemeTemplateModel) -> Unit
) : ListAdapter<ThemeTemplateModel, PreviewOptionsAdapter.ThemeOptionViewHolder>(ThemeDiffCallback()) {

    private var selectedPosition = 0

    fun updateSelection(position: Int) {
        val oldPosition = selectedPosition
        selectedPosition = position

        val currentList = currentList.toMutableList()
        currentList.forEachIndexed { index, item ->
            item.isSelected = index == selectedPosition
        }

        notifyItemChanged(oldPosition)
        notifyItemChanged(selectedPosition)

        submitList(currentList)
    }

    fun getSelectedTheme(): ThemeTemplateModel? {
        return getItem(selectedPosition)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ThemeOptionViewHolder {
        val binding = ItemThemeOptionBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ThemeOptionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ThemeOptionViewHolder, position: Int) {
        holder.bind(getItem(position), position == selectedPosition)
    }

    inner class ThemeOptionViewHolder(private val binding: ItemThemeOptionBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onThemeOptionSelected(position, getItem(position))
                }
            }
        }

        fun bind(theme: ThemeTemplateModel, isSelected: Boolean) {
            binding.ivThemeThumbnail.loadImageIcon(theme.image)

            if (isSelected) {
                binding.cardThemeOption.strokeWidth = 8
                binding.cardThemeOption.strokeColor = ContextCompat.getColor(
                    binding.root.context,
                    R.color.colorPrimary
                )
                binding.ivSelected.visible()
            } else {
                binding.cardThemeOption.strokeWidth = 0
                binding.ivSelected.gone()
            }
        }
    }
}

class ThemeDiffCallback : DiffUtil.ItemCallback<ThemeTemplateModel>() {
    override fun areItemsTheSame(oldItem: ThemeTemplateModel, newItem: ThemeTemplateModel): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: ThemeTemplateModel, newItem: ThemeTemplateModel): Boolean {
        return oldItem == newItem
    }
}