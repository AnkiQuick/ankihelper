package com.mmjang.ankihelper.ui.ai;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.mmjang.ankihelper.R;
import com.mmjang.ankihelper.data.ai.TTSConfig;

import java.util.List;

public class TTSConfigAdapter extends RecyclerView.Adapter<TTSConfigAdapter.TTSConfigViewHolder> {
    private List<TTSConfig> ttsConfigList;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(TTSConfig config);
        void onDeleteClick(TTSConfig config);
    }

    public TTSConfigAdapter(List<TTSConfig> ttsConfigList, OnItemClickListener listener) {
        this.ttsConfigList = ttsConfigList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public TTSConfigViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_tts_config, parent, false);
        return new TTSConfigViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TTSConfigViewHolder holder, int position) {
        TTSConfig config = ttsConfigList.get(position);
        holder.bind(config);
    }

    @Override
    public int getItemCount() {
        return ttsConfigList.size();
    }

    public void updateList(List<TTSConfig> newList) {
        this.ttsConfigList = newList;
        notifyDataSetChanged();
    }

    class TTSConfigViewHolder extends RecyclerView.ViewHolder {
        private TextView textViewName;
        private MaterialButton buttonEdit;
        private MaterialButton buttonDelete;

        public TTSConfigViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewName = itemView.findViewById(R.id.text_view_name);
            buttonEdit = itemView.findViewById(R.id.button_edit);
            buttonDelete = itemView.findViewById(R.id.button_delete);
        }

        public void bind(TTSConfig config) {
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