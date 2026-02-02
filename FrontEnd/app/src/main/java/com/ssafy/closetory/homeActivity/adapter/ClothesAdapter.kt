package com.ssafy.closetory.homeActivity.adapter

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

    inner class VH(private val binding: ItemClothesBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ClothesItemDto) = with(binding) {
            // 이미지 로드
            Glide.with(ivPhoto)
                .load(item.photoUrl)
                .placeholder(R.drawable.placeholder)
                .error(R.drawable.error)
                .into(ivPhoto)

            // 북마크 표시 여부: isMine == false 일 때만 표시
            val showBookmark = (item.isMine == false)
            ivBookmark.visibility = if (showBookmark) View.VISIBLE else View.GONE
            ivBookmark.setImageResource(R.drawable.baseline_bookmark_24)

            // 북마크 클릭
            if (showBookmark) {
                ivBookmark.setOnClickListener { onBookmarkClick?.invoke(item) }
            } else {
                ivBookmark.setOnClickListener(null)
            }

            // 아이템 클릭 (현재 레이아웃 기준 imgBtn이 전체 클릭영역)
            imgBtn.setOnClickListener { onItemClick?.invoke(item) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemClothesBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(getItem(position))
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
