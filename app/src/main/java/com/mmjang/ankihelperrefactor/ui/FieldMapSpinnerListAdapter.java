package com.mmjang.ankihelperrefactor.ui;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.mmjang.ankihelperrefactor.R;
import com.mmjang.ankihelperrefactor.app.FieldsMapItem;
import com.mmjang.ankihelperrefactor.app.MyApplication;

import java.util.List;

/**
 * Created by liao on 2017/4/28.
 */

public class FieldMapSpinnerListAdapter
        extends RecyclerView.Adapter<FieldMapSpinnerListAdapter.ViewHolder>{
    private List<FieldsMapItem> mFieldsMapItemList;

    static class ViewHolder extends RecyclerView.ViewHolder{
        TextView exportElementName;
        Spinner fieldsSpinner;
        public ViewHolder(View view){
            super(view);
            exportElementName = (TextView) view.findViewById(R.id.tv_export_element);
            fieldsSpinner = (Spinner) view.findViewById(R.id.spinner_fields);
        }
    }

    public FieldMapSpinnerListAdapter(List<FieldsMapItem> itemList){
        mFieldsMapItemList = itemList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.field_map_item, parent, false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position){

        final FieldsMapItem item = mFieldsMapItemList.get(position);
        holder.exportElementName.setText(item.getField());
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
                MyApplication.getContext(),
                android.R.layout.simple_spinner_dropdown_item,
                item.getExportedElementNames()
        );
        holder.fieldsSpinner.setAdapter(arrayAdapter);
        holder.fieldsSpinner.setSelection(item.getSelectedFieldPos());
        holder.fieldsSpinner.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        item.setSelectedFieldPos(position);
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                }
        );
    }

    @Override
    public int getItemCount(){
        return mFieldsMapItemList.size();
    }
}
