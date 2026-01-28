package com.ssafy.closetory.homeActivity.codyRepository

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ssafy.closetory.databinding.ItemCodyBinding
import com.ssafy.closetory.dto.CodyRepositoryResponse

class CodyAdapter(private val onItemClick: (CodyRepositoryResponse) -> Unit) :
    RecyclerView.Adapter<CodyAdapter.CodyViewHolder>() {

    // 코디 데이터 목록
    private val items: MutableList<CodyRepositoryResponse> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CodyViewHolder {
        // ViewBinding사용해서 아이디 사용할 필요는 없음
        val binding = ItemCodyBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CodyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CodyViewHolder, position: Int) {
        Log.d("CodyAdapter", "onBindViewHolder - position: $position, date: ${items[position].date}")
        holder.bind(items[position])
    }

    override fun getItemCount(): Int {
        Log.d("CodyAdapter", "getItemCount: ${items.size}")
        return items.size
    }

    fun submitItems(newItems: List<CodyRepositoryResponse>) {
        Log.d("CodyAdapter", "submitItems - 받은 아이템 개수: ${newItems.size}")
        newItems.forEachIndexed { index, item ->
            Log.d("CodyAdapter", "아이템 $index: date=${item.date}, onlyMine=${item.onlyMine}")
        }
        // 기존 리스트를 지우고
        items.clear()
        // addAll새로운 리스트를 다시 채우고
        items.addAll(newItems)
        // 갱신해주고
        notifyDataSetChanged()
    }

    // 아이템의 한칸을 관리하는 부분
    inner class CodyViewHolder(private val binding: ItemCodyBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: CodyRepositoryResponse) {
            Log.d("CodyAdapter", "bind - 날짜 설정: ${item.date}")

            // 이미지 로딩
            Glide.with(binding.root.context)
                .load(item.photoUrl)
                .centerCrop()
                .into(binding.ivCody)

            // 날짜 표시
            binding.tvCodyDate.text = item.date
            Log.d("CodyAdapter", "TextView에 설정된 텍스트: ${binding.tvCodyDate.text}")

            // "내 옷만" 뱃지 표시 여부
            binding.ivOnlyMineBadge.visibility = if (item.onlyMine) {
                Log.d("CodyAdapter", "뱃지 표시: VISIBLE")
                android.view.View.VISIBLE
            } else {
                Log.d("CodyAdapter", "뱃지 표시: GONE")
                android.view.View.GONE
            }

            // 클릭 했을 떄 감지만 기능은 Fragment에서 추가해줘야함
            binding.root.setOnClickListener {
                onItemClick(item)
            }
        }
    }
}
