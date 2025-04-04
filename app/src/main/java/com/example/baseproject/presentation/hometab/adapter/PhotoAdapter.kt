package com.example.baseproject.presentation.hometab.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.baseproject.data.models.Photo
import com.example.baseproject.databinding.ItemDateHeaderBinding
import com.example.baseproject.databinding.ItemPhotoSelectedBinding
import com.example.baseproject.utils.loadImageIcon

class PhotoAdapter(private val photoClick:(Photo)->Unit): RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    internal companion object{
        const val VIEW_TYPE_DATE_HEADER = 0
        const val VIEW_TYPE_PHOTO = 1
    }
    private val items = mutableListOf<Any>()
    fun submitList(photosByDate: Map<String, List<Photo>>) {
        items.clear()
        for ((date,photos) in photosByDate){
            items.add(date)
            items.addAll(photos)
        }
        notifyDataSetChanged()
    }
    override fun getItemViewType(position: Int): Int {
        return when(items[position]){
            is String -> VIEW_TYPE_DATE_HEADER
            is Photo -> VIEW_TYPE_PHOTO
            else -> throw IllegalArgumentException("Loi")
        }
    }
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerView.ViewHolder {
        return when(viewType){
            VIEW_TYPE_DATE_HEADER -> {
                val binding = ItemDateHeaderBinding.inflate(LayoutInflater.from(parent.context),parent,false)
                DateHeaderViewHolder(binding)
            }
            VIEW_TYPE_PHOTO ->{
                val binding = ItemPhotoSelectedBinding.inflate(LayoutInflater.from(parent.context),parent,false)
                PhotoViewHolder(binding)
            }
            else -> throw IllegalArgumentException("Loi")
        }
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int
    ) {
        when(holder){
            is DateHeaderViewHolder -> holder.bind(items[position] as String)
            is PhotoViewHolder -> holder.bind(items[position] as Photo)
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }
    inner class DateHeaderViewHolder(private val binding: ItemDateHeaderBinding): RecyclerView.ViewHolder(binding.root){
        fun bind(date: String){
            binding.tvDate.text = date
        }
    }
    inner class PhotoViewHolder(private val binding: ItemPhotoSelectedBinding): RecyclerView.ViewHolder(binding.root){
        fun bind(photo: Photo){
            binding.imvTheme.loadImageIcon(photo.path)
            binding.root.setOnClickListener {
                photoClick(photo)
            }
        }
    }
}