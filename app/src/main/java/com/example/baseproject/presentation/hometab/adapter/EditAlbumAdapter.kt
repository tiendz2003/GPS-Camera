package com.example.baseproject.presentation.hometab.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.baseproject.data.models.Album
import com.example.baseproject.databinding.EditAlbumItemBinding
import com.example.baseproject.utils.loadImageIcon

class EditAlbumAdapter(private val onAlbumClick: (Album) -> Unit) :
    ListAdapter<Album, EditAlbumAdapter.AlbumViewHolder>(AlbumDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlbumViewHolder {
        val binding = EditAlbumItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return AlbumViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AlbumViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class AlbumViewHolder(private val binding: EditAlbumItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("SetTextI18n")
        fun bind(album: Album) {
            binding.tvName.text = album.name
            binding.tvCount.text = "(${album.photoCount})"
            // tỉa ảnh bìa
            binding.imvPreview.loadImageIcon(album.coverPath)

            binding.root.setOnClickListener {
                onAlbumClick(album)
            }
        }
    }

    class AlbumDiffCallback : DiffUtil.ItemCallback<Album>() {
        override fun areItemsTheSame(oldItem: Album, newItem: Album): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Album, newItem: Album): Boolean {
            return oldItem == newItem
        }
    }
}