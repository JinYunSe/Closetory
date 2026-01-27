package com.ssafy.closetory.homeActivity.adpter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ssafy.closetory.ApplicationClass
import com.ssafy.closetory.R
import com.ssafy.closetory.databinding.ItemClothBinding
import com.ssafy.closetory.dto.ClothItemDto

private const val TAG = "ClothAdapter_싸피"
class ClothAdapter : ListAdapter<ClothItemDto, ClothAdapter.ViewHodler>(diffCallback) {

    // ✅ 클릭 이벤트를 StylingFragment로 전달하기 위한 람다 함수
    var onItemClickListener: ((ClothItemDto) -> Unit)? = null

    companion object {
        private val diffCallback = object : DiffUtil.ItemCallback<ClothItemDto>() {
            override fun areItemsTheSame(oldItem: ClothItemDto, newItem: ClothItemDto): Boolean =
                oldItem.clothesId == newItem.clothesId

            override fun areContentsTheSame(oldItem: ClothItemDto, newItem: ClothItemDto): Boolean = oldItem == newItem
        }
    }

    inner class ViewHodler(private val binding: ItemClothBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ClothItemDto) = with(binding) {
            Log.d(TAG, "SERVER URL : ${ApplicationClass.SERVER_URL}")
            Log.d(TAG, "clothesId : ${item.clothesId}")
            Log.d(TAG, "photoUrl : ${item.photoUrl}")

            val imageUrl = "${ApplicationClass.SERVER_URL}${item.photoUrl}"

            Glide.with(binding.imgBtn.context)
                .load(item.photoUrl)
                .placeholder(R.drawable.placeholder)
                .error(R.drawable.error)
                .into(binding.imgBtn)

            // 클릭 시 프래그먼트에 알림
            imgBtn.setOnClickListener {
                Log.d(TAG, "아이템 클릭됨: ${item.clothesId}")
                onItemClickListener?.invoke(item)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHodler {
        val binding = ItemClothBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHodler(binding)
    }

    override fun onBindViewHolder(holder: ViewHodler, position: Int) {
        holder.bind(getItem(position))
    }
}
