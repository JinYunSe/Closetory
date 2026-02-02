package com.ssafy.closetory.homeActivity.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.ssafy.closetory.R
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
            binding.tvAuthor.text = "게시자 X"
            binding.tvAuthor.visibility = View.GONE

            // 3) 조회수 / 좋아요 수
            binding.tvViews.text = item.views.toString()
            binding.tvLikes.text = item.likes.toString()

            // 0) 썸네일 이미지
            Glide.with(binding.ivThumbnail)
                .load(item.photoUrl)
                .placeholder(R.drawable.ic_body_default)
                .error(R.drawable.ic_body_default)
                // 원본 데이터 + 반환된 결과 둘 다 디스크에 캐시 : 같은 URL이미지를 보여줄 때 좋음.
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(binding.ivThumbnail)

            // 카드 클릭 이벤트
            binding.root.setOnClickListener {
                onItemClick(item)
            }
        }
    }
}
