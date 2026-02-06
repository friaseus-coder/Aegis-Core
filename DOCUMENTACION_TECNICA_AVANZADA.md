# Documentación Técnica Avanzada - Aegis Core

**Versión:** 1.0.0 (Debug)
**Plataforma:** Android (Min SDK 26, Target SDK 34)
**Lenguaje:** Kotlin 1.9+

---

## 1. Visión General de la Arquitectura

Aegis Core sigue una **Arquitectura Limpia (Clean Architecture)** estricta, implementada sobre el patrón **MVVM (Model-View-ViewModel)** recomendado por Google. El sistema está diseñado para ser modular, escalable y testearse fácilmente.

La aplicación se divide en tres capas principales:

1.  **Presentation (UI)**: Jetpack Compose + ViewModels.
2.  **Domain (Business Logic)**: Modelos puros, Interfaces de Repositorio y Servicios de Dominio.
3.  **Data (Persistence & Network)**: Room Database, Implementación de Repositorios, Data Sources (OCR, CSV).

---

## 2. Pila Tecnológica (Tech Stack)

*   **UI Framework**: Jetpack Compose (Material Design 3).
*   **Inyección de Dependencias**: Hilt (Dagger).
*   **Base de Datos**: Room (SQLite abstraido) con SQLCipher (cifrado AES-256).
*   **Concurrencia**: Kotlin Coroutines & Flow.
*   **Procesamiento de Imágenes**: ML Kit (Text Recognition v2) para OCR.
*   **Generación de Documentos**: Android PdfDocument API nativa.
*   **Serialización**: Kotlin Serialization / CSV (Custom Parser).
*   **Navegación**: Jetpack Navigation Compose.

---

## 3. Estructura del Proyecto (Árbol de Directorios detallado)

```
app/src/main/java/com/antigravity/aegis/
├── data/                       # CAPA DE DATOS
│   ├── datasource/             # Fuentes de datos (ej. SharedPreferencesWrapper)
│   ├── di/                     # Módulos Hilt (Auth, Crm, Database, Finance, Settings)
│   ├── local/                  # Base de Datos Room
│   │   ├── dao/                # Data Access Objects (BudgetDao, CrmDao, ExpenseDao...)
│   │   ├── Converters.kt       # TypeConverters para Room (Listas, Fechas)
│   │   └── AegisDatabase.kt    # Definición de la BBDD
│   ├── model/                  # Entidades de Datos (Entities)
│   │   ├── ClientEntity.kt
│   │   ├── ProjectEntity.kt
│   │   ├── ExpenseEntity.kt    # Incluye campos para contabilidad (merchant, category)
│   │   ├── PdfGenerator.kt     # (Ubicación legacy, ver domain services)
│   │   └── ... (Quote, Task, WorkReport, UserEntity)
│   ├── repository/             # Implementación de las interfaces de Dominio
│   │   ├── CrmRepositoryImpl.kt      # Repositorio monolítico para CRM (facade de DAOs)
│   │   ├── BudgetRepositoryImpl.kt   # Gestión específica de presupuestos
│   │   ├── ExpenseRepositoryImpl.kt  # Gestión financiera
│   │   └── ...
│   ├── preferences/            # DataStore / SharedPrefs (ThemePreference)
│   └── security/               # Criptografía y Seguridad
│       ├── BiometricPromptManager.kt # Autenticación biométrica
│       ├── FileCryptoManager.kt      # Cifrado de archivos adjuntos
│       └── SQLCipher...              # Configuración de cifrado de BBDD
│
├── domain/                     # CAPA DE DOMINIO
│   ├── expenses/               # Lógica de negocio específica de gastos
│   │   ├── OcrManager.kt       # Servicio de ML Kit para escanear tickets
│   │   └── ExportManager.kt    # (Legacy, ver DataTransfer)
│   ├── inventory/              # Lógica de inventario
│   │   └── BarcodeAnalyzer.kt  # Analizador de ImageAnalysis para códigos de barras
│   ├── repository/             # Interfaces (Contratos)
│   │   ├── CrmRepository.kt
│   │   ├── BudgetRepository.kt
│   │   └── ...
│   ├── reports/                # Generación de Reportes
│   │   └── PdfGenerator.kt     # (Interfaz/Clase de utilidad)
│   ├── services/               # Servicios puros de dominio
│   │   └── PdfGeneratorService.kt # Implementación robusta de generación PDF
│   └── transfer/               # Importación/Exportación
│       ├── DataTransferManager.kt # Orquestador de CSV (Import/Export)
│       └── CsvHelper.kt        # Parser manual de CSV
│
├── presentation/               # CAPA DE PRESENTACIÓN
│   ├── auth/                   # Pantallas de Autenticación (Login, Registro, Recovery)
│   ├── common/                 # Componentes UI reutilizables (Dialogs, Buttons)
│   ├── components/             # Componentes específicos (SignatureCanvas, AegisTopAppBar)
│   ├── crm/                    # Módulos principales de CRM
│   │   ├── DashboardScreen.kt  # Pantalla principal
│   │   ├── ClientList/Detail/EditScreen.kt
│   │   ├── ProjectList/DetailScreen.kt
│   │   ├── BudgetEditorScreen.kt # Editor complejo de presupuestos
│   │   ├── CrmViewModel.kt     # ViewModel "God Object" para orquestación CRM
│   │   └── BudgetViewModel.kt  # ViewModel específico para presupuestos
│   ├── expenses/               # Módulo de Gastos
│   │   ├── ExpensesScreen.kt   # Lista + Cámara + OCR trigger
│   │   └── ExpensesViewModel.kt
│   ├── navigation/             # Grafo de Navegación
│   │   └── NavigationGraph.kt  # Definición de rutas (Composable destinations)
│   ├── settings/               # Configuración
│   ├── ui/theme/               # Define colores, tipografía y formas (Material 3)
│   └── MainViewModel.kt        # Estado global de la app (Usuario activo, Tema)
│
└── AegisApplication.kt         # Entry point Hilt (@HiltAndroidApp)
```

