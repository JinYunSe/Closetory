package com.ssafy.closetory.homeActivity.post.create.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ssafy.closetory.ApplicationClass
import com.ssafy.closetory.R
import com.ssafy.closetory.databinding.ItemPostItemBinding
import com.ssafy.closetory.dto.PostCreateSelectedItem

class PostItemAdapter : ListAdapter<PostCreateSelectedItem, PostItemAdapter.ViewHolder>(diffCallback) {

    // 외부로 클릭 이벤트 전달
    var onItemClickListener: ((PostCreateSelectedItem) -> Unit)? = null

    // X(삭제)도 외부로 전달
    var onRemoveClickListener: ((PostCreateSelectedItem) -> Unit)? = null

    companion object {
        private val diffCallback = object : DiffUtil.ItemCallback<PostCreateSelectedItem>() {
            override fun areItemsTheSame(oldItem: PostCreateSelectedItem, newItem: PostCreateSelectedItem): Boolean =
                oldItem.clothesId == newItem.clothesId

            override fun areContentsTheSame(oldItem: PostCreateSelectedItem, newItem: PostCreateSelectedItem): Boolean =
                oldItem == newItem
        }
    }

    inner class ViewHolder(private val binding: ItemPostItemBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: PostCreateSelectedItem) = with(binding) {
            val imageUrl = "${ApplicationClass.API_BASE_URL}${item.photoUrl}"

            Glide.with(ivItem.context)
                .load(imageUrl)
                .placeholder(R.drawable.placeholder)
                .error(R.drawable.error)
                .centerCrop()
                .into(ivItem)

            // 아이템 클릭
            ivItem.setOnClickListener {
                onItemClickListener?.invoke(item)
            }

            // X 클릭(삭제)
            btnRemove.setOnClickListener {
                onRemoveClickListener?.invoke(item)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemPostItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}
