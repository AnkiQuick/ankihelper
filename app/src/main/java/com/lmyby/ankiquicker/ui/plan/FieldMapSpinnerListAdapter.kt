package com.lmyby.ankiquicker.ui.plan

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.lmyby.ankiquicker.R

/**
 * Adapter for field mapping spinner list
 * Converted to Kotlin as part of Phase 4 adapter migration
 */
class FieldMapSpinnerListAdapter(
    private val mActivity: Activity,
    private val mFieldsMapItemList: List<FieldsMapItem>
) : RecyclerView.Adapter<FieldMapSpinnerListAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val exportElementName: TextView = view.findViewById(R.id.tv_export_element)
        val fieldsSpinner: Spinner = view.findViewById(R.id.spinner_fields)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.field_map_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = mFieldsMapItemList[position]
        holder.exportElementName.text = item.field

        val arrayAdapter = ArrayAdapter(
            mActivity,
            android.R.layout.simple_spinner_dropdown_item,
            item.exportedElementNames
        )
        holder.fieldsSpinner.adapter = arrayAdapter
        holder.fieldsSpinner.setSelection(item.selectedFieldPos)
        holder.fieldsSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                item.selectedFieldPos = position
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // No action needed
            }
        }
    }

    override fun getItemCount(): Int = mFieldsMapItemList.size
}
