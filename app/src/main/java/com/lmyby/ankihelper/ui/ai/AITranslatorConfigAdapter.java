package com.lmyby.ankihelper.ui.ai;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.lmyby.ankihelper.R;
import com.lmyby.ankihelper.data.ai.AITranslatorConfig;

import java.util.List;

public class AITranslatorConfigAdapter extends RecyclerView.Adapter<AITranslatorConfigAdapter.TranslatorConfigViewHolder> {
    private List<AITranslatorConfig> translatorConfigList;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(AITranslatorConfig config);
        void onDeleteClick(AITranslatorConfig config);
    }

    public AITranslatorConfigAdapter(List<AITranslatorConfig> translatorConfigList, OnItemClickListener listener) {
        this.translatorConfigList = translatorConfigList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public TranslatorConfigViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_ai_translator_config, parent, false);
        return new TranslatorConfigViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TranslatorConfigViewHolder holder, int position) {
        AITranslatorConfig config = translatorConfigList.get(position);
        holder.bind(config);
    }

    @Override
    public int getItemCount() {
        return translatorConfigList.size();
    }

    public void updateList(List<AITranslatorConfig> newList) {
        this.translatorConfigList = newList;
        notifyDataSetChanged();
    }

    class TranslatorConfigViewHolder extends RecyclerView.ViewHolder {
        private TextView textViewTranslatorName;
        private TextView textViewDefault;
        private MaterialButton buttonEdit;
        private MaterialButton buttonDelete;

        public TranslatorConfigViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewTranslatorName = itemView.findViewById(R.id.text_view_translator_name);
            textViewDefault = itemView.findViewById(R.id.text_view_default);
            buttonEdit = itemView.findViewById(R.id.button_edit);
            buttonDelete = itemView.findViewById(R.id.button_delete);
        }

        public void bind(AITranslatorConfig config) {
            textViewTranslatorName.setText(config.getTranslatorName());
            textViewDefault.setText(config.isDefault() ? "Default" : "");

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