---

## 4. Detalles de Implementación Crítica

### 4.1 Base de Datos & Relaciones (Room)
El sistema utiliza una base de datos relacional local.
- **Entidades Clave**: `Client`, `Project` (con estado `Active`/`Closed`/`Archived`), `Quote` (con control de versiones y cálculo total), `BudgetLine` (partidas de presupuesto), `Task`, `Expense`.
- **Integridad Referencial**: Se utilizan claves foráneas (`ForeignKey`) con `onDelete = CASCADE`.
- **Acceso a Datos**: Se utilizan `Flow` para exposición reactiva y `suspend functions` para operaciones I/O.

### 4.2 Lógica de Presupuestos (BudgetViewModel & Kanban)
El editor de presupuestos y el flujo Kanban han sido renovados.
1.  **Entidad `BudgetLineEntity`**: Permite un desglose detallado (Concepto, Cantidad, Precio, Impuestos) en lugar de un monto único.
2.  **Cálculo Automático**: El total del presupuesto se calcula dinámicamente (`sum(qty * price) + taxes`).
3.  **Conversión a Proyecto**: Al aceptar un presupuesto, se dispara `ConvertBudgetToProjectUseCase` que crea el proyecto y convierte las líneas del presupuesto en tareas pendientes automáticamente.
4.  **Generación PDF**: `PdfService` utiliza la API nativa `android.graphics.pdf` para generar documentos profesionales sin dependencias externas pesadas.

### 4.3 Gestión de Rentabilidad (Profit Algorithm)
El núcleo financiero reside en `GetProjectRealProfitUseCase`.
- **Ingresos**: Suma de presupuestos aceptados.
- **Gastos Directos**: Gastos asignados explícitamente al proyecto.
- **Gastos Generales (Imputados)**: Algoritmo de reparto proporcional.
    - *Fórmula*: `(GastoGeneral / ProyectosActivosEnEseMes)`.
    - Distribuye el coste de estructura (seguros, alquileres, etc.) entre los proyectos activos durante el periodo del gasto, ofreciendo una visión realista del **Beneficio Neto**.

### 4.4 Automatización & CRM
- **WorkManager**: `BudgetFollowUpWorker` se ejecuta diariamente para identificar presupuestos enviados hace más de 14 días sin respuesta, notificando al usuario.
- **Proyectos Archivados**: Sistema para "congelar" proyectos finalizados (`isArchived`), manteniéndolos fuera de la vista principal pero conservando sus datos financieros para históricos.

### 4.5 Gestión de Gastos & OCR
1.  **Captura**: `ExpensesScreen` con `ActivityResultContracts` para cámara.
2.  **Procesamiento**: ML Kit para OCR y Regex para extracción de datos.
3.  **Persistencia**: Validación manual antes de guardar en `ExpenseEntity`.

### 4.6 Data Transfer (Import/Export)
- **Formato**: CSV estándar validado por `DataTransferManager`.
- **Transaccionalidad**: Operaciones atómicas para importación segura.

### 4.7 Inyección de Dependencias (DI)
- **Hilt**: Gestión del grafo de dependencias para Repositorios, Casos de Uso y Servicios (`PdfService`, `TransferManager`).

---

## 5. Notas para el Desarrollador

1.  **Compilación**: Si encuentras errores de KSP, ejecuta `./gradlew clean`.
2.  **UI**: Nuevas pantallas `ArchivedProjectsScreen` y `BudgetEditorScreen` (dinámico).
3.  **Permisos**: Cámara, Biometría y Notificaciones (Post Android 13).

---
*Documento actualizado por Antigravity AI Agent (Refactorización Core - Rentabilidad & PDF).*
