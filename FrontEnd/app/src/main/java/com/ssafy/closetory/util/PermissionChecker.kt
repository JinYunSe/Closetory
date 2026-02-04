package com.ssafy.closetory.util

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment

fun interface OnGrantedListener {
    fun onGranted()
}

private const val TAG = "CheckPermission_싸피"

class PermissionChecker {

    private var context: Context? = null

    private var permitted: OnGrantedListener? = null
    fun setOnGrantedListener(listener: OnGrantedListener) {
        permitted = listener
    }

    // init을 통해 미리 registerForActivityResult 등록
    private var requestPermissionLauncher: ActivityResultLauncher<Array<String>>? = null

    fun init(activity: AppCompatActivity) {
        if (requestPermissionLauncher != null) return
        requestPermissionLauncher =
            activity.registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
                resultChecking(it)
            }
    }

    fun init(fragment: Fragment) {
        if (requestPermissionLauncher != null) return
        requestPermissionLauncher =
            fragment.registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
                resultChecking(it)
            }
    }

    // 권한 체크
    fun checkPermission(context: Context, permissions: Array<String>): Boolean {
        this.context = context
        for (permission in permissions) {
            if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                return false
            }
        }
        return true
    }

    // 권한 요청
    fun requestPermissions(permissions: Array<String>) {
        val launcher = requestPermissionLauncher
            ?: throw IllegalStateException("PermissionChecker.init(fragment/activity)를 먼저 호출해야 합니다.")
        launcher.launch(permissions)
    }

    private fun resultChecking(result: Map<String, Boolean>) {
        val ctx = context ?: return
        Log.d(TAG, "requestPermissionLauncher: 건수 : ${result.size}")

        if (result.values.contains(false)) {
            Toast.makeText(ctx, "권한이 필요합니다.", Toast.LENGTH_SHORT).show()
            moveToSettings(ctx)
        } else {
            permitted?.onGranted()
        }
    }

    private fun moveToSettings(ctx: Context) {
        AlertDialog.Builder(ctx)
            .setTitle("권한이 필요합니다.")
            .setMessage("설정으로 이동합니다.")
            .setPositiveButton("확인") { dialog, _ ->
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    .setData(Uri.parse("package:${ctx.packageName}"))
                ctx.startActivity(intent)
                dialog.dismiss()
            }
            .setNegativeButton("취소") { dialog, _ -> dialog.dismiss() }
            .show()
    }
}

