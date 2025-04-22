package com.ssquad.gps.camera.geotag.presentation.hometab.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ssquad.gps.camera.geotag.data.models.Photo
import com.ssquad.gps.camera.geotag.databinding.ItemDateHeaderBinding
import com.ssquad.gps.camera.geotag.databinding.ItemPhotoSelectedBinding
import com.ssquad.gps.camera.geotag.databinding.ItemVideoSelectedBinding
import com.ssquad.gps.camera.geotag.utils.formatDuration
import com.ssquad.gps.camera.geotag.utils.loadImageIcon
import kotlin.collections.iterator

class PhotoAdapter(
    private val isVideo: Boolean = false,
    private val photoClick: (Photo) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    internal companion object {
        const val VIEW_TYPE_DATE_HEADER = 0
        const val VIEW_TYPE_PHOTO = 1
    }

    private val items = mutableListOf<Any>()

    @SuppressLint("NotifyDataSetChanged")
    fun submitList(photosByDate: Map<String, List<Photo>>) {
        items.clear()
        for ((date, photos) in photosByDate) {
            items.add(date)
            items.addAll(photos)
        }
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is String -> VIEW_TYPE_DATE_HEADER
            else  -> VIEW_TYPE_PHOTO
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_DATE_HEADER -> {
                val binding = ItemDateHeaderBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                DateHeaderViewHolder(binding)
            }
            else -> {
                val binding = if (isVideo) {
                    ItemVideoSelectedBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                } else {
                    ItemPhotoSelectedBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                }
                if (isVideo) {
                    VideoViewHolder(binding as ItemVideoSelectedBinding)
                } else{
                    PhotoViewHolder(binding as ItemPhotoSelectedBinding)
                }

            }

        }
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int
    ) {
        when (holder) {
            is DateHeaderViewHolder -> holder.bind(items[position] as String)
            is PhotoViewHolder -> holder.bind(items[position] as Photo)
            is VideoViewHolder -> holder.bind(items[position] as Photo)
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    inner class DateHeaderViewHolder(private val binding: ItemDateHeaderBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(date: String) {
            binding.tvDate.text = date
        }
    }

    inner class PhotoViewHolder(private val binding: ItemPhotoSelectedBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(photo: Photo) {
            binding.imvTheme.loadImageIcon(photo.path)
            binding.root.setOnClickListener {
                photoClick(photo)
            }
        }
    }
    inner class VideoViewHolder(private val binding: ItemVideoSelectedBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(photo: Photo) {
            with(binding) {
                imvTheme.loadImageIcon(photo.path)
                tvDuration.text = photo.duration?.formatDuration()
                root.setOnClickListener {
                    photoClick(photo)
                }
            }
        }
    }
}