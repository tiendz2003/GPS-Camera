package com.ssquad.gps.camera.geotag.presentation.settingtab.adapter

import android.view.LayoutInflater
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.mapbox.search.autocomplete.PlaceAutocompleteSuggestion
import com.ssquad.gps.camera.geotag.databinding.ItemPlaceSuggestionBinding
import com.ssquad.gps.camera.geotag.utils.gone
import com.ssquad.gps.camera.geotag.utils.visible
import java.util.Locale

class PlaceAutoCompleteAdapter(
    private val onItemClick:(PlaceAutocompleteSuggestion)-> Unit
) :ListAdapter<PlaceAutocompleteSuggestion,  PlaceAutoCompleteAdapter.ViewHolder>(PlaceAutoCompleteDiffUtil()) {
    inner class ViewHolder(
        private val binding:ItemPlaceSuggestionBinding
    ):RecyclerView.ViewHolder(binding.root){
        init {
            binding.root.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val item = getItem(position)
                    onItemClick(item)
                }
            }
        }
        fun bind(suggestion:PlaceAutocompleteSuggestion){
            binding.apply {
                tvPlaceName.text = suggestion.name
                tvPlaceAddress.text = suggestion.formattedAddress
                suggestion.categories?.firstOrNull()?.let { category ->
                    tvPlaceCategory.text = category
                    tvPlaceCategory.visible()
                } ?: run {
                    tvPlaceCategory.gone()
                }
                suggestion.distanceMeters?.let { distance ->
                    tvDistance.text = String.format(Locale.getDefault(),"%.2f km", distance / 1000)
                    tvDistance.visible()
                } ?: run {
                    tvDistance.gone()
                }
            }
        }
    }
    override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemPlaceSuggestionBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    private class PlaceAutoCompleteDiffUtil : DiffUtil.ItemCallback<PlaceAutocompleteSuggestion>() {
        override fun areItemsTheSame(
            oldItem: PlaceAutocompleteSuggestion,
            newItem: PlaceAutocompleteSuggestion
        ): Boolean {
            return oldItem.distanceMeters == newItem.distanceMeters
        }

        override fun areContentsTheSame(
            oldItem: PlaceAutocompleteSuggestion,
            newItem: PlaceAutocompleteSuggestion
        ): Boolean {
            return oldItem == newItem
        }
    }
}