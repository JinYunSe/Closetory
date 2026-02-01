package com.ssafy.closetory.homeActivity.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ssafy.closetory.databinding.ItemPreferenceTagBinding
import com.ssafy.closetory.homeActivity.tagOnboarding.TagOption

class TagOnboardingAdapter(
    private val items: List<TagOption>,
    private val onSelectionChanged: (selectedIds: List<Int>) -> Unit
) : RecyclerView.Adapter<TagOnboardingAdapter.VH>() {

    private val selectedIds = linkedSetOf<Int>() // 다중 선택

    inner class VH(val binding: ItemPreferenceTagBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemPreferenceTagBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = items[position]
        val btn = holder.binding.btnOption

        btn.text = item.name

        // 선택 상태 반영 (selector가 state_selected 보고 색 변경)
        btn.isSelected = selectedIds.contains(item.id)

        btn.setOnClickListener {
            if (selectedIds.contains(item.id)) {
                selectedIds.remove(item.id)
            } else {
                selectedIds.add(item.id)
            }
            // 버튼 상태 갱신
            btn.isSelected = selectedIds.contains(item.id)

            onSelectionChanged(selectedIds.toList())
        }
    }

    override fun getItemCount(): Int = items.size

    fun getSelectedIds(): List<Int> = selectedIds.toList()
}
