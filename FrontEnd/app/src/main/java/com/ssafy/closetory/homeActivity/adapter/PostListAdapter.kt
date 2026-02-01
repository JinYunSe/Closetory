package com.ssafy.closetory.homeActivity.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ssafy.closetory.databinding.ItemPostCardBinding
import com.ssafy.closetory.dto.PostItemResponse

// 게시글 목록 RecyclerView 어댑터 (DiffUtil 없이, notifyDataSetChanged() 방식)
class PostListAdapter(private val onItemClick: (PostItemResponse) -> Unit) :
    RecyclerView.Adapter<PostListAdapter.PostViewHolder>() {

    // 화면에 뿌릴 데이터 리스트
    private val items = mutableListOf<PostItemResponse>()

    // Fragment/ViewModel에서 새 리스트를 받아서 전체 갱신
    fun submitList(newItems: List<PostItemResponse>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val binding = ItemPostCardBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PostViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class PostViewHolder(private val binding: ItemPostCardBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: PostItemResponse) {
            // 1) 글 제목 + (댓글 수)
            binding.tvTitle.text = item.title
            binding.tvCommentCount.text = "(${item.comments})"

            // 2) 작성자(username)
            // 현재 PostItemResponse에 username 필드가 없다면 빈 값 처리
            // TODO: 서버 응답에 username이 추가되면 PostItemResponse에 필드 추가 후 바인딩
            binding.tvAuthor.text = ""
            binding.tvAuthor.visibility = View.GONE

            // 3) 조회수 / 좋아요 수
            binding.tvViews.text = item.views.toString()
            binding.tvLikes.text = item.likes.toString()

            // 0) 썸네일 이미지
            // photoUrl 실제 로딩은 Coil/Glide 등 이미지 라이브러리로 처리
            // TODO: 이미지 로딩 라이브러리 적용 후 아래처럼 사용
            // binding.ivThumbnail.load(item.photoUrl)

            // 카드 클릭 이벤트
            binding.root.setOnClickListener {
                onItemClick(item)
            }
        }
    }
}
