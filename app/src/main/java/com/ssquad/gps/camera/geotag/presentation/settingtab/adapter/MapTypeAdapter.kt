package com.ssquad.gps.camera.geotag.presentation.settingtab.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ssquad.gps.camera.geotag.data.models.MapType
import com.ssquad.gps.camera.geotag.utils.gone
import com.ssquad.gps.camera.geotag.utils.visible
import com.google.android.gms.maps.GoogleMap
import com.ssquad.gps.camera.geotag.R
import com.ssquad.gps.camera.geotag.databinding.ItemMapTypeBinding

class MapTypeAdapter(
    private val onMapTypeClick: (MapType) -> Unit
) : ListAdapter<MapType, MapTypeAdapter.MapTypeViewHolder>(MapTypeDiffCallback()) {
    private var selectedMapTypeId: Int = GoogleMap.MAP_TYPE_NORMAL
    @SuppressLint("NotifyDataSetChanged")
    fun setSelectedMapType(mapTypeId: Int) {
        selectedMapTypeId = mapTypeId
        notifyDataSetChanged()
    }
    inner class MapTypeViewHolder(private val binding: ItemMapTypeBinding) :
        RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("NotifyDataSetChanged")
        fun bind(mapType: MapType, isSelected: Boolean) {
            with(binding) {
                ivThemeThumbnail.setImageResource(mapType.thumbnailRes)
                ivSelected.isVisible = mapType.type == selectedMapTypeId
                tvThemeName.text = mapType.name

                cardThemeOption.setOnClickListener {
                    onMapTypeClick(mapType)
                    selectedMapTypeId = mapType.type
                    notifyDataSetChanged()
                }
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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MapTypeViewHolder {
        val binding = ItemMapTypeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MapTypeViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MapTypeViewHolder, position: Int) {
        val mapType = getItem(position)
        val isSelected = mapType.type == selectedMapTypeId
        holder.bind(mapType, isSelected)
    }

}

class MapTypeDiffCallback : DiffUtil.ItemCallback<MapType>() {
    override fun areItemsTheSame(oldItem: MapType, newItem: MapType): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: MapType, newItem: MapType): Boolean {
        return oldItem == newItem
    }
}
