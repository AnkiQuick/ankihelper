package com.lmyby.ankiquicker.data.plan

import androidx.room.*

/**
 * DAO for OutputPlanEntity
 * Provides all CRUD operations for plans using suspend functions for async operations
 */
@Dao
interface OutputPlanDao {

    /**
     * Get all plans
     * @return List of all output plans
     */
    @Query("SELECT * FROM plan")
    suspend fun getAllPlans(): List<OutputPlanEntity>

    /**
     * Get a plan by name
     * @param planName The name of the plan
     * @return The plan or null if not found
     */
    @Query("SELECT * FROM plan WHERE planname = :planName")
    suspend fun getPlanByName(planName: String): OutputPlanEntity?

    /**
     * Insert a new plan
     * @param plan The plan to insert
     * @return The row ID of the inserted plan
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlan(plan: OutputPlanEntity): Long

    /**
     * Insert multiple plans
     * @param plans The plans to insert
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlans(plans: List<OutputPlanEntity>)

    /**
     * Update an existing plan
     * @param plan The plan to update
     * @return Number of rows updated
     */
    @Update
    suspend fun updatePlan(plan: OutputPlanEntity): Int

    /**
     * Delete a plan
     * @param plan The plan to delete
     * @return Number of rows deleted
     */
    @Delete
    suspend fun deletePlan(plan: OutputPlanEntity): Int

    /**
     * Delete a plan by name
     * @param planName The name of the plan to delete
     * @return Number of rows deleted
     */
    @Query("DELETE FROM plan WHERE planname = :planName")
    suspend fun deletePlanByName(planName: String): Int

    /**
     * Delete all plans
     * @return Number of rows deleted
     */
    @Query("DELETE FROM plan")
    suspend fun deleteAllPlans(): Int

    /**
     * Check if a plan exists
     * @param planName The name of the plan
     * @return True if plan exists, false otherwise
     */
    @Query("SELECT EXISTS(SELECT 1 FROM plan WHERE planname = :planName)")
    suspend fun planExists(planName: String): Boolean

    /**
     * Get count of all plans
     * @return Number of plans
     */
    @Query("SELECT COUNT(*) FROM plan")
    suspend fun getPlanCount(): Int

    /**
     * Get all plan names
     * @return List of plan names
     */
    @Query("SELECT planname FROM plan")
    suspend fun getAllPlanNames(): List<String>

    /**
     * Refresh a plan with new data (transaction-based update)
     * This method combines delete and insert to ensure clean replacement
     */
    @Transaction
    suspend fun refreshPlanWithTransaction(planName: String, newPlan: OutputPlanEntity) {
        deletePlanByName(planName)
        insertPlan(newPlan)
    }

    /**
     * Refresh all plans with a new list (transaction-based replacement)
     * This method deletes all existing plans and inserts the new list atomically
     * Useful for reordering plans or bulk updates
     */
    @Transaction
    suspend fun refreshAllPlansWithTransaction(newPlans: List<OutputPlanEntity>) {
        deleteAllPlans()
        insertPlans(newPlans)
    }
}
