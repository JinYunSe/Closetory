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
            // 순위 뱃지 이미지 (추가)
            val badgeRes = when (item.rank) {
                1 -> R.drawable.badge_rank_1
                2 -> R.drawable.badge_rank_2
                3 -> R.drawable.badge_rank_3
                else -> null
            }

            if (badgeRes != null) {
                binding.ivRank.visibility = android.view.View.VISIBLE
                binding.ivRank.setImageResource(badgeRes)
            } else {
                binding.ivRank.visibility = android.view.View.GONE
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
