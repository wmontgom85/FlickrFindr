package com.wmontgom85.flickrfindr.ui.fragment

import android.app.Dialog
import android.widget.NumberPicker
import android.os.Bundle
import androidx.fragment.app.DialogFragment

class NumberPickerDialog : DialogFragment() {
    var valueChangeListener: NumberPicker.OnValueChangeListener? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        super.onCreateDialog(savedInstanceState)

        val numberPicker = NumberPicker(activity)
        numberPicker.minValue = 0
        numberPicker.maxValue = 3
        numberPicker.displayedValues = arrayOf("10", "25", "50", "100")

        val alertBuilder = android.app.AlertDialog.Builder(activity)
        alertBuilder.setTitle("Per Page Count")
        alertBuilder.setMessage("Make a selection:")
        alertBuilder.setPositiveButton("Done") { dialog, _ ->
            valueChangeListener!!.onValueChange(numberPicker, numberPicker.value, numberPicker.value)
        }
        alertBuilder.setNegativeButton("Cancel") { dialog, _ ->
           dialog.dismiss()
        }

        alertBuilder.setView(numberPicker)

        return alertBuilder.create()
    }
}