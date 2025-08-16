package com.mmjang.ankihelper.ui.ai;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.mmjang.ankihelper.R;
import com.mmjang.ankihelper.data.ai.AIDictionaryConfig;

import java.util.List;

public class AIDictionaryConfigAdapter extends RecyclerView.Adapter<AIDictionaryConfigAdapter.DictionaryConfigViewHolder> {
    private List<AIDictionaryConfig> dictionaryConfigList;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(AIDictionaryConfig config);
        void onDeleteClick(AIDictionaryConfig config);
    }

    public AIDictionaryConfigAdapter(List<AIDictionaryConfig> dictionaryConfigList, OnItemClickListener listener) {
        this.dictionaryConfigList = dictionaryConfigList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public DictionaryConfigViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_ai_dictionary_config, parent, false);
        return new DictionaryConfigViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DictionaryConfigViewHolder holder, int position) {
        AIDictionaryConfig config = dictionaryConfigList.get(position);
        holder.bind(config);
    }

    @Override
    public int getItemCount() {
        return dictionaryConfigList.size();
    }

    public void updateList(List<AIDictionaryConfig> newList) {
        this.dictionaryConfigList = newList;
        notifyDataSetChanged();
    }

    class DictionaryConfigViewHolder extends RecyclerView.ViewHolder {
        private TextView textViewDictionaryName;
        private MaterialButton buttonEdit;
        private MaterialButton buttonDelete;

        public DictionaryConfigViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewDictionaryName = itemView.findViewById(R.id.text_view_dictionary_name);
            buttonEdit = itemView.findViewById(R.id.button_edit);
            buttonDelete = itemView.findViewById(R.id.button_delete);
        }

        public void bind(AIDictionaryConfig config) {
            textViewDictionaryName.setText(config.getDictionaryName());

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