package com.ssafy.closetory.homeActivity.codyRepository

import android.graphics.Typeface
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

        // 디버깅용: 각 아이템의 날짜 출력
        newItems.forEachIndexed { index, item ->
            Log.d(TAG, "  [$index] lookId=${item.lookId}, date='${item.date}', photoUrl=${item.photoUrl}")
        }

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
                // 앞에 슬래시 제거 (중복 방지)
                val cleanPath = item.photoUrl.removePrefix("/")
                "${ApplicationClass.API_BASE_URL}$cleanPath"
            }

            Log.d(TAG, "🖼️ bind - lookId: ${item.lookId}, date: '${item.date}', url: $fullImageUrl")

            // 이미지 로딩
            Glide.with(binding.root.context)
                .load(fullImageUrl)
                .centerCrop()
                .placeholder(R.drawable.bg_slot_empty)
                .error(R.drawable.error)
                .into(binding.ivCody)

            if (item.date.isNullOrEmpty()) {
                // 날짜가 없으면 "미등록" 표시
                binding.tvCodyDate.text = "미등록"
                binding.tvCodyDate.visibility = android.view.View.VISIBLE
            } else {
                // 날짜가 있으면 정상 표시
                binding.tvCodyDate.text = item.date
                binding.tvCodyDate.visibility = android.view.View.VISIBLE
            }

            // 자물쇠 배지 표시 여부
            binding.ivOnlyMineBadge.visibility = if (item.onlyMine) {
                // 내 옷의 경우 자물쇠 안 보이게 처리
                android.view.View.GONE
            } else {
                // 남의 옷이 포함 돼 있으면 자물쇠 보이게 처리
                android.view.View.VISIBLE
            }

            // 클릭 이벤트
            binding.root.setOnClickListener {
                Log.d(TAG, "👆 코디 클릭 - lookId: ${item.lookId}")
                onItemClick(item)
            }
        }

        /**
         * 날짜 포맷팅: "2026-01-14" → "1/14"
         *
         * 안전하게 처리:
         * - null이면 "-" 반환
         * - 빈 문자열이면 "-" 반환
         * - 파싱 실패하면 원본 반환
         * - 절대 크래시 안 남
         */
        private fun formatDate(dateString: String?): String {
            // 1) null / 빈 문자열 방어
            val s = dateString?.trim()
            if (s.isNullOrEmpty() || s.isBlank()) {
                Log.w(TAG, "⚠️ formatDate: 날짜가 null 또는 empty")
                return "-"
            }

            return try {
                // 2) 앞 10자리(yyyy-MM-dd)만 안전하게 추출
                val head = if (s.length >= 10) s.substring(0, 10) else s

                // 3) yyyy-MM-dd 파싱
                val parts = head.split("-")
                if (parts.size >= 3) {
                    val month = parts[1].toIntOrNull()
                    val day = parts[2].toIntOrNull()

                    // 4) 유효성 검사
                    if (month != null && day != null && month in 1..12 && day in 1..31) {
                        "$month/$day"
                    } else {
                        Log.w(TAG, "formatDate: 잘못된 날짜 범위 - month=$month, day=$day")
                        head
                    }
                } else {
                    Log.w(TAG, "formatDate: 파싱 실패 (parts.size=${parts.size}) - $head")
                    head
                }
            } catch (e: Exception) {
                Log.e(TAG, "formatDate: 예외 발생 - dateString='$s'", e)
                // 5) fallback도 절대 안 터지게
                if (s.length >= 10) s.substring(0, 10) else s
            }
        }
    }
}
