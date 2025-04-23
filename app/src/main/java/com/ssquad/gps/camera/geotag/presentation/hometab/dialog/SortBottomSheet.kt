package com.ssquad.gps.camera.geotag.presentation.hometab.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.RadioGroup
import android.widget.TextView
import com.ssquad.gps.camera.geotag.data.models.SortOption
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.ssquad.gps.camera.geotag.R


class SortBottomSheet(
    private val currentSortOptions: SortOption,
    private val onSortSelected: (SortOption) -> Unit,
) : BottomSheetDialogFragment() {

    override fun getTheme(): Int = R.style.BottomSheetDialogTheme

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.bottom_sheet_sort, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tvName = view.findViewById<TextView>(R.id.tvName)
        val tvFileSize = view.findViewById<TextView>(R.id.tvFileSize)
        val tvDateAdded = view.findViewById<TextView>(R.id.tvDateAdded)
        val btnClose = view.findViewById<ImageButton>(R.id.btnClose)

        val optionsMap = mapOf(
            tvName to SortOption.NAME,
            tvFileSize to SortOption.FILE_SIZE,
            tvDateAdded to SortOption.DATE_ADDED
        )

        fun updateSelection(selectedView: TextView) {
            optionsMap.forEach { (viewItem, option) ->
                val icon = if (viewItem == selectedView) R.drawable.ic_selected else R.drawable.ic_unchecked_language
                viewItem.setCompoundDrawablesWithIntrinsicBounds(icon, 0, 0, 0)
            }
        }

        optionsMap.forEach { (viewItem, option) ->
            if (option == currentSortOptions) {
                updateSelection(viewItem)
            }

            viewItem.setOnClickListener {
                updateSelection(viewItem)
                onSortSelected(optionsMap[viewItem] ?: SortOption.DATE_ADDED)
                dismiss()
            }
        }

        btnClose.setOnClickListener { dismiss() }
    }
}
