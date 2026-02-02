package com.ssafy.closetory.homeActivity.adapter

import android.util.Log
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

class PostDetailItemAdapter(
    private val onItemClick: (PostDetailItemDto) -> Unit,
    private val onSaveClick: (PostDetailItemDto) -> Unit
) : RecyclerView.Adapter<PostDetailItemAdapter.ViewHolder>() {

    private val items = mutableListOf<PostDetailItemDto>()
    private var isMinePost: Boolean = false

    fun setIsMinePost(value: Boolean) {
        isMinePost = value
        notifyDataSetChanged()
    }

    fun submitList(newItems: List<PostDetailItemDto>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    inner class ViewHolder(private val binding: ItemPostDetailItemBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: PostDetailItemDto) {
            Log.d(TAG, "bind item: clothesId=${item.clothesId}, isSaved=${item.isSaved}, photoUrl=${item.photoUrl}")

            val rawUrl = item.photoUrl
            val finalUrl = if (rawUrl.startsWith("http")) rawUrl else "${ApplicationClass.API_BASE_URL}${item.photoUrl}"

            Glide.with(binding.root)
                .load(finalUrl)
                .placeholder(R.drawable.bg_gray_box)
                .error(R.drawable.bg_gray_box)
                .into(binding.ivItem)

            binding.ivSave.visibility = if (isMinePost) View.GONE else View.VISIBLE

            if (!isMinePost) {
                binding.ivSave.setImageResource(
                    if (item.isSaved) {
                        R.drawable.baseline_bookmark_24
                    } else {
                        R.drawable.baseline_bookmark_border_24
                    }
                )

                // 클릭은 그대로 Fragment로 전달 (거기서 resolve해서 처리)
                binding.ivSave.setOnClickListener { onSaveClick(item) }
            } else {
                binding.ivSave.setOnClickListener(null)
            }

            binding.root.setOnClickListener { onItemClick(item) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemPostDetailItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        Log.d(
            TAG,
            "onBindViewHolder pos=$position clothesId=${item.clothesId} isSaved=${item.isSaved} url=${item.photoUrl}"
        )
        holder.bind(item)
    }

    override fun getItemCount(): Int = items.size
}
