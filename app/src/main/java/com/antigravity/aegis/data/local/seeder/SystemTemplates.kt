package com.antigravity.aegis.data.local.seeder

object SystemTemplates {
    val data = listOf(
        // SECCIÓN I: PROGRAMACIÓN Y SISTEMAS
        TemplateData(
            name = "Desarrollo de Web Corporativa",
            description = "Creación de página web informativa. Complejidad: Baja.",
            category = "Sistemas - Programación",
            tasks = listOf(
                "Diseño de interfaz (UI/UX)",
                "Maquetación responsive (HTML/CSS)",
                "Configuración de dominio y hosting",
                "Implementación de SEO básico",
                "Formulario de contacto y mapas"
            ),
            durationMs = 10L * 24 * 3600 * 1000 // 1.5 semanas
        ),
        TemplateData(
            name = "Limpieza de Virus y Malware",
            description = "Desinfección de sitios web o equipos. Complejidad: Media.",
            category = "Sistemas - Programación",
            tasks = listOf(
                "Escaneo de amenazas y scripts maliciosos",
                "Eliminación de malware",
                "Endurecimiento de seguridad (Hardening)",
                "Reporte de limpieza"
            ),
            durationMs = 2L * 24 * 3600 * 1000 // 2 días
        ),
        
        // SECCIÓN II: SOPORTE TÉCNICO DE HARDWARE
        TemplateData(
            name = "Mantenimiento Preventivo Laptops",
            description = "Optimización de equipos portátiles. Complejidad: Baja.",
            category = "Sistemas - Soporte",
            tasks = listOf(
                "Limpieza interna de ventiladores",
                "Revisión de bisagras y carcasa",
                "Test de salud de batería",
                "Limpieza de contactos RAM"
            ),
            durationMs = 4L * 3600 * 1000 // ~4 horas (0.5 días aprox para efectos de gestión)
        ),
        TemplateData(
            name = "Reparación Hardware Móviles",
            description = "Arreglo de componentes en smartphones/tablets. Complejidad: Alta.",
            category = "Sistemas - Soporte",
            tasks = listOf(
                "Diagnóstico de fallo",
                "Apertura del dispositivo",
                "Sustitución de componente dañado (pantalla/batería)",
                "Cierre y pruebas funcionales"
            ),
            durationMs = 2L * 3600 * 1000 // ~2 horas
        ),
        
        // SECCIÓN III: REDES DE TELECOMUNICACIONES
        TemplateData(
            name = "Auditoría y Diseño WiFi (Heatmaps)",
            description = "Análisis de cobertura inalámbrica. Complejidad: Media.",
            category = "Sistemas - Redes",
            tasks = listOf(
                "Escaneo de frecuencias actual",
                "Generación de mapa de calor",
                "Diseño de ubicación de APs",
                "Informe de mejoras"
            ),
            durationMs = 3L * 24 * 3600 * 1000 // 3 días
        ),
        TemplateData(
            name = "Instalación de Fibra Óptica (FTTH)",
            description = "Despliegue de infraestructura de fibra. Complejidad: Muy Alta.",
            category = "Sistemas - Redes",
            tasks = listOf(
                "Tendido de cable de fibra",
                "Fusionado de núcleos",
                "Instalación de cajas de terminación (CTO)",
                "Certificación de enlaces con OTDR"
            ),
            durationMs = 5L * 24 * 3600 * 1000 // 5 días
        ),
        TemplateData(
            name = "Configuración de Switches (VLANs)",
            description = "Segmentación de red local. Complejidad: Media-Alta.",
            category = "Sistemas - Redes",
            tasks = listOf(
                "Diseño de esquema de VLANs",
                "Configuración de Trunking en switches",
                "Asignación de puertos",
                "Pruebas de aislamiento y conectividad"
            ),
            durationMs = 2L * 24 * 3600 * 1000 // 2 días
        ),
        TemplateData(
            name = "Implementación VPN Site-to-Site",
            description = "Conexión segura entre oficinas. Complejidad: Alta.",
            category = "Sistemas - Redes",
            tasks = listOf(
                "Configuración de túneles IPsec",
                "Intercambio de llaves (IKEv2)",
                "Configuración de enrutamiento",
                "Pruebas de estabilidad"
            ),
            durationMs = 3L * 24 * 3600 * 1000 // 3 días
        ),
        TemplateData(
            name = "Despliegue Infraestructura SD-WAN",
            description = "Gestión inteligente de conexiones. Complejidad: Muy Alta.",
            category = "Sistemas - Redes",
            tasks = listOf(
                "Instalación de appliances SD-WAN",
                "Configuración de políticas de tráfico",
                "Configuración de balanceo en la nube",
                "Validación de failover"
            ),
            durationMs = 10L * 24 * 3600 * 1000 // 1.5 semanas
        ),
        TemplateData(
            name = "Enlaces Inalámbricos Punto a Punto",
            description = "Conexión por radioenlace entre edificios. Complejidad: Alta.",
            category = "Sistemas - Redes",
            tasks = listOf(
                "Instalación de mástiles y soportes",
                "Montaje y alineación de antenas",
                "Configuración de bridge inalámbrico",
                "Test de velocidad y latencia"
            ),
            durationMs = 2L * 24 * 3600 * 1000 // 2 días
        ),
        TemplateData(
            name = "Configuración Telefonía IP (VoIP)",
            description = "Sistema de voz sobre IP. Complejidad: Media-Alta.",
            category = "Sistemas - Redes",
            tasks = listOf(
                "Instalación de servidor PBX",
                "Configuración de extensiones SIP",
                "Configuración de colas de llamadas/IVR",
                "Despliegue de teléfonos IP"
            ),
            durationMs = 4L * 24 * 3600 * 1000 // 4 días
        ),
        TemplateData(
            name = "Alta Disponibilidad Firewalls (HA)",
            description = "Clustering de firewalls. Complejidad: Muy Alta.",
            category = "Sistemas - Redes",
            tasks = listOf(
                "Conexión física de heartbeat",
                "Configuración de cluster HA",
                "Sincronización de reglas y estados",
                "Pruebas de failover (corte de energía)"
            ),
            durationMs = 3L * 24 * 3600 * 1000 // 3 días
        ),
        TemplateData(
            name = "Cableado Estructurado Voz/Datos",
            description = "Instalación física de red. Complejidad: Media.",
            category = "Sistemas - Redes",
            tasks = listOf(
                "Tirada de cable UTP Cat 6/6A",
                "Conexionado de paneles de parcheo",
                "Grimpar conectores RJ45/Rosetas",
                "Certificación de tomas (Fluke)"
            ),
            durationMs = 3L * 24 * 3600 * 1000 // Variable, estimamos 3 días para un proyecto medio
        ),
        TemplateData(
            name = "Sistemas Monitorización Red (NMS)",
            description = "Panel de control en tiempo real. Complejidad: Media-Alta.",
            category = "Sistemas - Redes",
            tasks = listOf(
                "Instalación de servidor de monitorización",
                "Configuración de agentes SNMP en equipos",
                "Diseño de mapas de topología",
                "Configuración de alertas y notificaciones"
            ),
            durationMs = 4L * 24 * 3600 * 1000 // 4 días
        ),
        TemplateData(
            name = "Configuración Balanceadores Carga",
            description = "Distribución de tráfico web. Complejidad: Alta.",
            category = "Sistemas - Redes",
            tasks = listOf(
                "Instalación de balanceador (SW/HW)",
                "Configuración de backend servers",
                "Configuración de algoritmos (Round Robin)",
                "Configuración de Health Checks"
            ),
            durationMs = 3L * 24 * 3600 * 1000 // 3 días
        ),
        TemplateData(
            name = "Segmentación Red IoT",
            description = "Aislamiento de dispositivos inteligentes. Complejidad: Media.",
            category = "Sistemas - Redes",
            tasks = listOf(
                "Creación de VLAN IoT aislada",
                "Configuración de ACLs de firewall",
                "Migración de dispositivos IoT",
                "Verificación de seguridad"
            ),
            durationMs = 2L * 24 * 3600 * 1000 // 2 días
        ),
        TemplateData(
            name = "Implementación Servidor RADIUS",
            description = "Seguridad WiFi Enterprise. Complejidad: Alta.",
            category = "Sistemas - Redes",
            tasks = listOf(
                "Instalación de servidor RADIUS/NPS",
                "Integración con Directorio Activo",
                "Configuración de APs para WPA2 Enterprise",
                "Pruebas de autenticación de usuario"
            ),
            durationMs = 3L * 24 * 3600 * 1000 // 3 días
        ),
        TemplateData(
            name = "Diagnóstico Latencia y Paquetes",
            description = "Resolución de problemas de red. Complejidad: Muy Alta.",
            category = "Sistemas - Redes",
            tasks = listOf(
                "Captura de tráfico (Sniffer/Wireshark)",
                "Análisis de trazas y colisiones",
                "Pruebas de carga (iPerf)",
                "Informe de diagnóstico y solución"
            ),
            durationMs = 2L * 24 * 3600 * 1000 // 2 días estimado
        ),
         TemplateData(
            name = "Despliegue Redes Privadas 4G/5G",
            description = "Red móvil privada industrial. Complejidad: Muy Alta.",
            category = "Sistemas - Redes",
            tasks = listOf(
                "Instalación de Small Cells / Antenas",
                "Configuración del Core de red móvil",
                "Provisionamiento de tarjetas SIM",
                "Pruebas de cobertura y velocidad"
            ),
            durationMs = 21L * 24 * 3600 * 1000 // 3 semanas
        ),
        TemplateData(
            name = "Instalación Internet Satelital",
            description = "Conexión en zonas remotas. Complejidad: Media.",
            category = "Sistemas - Redes",
            tasks = listOf(
                "Montaje de antena parabólica",
                "Alineación con satélite",
                "Configuración de router satelital",
                "Integración en red local"
            ),
            durationMs = 1L * 24 * 3600 * 1000 // 1 día
        ),
        TemplateData(
            name = "Implementación CDN",
            description = "Aceleración de contenido web. Complejidad: Media.",
            category = "Sistemas - Redes",
            tasks = listOf(
                "Configuración de zona DNS",
                "Definición de reglas de caché",
                "Configuración de seguridad (WAF/DDoS)",
                "Purga y pruebas de rendimiento"
            ),
            durationMs = 3L * 24 * 3600 * 1000 // 3 días
        ),
        TemplateData(
            name = "Interconexión Data Centers (DCI)",
            description = "Enlace alta capacidad entre CPDs. Complejidad: Muy Alta.",
            category = "Sistemas - Redes",
            tasks = listOf(
                "Configuración de equipos DWDM",
                "Configuración de enlaces de fibra oscura",
                "Configuración de replicación SAN/IP",
                "Pruebas de latencia y throughput"
            ),
            durationMs = 10L * 24 * 3600 * 1000 // 1.5 semanas
        ),
        TemplateData(
            name = "Portal Cautivo WiFi Invitados",
            description = "Acceso WiFi con registro. Complejidad: Media.",
            category = "Sistemas - Redes",
            tasks = listOf(
                "Diseño de página de aterrizaje (Splash Page)",
                "Configuración de métodos de login (Social/Form)",
                "Definición de límites de ancho de banda/tiempo",
                "Test de experiencia de usuario"
            ),
            durationMs = 2L * 24 * 3600 * 1000 // 2 días
        ),
        TemplateData(
            name = "Optimización QoS",
            description = "Priorización de tráfico crítico. Complejidad: Media-Alta.",
            category = "Sistemas - Redes",
            tasks = listOf(
                "Identificación de flujos de tráfico (Voz/Video)",
                "Marcado de paquetes (DSCP/CoS)",
                "Configuración de colas de prioridad en routers",
                "Pruebas de congestión controlada"
            ),
            durationMs = 1L * 24 * 3600 * 1000 // 1 día
        )
    )
}
