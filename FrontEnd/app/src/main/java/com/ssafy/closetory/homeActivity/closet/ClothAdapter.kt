package com.ssafy.closetory.homeActivity.closet

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ssafy.closetory.R
import com.ssafy.closetory.databinding.ItemClothBinding
import com.ssafy.closetory.dto.ClothItemDto
import com.ssafy.closetory.ApplicationClass

class ClothAdapter() : ListAdapter<ClothItemDto, ClothAdapter.ViewHodler>(diffCallback){

    companion object{
        private val diffCallback = object : DiffUtil.ItemCallback<ClothItemDto>() {
            override fun areItemsTheSame(
                oldItem: ClothItemDto,
                newItem: ClothItemDto
            ): Boolean {
                return oldItem.clothId == newItem.clothId
            }

            override fun areContentsTheSame(
                oldItem: ClothItemDto,
                newItem: ClothItemDto
            ): Boolean {
                return oldItem == newItem
            }
        }
    }

    inner class ViewHodler(
        private val binding : ItemClothBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item : ClothItemDto) = with(binding){

            val imageUrl = "${ApplicationClass.SERVER_URL}${item.clothImage}"

            Glide.with(binding.imgBtn.context)
                .load(imageUrl)
                .placeholder(R.drawable.placeholder)
                .error(R.drawable.error)
                .into(binding.imgBtn)
            
            // 클릭에 따른 동작
            binding.root.setOnClickListener { 
                
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHodler {
        val binding = ItemClothBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHodler(binding)
    }

    override fun onBindViewHolder(
        holder: ViewHodler,
        position: Int
    ) {
        holder.bind(getItem(position))
    }
}
