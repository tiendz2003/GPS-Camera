package com.example.baseproject.presentation.settingtab.dialog

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.text.format.DateFormat.is24HourFormat
import android.util.Log
import android.view.LayoutInflater
import android.view.WindowManager
import androidx.fragment.app.FragmentActivity
import com.example.baseproject.R
import com.example.baseproject.databinding.DialogCustomTimeBinding
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.TimeZone

class CustomDateTimeDialog @SuppressLint("SimpleDateFormat")
constructor(
    private val context: Context,
    date: String,
    time: String,
    private val onBtnSaveClick: () -> Unit
) :
    Dialog(context, R.style.BaseDialog) {
    var currDate: String = ""
    var currTime: String = ""
    private val binding: DialogCustomTimeBinding

    private val calendar: Calendar
    private val dateFormat: SimpleDateFormat
    private val timeFormat: SimpleDateFormat

    init {
        Log.d("CustomDateTimeDialog", "CustomDateTimeDialog:$date $time")
        val attributes = window!!.attributes
        attributes.width = WindowManager.LayoutParams.MATCH_PARENT
        attributes.height = WindowManager.LayoutParams.WRAP_CONTENT
        window!!.attributes = attributes
        window!!.setSoftInputMode(16)
        binding = DialogCustomTimeBinding.inflate(LayoutInflater.from(context))
        calendar = Calendar.getInstance()

        dateFormat = SimpleDateFormat("MM/dd/yyyy")
        timeFormat = SimpleDateFormat("HH:mm a")

        currDate = dateFormat.format(calendar.time)
        currTime = timeFormat.format(calendar.time)
        binding.tvValueDate.text = currDate
        binding.tvValueTime.text = currTime
        if (date.isEmpty() || time.isEmpty()) {
            Log.d("CustomDateTimeDialog", "CustomDateTimeDialog:$currDate $currTime")
            setText(currDate, currTime)
        } else {
            setText(date, time)
            try {
                val dateObj = dateFormat.parse(date)
                val timeObj = timeFormat.parse(time)
                if (dateObj != null) {
                    val dateCalendar = Calendar.getInstance()
                    dateCalendar.time = dateObj
                    calendar.set(Calendar.YEAR, dateCalendar.get(Calendar.YEAR))
                    calendar.set(Calendar.MONTH, dateCalendar.get(Calendar.MONTH))
                    calendar.set(Calendar.DAY_OF_MONTH, dateCalendar.get(Calendar.DAY_OF_MONTH))
                }
                if (timeObj != null) {
                    val timeCalendar = Calendar.getInstance()
                    timeCalendar.time = timeObj
                    calendar.set(Calendar.HOUR_OF_DAY, timeCalendar.get(Calendar.HOUR_OF_DAY))
                    calendar.set(Calendar.MINUTE, timeCalendar.get(Calendar.MINUTE))
                }
            } catch (e: Exception) {
                calendar.time = Date()
            }
        }


        binding.tvValueDate.setOnClickListener { showDatePicker() }
        binding.tvValueTime.setOnClickListener { showTimePicker() }
        binding.btnCancel.setOnClickListener {
            dismiss()
        }
        binding.btnSave.setOnClickListener {
            onBtnSaveClick()
            dismiss()
        }
        setContentView(binding.root)
    }

    private fun showTimePicker() {
        val picker = MaterialTimePicker.Builder()
            .setTimeFormat(if (is24HourFormat(context)) TimeFormat.CLOCK_24H else TimeFormat.CLOCK_12H)
            .setHour(calendar[Calendar.HOUR_OF_DAY])
            .setMinute(calendar[Calendar.MINUTE])
            .setTitleText(context.getString(R.string.select_time))
            .setTheme(R.style.ThemeOverlay_App_MaterialTimePicker)
            .setInputMode(MaterialTimePicker.INPUT_MODE_CLOCK)
            .build()

        picker.addOnPositiveButtonClickListener {
            calendar[Calendar.HOUR_OF_DAY] = picker.hour
            calendar[Calendar.MINUTE] = picker.minute
            setValueTime()
        }

        picker.show((context as FragmentActivity).supportFragmentManager, "TIME_PICKER")
    }

    private fun setValueTime() {
        currTime = timeFormat.format(calendar.time)
        binding.tvValueTime.text = currTime
    }

    private fun showDatePicker() {
        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText(context.getString(R.string.select_date))
            .setTheme(R.style.ThemeOverlay_App_MaterialCalendar)
            .setSelection(calendar.timeInMillis)
            .build()

        datePicker.addOnPositiveButtonClickListener { selection ->
            val selectedCalendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
            selectedCalendar.timeInMillis = selection

            calendar.set(Calendar.YEAR, selectedCalendar.get(Calendar.YEAR))
            calendar.set(Calendar.MONTH, selectedCalendar.get(Calendar.MONTH))
            calendar.set(Calendar.DAY_OF_MONTH, selectedCalendar.get(Calendar.DAY_OF_MONTH))

            setValueDate()
        }

        datePicker.show((context as FragmentActivity).supportFragmentManager, "DATE_PICKER")
    }

    private fun setValueDate() {
        currDate = dateFormat.format(calendar.time)
        binding.tvValueDate.text = currDate
    }

    private fun setText(date: String, time: String) {
        binding.tvValueDate.text = date
        binding.tvValueTime.text = time
    }

    fun showDialog() {
        show()
    }
}