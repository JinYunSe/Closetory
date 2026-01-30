package com.ssafy.closetory.homeActivity.adpter

import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ssafy.closetory.R
import com.ssafy.closetory.databinding.ItemClothesBinding
import com.ssafy.closetory.dto.ClothesItemDto

class RecommendClothesAdapter : ListAdapter<ClothesItemDto, RecommendClothesAdapter.ViewHolder>(diffCallback) {

    var onItemClickListener: ((ClothesItemDto) -> Unit)? = null

    inner class ViewHolder(private val binding: ItemClothesBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ClothesItemDto) = with(binding) {
            Glide.with(ivPhoto)
                .load(item.photoUrl)
                .placeholder(R.drawable.placeholder)
                .error(R.drawable.error)
                .into(ivPhoto)

            imgBtn.setOnClickListener {
                onItemClickListener?.invoke(item)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemClothesBinding.inflate(
            android.view.LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object {
        private val diffCallback = object : DiffUtil.ItemCallback<ClothesItemDto>() {
            override fun areItemsTheSame(oldItem: ClothesItemDto, newItem: ClothesItemDto): Boolean =
                oldItem.clothesId == newItem.clothesId

            override fun areContentsTheSame(oldItem: ClothesItemDto, newItem: ClothesItemDto): Boolean =
                oldItem == newItem
        }
    }
}
