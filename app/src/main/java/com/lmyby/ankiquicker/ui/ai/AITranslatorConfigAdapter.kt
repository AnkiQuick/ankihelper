package com.lmyby.ankiquicker.ui.ai

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.lmyby.ankiquicker.R
import com.lmyby.ankiquicker.data.ai.AITranslatorConfig

/**
 * Adapter for AI translator configuration list
 * Converted to Kotlin as part of Phase 4 adapter migration
 */
class AITranslatorConfigAdapter(
    private var translatorConfigList: List<AITranslatorConfig>,
    private val listener: OnItemClickListener
) : RecyclerView.Adapter<AITranslatorConfigAdapter.TranslatorConfigViewHolder>() {

    interface OnItemClickListener {
        fun onItemClick(config: AITranslatorConfig)
        fun onDeleteClick(config: AITranslatorConfig)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TranslatorConfigViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_ai_translator_config, parent, false)
        return TranslatorConfigViewHolder(view)
    }

    override fun onBindViewHolder(holder: TranslatorConfigViewHolder, position: Int) {
        val config = translatorConfigList[position]
        holder.bind(config)
    }

    override fun getItemCount(): Int = translatorConfigList.size

    fun updateList(newList: List<AITranslatorConfig>) {
        translatorConfigList = newList
        notifyDataSetChanged()
    }

    inner class TranslatorConfigViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textViewTranslatorName: TextView = itemView.findViewById(R.id.text_view_translator_name)
        private val textViewDefault: TextView = itemView.findViewById(R.id.text_view_default)
        private val buttonEdit: MaterialButton = itemView.findViewById(R.id.button_edit)
        private val buttonDelete: MaterialButton = itemView.findViewById(R.id.button_delete)

        fun bind(config: AITranslatorConfig) {
            textViewTranslatorName.text = config.translatorName
            textViewDefault.text = if (config.isDefault) "Default" else ""

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
