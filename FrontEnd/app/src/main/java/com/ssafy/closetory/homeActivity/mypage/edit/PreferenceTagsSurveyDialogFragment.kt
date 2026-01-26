package com.ssafy.closetory.homeActivity.edit

import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.ssafy.closetory.R
import com.ssafy.closetory.util.PreferenceTagOptions

class PreferenceTagsSurveyDialogFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val root: View = layoutInflater.inflate(R.layout.dialog_preference_tags_survey, null)

        // ✅ 그리드 렌더링
        PreferenceTagOptions.render(root, requireContext(), spanCount = 3)

        val btnNext = root.findViewById<Button>(R.id.btnNext)
        btnNext.setOnClickListener {
            val selectedCodes = PreferenceTagOptions.getSelectedTag(root)

            // ✅ EditProfileFragment로 결과 전달
            parentFragmentManager.setFragmentResult(
                "pref_tags_result",
                bundleOf("codes" to selectedCodes.toIntArray())
            )
            dismiss()
        }

        return MaterialAlertDialogBuilder(requireContext())
            .setView(root)
            .create()
    }
}
