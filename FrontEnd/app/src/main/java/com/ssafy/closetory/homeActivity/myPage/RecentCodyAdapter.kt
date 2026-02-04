package com.ssafy.closetory.homeActivity.mypage

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ssafy.closetory.ApplicationClass
import com.ssafy.closetory.R
import com.ssafy.closetory.databinding.ItemTop3ClothesBinding
import com.ssafy.closetory.dto.CodyRepositoryResponse

class RecentCodyAdapter : ListAdapter<CodyRepositoryResponse, RecentCodyAdapter.RecentCodyViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecentCodyViewHolder {
        val binding = ItemTop3ClothesBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return RecentCodyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecentCodyViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class RecentCodyViewHolder(private val binding: ItemTop3ClothesBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: CodyRepositoryResponse) {
            // 날짜 표시 (tvRank에 날짜)
            binding.tvRank.text = formatDate(item.date)

            // 횟수는 숨김
            binding.tvUsageCount.visibility = View.GONE

            // 이미지 URL 처리
            val fullImageUrl = if (item.photoUrl.startsWith("http")) {
                item.photoUrl
            } else {
                val cleanPath = item.photoUrl.removePrefix("/")
                "${ApplicationClass.API_BASE_URL}$cleanPath"
            }

            // 이미지 로드
            Glide.with(binding.root.context)
                .load(fullImageUrl)
                .placeholder(R.drawable.error)
                .error(R.drawable.error)
                .centerCrop()
                .into(binding.ivClothes)
        }

        // 날짜 포맷: "2026-02-04" → "2/4"
        private fun formatDate(dateString: String?): String {
            val s = dateString?.trim()
            if (s.isNullOrEmpty() || s.isBlank()) {
                return "-"
            }

            return try {
                val head = if (s.length >= 10) s.substring(0, 10) else s
                val parts = head.split("-")

                if (parts.size >= 3) {
                    val year = parts[0].toIntOrNull()
                    val month = parts[1].toIntOrNull()
                    val day = parts[2].toIntOrNull()

                    if (
                        year != null &&
                        month != null &&
                        day != null &&
                        month in 1..12 &&
                        day in 1..31
                    ) {
                        "%04d-%02d-%02d".format(year, month, day)
                    } else {
                        head
                    }
                } else {
                    head
                }
            } catch (e: Exception) {
                if (s.length >= 10) s.substring(0, 10) else s
            }
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<CodyRepositoryResponse>() {
        override fun areItemsTheSame(oldItem: CodyRepositoryResponse, newItem: CodyRepositoryResponse): Boolean =
            oldItem.lookId == newItem.lookId

        override fun areContentsTheSame(oldItem: CodyRepositoryResponse, newItem: CodyRepositoryResponse): Boolean =
            oldItem == newItem
    }
}
