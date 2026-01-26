package com.ssafy.closetory.util

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.ssafy.closetory.R

// ✅ 기존 TagOptions처럼: items + render() + getSelectedTag() 형태 유지
object PreferenceTagOptions {

    val items = listOf(
        OptionItem("캐주얼", 1),
        OptionItem("귀여움", 2),
        OptionItem("시크", 3),
        OptionItem("화려함", 4),
        OptionItem("밝음", 5),
        OptionItem("유니크", 6),
        OptionItem("여성스러움", 7),
        OptionItem("남성스러움", 8),
        OptionItem("트렌디", 9),
        OptionItem("빈티지", 10),
        OptionItem("데이트", 11),
        OptionItem("출근/업무", 12),
        OptionItem("일상", 13),
        OptionItem("여행", 14),
        OptionItem("격식 있는 자리", 15),
        OptionItem("운동", 16)
    )

    // ✅ dialogRoot(또는 sectionRoot)에 rvOptions가 있어야 함
    fun render(sectionRoot: View, context: Context, spanCount: Int = 3) {
        val rv = sectionRoot.findViewById<RecyclerView>(R.id.rvOptions)

        // 이미 세팅되어 있으면 중복 세팅 방지
        if (rv.layoutManager == null) {
            rv.layoutManager = GridLayoutManager(context, spanCount)
        }
        if (rv.adapter == null) {
            rv.adapter = TagGridAdapter(items)
        }
    }

    // ✅ 기존 TagOptions.getSelectedTag 처럼 동일한 사용감
    fun getSelectedTag(sectionRoot: View): List<Int> {
        val rv = sectionRoot.findViewById<RecyclerView>(R.id.rvOptions)
        val adapter = rv.adapter as? TagGridAdapter ?: return emptyList()
        return adapter.getSelectedCodes()
    }

    // (선택) 초기 선택값 세팅하고 싶을 때
    fun setSelected(sectionRoot: View, codes: Collection<Int>) {
        val rv = sectionRoot.findViewById<RecyclerView>(R.id.rvOptions)
        val adapter = rv.adapter as? TagGridAdapter ?: return
        adapter.setSelected(codes)
    }
}

// ✅ 한 파일 안에 Adapter까지 같이 둠
class TagGridAdapter(private val items: List<OptionItem>) : RecyclerView.Adapter<TagGridAdapter.VH>() {

    private val selected = linkedSetOf<Int>() // code 저장

    fun getSelectedCodes(): List<Int> = selected.toList()

    fun setSelected(codes: Collection<Int>) {
        selected.clear()
        selected.addAll(codes)
        notifyDataSetChanged()
    }

    inner class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val btn: MaterialButton = itemView.findViewById(R.id.btnOption)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_preference_tag, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = items[position]

        holder.btn.text = item.labelKorean
        holder.btn.isSelected = selected.contains(item.code)

        holder.btn.setOnClickListener {
            if (selected.contains(item.code)) selected.remove(item.code) else selected.add(item.code)
            notifyItemChanged(position) // 선택 색 갱신
        }
    }

    override fun getItemCount(): Int = items.size
}
