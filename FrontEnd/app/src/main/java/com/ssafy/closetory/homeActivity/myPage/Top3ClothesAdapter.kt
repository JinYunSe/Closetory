package com.ssafy.closetory.homeActivity.mypage

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ssafy.closetory.R
import com.ssafy.closetory.databinding.ItemTop3ClothesBinding
import com.ssafy.closetory.dto.Top3ClothesResponse

class Top3ClothesAdapter : ListAdapter<Top3ClothesResponse, Top3ClothesAdapter.Top3ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Top3ViewHolder {
        val binding = ItemTop3ClothesBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return Top3ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: Top3ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class Top3ViewHolder(private val binding: ItemTop3ClothesBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Top3ClothesResponse) {
            // 순위 표시
            binding.tvRank.text = when (item.rank) {
                1 -> "1위"
                2 -> "2위"
                3 -> "3위"
                else -> "${item.rank}위"
            }

            // 착용 횟수 표시
            binding.tvUsageCount.text = "${item.usageCount}회"

            // 옷 이미지 로드
            Glide.with(binding.root.context)
                .load(item.photoUrl)
                .placeholder(R.drawable.error) // 기본 이미지
                .error(R.drawable.error) // 에러 시 기본 이미지
                .centerCrop()
                .into(binding.ivClothes)
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<Top3ClothesResponse>() {
        override fun areItemsTheSame(oldItem: Top3ClothesResponse, newItem: Top3ClothesResponse): Boolean =
            oldItem.clothesId == newItem.clothesId

        override fun areContentsTheSame(oldItem: Top3ClothesResponse, newItem: Top3ClothesResponse): Boolean =
            oldItem == newItem
    }
}
