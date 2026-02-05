package com.ssafy.closetory.homeActivity.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ssafy.closetory.ApplicationClass
import com.ssafy.closetory.R
import com.ssafy.closetory.databinding.ItemCommentBinding
import com.ssafy.closetory.dto.CommentDto
import kotlin.math.log

private val TAG = "CommentAdapter_싸피"
class CommentAdapter(
    private val onEditClick: (CommentDto) -> Unit = {},
    private val onDeleteClick: (CommentDto) -> Unit = {}
) : ListAdapter<CommentDto, CommentAdapter.CommentViewHolder>(CommentDiffCallback()) {

    inner class CommentViewHolder(private val binding: ItemCommentBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(comment: CommentDto) {
            binding.tvNickname.text = comment.nickname
            binding.tvContent.text = comment.content
            binding.tvCreatedAt.text = formatCreatedAt(comment.createdAt)

            // 프로필 이미지 로드
            Glide.with(binding.root.context)
                .load(comment.profileImage)
                .placeholder(R.drawable.ic_my_page)
                .error(R.drawable.ic_my_page)
                .circleCrop()
                .into(binding.ivProfile)

            val isMyComment = comment.isMine
            val userNickName = ApplicationClass.sharedPreferences.getUserNickName()

            Log.d(TAG, "========================================")
            Log.d(TAG, "댓글 바인딩 - ID: ${comment.commentId}")
            Log.d(TAG, "닉네임: ${comment.nickname}")
            Log.d(TAG, "내용: ${comment.content}")
            Log.d(TAG, "isMine: $isMyComment")
            Log.d(TAG, "SharedPreference ${ApplicationClass.sharedPreferences.getUserNickName()}")
            Log.d(TAG, "========================================")

            // 내 댓글인 경우에만 수정/삭제 버튼 표시
            if (userNickName.equals(comment.nickname)) {
                Log.d(TAG, "✅ 내 댓글입니다! 수정/삭제 버튼 표시")
                binding.ivEdit.visibility = View.VISIBLE

                binding.ivEdit.setOnClickListener {
                    Log.d(TAG, "📝 수정 버튼 클릭 - 댓글 ID: ${comment.commentId}")
                    onEditClick(comment)
                }

                binding.ivDelete.setOnClickListener {
                    Log.d(TAG, "🗑️ 삭제 버튼 클릭 - 댓글 ID: ${comment.commentId}")
                    onDeleteClick(comment)
                }
            } else {
                Log.d(TAG, "❌ 다른 사람 댓글입니다. 버튼 숨김")
                binding.ivEdit.visibility = View.GONE
                binding.ivDelete.visibility = View.GONE
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val binding = ItemCommentBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CommentViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    private fun formatCreatedAt(raw: String?): String {
        if (raw.isNullOrBlank()) return ""
        val s = raw.trim().replace("T", " ")
        val spaceIdx = s.indexOf(' ')
        if (spaceIdx == -1) return s
        val datePart = s.substring(0, spaceIdx)
        val timePart = s.substring(spaceIdx + 1)
        val hhmm = timePart.take(5)
        return if (hhmm.length == 5) "$datePart $hhmm" else s
    }

    private class CommentDiffCallback : DiffUtil.ItemCallback<CommentDto>() {
        override fun areItemsTheSame(oldItem: CommentDto, newItem: CommentDto): Boolean =
            oldItem.commentId == newItem.commentId

        override fun areContentsTheSame(oldItem: CommentDto, newItem: CommentDto): Boolean = oldItem == newItem
    }
}
