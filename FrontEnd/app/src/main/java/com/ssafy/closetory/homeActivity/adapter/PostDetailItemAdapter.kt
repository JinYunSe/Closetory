package com.ssafy.closetory.homeActivity.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ssafy.closetory.ApplicationClass
import com.ssafy.closetory.R
import com.ssafy.closetory.databinding.ItemPostDetailItemBinding
import com.ssafy.closetory.dto.PostDetailItemDto

private const val TAG = "PostDetailItemAdapter_싸피"

// 게시글 상세 페이지의 옷 요소(items)를 가로 리스트로 보여주는 어댑터
class PostDetailItemAdapter(private val onItemClick: (PostDetailItemDto) -> Unit) :
    RecyclerView.Adapter<PostDetailItemAdapter.ViewHolder>() {

    private val items = mutableListOf<PostDetailItemDto>()

    // 내 게시글인지 확인
    private var isMinePost: Boolean = false

    // Fragment에서 이용
    fun setIsMinePost(value: Boolean) {
        isMinePost = value
        notifyDataSetChanged()
    }

    // items 리스트 갱신
    fun submitList(newItems: List<PostDetailItemDto>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    inner class ViewHolder(private val binding: ItemPostDetailItemBinding) : RecyclerView.ViewHolder(binding.root) {

        // item 1개 바인딩
        fun bind(item: PostDetailItemDto) {
            val rawUrl = item.photoUrl
            val finalUrl = if (rawUrl.startsWith("http")) {
                rawUrl
            } else {
                "${ApplicationClass.API_BASE_URL}${item.photoUrl}"
            }

            Glide.with(binding.root)
                .load(finalUrl)
                .placeholder(R.drawable.bg_gray_box) // 없으면 placeholder로 교체
                .error(R.drawable.bg_gray_box)
                .into(binding.ivItem)

            // 내 게시글 : 저장 버튼 숨김
            binding.ivSave.visibility = if (isMinePost) View.GONE else View.VISIBLE

            if (!isMinePost) {
                binding.ivSave.setImageResource(
                    if (item.isSaved) {
                        R.drawable.baseline_bookmark_24
                    } else {
                        R.drawable.baseline_bookmark_border_24
                    }
                )
            } else {
                // 재사용 대비: 리스너 제거
                binding.ivSave.setOnClickListener(null)
            }

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
