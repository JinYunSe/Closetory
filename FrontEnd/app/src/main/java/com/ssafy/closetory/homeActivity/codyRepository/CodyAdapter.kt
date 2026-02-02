package com.ssafy.closetory.homeActivity.codyRepository

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ssafy.closetory.ApplicationClass
import com.ssafy.closetory.R
import com.ssafy.closetory.databinding.ItemCodyBinding
import com.ssafy.closetory.dto.CodyRepositoryResponse

private const val TAG = "CodyAdapter"

class CodyAdapter(private val onItemClick: (CodyRepositoryResponse) -> Unit) :
    RecyclerView.Adapter<CodyAdapter.CodyViewHolder>() {

    private val items: MutableList<CodyRepositoryResponse> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CodyViewHolder {
        val binding = ItemCodyBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CodyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CodyViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    fun submitItems(newItems: List<CodyRepositoryResponse>) {
        Log.d(TAG, "submitItems - 받은 아이템 개수: ${newItems.size}")

        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    inner class CodyViewHolder(private val binding: ItemCodyBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: CodyRepositoryResponse) {
            // 이미지 URL 처리 (상대경로면 base URL 추가)
            val fullImageUrl = if (item.photoUrl.startsWith("http")) {
                item.photoUrl
            } else {
                "${ApplicationClass.API_BASE_URL}${item.photoUrl}"
            }

            Log.d(TAG, "bind - lookId: ${item.lookId}, url: $fullImageUrl")

            // 이미지 로딩
            Glide.with(binding.root.context)
                .load(fullImageUrl)
                .centerCrop()
                .placeholder(R.drawable.bg_slot_empty)
                .error(R.drawable.error)
                .into(binding.ivCody)

            // 날짜 표시
            binding.tvCodyDate.text = item.date

            // "내 옷만" 뱃지 표시 여부
            binding.ivOnlyMineBadge.visibility = if (item.onlyMine) {
                android.view.View.VISIBLE
            } else {
                android.view.View.GONE
            }

            // 클릭 이벤트
            binding.root.setOnClickListener {
                onItemClick(item)
            }
        }
    }
}
