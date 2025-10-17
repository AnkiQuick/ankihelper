package com.lmyby.ankiquicker.ui.plan

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import com.lmyby.ankiquicker.MyApplication
import com.lmyby.ankiquicker.R
import com.lmyby.ankiquicker.data.database.AppDatabase
import com.lmyby.ankiquicker.data.plan.OutputPlanEntity
import com.lmyby.ankiquicker.data.plan.OutputPlanPOJO
import com.lmyby.ankiquicker.data.plan.OutputPlanRepository
import com.lmyby.ankiquicker.data.plan.OutputPlanRepositoryHelper
import com.lmyby.ankiquicker.ui.plan.helper.ItemTouchHelperAdapter
import com.lmyby.ankiquicker.ui.plan.helper.ItemTouchHelperViewHolder
import com.lmyby.ankiquicker.util.DialogUtil

/**
 * Adapter for output plans list with drag-and-drop support
 * Converted to Kotlin as part of Phase 4 adapter migration
 */
class PlansAdapter(
    private val mActivity: Activity,
    private val mPlansList: MutableList<OutputPlanPOJO>
) : RecyclerView.Adapter<PlansAdapter.ViewHolder>(), ItemTouchHelperAdapter {

    private val repositoryHelper: OutputPlanRepositoryHelper

    init {
        // Initialize repository helper
        val database = AppDatabase.getInstance(mActivity.applicationContext)
        val repository = OutputPlanRepository(database.outputPlanDao())
        repositoryHelper = OutputPlanRepositoryHelper(repository, mActivity as LifecycleOwner)
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view), ItemTouchHelperViewHolder {
        val container: RelativeLayout = view.findViewById(R.id.plan_item)
        val planName: TextView = view.findViewById(R.id.plans_name)
        val dictName: TextView = view.findViewById(R.id.plans_dictionary_name)
        val layoutEdit: LinearLayout = view.findViewById(R.id.layout_edit)
        val layoutDelete: LinearLayout = view.findViewById(R.id.layout_delete)

        override fun onItemSelected() {
            container.setBackgroundColor(Color.LTGRAY)
        }

        override fun onItemClear() {
            container.setBackgroundColor(0)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_plans, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val plan = mPlansList[position]
        holder.planName.text = plan.planName
        holder.dictName.text = plan.dictionaryKey

        holder.layoutDelete.setOnClickListener {
            AlertDialog.Builder(mActivity)
                .setTitle(R.string.confirm_deletion)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(android.R.string.yes) { _, _ ->
                    val pos = holder.bindingAdapterPosition
                    val planName = mPlansList[pos].planName

                    // Delete plan using repository (fire-and-forget)
                    repositoryHelper.deletePlanFireAndForget(planName)

                    mPlansList.removeAt(pos)
                    notifyItemRemoved(pos)
                }
                .setNegativeButton(android.R.string.no, null)
                .show()
        }

        holder.layoutEdit.setOnClickListener {
            if (MyApplication.getAnkiDroid(MyApplication.getContext()).isAnkiDroidRunning) {
                val pos = holder.bindingAdapterPosition
                val planName = mPlansList[pos].planName
                val intent = Intent(mActivity, PlanEditorActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, planName)
                }
                MyApplication.getContext().startActivity(intent)
            } else {
                DialogUtil.showStartAnkiDialog(mActivity)
            }
        }
    }

    override fun onItemMove(fromPosition: Int, toPosition: Int) {
        val from = mPlansList.removeAt(fromPosition)
        mPlansList.add(toPosition, from)
        notifyItemMoved(fromPosition, toPosition)
    }

    override fun onMoveFinished() {
        // Convert POJOs to Entities
        val entities = mPlansList.map { pojo ->
            OutputPlanEntity().apply {
                planName = pojo.planName
                dictionaryKey = pojo.dictionaryKey
                outputDeckId = pojo.outputDeckId
                outputModelId = pojo.outputModelId
                fieldsMap = pojo.getFieldsMapString()
            }
        }

        // Refresh all plans using repository (fire-and-forget)
        repositoryHelper.refreshAllPlansFireAndForget(entities)
    }

    override fun getItemCount(): Int = mPlansList.size
}
