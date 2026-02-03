// package com.ssafy.closetory.homeActivity.adapter
//
// import android.view.LayoutInflater
// import android.view.ViewGroup
// import androidx.recyclerview.widget.DiffUtil
// import androidx.recyclerview.widget.ListAdapter
// import androidx.recyclerview.widget.RecyclerView
// import com.bumptech.glide.Glide
// import com.ssafy.closetory.R
// import com.ssafy.closetory.databinding.ItemCommentBinding
// import com.ssafy.closetory.dto.CommentDto
//
// class CommentAdapter : ListAdapter<CommentDto, CommentAdapter.CommentViewHolder>(CommentDiffCallback()) {
//
//    inner class CommentViewHolder(private val binding: ItemCommentBinding) :
//        RecyclerView.ViewHolder(binding.root) {
//
//        fun bind(comment: CommentDto) {
//            binding.tvNickname.text = comment.nickname
//            binding.tvContent.text = comment.content
//            binding.tvCreatedAt.text = comment.createdAt
//
//            // 프로필 이미지 로드
//            Glide.with(binding.root.context)
//                .load(comment.profileImage)
//                .placeholder(R.drawable.ic_profile_default)
//                .error(R.drawable.ic_profile_default)
//                .circleCrop()
//                .into(binding.ivProfile)
//        }
//    }
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
//        val binding = ItemCommentBinding.inflate(
//            LayoutInflater.from(parent.context),
//            parent,
//            false
//        )
//        return CommentViewHolder(binding)
//    }
//
//    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
//        holder.bind(getItem(position))
//    }
//
//    private class CommentDiffCallback : DiffUtil.ItemCallback<CommentDto>() {
//        override fun areItemsTheSame(oldItem: CommentDto, newItem: CommentDto): Boolean =
//            oldItem.commentId == newItem.commentId
//
//        override fun areContentsTheSame(oldItem: CommentDto, newItem: CommentDto): Boolean = oldItem == newItem
//    }
// }

// package com.ssafy.closetory.homeActivity.adapter
//
// import android.view.LayoutInflater
// import android.view.View
// import android.view.ViewGroup
// import androidx.recyclerview.widget.DiffUtil
// import androidx.recyclerview.widget.ListAdapter
// import androidx.recyclerview.widget.RecyclerView
// import com.bumptech.glide.Glide
// import com.ssafy.closetory.R
// import com.ssafy.closetory.databinding.ItemCommentBinding
// import com.ssafy.closetory.dto.CommentDto
//
// class CommentAdapter(
//    private val onEditClick: (CommentDto) -> Unit = {},
//    private val onDeleteClick: (CommentDto) -> Unit = {}
// ) : ListAdapter<CommentDto, CommentAdapter.CommentViewHolder>(CommentDiffCallback()) {
//
//    inner class CommentViewHolder(private val binding: ItemCommentBinding) :
//        RecyclerView.ViewHolder(binding.root) {
//
//        fun bind(comment: CommentDto) {
//            binding.tvNickname.text = comment.nickname
//            binding.tvContent.text = comment.content
//            binding.tvCreatedAt.text = comment.createdAt
//
//            // 프로필 이미지 로드
//            Glide.with(binding.root.context)
//                .load(comment.profileImage)
//                .placeholder(R.drawable.ic_profile_default)
//                .error(R.drawable.ic_profile_default)
//                .circleCrop()
//                .into(binding.ivProfile)
//
//            // 내 댓글인 경우에만 수정/삭제 버튼 표시
//            if (comment.isMine) {
//                binding.layoutCommentActions.visibility = View.VISIBLE
//                binding.btnEditComment.setOnClickListener { onEditClick(comment) }
//                binding.btnDeleteComment.setOnClickListener { onDeleteClick(comment) }
//            } else {
//                binding.layoutCommentActions.visibility = View.GONE
//            }
//        }
//    }
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
//        val binding = ItemCommentBinding.inflate(
//            LayoutInflater.from(parent.context),
//            parent,
//            false
//        )
//        return CommentViewHolder(binding)
//    }
//
//    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
//        holder.bind(getItem(position))
//    }
//
//    private class CommentDiffCallback : DiffUtil.ItemCallback<CommentDto>() {
//        override fun areItemsTheSame(oldItem: CommentDto, newItem: CommentDto): Boolean =
//            oldItem.commentId == newItem.commentId
//
//        override fun areContentsTheSame(oldItem: CommentDto, newItem: CommentDto): Boolean = oldItem == newItem
//    }
// }

