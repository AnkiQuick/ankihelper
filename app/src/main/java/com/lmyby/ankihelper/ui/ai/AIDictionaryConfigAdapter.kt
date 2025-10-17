package com.lmyby.ankihelper.ui.ai

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.lmyby.ankihelper.R
import com.lmyby.ankihelper.data.ai.AIDictionaryConfig

/**
 * Adapter for AI dictionary configuration list
 * Converted to Kotlin as part of Phase 4 adapter migration
 */
class AIDictionaryConfigAdapter(
    private var dictionaryConfigList: List<AIDictionaryConfig>,
    private val listener: OnItemClickListener
) : RecyclerView.Adapter<AIDictionaryConfigAdapter.DictionaryConfigViewHolder>() {

    interface OnItemClickListener {
        fun onItemClick(config: AIDictionaryConfig)
        fun onDeleteClick(config: AIDictionaryConfig)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DictionaryConfigViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_ai_dictionary_config, parent, false)
        return DictionaryConfigViewHolder(view)
    }

    override fun onBindViewHolder(holder: DictionaryConfigViewHolder, position: Int) {
        val config = dictionaryConfigList[position]
        holder.bind(config)
    }

    override fun getItemCount(): Int = dictionaryConfigList.size

    fun updateList(newList: List<AIDictionaryConfig>) {
        dictionaryConfigList = newList
        notifyDataSetChanged()
    }

    inner class DictionaryConfigViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textViewDictionaryName: TextView = itemView.findViewById(R.id.text_view_dictionary_name)
        private val buttonEdit: MaterialButton = itemView.findViewById(R.id.button_edit)
        private val buttonDelete: MaterialButton = itemView.findViewById(R.id.button_delete)

        fun bind(config: AIDictionaryConfig) {
            textViewDictionaryName.text = config.dictionaryName

            // Set click listener for the entire item (for editing)
            itemView.setOnClickListener {
                listener.onItemClick(config)
            }

            // Set click listener for edit button
            buttonEdit.setOnClickListener {
                listener.onItemClick(config)
            }

            // Set click listener for delete button
            buttonDelete.setOnClickListener {
                listener.onDeleteClick(config)
            }
        }
    }
}
