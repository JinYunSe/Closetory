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

private const val TAG = "ClothAdapter_싸피"
class ClothesAdapter : ListAdapter<ClothesItemDto, ClothesAdapter.VH>(DIFF) {

    var onItemClick: ((ClothesItemDto) -> Unit)? = null
    var onBookmarkClick: ((ClothesItemDto) -> Unit)? = null // 필요하면

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemClothesBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(getItem(position), onItemClick, onBookmarkClick)
    }

    class VH(private val binding: ItemClothesBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(
            item: ClothesItemDto,
            onItemClick: ((ClothesItemDto) -> Unit)?,
            onBookmarkClick: ((ClothesItemDto) -> Unit)?
        ) {
            // 이미지 바인딩
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

            // 아이템 클릭
            binding.root.setOnClickListener { onItemClick?.invoke(item) }
        }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<ClothesItemDto>() {
            override fun areItemsTheSame(oldItem: ClothesItemDto, newItem: ClothesItemDto) =
                oldItem.clothesId == newItem.clothesId

            override fun areContentsTheSame(oldItem: ClothesItemDto, newItem: ClothesItemDto) = oldItem == newItem
        }
    }
}
