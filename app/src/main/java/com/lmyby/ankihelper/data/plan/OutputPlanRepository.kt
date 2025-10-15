package com.lmyby.ankihelper.data.plan

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Repository for OutputPlan operations
 *
 * Provides an abstraction layer between the UI and the data layer,
 * handling all plan-related database operations with proper coroutine context.
 */
class OutputPlanRepository(private val outputPlanDao: OutputPlanDao) {

    /**
     * Get all plans from the database
     * @return List of all OutputPlanEntity objects, sorted by plan name
     */
    suspend fun getAllPlans(): List<OutputPlanEntity> = withContext(Dispatchers.IO) {
        outputPlanDao.getAllPlans()
    }

    /**
     * Get a specific plan by its name
     * @param planName The name of the plan to retrieve
     * @return The OutputPlanEntity if found, null otherwise
     */
    suspend fun getPlanByName(planName: String): OutputPlanEntity? = withContext(Dispatchers.IO) {
        outputPlanDao.getPlanByName(planName)
    }

    /**
     * Check if a plan exists with the given name
     * @param planName The name to check
     * @return true if the plan exists, false otherwise
     */
    suspend fun planExists(planName: String): Boolean = withContext(Dispatchers.IO) {
        outputPlanDao.getPlanByName(planName) != null
    }

    /**
     * Insert or update a plan
     * Uses REPLACE conflict strategy, so it will update if the plan already exists
     * @param plan The OutputPlanEntity to insert/update
     * @return The row ID of the inserted/updated plan
     */
    suspend fun savePlan(plan: OutputPlanEntity): Long = withContext(Dispatchers.IO) {
        outputPlanDao.insertPlan(plan)
    }

    /**
     * Update an existing plan
     * @param plan The OutputPlanEntity with updated data
     * @return The number of rows updated (should be 1 if successful)
     */
    suspend fun updatePlan(plan: OutputPlanEntity): Int = withContext(Dispatchers.IO) {
        outputPlanDao.updatePlan(plan)
    }

    /**
     * Delete a plan by name
     * @param planName The name of the plan to delete
     * @return The number of rows deleted (should be 1 if successful)
     */
    suspend fun deletePlan(planName: String): Int = withContext(Dispatchers.IO) {
        outputPlanDao.deletePlanByName(planName)
    }

    /**
     * Delete all plans from the database
     * @return The number of rows deleted
     */
    suspend fun deleteAllPlans(): Int = withContext(Dispatchers.IO) {
        outputPlanDao.deleteAllPlans()
    }

    /**
     * Refresh a plan with new data in a transaction
     * Deletes the old plan and inserts the new one atomically
     * @param oldPlanName The name of the plan to replace
     * @param newPlan The new plan data
     */
    suspend fun refreshPlan(oldPlanName: String, newPlan: OutputPlanEntity) = withContext(Dispatchers.IO) {
        outputPlanDao.refreshPlanWithTransaction(oldPlanName, newPlan)
    }

    /**
     * Get the count of all plans
     * @return The number of plans in the database
     */
    suspend fun getPlanCount(): Int = withContext(Dispatchers.IO) {
        outputPlanDao.getPlanCount()
    }

    /**
     * Get all plan names
     * Useful for displaying a list of plan names without loading full plan data
     * @return List of all plan names
     */
    suspend fun getAllPlanNames(): List<String> = withContext(Dispatchers.IO) {
        outputPlanDao.getAllPlanNames()
    }

    /**
     * Refresh all plans with a new list in a transaction
     * Deletes all existing plans and inserts the new list atomically
     * Useful for reordering plans after drag-and-drop operations
     * @param newPlans The complete new list of plans
     */
    suspend fun refreshAllPlans(newPlans: List<OutputPlanEntity>) = withContext(Dispatchers.IO) {
        outputPlanDao.refreshAllPlansWithTransaction(newPlans)
    }
}
