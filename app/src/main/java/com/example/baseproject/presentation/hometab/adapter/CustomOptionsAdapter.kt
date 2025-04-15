package com.example.baseproject.presentation.hometab.adapter

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.baseproject.R
import com.example.baseproject.data.models.CustomTemplateModel
import com.example.baseproject.databinding.ItemCustomTemplateBinding
import com.example.baseproject.utils.loadImageIcon

class CustomOptionsAdapter(
    private val onItemClick: (CustomTemplateModel,Int) -> Unit
) :
    ListAdapter<CustomTemplateModel, CustomOptionsAdapter.CustomViewHolder>(CustomDiffCallback()) {

    inner class CustomViewHolder(private val binding: ItemCustomTemplateBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: CustomTemplateModel,position: Int) {
            with(binding){
                tvControlName.text = item.name
                ivControlIcon.setImageResource(item.icon)

                if (item.isSelected) {
                    cardControlIcon.strokeWidth = 3
                    cardControlIcon.strokeColor = ContextCompat.getColor(root.context, R.color.colorPrimary)
                } else {
                    cardControlIcon.strokeWidth = 0
                }
                root.setOnClickListener {
                   if(item.isActive){
                       onItemClick(item,position)
                   }
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemCustomTemplateBinding.inflate(inflater, parent, false)
        return CustomViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {
        holder.bind(getItem(position),position)
    }
}


class CustomDiffCallback : DiffUtil.ItemCallback<CustomTemplateModel>() {
    override fun areItemsTheSame(
        oldItem: CustomTemplateModel,
        newItem: CustomTemplateModel
    ): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(
        oldItem: CustomTemplateModel,
        newItem: CustomTemplateModel
    ): Boolean {
        return oldItem == newItem && oldItem.isSelected == newItem.isSelected &&
                oldItem.isActive == newItem.isActive
    }
}