package com.mmjang.ankihelper.ui.ai;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.mmjang.ankihelper.R;
import com.mmjang.ankihelper.data.ai.LLMConfig;

import java.util.List;

public class LLMConfigAdapter extends RecyclerView.Adapter<LLMConfigAdapter.LLMConfigViewHolder> {
    private List<LLMConfig> llmConfigList;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(LLMConfig config);
        void onDeleteClick(LLMConfig config);
    }

    public LLMConfigAdapter(List<LLMConfig> llmConfigList, OnItemClickListener listener) {
        this.llmConfigList = llmConfigList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public LLMConfigViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_llm_config, parent, false);
        return new LLMConfigViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LLMConfigViewHolder holder, int position) {
        LLMConfig config = llmConfigList.get(position);
        holder.bind(config);
    }

    @Override
    public int getItemCount() {
        return llmConfigList.size();
    }

    public void updateList(List<LLMConfig> newList) {
        this.llmConfigList = newList;
        notifyDataSetChanged();
    }

    class LLMConfigViewHolder extends RecyclerView.ViewHolder {
        private TextView textViewName;
        private MaterialButton buttonEdit;
        private MaterialButton buttonDelete;

        public LLMConfigViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewName = itemView.findViewById(R.id.text_view_name);
            buttonEdit = itemView.findViewById(R.id.button_edit);
            buttonDelete = itemView.findViewById(R.id.button_delete);
        }

        public void bind(LLMConfig config) {
            textViewName.setText(config.getName());

            // Set click listener for the entire item (for editing)
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        listener.onItemClick(config);
                    }
                }
            });

            // Set click listener for edit button
            buttonEdit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        listener.onItemClick(config);
                    }
                }
            });

            // Set click listener for delete button
            buttonDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        listener.onDeleteClick(config);
                    }
                }
            });
        }
    }
}