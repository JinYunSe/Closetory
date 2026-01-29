package com.ssafy.closetory.homeActivity.post.create.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ssafy.closetory.databinding.ItemPostDetailItemBinding
import com.ssafy.closetory.dto.ItemDto

// 게시글 상세 페이지의 옷 요소(items)를 가로 리스트로 보여주는 어댑터
class PostDetailItemAdapter(private val onItemClick: (ItemDto) -> Unit) :
    RecyclerView.Adapter<PostDetailItemAdapter.ViewHolder>() {

    private val items = mutableListOf<ItemDto>()

    // items 리스트 갱신
    fun submitList(newItems: List<ItemDto>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    inner class ViewHolder(private val binding: ItemPostDetailItemBinding) : RecyclerView.ViewHolder(binding.root) {

        // item 1개 바인딩
        fun bind(item: ItemDto) {
            // 옷 요소 이미지 표시
            // Glide.with(binding.root).load(item.photoUrl).into(binding.ivItem)

            // 옷 요소 클릭 이벤트 처리
            binding.root.setOnClickListener {
                onItemClick(item)
            }
        }
    }

    // ViewHolder 생성
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemPostDetailItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    // ViewHolder 바인딩
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    // 아이템 개수 반환
    override fun getItemCount(): Int = items.size
}
