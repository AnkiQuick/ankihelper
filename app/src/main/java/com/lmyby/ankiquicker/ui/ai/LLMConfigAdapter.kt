package com.lmyby.ankiquicker.ui.ai

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.lmyby.ankiquicker.R
import com.lmyby.ankiquicker.data.ai.LLMConfig

/**
 * Adapter for LLM configuration list
 * Converted to Kotlin as part of Phase 4 adapter migration
 */
class LLMConfigAdapter(
    private var llmConfigList: List<LLMConfig>,
    private val listener: OnItemClickListener
) : RecyclerView.Adapter<LLMConfigAdapter.LLMConfigViewHolder>() {

    interface OnItemClickListener {
        fun onItemClick(config: LLMConfig)
        fun onDeleteClick(config: LLMConfig)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LLMConfigViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_llm_config, parent, false)
        return LLMConfigViewHolder(view)
    }

    override fun onBindViewHolder(holder: LLMConfigViewHolder, position: Int) {
        val config = llmConfigList[position]
        holder.bind(config)
    }

    override fun getItemCount(): Int = llmConfigList.size

    fun updateList(newList: List<LLMConfig>) {
        llmConfigList = newList
        notifyDataSetChanged()
    }

    inner class LLMConfigViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textViewName: TextView = itemView.findViewById(R.id.text_view_name)
        private val buttonEdit: MaterialButton = itemView.findViewById(R.id.button_edit)
        private val buttonDelete: MaterialButton = itemView.findViewById(R.id.button_delete)

        fun bind(config: LLMConfig) {
            textViewName.text = config.name

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
