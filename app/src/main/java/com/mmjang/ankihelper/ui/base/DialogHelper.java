package com.mmjang.ankihelper.ui.base;

import android.content.Context;
import android.content.DialogInterface;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.mmjang.ankihelper.R;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import androidx.appcompat.app.AlertDialog;

/**
 * Helper class for creating consistent dialogs throughout the app
 */
public class DialogHelper {

    /**
     * Show confirmation dialog with standard buttons
     */
    public static void showConfirmDialog(Context context, String title, String message, 
                                        DialogInterface.OnClickListener onConfirm) {
        showConfirmDialog(context, title, message, onConfirm, null);
    }

    /**
     * Show confirmation dialog with custom positive/negative text
     */
    public static void showConfirmDialog(Context context, String title, String message, 
                                        DialogInterface.OnClickListener onConfirm, 
                                        DialogInterface.OnClickListener onCancel) {
        new MaterialAlertDialogBuilder(context)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(R.string.confirm, onConfirm)
                .setNegativeButton(R.string.cancel, onCancel)
                .setCancelable(true)
                .show();
    }

    /**
     * Show input dialog with a single text field
     */
    public static void showInputDialog(Context context, String title, String hint, 
                                       String defaultValue, 
                                       DialogInterface.OnClickListener onConfirm) {
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_input, null);
        EditText input = view.findViewById(R.id.edit_text_input);
        
        if (!TextUtils.isEmpty(hint)) {
            input.setHint(hint);
        }
        
        if (!TextUtils.isEmpty(defaultValue)) {
            input.setText(defaultValue);
            input.setSelection(defaultValue.length());
        }

        new AlertDialog.Builder(context)
                .setTitle(title)
                .setView(view)
                .setPositiveButton(R.string.confirm, (dialog, which) -> {
                    String value = input.getText().toString().trim();
                    if (onConfirm != null) {
                        // Pass the input value through the dialog
                        onConfirm.onClick(dialog, which);
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .setCancelable(true)
                .show();
    }

    /**
     * Show selection dialog with a list of options
     */
    public static void showSelectionDialog(Context context, String title, 
                                          CharSequence[] items, 
                                          DialogInterface.OnClickListener onItemSelected) {
        showSelectionDialog(context, title, items, -1, onItemSelected);
    }

    /**
     * Show selection dialog with pre-selected item
     */
    public static void showSelectionDialog(Context context, String title, 
                                          CharSequence[] items, int selectedIndex, 
                                          DialogInterface.OnClickListener onItemSelected) {
        new MaterialAlertDialogBuilder(context)
                .setTitle(title)
                .setSingleChoiceItems(items, selectedIndex, null)
                .setPositiveButton(R.string.confirm, (dialog, which) -> {
                    int selectedPosition = ((AlertDialog) dialog).getListView().getCheckedItemPosition();
                    if (onItemSelected != null) {
                        onItemSelected.onClick(dialog, selectedPosition);
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .setCancelable(true)
                .show();
    }

    /**
     * Show multi-selection dialog
     */
    public static void showMultiSelectionDialog(Context context, String title, 
                                               CharSequence[] items, boolean[] selectedItems, 
                                               DialogInterface.OnClickListener onConfirm) {
        new MaterialAlertDialogBuilder(context)
                .setTitle(title)
                .setMultiChoiceItems(items, selectedItems, null)
                .setPositiveButton(R.string.confirm, onConfirm)
                .setNegativeButton(R.string.cancel, null)
                .setCancelable(true)
                .show();
    }

    /**
     * Show error dialog
     */
    public static void showErrorDialog(Context context, String title, String message) {
        new MaterialAlertDialogBuilder(context)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(R.string.ok, null)
                .setCancelable(true)
                .show();
    }

    /**
     * Show warning dialog
     */
    public static void showWarningDialog(Context context, String title, String message) {
        new MaterialAlertDialogBuilder(context)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(R.string.ok, null)
                .setCancelable(true)
                .show();
    }

    /**
     * Show info dialog
     */
    public static void showInfoDialog(Context context, String title, String message) {
        new MaterialAlertDialogBuilder(context)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(R.string.ok, null)
                .setCancelable(true)
                .show();
    }

    /**
     * Show loading dialog
     */
    public static AlertDialog showLoadingDialog(Context context, String message) {
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_loading, null);
        
        AlertDialog dialog = new MaterialAlertDialogBuilder(context)
                .setView(view)
                .setCancelable(false)
                .create();
        
        dialog.show();
        return dialog;
    }

    /**
     * Show delete confirmation dialog
     */
    public static void showDeleteConfirmDialog(Context context, String itemName, 
                                               DialogInterface.OnClickListener onDelete) {
        String message = context.getString(R.string.delete_confirm_message, itemName);
        new MaterialAlertDialogBuilder(context)
                .setTitle(R.string.delete)
                .setMessage(message)
                .setPositiveButton(R.string.delete, onDelete)
                .setNegativeButton(R.string.cancel, null)
                .setCancelable(true)
                .show();
    }

    /**
     * Show simple toast message
     */
    public static void showToast(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    /**
     * Show long toast message
     */
    public static void showLongToast(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }
}