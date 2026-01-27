package com.ssafy.closetory.homeActivity.codyRepository

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ssafy.closetory.databinding.ItemCodyBinding

class CodyAdapter : RecyclerView.Adapter<CodyAdapter.CodyViewHolder>() {

    private val items: MutableList<String> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CodyViewHolder {
        val binding = ItemCodyBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CodyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CodyViewHolder, position: Int) {
    }

    override fun getItemCount(): Int = items.size

    fun submitItems(newItems: List<String>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    class CodyViewHolder(val binding: ItemCodyBinding) : RecyclerView.ViewHolder(binding.root)
}
