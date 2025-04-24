package com.ssquad.gps.camera.geotag.presentation.settingtab.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ssquad.gps.camera.geotag.R
import com.ssquad.gps.camera.geotag.data.models.FormatItem
import com.ssquad.gps.camera.geotag.databinding.ItemFormatBinding

class FormatAdapter(
    private val items: List<FormatItem>,
    private val onClick: (FormatItem) -> Unit,
) : RecyclerView.Adapter<FormatAdapter.FormatViewHolder>() {

    inner class FormatViewHolder(private val binding: ItemFormatBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: FormatItem) {
            binding.apply {
                tvFormatName.text = item.id

                imgRadio.setImageResource(
                    if (item.isSelected) R.drawable.ic_selected
                    else R.drawable.ic_unchecked_language
                )
                root.setOnClickListener { onClick(item) }
                imgRadio.setOnClickListener { onClick(item) }
            }
        }
    }
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): FormatViewHolder {
        val binding = ItemFormatBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return FormatViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FormatViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int {
        return items.size
    }

}