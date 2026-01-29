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
import com.ssafy.closetory.dto.ClothesItemDto

private const val TAG = "ClothAdapter_싸피"
class ClothesAdapter : ListAdapter<ClothesItemDto, ClothesAdapter.ViewHodler>(diffCallback) {

    // 클릭 이벤트를 StylingFragment로 전달하기 위한 람다 함수
    var onItemClickListener: ((ClothesItemDto) -> Unit)? = null

    companion object {
        private val diffCallback = object : DiffUtil.ItemCallback<ClothesItemDto>() {
            override fun areItemsTheSame(oldItem: ClothesItemDto, newItem: ClothesItemDto): Boolean =
                oldItem.clothesId == newItem.clothesId

            override fun areContentsTheSame(oldItem: ClothesItemDto, newItem: ClothesItemDto): Boolean =
                oldItem == newItem
        }
    }

    inner class ViewHodler(private val binding: ItemClothBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ClothesItemDto) = with(binding) {
            Log.d(TAG, "SERVER URL : ${ApplicationClass.API_BASE_URL}")
            Log.d(TAG, "clothesId : ${item.clothesId}")
            Log.d(TAG, "photoUrl : ${item.photoUrl}")

            Glide.with(binding.ivPhoto)
                .load(item.photoUrl)
                .placeholder(R.drawable.placeholder)
                .error(R.drawable.error)
                .into(binding.ivPhoto)

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
