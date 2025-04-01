package com.example.baseproject.presentation.hometab.activities

import android.content.Context
import androidx.core.content.ContextCompat
import com.example.baseproject.bases.BaseActivity
import com.example.baseproject.databinding.ActivityMainBinding
import androidx.core.view.get
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.ShapeAppearanceModel

class MainActivity : BaseActivity<ActivityMainBinding>(ActivityMainBinding::inflate) {

    override fun initData() {

    }

    override fun initView() {
        binding.bottomNavigationView.background = null
        binding.bottomNavigationView.menu[1].isEnabled = false

        val materialShapeDrawable = binding.bottomAppBar.background as MaterialShapeDrawable

        // Tạo ShapeAppearanceModel mới với góc bo tròn trên
        // và giữ nguyên các thuộc tính khác của model hiện tại
        val shapeAppearanceModel = materialShapeDrawable.shapeAppearanceModel.toBuilder()
            .setTopLeftCornerSize(16f.dpToPx(this))
            .setTopRightCornerSize(16f.dpToPx(this))
            .build()

        // Thiết lập model mới và giữ nguyên các thuộc tính đường cong cho FAB
        materialShapeDrawable.shapeAppearanceModel = shapeAppearanceModel

        // Thiết lập đổ bóng
        materialShapeDrawable.shadowCompatibilityMode = MaterialShapeDrawable.SHADOW_COMPAT_MODE_ALWAYS
        materialShapeDrawable.initializeElevationOverlay(this)
        materialShapeDrawable.elevation = 4f.dpToPx(this)

    }

    override fun initActionView() {

    }

}
fun Float.dpToPx(context: Context): Float {
    return this * context.resources.displayMetrics.density
}