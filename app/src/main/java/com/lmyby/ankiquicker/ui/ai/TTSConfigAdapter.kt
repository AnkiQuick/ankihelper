package com.lmyby.ankiquicker.ui.ai

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.lmyby.ankiquicker.R
import com.lmyby.ankiquicker.data.ai.TTSConfig

/**
 * Adapter for TTS configuration list
 * Converted to Kotlin as part of Phase 4 adapter migration
 */
class TTSConfigAdapter(
    private var ttsConfigList: List<TTSConfig>,
    private val listener: OnItemClickListener
) : RecyclerView.Adapter<TTSConfigAdapter.TTSConfigViewHolder>() {

    interface OnItemClickListener {
        fun onItemClick(config: TTSConfig)
        fun onDeleteClick(config: TTSConfig)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TTSConfigViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_tts_config, parent, false)
        return TTSConfigViewHolder(view)
    }

    override fun onBindViewHolder(holder: TTSConfigViewHolder, position: Int) {
        val config = ttsConfigList[position]
        holder.bind(config)
    }

    override fun getItemCount(): Int = ttsConfigList.size

    fun updateList(newList: List<TTSConfig>) {
        ttsConfigList = newList
        notifyDataSetChanged()
    }

    inner class TTSConfigViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textViewName: TextView = itemView.findViewById(R.id.text_view_name)
        private val buttonEdit: MaterialButton = itemView.findViewById(R.id.button_edit)
        private val buttonDelete: MaterialButton = itemView.findViewById(R.id.button_delete)

        fun bind(config: TTSConfig) {
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
