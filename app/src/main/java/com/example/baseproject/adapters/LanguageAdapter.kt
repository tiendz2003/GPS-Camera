package com.example.baseproject.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.baseproject.R
import com.example.baseproject.databinding.ItemLanguageBinding
import com.example.baseproject.data.models.LanguageModel
import com.example.baseproject.utils.Common

class LanguageAdapter(private val context: Context, private val languageList: MutableList<LanguageModel>) :
    RecyclerView.Adapter<LanguageAdapter.ViewHolder>() {

    private var selectedLanguage = Common.getSelectedLanguage()

    class ViewHolder(val binding: ItemLanguageBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return ViewHolder(
            ItemLanguageBinding.inflate(inflater, parent, false)
        )
    }

    @SuppressLint("ResourceAsColor")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val language = languageList[position]
        if (selectedLanguage == language){
            holder.binding.layoutRoot.setBackgroundResource(R.drawable.bg_language_selected)
            holder.binding.languageName.typeface = ResourcesCompat.getFont(context, R.font.nunito_sans_semi_bold)
        }else{
            holder.binding.layoutRoot.setBackgroundResource(R.drawable.bg_language_unselected)
            holder.binding.languageName.typeface = ResourcesCompat.getFont(context, R.font.nunito_sans_regular)
        }

        holder.binding.ivLanguage.setImageResource(language.img)
        holder.binding.languageName.text = context.getString(language.name)
        holder.binding.languageName.setHorizontallyScrolling(true)
        holder.binding.languageName.isSelected = true

        holder.itemView.setOnClickListener {
            selectedLanguage = language
            notifyDataSetChanged()
        }

    }

    fun getSelectedPositionLanguage() : LanguageModel {
        return selectedLanguage
    }

    fun setSelectedPositionLanguage(language: LanguageModel) {
        selectedLanguage = language
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return Common.getLanguageList().size
    }

}