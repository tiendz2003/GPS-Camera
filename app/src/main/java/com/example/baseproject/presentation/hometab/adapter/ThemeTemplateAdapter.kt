package com.example.baseproject.presentation.hometab.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.example.baseproject.bases.BaseViewHolder
import com.example.baseproject.models.TemplateType
import com.example.baseproject.models.ThemeTemplateModel

class ThemeTemplateAdapter(
    private val onItemClick: (ThemeTemplateModel) -> Unit
): ListAdapter<ThemeTemplateModel, BaseViewHolder>(ThemeTemplateDiffUtil()) {

    companion object{
        private const val DAILY = 0
        private const val TRAVEL = 1
        private const val GPS = 2
    }

    override fun getItemViewType(position: Int): Int {
        return when(getItem(position).type){
            TemplateType.DAILY -> DAILY
            TemplateType.TRAVEL -> TRAVEL
            TemplateType.GPS -> GPS
        }
    }
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BaseViewHolder {
        return when(viewType){
            DAILY -> BaseViewHolder.ViewHolder.create(parent)
            TRAVEL -> BaseViewHolder.ViewHolder.create(parent)
            GPS -> BaseViewHolder.ViewHolder.create(parent)
            else -> throw IllegalArgumentException("Looix")
        }
    }

    override fun onBindViewHolder(
        holder: BaseViewHolder,
        position: Int
    ) {
        holder.bind(getItem(position))
        holder.itemView.setOnClickListener {
            onItemClick(getItem(position))
        }
    }

    class ThemeTemplateDiffUtil : DiffUtil.ItemCallback<ThemeTemplateModel>() {
        override fun areItemsTheSame(oldItem: ThemeTemplateModel, newItem: ThemeTemplateModel): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: ThemeTemplateModel, newItem: ThemeTemplateModel): Boolean {
            return oldItem == newItem
        }
    }
}