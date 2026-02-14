package com.antigravity.aegis.data.local.seeder

data class TemplateData(
    val name: String,
    val description: String,
    val category: String,
    val tasks: List<String>,
    val durationMs: Long? = null
)

object MarketingTemplates {
    val data = listOf(
        // SECCIÓN I: SEO Y CONTENIDOS
        TemplateData(
            name = "Auditoría SEO Técnica",
            description = "Análisis profundo de la salud de un sitio web para buscadores. Complejidad: Alta.",
            category = "Marketing Digital - SEO",
            tasks = listOf(
                "Revisión de indexabilidad y errores 404",
                "Análisis de velocidad de carga y Core Web Vitals",
                "Revisión de arquitectura web",
                "Informe de salud del sitio"
            ),
            durationMs = 7L * 24 * 3600 * 1000 // 1 semana
        ),
        TemplateData(
            name = "SEO Semántico y Entidades",
            description = "Optimización del contenido basada en temas y contextos. Complejidad: Alta.",
            category = "Marketing Digital - SEO",
            tasks = listOf(
                "Creación de clusters de contenido",
                "Marcado de datos estructurados avanzado",
                "Optimización de intención de búsqueda",
                "Análisis de entidades semánticas"
            ),
            durationMs = 21L * 24 * 3600 * 1000 // 3 semanas (tomando promedio de 3-4)
        ),
        TemplateData(
            name = "Optimización para Búsqueda por Voz",
            description = "Ajuste de contenidos para asistentes como Alexa, Siri. Complejidad: Media.",
            category = "Marketing Digital - SEO",
            tasks = listOf(
                "Implementación de FAQ con lenguaje natural",
                "Optimización de fragmentos destacados (snippet)",
                "Revisión de compatibilidad móvil"
            ),
            durationMs = 14L * 24 * 3600 * 1000 // 2 semanas
        ),
        TemplateData(
            name = "SEO Internacional (Hreflang)",
            description = "Configuración técnica para webs multi-idioma. Complejidad: Muy Alta.",
            category = "Marketing Digital - SEO",
            tasks = listOf(
                "Implementación de etiquetas hreflang",
                "Configuración de subdominios/subcarpetas",
                "Estrategia de SEO localizado",
                "Validación en Google Search Console"
            ),
            durationMs = 28L * 24 * 3600 * 1000 // 4 semanas
        ),
        TemplateData(
            name = "Estrategia de E-E-A-T",
            description = "Mejora de autoridad y confianza del sitio. Complejidad: Alta.",
            category = "Marketing Digital - SEO",
            tasks = listOf(
                "Creación de páginas de autor",
                "Revisión de fuentes bibliográficas",
                "Auditoría de reputación de marca",
                "Optimización de página 'Sobre Nosotros'"
            ),
            durationMs = 60L * 24 * 3600 * 1000 // 2 meses
        ),
        TemplateData(
            name = "SEO para Imágenes y Visual Search",
            description = "Optimización para Google Imágenes y Lens. Complejidad: Baja-Media.",
            category = "Marketing Digital - SEO",
            tasks = listOf(
                "Compresión avanzada de imágenes",
                "Nombrado descriptivo de archivos",
                "Implementación de textos ALT",
                "Creación de sitemap de imágenes"
            ),
            durationMs = 10L * 24 * 3600 * 1000 // 1.5 semanas
        ),
        TemplateData(
            name = "Recuperación de Penalizaciones",
            description = "Limpieza del sitio tras caídas por algoritmo o acciones manuales. Complejidad: Muy Alta.",
            category = "Marketing Digital - SEO",
            tasks = listOf(
                "Auditoría de enlaces tóxicos",
                "Desautorización (disavow) de enlaces",
                "Reescritura de contenido de baja calidad",
                "Solicitud de reconsideración (si aplica)"
            ),
            durationMs = 56L * 24 * 3600 * 1000 // 8 semanas (promedio 4-12)
        ),
        TemplateData(
            name = "SEO para News y Google Discover",
            description = "Optimización técnica para medios y blogs. Complejidad: Alta.",
            category = "Marketing Digital - SEO",
            tasks = listOf(
                "Configuración de News Sitemap",
                "Implementación de marcado NewsArticle",
                "Optimización de CTR para Discover",
                "Revisión de directrices de contenido"
            ),
            durationMs = 17L * 24 * 3600 * 1000 // 2.5 semanas
        ),
        TemplateData(
            name = "JavaScript SEO (SPA)",
            description = "Asegurar renderizado en React/Angular/Vue. Complejidad: Muy Alta.",
            category = "Marketing Digital - SEO",
            tasks = listOf(
                "Configuración de SSR o prerenderizado",
                "Revisión de renderizado en GSC",
                "Gestión de meta etiquetas dinámicas",
                "Optimización de carga de scripts"
            ),
            durationMs = 28L * 24 * 3600 * 1000 // 4 semanas
        ),
        TemplateData(
            name = "SEO para Marketplaces",
            description = "Posicionamiento en eBay, Etsy, Wallapop. Complejidad: Media.",
            category = "Marketing Digital - SEO",
            tasks = listOf(
                "Optimización de títulos internos",
                "Gestión de categorías",
                "Investigación de keywords específicas",
                "Mejora de descripciones de producto"
            ),
            durationMs = 14L * 24 * 3600 * 1000 // 2 semanas
        ),
        TemplateData(
            name = "Auditoría de Arquitectura de Información",
            description = "Reestructuración de menús y navegación. Complejidad: Alta.",
            category = "Marketing Digital - SEO",
            tasks = listOf(
                "Diseño de estructura de silos",
                "Reducción de niveles de profundidad",
                "Optimización de enlazado interno",
                "Validación de experiencia de usuario"
            ),
            durationMs = 21L * 24 * 3600 * 1000 // 3 semanas
        ),
        
        // SECCIÓN II: PUBLICIDAD PAGADA (SEM Y ADS)
        TemplateData(
            name = "Campaña en Pinterest Ads",
            description = "Publicidad visual enfocada a inspiración. Complejidad: Media.",
            category = "Marketing Digital - Ads",
            tasks = listOf(
                "Creación de Pins promocionados",
                "Configuración de catálogos",
                "Segmentación por intereses",
                "Análisis de rendimiento"
            ),
            durationMs = 30L * 24 * 3600 * 1000 // Mensual
        ),
        TemplateData(
            name = "Twitter (X) Ads",
            description = "Campañas de visibilidad en X. Complejidad: Media.",
            category = "Marketing Digital - Ads",
            tasks = listOf(
                "Configuración de campañas de seguidores",
                "Estrategia de engagement",
                "Configuración de clics al sitio web",
                "Monitorización de campaña"
            ),
            durationMs = 30L * 24 * 3600 * 1000 // Mensual
        ),
        TemplateData(
            name = "Publicidad en Spotify Audio",
            description = "Cuñas de audio para branding. Complejidad: Media-Alta.",
            category = "Marketing Digital - Ads",
            tasks = listOf(
                "Producción de guion de audio",
                "Grabación de locución",
                "Segmentación por gustos musicales",
                "Lanzamiento de campaña"
            ),
            durationMs = 30L * 24 * 3600 * 1000 // Mensual
        ),
        TemplateData(
            name = "Apple Search Ads",
            description = "Publicidad en App Store. Complejidad: Alta.",
            category = "Marketing Digital - Ads",
            tasks = listOf(
                "Selección de keywords de búsqueda",
                "Optimización de puja por descarga",
                "Diseño de creatividad",
                "Ajuste de segmentación"
            ),
            durationMs = 30L * 24 * 3600 * 1000 // Mensual
        ),
        TemplateData(
            name = "Reddit Ads",
            description = "Publicidad en Subreddits. Complejidad: Media.",
            category = "Marketing Digital - Ads",
            tasks = listOf(
                "Selección de nichos (subreddits)",
                "Redacción de copies nativos",
                "Gestión de comentarios en anuncios",
                "Análisis de métricas"
            ),
            durationMs = 30L * 24 * 3600 * 1000 // Mensual
        ),
        TemplateData(
            name = "Campañas de Generación de Leads",
            description = "Formularios nativos en FB/LinkedIn. Complejidad: Media.",
            category = "Marketing Digital - Ads",
            tasks = listOf(
                "Diseño de formulario",
                "Integración con CRM",
                "Automatización de respuesta",
                "Configuración de audiencias"
            ),
            durationMs = 14L * 24 * 3600 * 1000 // 2 semanas
        ),
        TemplateData(
            name = "Publicidad en Waze",
            description = "Atraer conductores cercanos. Complejidad: Baja.",
            category = "Marketing Digital - Ads",
            tasks = listOf(
                "Configuración de pines de ubicación",
                "Configuración de anuncios de búsqueda",
                "Definición de radio de acción",
                "Reporte de visitas"
            ),
            durationMs = 30L * 24 * 3600 * 1000 // Mensual
        ),
        TemplateData(
            name = "YouTube Bumper Ads",
            description = "Anuncios de 6s no saltables. Complejidad: Media.",
            category = "Marketing Digital - Ads",
            tasks = listOf(
                "Producción de vídeo ultra-corto (6s)",
                "Segmentación de audiencia",
                "Configuración en Google Ads",
                "Medición de Brand Lift"
            ),
            durationMs = 7L * 24 * 3600 * 1000 // Puntual -> 1 semana est.
        ),
        TemplateData(
            name = "Quora Ads",
            description = "Publicidad en preguntas y respuestas. Complejidad: Media.",
            category = "Marketing Digital - Ads",
            tasks = listOf(
                "Segmentación por temas de preguntas",
                "Redacción de respuestas patrocinadas",
                "Selección de keywords contextuales",
                "Optimización de CTR"
            ),
            durationMs = 30L * 24 * 3600 * 1000 // Mensual
        ),
        TemplateData(
            name = "Programática de Video (Outstream)",
            description = "Anuncios de vídeo fuera de reproductores. Complejidad: Muy Alta.",
            category = "Marketing Digital - Ads",
            tasks = listOf(
                "Gestión de pujas RTB",
                "Optimización de viewability",
                "Selección de inventario premium",
                "Análisis de alcance"
            ),
            durationMs = 30L * 24 * 3600 * 1000 // Mensual
        ),
        
        // SECCIÓN IV: EMAIL MARKETING
        TemplateData(
            name = "Estrategia Omnicanal Email/SMS",
            description = "Coordinación de mensajes. Complejidad: Alta.",
            category = "Marketing Digital - Email",
            tasks = listOf(
                "Configuración de flujos cruzados",
                "Implementación de lógica condicional (SMS si no abre)",
                "Diseño de plantillas de email y SMS",
                "Test de entregabilidad"
            ),
            durationMs = 25L * 24 * 3600 * 1000 // 3.5 semanas
        ),
        TemplateData(
            name = "Campaña Reactivación Clientes",
            description = "Recuperar usuarios inactivos. Complejidad: Media.",
            category = "Marketing Digital - Email",
            tasks = listOf(
                "Segmentación por inactividad",
                "Diseño de oferta 'te echamos de menos'",
                "Secuenciación de envíos",
                "Análisis de recuperación"
            ),
            durationMs = 14L * 24 * 3600 * 1000 // 2 semanas
        ),
        TemplateData(
            name = "Programa de Fidelización (Loyalty)",
            description = "Sistema de puntos y recompensas. Complejidad: Alta.",
            category = "Marketing Digital - Email",
            tasks = listOf(
                "Definición de niveles de cliente",
                "Automatización de cupones por hitos",
                "Integración con e-commerce",
                "Campaña de lanzamiento del programa"
            ),
            durationMs = 35L * 24 * 3600 * 1000 // 5 semanas
        ),
        TemplateData(
            name = "A/B Testing Avanzado Email",
            description = "Optimización de tasas de apertura. Complejidad: Media.",
            category = "Marketing Digital - Email",
            tasks = listOf(
                "Definición de variables (Asunto, Hora, Remitente)",
                "Ejecución de pruebas masivas",
                "Análisis de significancia estadística",
                "Implementación de ganadores"
            ),
            durationMs = 30L * 24 * 3600 * 1000 // Recurrente -> Mensual
        ),
        TemplateData(
            name = "Emails Interactivos (AMP)",
            description = "Interacción dentro del email. Complejidad: Muy Alta.",
            category = "Marketing Digital - Email",
            tasks = listOf(
                "Desarrollo de plantillas AMP",
                "Validación de remitente dinámico",
                "Pruebas de compatibilidad con clientes de correo",
                "Configuración de fallback HTML"
            ),
            durationMs = 17L * 24 * 3600 * 1000 // 2.5 semanas
        ),
        TemplateData(
            name = "Encuestas NPS y Feedback",
            description = "Medición de satisfacción post-compra. Complejidad: Baja.",
            category = "Marketing Digital - Email",
            tasks = listOf(
                "Diseño de encuesta NPS",
                "Automatización de envío post-venta",
                "Configuración de alertas para detractores",
                "Reporte de resultados"
            ),
            durationMs = 7L * 24 * 3600 * 1000 // 1 semana
        ),
         TemplateData(
            name = "Serie de Bienvenida Avanzada",
            description = "Flujo de presentación de marca. Complejidad: Media.",
            category = "Marketing Digital - Email",
            tasks = listOf(
                "Diseño de narrativa (storytelling)",
                "Creación de 5-10 correos secuenciales",
                "Entrega progresiva de Lead Magnets",
                "Segmentación por interés"
            ),
            durationMs = 14L * 24 * 3600 * 1000 // 2 semanas
        ),
        TemplateData(
            name = "Triggers por Cumpleaños",
            description = "Felicitación automática. Complejidad: Baja.",
            category = "Marketing Digital - Email",
            tasks = listOf(
                "Captación de fecha de nacimiento",
                "Configuración de flujo anual",
                "Diseño de regalo/descuento personalizado",
                "Prueba de envío"
            ),
            durationMs = 4L * 24 * 3600 * 1000 // 3-5 días
        ),
        TemplateData(
            name = "Email de Precios Dinámicos",
            description = "Alertas de bajada de precio. Complejidad: Muy Alta.",
            category = "Marketing Digital - Email",
            tasks = listOf(
                "Integración de catálogo de productos",
                "Configuración de disparadores por cambio de precio",
                "Diseño de plantilla dinámica",
                "Gestión de lista de deseos"
            ),
            durationMs = 25L * 24 * 3600 * 1000 // 3.5 semanas
        ),
        TemplateData(
            name = "Gestión de Patrocinios en Newsletters",
            description = "Compra de espacios en terceros. Complejidad: Media.",
            category = "Marketing Digital - Email",
            tasks = listOf(
                "Selección de newsletters de nicho",
                "Negociación de tarifas",
                "Diseño de piezas publicitarias",
                "Seguimiento de clics y conversiones"
            ),
            durationMs = 30L * 24 * 3600 * 1000 // Mensual
        ),
        
        // SECCIÓN V: ANALÍTICA
        TemplateData(
            name = "Server-Side Tagging",
            description = "Medición desde el servidor. Complejidad: Muy Alta.",
            category = "Marketing Digital - Analítica",
            tasks = listOf(
                "Configuración de contenedor de servidor (GTM)",
                "Configuración de infraestructura (GCP/Stape)",
                "Migración de etiquetas de cliente a servidor",
                "Validación de datos"
            ),
            durationMs = 28L * 24 * 3600 * 1000 // 4 semanas
        ),
        TemplateData(
            name = "Análisis BigQuery GA4",
            description = "Consultas SQL sobre datos crudos. Complejidad: Muy Alta.",
            category = "Marketing Digital - Analítica",
            tasks = listOf(
                "Conexión GA4 con BigQuery",
                "Creación de tablas de usuario",
                "Diseño de consultas SQL personalizadas",
                "Visualización en Looker Studio"
            ),
            durationMs = 28L * 24 * 3600 * 1000 // 4 semanas
        ),
        TemplateData(
            name = "Modelado de Atribución Personalizado",
            description = "Análisis de rutas de conversión. Complejidad: Alta.",
            category = "Marketing Digital - Analítica",
            tasks = listOf(
                "Análisis de modelos de atribución actuales",
                "Definición de pesos por canal",
                "Configuración en herramienta de atribución",
                "Comparativa de modelos"
            ),
            durationMs = 25L * 24 * 3600 * 1000 // 3.5 semanas
        ),
        TemplateData(
            name = "Importación de Conversiones Offline",
            description = "Conectar tienda física con online. Complejidad: Alta.",
            category = "Marketing Digital - Analítica",
            tasks = listOf(
                "Preparación de archivos de datos (hashes)",
                "Configuración de subida en Google/Meta Ads",
                "Automatización de importación",
                "Validación de coincidencias"
            ),
            durationMs = 17L * 24 * 3600 * 1000 // 2.5 semanas
        ),
        TemplateData(
            name = "User ID Tracking (Cross-device)",
            description = "Seguimiento multidispositivo. Complejidad: Alta.",
            category = "Marketing Digital - Analítica",
            tasks = listOf(
                "Implementación de ID de usuario en Data Layer",
                "Configuración en GTM y GA4",
                "Unificación de sesiones",
                "Validación de privacidad"
            ),
            durationMs = 14L * 24 * 3600 * 1000 // 2 semanas
        ),
        TemplateData(
            name = "Visualización de Embudos",
            description = "Análisis de abandono de carrito. Complejidad: Media.",
            category = "Marketing Digital - Analítica",
            tasks = listOf(
                "Configuración de pasos de checkout en GA4",
                "Creación de exploraciones de embudo",
                "Identificación de puntos de fuga",
                "Propuesta de mejoras"
            ),
            durationMs = 7L * 24 * 3600 * 1000 // 1 semana
        ),
        TemplateData(
            name = "Análisis de Cohortes",
            description = "Estudio de retención por grupos. Complejidad: Alta.",
            category = "Marketing Digital - Analítica",
            tasks = listOf(
                "Definición de cohortes (fecha, adquisición)",
                "Análisis de recurrencia a 30/60/90 días",
                "Identificación de patrones de churn",
                "Informe de ciclo de vida"
            ),
            durationMs = 14L * 24 * 3600 * 1000 // 2 semanas
        ),
         TemplateData(
            name = "Auditoría de Capa de Datos",
            description = "Revisión de integridad de datos. Complejidad: Media.",
            category = "Marketing Digital - Analítica",
            tasks = listOf(
                "Debugging de variables de Data Layer",
                "Validación de formatos (moneda, IDs)",
                "Detección de eventos duplicados",
                "Documentación de estructura de datos"
            ),
            durationMs = 7L * 24 * 3600 * 1000 // 1 semana
        ),
        TemplateData(
            name = "Analítica Predictiva (IA)",
            description = "Predicción de probabilidad de compra. Complejidad: Media-Alta.",
            category = "Marketing Digital - Analítica",
            tasks = listOf(
                "Configuración de audiencias predictivas en GA4",
                "Activación de audiencias en Google Ads",
                "Análisis de métricas predictivas",
                "Ajuste de estrategias de puja"
            ),
            durationMs = 14L * 24 * 3600 * 1000 // 2 semanas
        ),
        TemplateData(
            name = "Análisis de Mapas de Calor",
            description = "Visualización de comportamiento UI. Complejidad: Baja-Media.",
            category = "Marketing Digital - Analítica",
            tasks = listOf(
                "Instalación de herramienta de Heatmaps",
                "Configuración de grabaciones de sesión",
                "Segmentación por dispositivo/fuente",
                "Análisis de scroll y clicks"
            ),
            durationMs = 10L * 24 * 3600 * 1000 // 1.5 semanas
        )
    )
}
