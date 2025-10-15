package com.lmyby.ankihelper.data.plan

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.lmyby.ankihelper.data.dict.CoroutineHelper
import kotlinx.coroutines.launch

/**
 * Helper class to provide Java-friendly access to OutputPlanRepository
 *
 * This class wraps the suspend functions in the repository with callback-based
 * methods that can be easily called from Java code.
 */
class OutputPlanRepositoryHelper(
    private val repository: OutputPlanRepository,
    private val lifecycleOwner: LifecycleOwner
) {

    /**
     * Callback interface for plan operations
     */
    interface PlansCallback {
        fun onSuccess(plans: MutableList<OutputPlanEntity>)
        fun onError(error: Throwable)
    }

    /**
     * Callback interface for single plan operations
     */
    interface PlanCallback {
        fun onSuccess(plan: OutputPlanEntity?)
        fun onError(error: Throwable)
    }

    /**
     * Callback interface for save/delete operations
     */
    interface OperationCallback {
        fun onSuccess()
        fun onError(error: Throwable)
    }

    /**
     * Get all plans
     */
    fun getAllPlans(callback: PlansCallback) {
        lifecycleOwner.lifecycleScope.launch {
            try {
                val plans = repository.getAllPlans().toMutableList()
                callback.onSuccess(plans)
            } catch (e: Exception) {
                callback.onError(e)
            }
        }
    }

    /**
     * Get a plan by name
     */
    fun getPlanByName(planName: String, callback: PlanCallback) {
        lifecycleOwner.lifecycleScope.launch {
            try {
                val plan = repository.getPlanByName(planName)
                callback.onSuccess(plan)
            } catch (e: Exception) {
                callback.onError(e)
            }
        }
    }

    /**
     * Save a plan (insert or update)
     */
    fun savePlan(plan: OutputPlanEntity, callback: OperationCallback) {
        lifecycleOwner.lifecycleScope.launch {
            try {
                repository.savePlan(plan)
                callback.onSuccess()
            } catch (e: Exception) {
                callback.onError(e)
            }
        }
    }

    /**
     * Delete a plan by name
     */
    fun deletePlan(planName: String, callback: OperationCallback) {
        lifecycleOwner.lifecycleScope.launch {
            try {
                repository.deletePlan(planName)
                callback.onSuccess()
            } catch (e: Exception) {
                callback.onError(e)
            }
        }
    }

    /**
     * Delete all plans
     */
    fun deleteAllPlans(callback: OperationCallback) {
        lifecycleOwner.lifecycleScope.launch {
            try {
                repository.deleteAllPlans()
                callback.onSuccess()
            } catch (e: Exception) {
                callback.onError(e)
            }
        }
    }

    /**
     * Refresh a plan (atomic replace)
     */
    fun refreshPlan(oldPlanName: String, newPlan: OutputPlanEntity, callback: OperationCallback) {
        lifecycleOwner.lifecycleScope.launch {
            try {
                repository.refreshPlan(oldPlanName, newPlan)
                callback.onSuccess()
            } catch (e: Exception) {
                callback.onError(e)
            }
        }
    }

    /**
     * Refresh all plans with a new list (atomic replace all)
     * Useful for reordering plans after drag-and-drop
     */
    fun refreshAllPlans(newPlans: List<OutputPlanEntity>, callback: OperationCallback) {
        lifecycleOwner.lifecycleScope.launch {
            try {
                repository.refreshAllPlans(newPlans)
                callback.onSuccess()
            } catch (e: Exception) {
                callback.onError(e)
            }
        }
    }

    /**
     * Delete a plan by name (fire-and-forget)
     * No callback, errors are logged but not propagated
     */
    fun deletePlanFireAndForget(planName: String) {
        lifecycleOwner.lifecycleScope.launch {
            try {
                repository.deletePlan(planName)
            } catch (e: Exception) {
                android.util.Log.e("OutputPlanRepoHelper", "Error deleting plan", e)
            }
        }
    }

    /**
     * Refresh all plans (fire-and-forget)
     * No callback, errors are logged but not propagated
     */
    fun refreshAllPlansFireAndForget(newPlans: List<OutputPlanEntity>) {
        lifecycleOwner.lifecycleScope.launch {
            try {
                repository.refreshAllPlans(newPlans)
            } catch (e: Exception) {
                android.util.Log.e("OutputPlanRepoHelper", "Error refreshing plans", e)
            }
        }
    }

    companion object {
        /**
         * Get all plans synchronously (blocking)
         *
         * WARNING: This method blocks the calling thread and should only be used when
         * necessary (e.g., from onCreate where async loading is not feasible).
         * The blocking is done on the IO dispatcher to prevent ANR.
         *
         * @return List of all output plans
         */
        @JvmStatic
        fun getAllPlansBlocking(repository: OutputPlanRepository): List<OutputPlanEntity> {
            return CoroutineHelper.executeBlocking {
                repository.getAllPlans()
            }
        }
    }
}
