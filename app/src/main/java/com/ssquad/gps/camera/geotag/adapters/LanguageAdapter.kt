package com.ssquad.gps.camera.geotag.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.ssquad.gps.camera.geotag.R
import com.ssquad.gps.camera.geotag.data.models.LanguageModel
import com.ssquad.gps.camera.geotag.databinding.ItemLanguageBinding

class LanguageAdapter(
    private val context: Context,
    private val languageList: MutableList<LanguageModel>,
    private val onItemClick: (LanguageModel) -> Unit,
    ) : RecyclerView.Adapter<LanguageAdapter.ViewHolder>() {
    private var selectedPosition = RecyclerView.NO_POSITION

    inner class ViewHolder(val binding: ItemLanguageBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(language: LanguageModel, position: Int) {
            binding.ivLanguage.setImageResource(language.img)
            binding.languageName.text = context.getString(language.name)
            binding.languageName.setHorizontallyScrolling(true)
            binding.languageName.isSelected = true
            if (position == selectedPosition) {
                binding.layoutRoot.setBackgroundResource(R.drawable.bg_language_selected)

            } else {
                binding.layoutRoot.setBackgroundResource(R.drawable.bg_language_unselected)

            }
            binding.root.setOnClickListener {
                val previousSelected = selectedPosition
                selectedPosition = position

                if (previousSelected != RecyclerView.NO_POSITION) {
                    notifyItemChanged(previousSelected)
                }
                notifyItemChanged(selectedPosition)
                onItemClick(language)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemLanguageBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val language = languageList[position]
        holder.bind(language, position)
    }

    override fun getItemCount(): Int = languageList.size

    fun getSelectedLanguage(): LanguageModel? {
        return if (selectedPosition != RecyclerView.NO_POSITION)
            languageList[selectedPosition]
        else null
    }

    fun setSelectedLanguage(language: LanguageModel) {
        val position = languageList.indexOf(language)
        if (position != -1) {
            selectedPosition = position
            notifyItemChanged(position)
        }
    }
}