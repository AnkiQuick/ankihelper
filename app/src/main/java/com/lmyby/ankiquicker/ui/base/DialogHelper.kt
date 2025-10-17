package com.lmyby.ankiquicker.ui.base

import android.content.Context
import android.content.DialogInterface
import android.text.TextUtils
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.lmyby.ankiquicker.R

/**
 * Helper class for creating consistent dialogs throughout the app
 * Converted to Kotlin as part of Phase 11 utility migration
 */
object DialogHelper {

    /**
     * Show confirmation dialog with standard buttons
     */
    @JvmStatic
    fun showConfirmDialog(
        context: Context,
        title: String,
        message: String,
        onConfirm: DialogInterface.OnClickListener
    ) {
        showConfirmDialog(context, title, message, onConfirm, null)
    }

    /**
     * Show confirmation dialog with custom positive/negative text
     */
    @JvmStatic
    fun showConfirmDialog(
        context: Context,
        title: String,
        message: String,
        onConfirm: DialogInterface.OnClickListener,
        onCancel: DialogInterface.OnClickListener?
    ) {
        MaterialAlertDialogBuilder(context)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(R.string.confirm, onConfirm)
            .setNegativeButton(R.string.cancel, onCancel)
            .setCancelable(true)
            .show()
    }

    /**
     * Show input dialog with a single text field
     */
    @JvmStatic
    fun showInputDialog(
        context: Context,
        title: String,
        hint: String?,
        defaultValue: String?,
        onConfirm: DialogInterface.OnClickListener
    ) {
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_input, null)
        val input = view.findViewById<EditText>(R.id.edit_text_input)

        if (!TextUtils.isEmpty(hint)) {
            input.hint = hint
        }

        if (!TextUtils.isEmpty(defaultValue)) {
            input.setText(defaultValue)
            input.setSelection(defaultValue!!.length)
        }

        AlertDialog.Builder(context)
            .setTitle(title)
            .setView(view)
            .setPositiveButton(R.string.confirm) { dialog, which ->
                val value = input.text.toString().trim()
                onConfirm.onClick(dialog, which)
            }
            .setNegativeButton(R.string.cancel, null)
            .setCancelable(true)
            .show()
    }

    /**
     * Show selection dialog with a list of options
     */
    @JvmStatic
    fun showSelectionDialog(
        context: Context,
        title: String,
        items: Array<CharSequence>,
        onItemSelected: DialogInterface.OnClickListener
    ) {
        showSelectionDialog(context, title, items, -1, onItemSelected)
    }

    /**
     * Show selection dialog with pre-selected item
     */
    @JvmStatic
    fun showSelectionDialog(
        context: Context,
        title: String,
        items: Array<CharSequence>,
        selectedIndex: Int,
        onItemSelected: DialogInterface.OnClickListener
    ) {
        MaterialAlertDialogBuilder(context)
            .setTitle(title)
            .setSingleChoiceItems(items, selectedIndex, null)
            .setPositiveButton(R.string.confirm) { dialog, _ ->
                val selectedPosition = (dialog as AlertDialog).listView.checkedItemPosition
                onItemSelected.onClick(dialog, selectedPosition)
            }
            .setNegativeButton(R.string.cancel, null)
            .setCancelable(true)
            .show()
    }

    /**
     * Show multi-selection dialog
     */
    @JvmStatic
    fun showMultiSelectionDialog(
        context: Context,
        title: String,
        items: Array<CharSequence>,
        selectedItems: BooleanArray,
        onConfirm: DialogInterface.OnClickListener
    ) {
        MaterialAlertDialogBuilder(context)
            .setTitle(title)
            .setMultiChoiceItems(items, selectedItems, null)
            .setPositiveButton(R.string.confirm, onConfirm)
            .setNegativeButton(R.string.cancel, null)
            .setCancelable(true)
            .show()
    }

    /**
     * Show error dialog
     */
    @JvmStatic
    fun showErrorDialog(context: Context, title: String, message: String) {
        MaterialAlertDialogBuilder(context)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(R.string.ok, null)
            .setCancelable(true)
            .show()
    }

    /**
     * Show warning dialog
     */
    @JvmStatic
    fun showWarningDialog(context: Context, title: String, message: String) {
        MaterialAlertDialogBuilder(context)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(R.string.ok, null)
            .setCancelable(true)
            .show()
    }

    /**
     * Show info dialog
     */
    @JvmStatic
    fun showInfoDialog(context: Context, title: String, message: String) {
        MaterialAlertDialogBuilder(context)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(R.string.ok, null)
            .setCancelable(true)
            .show()
    }

    /**
     * Show loading dialog
     */
    @JvmStatic
    fun showLoadingDialog(context: Context, message: String): AlertDialog {
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_loading, null)

        val dialog = MaterialAlertDialogBuilder(context)
            .setView(view)
            .setCancelable(false)
            .create()

        dialog.show()
        return dialog
    }

    /**
     * Show delete confirmation dialog
     */
    @JvmStatic
    fun showDeleteConfirmDialog(
        context: Context,
        itemName: String,
        onDelete: DialogInterface.OnClickListener
    ) {
        val message = context.getString(R.string.delete_confirm_message, itemName)
        MaterialAlertDialogBuilder(context)
            .setTitle(R.string.delete)
            .setMessage(message)
            .setPositiveButton(R.string.delete, onDelete)
            .setNegativeButton(R.string.cancel, null)
            .setCancelable(true)
            .show()
    }

    /**
     * Show simple toast message
     */
    @JvmStatic
    fun showToast(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    /**
     * Show long toast message
     */
    @JvmStatic
    fun showLongToast(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }
}