// package com.ssafy.closetory.homeActivity.adapter
//
// import android.util.Log
// import android.view.LayoutInflater
// import android.view.View
// import android.view.ViewGroup
// import androidx.recyclerview.widget.DiffUtil
// import androidx.recyclerview.widget.ListAdapter
// import androidx.recyclerview.widget.RecyclerView
// import com.bumptech.glide.Glide
// import com.ssafy.closetory.R
// import com.ssafy.closetory.databinding.ItemCommentBinding
// import com.ssafy.closetory.dto.CommentDto
//
// class CommentAdapter(
//    private val onEditClick: (CommentDto) -> Unit = {},
//    private val onDeleteClick: (CommentDto) -> Unit = {}
// ) : ListAdapter<CommentDto, CommentAdapter.CommentViewHolder>(CommentDiffCallback()) {
//
//    private val TAG = "CommentAdapter_Debug"
//
//    inner class CommentViewHolder(private val binding: ItemCommentBinding) :
//        RecyclerView.ViewHolder(binding.root) {
//
//        fun bind(comment: CommentDto) {
//            binding.tvNickname.text = comment.nickname
//            binding.tvContent.text = comment.content
//            binding.tvCreatedAt.text = comment.createdAt
//
//            // 프로필 이미지 로드
//            Glide.with(binding.root.context)
//                .load(comment.profileImage)
//                .placeholder(R.drawable.ic_profile_default)
//                .error(R.drawable.ic_profile_default)
//                .circleCrop()
//                .into(binding.ivProfile)
//
//            // ✅ 상세 디버깅 로그
//            Log.d(TAG, "========================================")
//            Log.d(TAG, "댓글 바인딩 - ID: ${comment.commentId}")
//            Log.d(TAG, "닉네임: ${comment.nickname}")
//            Log.d(TAG, "내용: ${comment.content}")
//            Log.d(TAG, "isMine: ${comment.isMine}")
//            Log.d(TAG, "========================================")
//
//            // 내 댓글인 경우에만 수정/삭제 버튼 표시
//            if (comment.isMine) {
//                Log.d(TAG, "✅ 내 댓글입니다! 버튼 표시")
//                binding.layoutCommentActions.visibility = View.VISIBLE
//
//                binding.btnEditComment.setOnClickListener {
//                    Log.d(TAG, "📝 수정 버튼 클릭 - 댓글 ID: ${comment.commentId}")
//                    onEditClick(comment)
//                }
//
//                binding.btnDeleteComment.setOnClickListener {
//                    Log.d(TAG, "🗑️ 삭제 버튼 클릭 - 댓글 ID: ${comment.commentId}")
//                    onDeleteClick(comment)
//                }
//            } else {
//                Log.d(TAG, "❌ 다른 사람 댓글입니다. 버튼 숨김")
//                binding.layoutCommentActions.visibility = View.GONE
//            }
//        }
//    }
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
//        val binding = ItemCommentBinding.inflate(
//            LayoutInflater.from(parent.context),
//            parent,
//            false
//        )
//        return CommentViewHolder(binding)
//    }
//
//    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
//        holder.bind(getItem(position))
//    }
//
//    private class CommentDiffCallback : DiffUtil.ItemCallback<CommentDto>() {
//        override fun areItemsTheSame(oldItem: CommentDto, newItem: CommentDto): Boolean =
//            oldItem.commentId == newItem.commentId
//
//        override fun areContentsTheSame(oldItem: CommentDto, newItem: CommentDto): Boolean = oldItem == newItem
//    }
// }

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

class CommentAdapter(
    private val onEditClick: (CommentDto) -> Unit = {},
    private val onDeleteClick: (CommentDto) -> Unit = {}
) : ListAdapter<CommentDto, CommentAdapter.CommentViewHolder>(CommentDiffCallback()) {

    private val TAG = "CommentAdapter_Debug"

    inner class CommentViewHolder(private val binding: ItemCommentBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(comment: CommentDto) {
            binding.tvNickname.text = comment.nickname
            binding.tvContent.text = comment.content
            binding.tvCreatedAt.text = comment.createdAt

            // 프로필 이미지 로드
            Glide.with(binding.root.context)
                .load(comment.profileImage)
                .placeholder(R.drawable.ic_profile_default)
                .error(R.drawable.ic_profile_default)
                .circleCrop()
                .into(binding.ivProfile)

            // ⚠️ 임시 해결책: 서버의 isMine이 false로 오는 문제가 해결될 때까지
            // 닉네임으로 비교 (또는 userId로 비교)
//            val myNickname = ApplicationClass.sharedPreferences.getString("nickname", "") ?: ""
//            val myNickname = ApplicationClass.sharedPreferences.getNickname() // 전용 메소드로 고정
//            val isActuallyMine = if (comment.isMine) {
//                // 서버가 제대로 판단한 경우
//                true
//            } else {
//                // 서버가 false로 보낸 경우, 닉네임으로 재확인
//                comment.nickname == myNickname
//            }

            val commentNickname = comment.nickname?.trim().orEmpty()
            val myNickname = ApplicationClass.sharedPreferences.getNickname().trim()

            val isActuallyMine = comment.isMine || (myNickname.isNotEmpty() && commentNickname == myNickname)

            Log.d(TAG, "========================================")
            Log.d(TAG, "댓글 바인딩 - ID: ${comment.commentId}")
            Log.d(TAG, "닉네임: ${comment.nickname}")
            Log.d(TAG, "내용: ${comment.content}")
            Log.d(TAG, "서버 isMine: ${comment.isMine}")
            Log.d(TAG, "내 닉네임: $myNickname")
            Log.d(TAG, "실제 isMine: $isActuallyMine")
            Log.d(TAG, "========================================")

            // 내 댓글인 경우에만 수정/삭제 버튼 표시
            if (isActuallyMine) {
                Log.d(TAG, "✅ 내 댓글입니다! 버튼 표시")
                binding.layoutCommentActions.visibility = View.VISIBLE

                binding.btnEditComment.setOnClickListener {
                    Log.d(TAG, "📝 수정 버튼 클릭 - 댓글 ID: ${comment.commentId}")
                    onEditClick(comment)
                }

                binding.btnDeleteComment.setOnClickListener {
                    Log.d(TAG, "🗑️ 삭제 버튼 클릭 - 댓글 ID: ${comment.commentId}")
                    onDeleteClick(comment)
                }
            } else {
                Log.d(TAG, "❌ 다른 사람 댓글입니다. 버튼 숨김")
                binding.layoutCommentActions.visibility = View.GONE
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

    private class CommentDiffCallback : DiffUtil.ItemCallback<CommentDto>() {
        override fun areItemsTheSame(oldItem: CommentDto, newItem: CommentDto): Boolean =
            oldItem.commentId == newItem.commentId

        override fun areContentsTheSame(oldItem: CommentDto, newItem: CommentDto): Boolean = oldItem == newItem
    }
}
