package com.example.baseproject.presentation.hometab.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import com.example.baseproject.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class InfoBottomSheet(
    private val onShareClick: () -> Unit,
    private val onInfoClick: () -> Unit,
    private val onDeleteClick: () -> Unit,
) : BottomSheetDialogFragment() {

    override fun getTheme(): Int {
        return R.style.BottomSheetDialogTheme
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.bottom_sheet_infor, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnClose = view.findViewById<ImageButton>(R.id.btnClose)
        val btnInfo = view.findViewById<LinearLayout>(R.id.optionInfo)
        val btnShare = view.findViewById<LinearLayout>(R.id.optionShare)
        val btnDelete = view.findViewById<LinearLayout>(R.id.optionDelete)
        btnInfo.setOnClickListener {
            onInfoClick()
        }
        btnShare.setOnClickListener {
            onShareClick()
        }
        btnDelete.setOnClickListener {
            onDeleteClick()
        }
        btnClose.setOnClickListener {
            dismiss()
        }
    }

}