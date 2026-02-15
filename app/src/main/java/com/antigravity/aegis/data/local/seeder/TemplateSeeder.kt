package com.antigravity.aegis.data.local.seeder

import com.antigravity.aegis.data.local.dao.ProjectDao
import com.antigravity.aegis.data.local.dao.TaskDao
import com.antigravity.aegis.data.local.dao.BudgetDao
import com.antigravity.aegis.data.local.entity.ProjectEntity
import com.antigravity.aegis.data.local.entity.TaskEntity
import com.antigravity.aegis.data.local.entity.BudgetLineEntity
import com.antigravity.aegis.data.local.entity.QuoteEntity
import com.antigravity.aegis.data.local.entity.ProjectStatus
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.BufferedReader
import java.io.InputStreamReader

class TemplateSeeder @Inject constructor(
    @ApplicationContext private val context: Context,
    private val projectDao: ProjectDao,
    private val taskDao: TaskDao,
    private val budgetDao: BudgetDao
) {

    suspend fun seedTemplates() = withContext(Dispatchers.IO) {
        try {
            val inputStream = context.assets.open("templates.csv")
            val reader = BufferedReader(InputStreamReader(inputStream))
            
            // Skip header
            reader.readLine()
            
            var line = reader.readLine()
            while (line != null) {
                val tokens = line.split(";")
                if (tokens.size >= 5) {
                    val category = tokens[0]
                    val name = tokens[1]
                    val description = tokens[2]
                    val tasksInfo = tokens[3]
                    val durationDaysInfo = tokens[4]
                    
                    val tasks = tasksInfo.split("|").filter { it.isNotBlank() }
                    val durationDays = durationDaysInfo.toIntOrNull() ?: 1
                    
                    createTemplate(
                        category = category,
                        name = name,
                        description = description,
                        tasks = tasks,
                        durationDays = if (durationDays < 1) 1 else durationDays,
                        materials = emptyList()
                    )
                }
                line = reader.readLine()
            }
            reader.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private suspend fun createTemplate(
        category: String,
        name: String,
        description: String,
        tasks: List<String>,
        durationDays: Int,
        materials: List<String>
    ) {
        // Check if template exists
        val existingTemplate = projectDao.getTemplateByName(name)
        
        if (existingTemplate != null) {
            // Update category if null or different? 
            // Mainly if null or if we want to enforce the category from seeder.
            if (existingTemplate.category != category) {
                 val updatedTemplate = existingTemplate.copy(category = category)
                 projectDao.updateProject(updatedTemplate)
            }
            // We could also update description etc, but let's stick to category for now to be safe against user edits.
            return
        }

        // Create Project Template
        // Duration in db is milliseconds ideally, but Task uses Long. Let's assume day = 8h work?
        // Or specific duration per task. For simplicity, we distribute evenly or just set total.
        // We'll set duration on template tasks.
        
        val template = ProjectEntity(
            name = name,
            description = description,
            clientId = null, // No client for template
            startDate = System.currentTimeMillis(),
            status = ProjectStatus.ACTIVE,
            isTemplate = true,
            category = category
        )
        
        val projectId = projectDao.insertProject(template)
        
        // Create Tasks
        val durationPerTask = if (tasks.isNotEmpty()) (durationDays * 8 * 3600 * 1000L) / tasks.size else 0L
        
        tasks.forEach { taskDesc ->
            val task = TaskEntity(
                projectId = projectId.toInt(),
                title = taskDesc,
                description = taskDesc,
                estimatedDuration = durationPerTask,
                status = "Pending"
            )
            taskDao.insertTask(task)
        }
        
        // Create Budget (Materials)
        if (materials.isNotEmpty()) {
            val quote = QuoteEntity(
                projectId = projectId.toInt(),
                clientId = 0, // No client
                title = "Materiales (Plantilla)",
                totalAmount = 0.0,
                status = "Draft", // String based on entity definition
                date = System.currentTimeMillis(),
                description = "Materiales predefinidos para la plantilla $name"
            )
            val quoteId = budgetDao.insertQuote(quote)
            
            materials.forEach { material ->
                val line = BudgetLineEntity(
                    quoteId = quoteId.toInt(),
                    description = material,
                    quantity = 1.0,
                    unitPrice = 0.0,
                    taxRate = 0.21
                )
                budgetDao.insertBudgetLine(line)
            }
        }
    }
}
