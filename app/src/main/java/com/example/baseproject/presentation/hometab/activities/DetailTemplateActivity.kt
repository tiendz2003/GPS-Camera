package com.example.baseproject.presentation.hometab.activities

import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.baseproject.R
import com.example.baseproject.bases.BaseActivity
import com.example.baseproject.databinding.ActivityDetailTemplateBinding
import com.example.baseproject.models.TemplateType
import com.example.baseproject.models.ThemeTemplateModel
import com.example.baseproject.presentation.hometab.adapter.ThemeTemplateAdapter

class DetailTemplateActivity : BaseActivity<ActivityDetailTemplateBinding>(
    ActivityDetailTemplateBinding::inflate
) {
    private val allItemsAdapter by lazy {
        ThemeTemplateAdapter(isFromDetail = true){

        }
    }
    companion object{
        const val TEMPLATE_NAME = "TEMPLATE_NAME"

        fun getIntent(context: Context, templateName: TemplateType): Intent {
            return Intent(context, DetailTemplateActivity::class.java).apply {
                putExtra(TEMPLATE_NAME, templateName)
            }
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
    }

    override fun initData() {

    }

    override fun initView() {
        val templateType: TemplateType? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getSerializableExtra(TEMPLATE_NAME, TemplateType::class.java)
        } else {
            intent.getSerializableExtra(TEMPLATE_NAME) as? TemplateType
        }
        Log.d("DetailTemplateActivity", "initView: $templateType")
        templateType?.let { type ->
            Log.d("DetailTemplateActivity", "initView: ${type.name}")
            when (type) {
                TemplateType.DAILY -> {
                    binding.tvTitle.text = "Daily"
                }

                TemplateType.TRAVEL -> {
                    binding.tvTitle.text = "Travel"
                }

                TemplateType.GPS -> {
                    binding.tvTitle.text = "GPS"
                }
            }
            val filteredList = ThemeTemplateModel.getTemplate().filter { it.type == type }
            Log.d("DetailTemplateActivity", "Filtered List: $filteredList")
            allItemsAdapter.submitList(filteredList)
            setupRecyclerView()
        } ?: Log.e("DetailTemplateActivity", "Méo nhận được!")
    }


    override fun initActionView() {

    }
    private fun setupRecyclerView() {
        binding.rvDetail.apply {
            setHasFixedSize(true)
            adapter = allItemsAdapter
            Log.d("DetailTemplateActivity", "size: ${allItemsAdapter.currentList.size}")
            val gridLayoutManager = GridLayoutManager(this@DetailTemplateActivity, 2)
            layoutManager = gridLayoutManager
            val spacing = resources.getDimensionPixelSize(R.dimen.grid_spacing)
            addItemDecoration(object:RecyclerView.ItemDecoration(){
                override fun getItemOffsets(
                    outRect: Rect,
                    view: View,
                    parent: RecyclerView,
                    state: RecyclerView.State
                ) {
                    val position = parent.getChildAdapterPosition(view)
                    val column = position % 2

                    outRect.left = spacing - column * spacing / 2
                    outRect.right =  (column + 1) * spacing / 2
                    if(position >=2){
                        outRect.top = spacing
                    }
                }
            })
        }
    }
}