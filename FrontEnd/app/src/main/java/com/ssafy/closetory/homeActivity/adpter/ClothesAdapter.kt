package com.ssafy.closetory.homeActivity.adpter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ssafy.closetory.R
import com.ssafy.closetory.databinding.ItemClothesBinding
import com.ssafy.closetory.dto.ClothesItemDto

class ClothesAdapter : ListAdapter<ClothesItemDto, ClothesAdapter.VH>(DIFF) {

    var onItemClick: ((ClothesItemDto) -> Unit)? = null
    var onBookmarkClick: ((ClothesItemDto) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemClothesBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = getItem(position)
        holder.bind(item, onItemClick, onBookmarkClick)
    }

    class VH(private val binding: ItemClothesBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(
            item: ClothesItemDto,
            onItemClick: ((ClothesItemDto) -> Unit)?,
            onBookmarkClick: ((ClothesItemDto) -> Unit)?
        ) {
            Glide.with(binding.ivPhoto)
                .load(item.photoUrl)
                .placeholder(R.drawable.placeholder)
                .error(R.drawable.error)
                .into(binding.ivPhoto)

            val showBookmark = (item.isMine == false)

            binding.ivBookmark.visibility = if (showBookmark) View.VISIBLE else View.GONE

            binding.ivBookmark.setImageResource(R.drawable.baseline_bookmark_24)

            if (showBookmark) {
                binding.ivBookmark.setOnClickListener { onBookmarkClick?.invoke(item) }
            } else {
                binding.ivBookmark.setOnClickListener(null)
            }

            binding.imgBtn.setOnClickListener {
                onItemClick?.invoke(item)
            }
        }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<ClothesItemDto>() {
            override fun areItemsTheSame(oldItem: ClothesItemDto, newItem: ClothesItemDto): Boolean =
                oldItem.clothesId == newItem.clothesId

            override fun areContentsTheSame(oldItem: ClothesItemDto, newItem: ClothesItemDto): Boolean =
                oldItem == newItem
        }
    }
}
