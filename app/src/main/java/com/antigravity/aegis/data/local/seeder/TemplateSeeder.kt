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

class TemplateSeeder @Inject constructor(
    private val projectDao: ProjectDao,
    private val taskDao: TaskDao,
    private val budgetDao: BudgetDao
) {

    suspend fun seedTemplates() = withContext(Dispatchers.IO) {
        // --- SECCIÓN IV: PINTURA ---
        createTemplate(
            category = "Pintura",
            name = "61. Pintura Plástica en Vivienda (Estándar)",
            description = "Pintado de paredes y techos con pintura al agua de alta calidad. Complejidad: Baja.",
            tasks = listOf(
                "Protección de mobiliario y suelos",
                "Emplastecido de grietas leves",
                "Lijado",
                "Aplicación de dos manos de pintura"
            ),
            durationDays = 4,
            materials = listOf("Pintura plástica mate/satinada", "Cinta de carrocero", "Plástico protector", "Plaste de masilla")
        )

        createTemplate(
            category = "Pintura",
            name = "62. Esmaltado de Puertas y Marcos",
            description = "Renovación estética de carpintería interior de madera sin cambiarla. Complejidad: Media.",
            tasks = listOf(
                "Lijado profundo",
                "Aplicación de imprimación (selladora)",
                "Aplicación de dos manos de esmalte laca"
            ),
            durationDays = 3,
            materials = listOf("Esmalte de poliuretano", "Imprimación", "Lijas de grano fino", "Rodillos de espuma de poro 0")
        )

        createTemplate(
            category = "Pintura",
            name = "63. Instalación de Papel Pintado",
            description = "Decoración de paredes mediante papel vinílico o tejido no tejido. Complejidad: Media.",
            tasks = listOf(
                "Preparación de la superficie",
                "Replanteo de dibujos (case)",
                "Encolado de pared/papel",
                "Colocación"
            ),
            durationDays = 1,
            materials = listOf("Papel pintado", "Cola específica", "Cepillo de empapelar", "Cúter de precisión")
        )

        createTemplate(
            category = "Pintura",
            name = "64. Pintura de Fachadas (Exteriores)",
            description = "Protección y embellecimiento de paramentos exteriores. Complejidad: Alta.",
            tasks = listOf(
                "Limpieza con agua a presión",
                "Reparación de fisuras con mortero elástico",
                "Aplicación de fijador",
                "Pintura de exteriores (revestimiento)"
            ),
            durationDays = 8,
            materials = listOf("Revestimiento acrílico o de silicato", "Masilla elástica exterior", "Fijador acrílico")
        )

        createTemplate(
            category = "Pintura",
            name = "65. Pintura Epoxi para Suelos de Garaje",
            description = "Revestimiento de alta resistencia para suelos de hormigón. Complejidad: Media-Alta.",
            tasks = listOf(
                "Limpieza mecánica del soporte",
                "Aplicación de imprimación epoxi",
                "Aplicación de dos capas de pintura autonivelante"
            ),
            durationDays = 4,
            materials = listOf("Resina epoxi bicomponente", "Disolvente específico", "Rodillos de nylon")
        )
        
        createTemplate(
            category = "Pintura",
            name = "66. Efecto Decorativo: Estuco Veneciano",
            description = "Acabado de lujo con aspecto marmóreo y brillo natural. Complejidad: Muy Alta.",
            tasks = listOf(
                "Preparación de base ultra-lisa",
                "Aplicación de varias capas finas con llana",
                "Abrillantado final con cera"
            ),
            durationDays = 4,
            materials = listOf("Estuco a la cal", "Espátulas de acero inoxidable", "Cera de protección")
        )

        // --- SECCIÓN V: CLIMATIZACIÓN ---
        createTemplate(
            category = "Climatización",
            name = "71. Instalación de Aire Acondicionado (Split)",
            description = "Montaje de unidad interior y exterior de climatización. Complejidad: Alta.",
            tasks = listOf(
                "Instalación de soportes",
                "Perforación de muro",
                "Conexión de líneas frigoríficas",
                "Desagüe y vacío de circuito"
            ),
            durationDays = 1, // 4-6 hours -> ~1 day
            materials = listOf("Equipo split", "Tubería de cobre aislada", "Soporte de pared", "Canaleta")
        )

        createTemplate(
            category = "Climatización",
            name = "72. Aire Acondicionado por Conductos",
            description = "Sistema centralizado oculto en falso techo. Complejidad: Muy Alta.",
            tasks = listOf(
                "Montaje de unidad interior en baño",
                "Embocado de conductos de fibra de vidrio",
                "Colocación de rejillas"
            ),
            durationDays = 4,
            materials = listOf("Unidad central", "Paneles de fibra de vidrio", "Rejillas de impulsión y retorno")
        )
        
        createTemplate(
            category = "Climatización",
            name = "73. Instalación de Sistema de Aerotermia",
            description = "Energía renovable para calefacción, refrigeración y agua caliente. Complejidad: Muy Alta.",
            tasks = listOf(
                "Montaje de unidad exterior",
                "Montaje de depósito de inercia/ACS interior",
                "Conexión al sistema de emisión (suelo/radiadores)"
            ),
            durationDays = 7,
            materials = listOf("Bomba de calor", "Acumulador de ACS", "Vasos de expansión", "Bombas de circulación")
        )

        createTemplate(
            category = "Climatización",
            name = "75. Sustitución de Radiadores por Fancoils",
            description = "Mejora del sistema de calefacción para que también emita frío. Complejidad: Alta.",
            tasks = listOf(
                "Desmontaje de radiadores convencionales",
                "Conexión de fancoils a la red de agua aislada"
            ),
            durationDays = 3,
            materials = listOf("Fancoil de suelo o pared", "Válvulas de 3 vías", "Aislamiento para tubería")
        )
        // --- SECCIÓN VI: MARKETING DIGITAL ---
        MarketingTemplates.data.forEach { t ->
            val durationDays = (t.durationMs ?: (7L * 24 * 3600 * 1000)) / (24 * 3600 * 1000)
            createTemplate(
                category = t.category,
                name = t.name,
                description = t.description,
                tasks = t.tasks,
                durationDays = durationDays.toInt(),
                materials = emptyList()
            )
        }

        // --- SECCIÓN VII: SISTEMAS (IT) ---
        SystemTemplates.data.forEach { t ->
             // Some system tasks are in hours (< 1 day). 
             // Logic: If durationMs < 1 day, pass 0 or 1? 
             // My createTemplate uses days and multiplies by 8h.
             // If I pass 0, duration is 0. 
             // Let's improve logic: if durationMs provided, use it directly?
             // But createTemplate signature takes 'durationDays'.
             // I should overload createTemplate or change signature.
             // For now, I'll stick to days approximation (Ceiling).
             val durationDays = Math.ceil((t.durationMs ?: (1L * 24 * 3600 * 1000)).toDouble() / (24 * 3600 * 1000)).toInt()
             
             createTemplate(
                category = t.category,
                name = t.name,
                description = t.description,
                tasks = t.tasks,
                durationDays = if (durationDays < 1) 1 else durationDays,
                materials = emptyList()
            )
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
            clientId = 0, // No client for template
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
