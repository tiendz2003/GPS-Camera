package com.ssquad.gps.camera.geotag.presentation.hometab.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.RadioGroup
import com.ssquad.gps.camera.geotag.data.models.SortOption
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.ssquad.gps.camera.geotag.R


class SortBottomSheet(
    private val currentSortOptions: SortOption,
    private val onSortSelected: (SortOption) -> Unit,
) : BottomSheetDialogFragment() {

    override fun getTheme(): Int {
        return  R.style.BottomSheetDialogTheme
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.bottom_sheet_sort, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val radioGroup = view.findViewById<RadioGroup>(R.id.rgSortOptions)
        val btnClose = view.findViewById<ImageButton>(R.id.btnClose)
        when (currentSortOptions) {
            SortOption.NAME-> radioGroup.check(R.id.rbName)
            SortOption.FILE_SIZE -> radioGroup.check(R.id.rbFileSize)
            SortOption.DATE_ADDED -> radioGroup.check(R.id.rbDateAdded)
        }
        radioGroup.setOnCheckedChangeListener { _, checkId ->
            val selectedOption = when (checkId) {
                R.id.rbName -> SortOption.NAME
                R.id.rbFileSize -> SortOption.FILE_SIZE
                R.id.rbDateAdded -> SortOption.DATE_ADDED
                else -> SortOption.DATE_ADDED
            }
            onSortSelected(selectedOption)
            dismiss()
        }
        btnClose.setOnClickListener {
            dismiss()
        }
    }

}