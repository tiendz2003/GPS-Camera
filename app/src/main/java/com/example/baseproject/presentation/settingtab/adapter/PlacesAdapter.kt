package com.example.baseproject.presentation.settingtab.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.baseproject.databinding.ItemPlaceBinding
import com.google.android.libraries.places.api.model.AutocompletePrediction

class PlacesAdapter(
    private val onPlaceClick: (AutocompletePrediction) -> Unit
) : ListAdapter<AutocompletePrediction, PlacesAdapter.PlaceViewHolder>(PlaceDiffCallback()) {
    inner class PlaceViewHolder(private val binding: ItemPlaceBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(place: AutocompletePrediction) {
            binding.tvPlaceName.text = place.getPrimaryText(null)
            binding.tvPlaceAddress.text = place.getSecondaryText(null)
            binding.root.setOnClickListener { onPlaceClick(place) }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): PlaceViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemPlaceBinding.inflate(inflater, parent, false)
        return PlaceViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: PlaceViewHolder,
        position: Int
    ) {
        holder.bind(getItem(position))
    }


    class PlaceDiffCallback : DiffUtil.ItemCallback<AutocompletePrediction>() {
        override fun areItemsTheSame(
            oldItem: AutocompletePrediction,
            newItem: AutocompletePrediction
        ): Boolean {
            return oldItem.placeId == newItem.placeId
        }

        @SuppressLint("DiffUtilEquals")
        override fun areContentsTheSame(
            oldItem: AutocompletePrediction,
            newItem: AutocompletePrediction
        ): Boolean {
            return oldItem.toString() == newItem.toString()
        }
    }
}