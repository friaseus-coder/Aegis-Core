# 📘 AEGIS CORE - Documentación Técnica para Programadores

> **Versión:** 2.0  
> **Última actualización:** Febrero 2026  
> **Arquitectura:** Clean Architecture + MVVM + Jetpack Compose

---

## 📋 Índice

1. [Visión General](#1-visión-general)
2. [Estructura del Proyecto](#2-estructura-del-proyecto)
3. [Capa Data](#3-capa-data)
4. [Capa Domain](#4-capa-domain)
5. [Capa Presentation](#5-capa-presentation)
6. [Módulos Funcionales](#6-módulos-funcionales)
7. [Sistema de Navegación](#7-sistema-de-navegación)
8. [Inyección de Dependencias](#8-inyección-de-dependencias)
9. [Seguridad y Cifrado](#9-seguridad-y-cifrado)
10. [Guía de Contribución](#10-guía-de-contribución)

---

## 1. Visión General

### ¿Qué es Aegis Core?

Aegis Core es una aplicación Android de gestión empresarial integral diseñada para autónomos y pequeñas empresas. Incluye:

- **CRM completo** (Clientes, Proyectos, Presupuestos)
- **Control de gastos** con OCR y categorización
- **Inventario** con escáner de códigos de barras
- **Partes de trabajo** con firma digital
- **Registro de kilometraje** con cálculo automático de costes
- **Sistema de backup** cifrado y exportable

### Stack Tecnológico

| Tecnología | Versión | Propósito |
|------------|---------|-----------|
| Kotlin | 1.9+ | Lenguaje principal |
| Jetpack Compose | 1.5+ | UI declarativa |
| Room | 2.6+ | Base de datos local |
| Hilt | 2.48+ | Inyección de dependencias |
| Coroutines/Flow | Latest | Programación asíncrona |
| Material Design 3 | Latest | Sistema de diseño |
| SQLCipher | 4.5+ | Cifrado de base de datos |

### Principios Arquitectónicos

```
┌─────────────────────────────────────────────────────────────┐
│                     PRESENTATION LAYER                       │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────────┐  │
│  │   Screens    │  │  ViewModels  │  │   UI States      │  │
│  │  (Compose)   │  │   (@Hilt)    │  │ (Sealed Classes) │  │
│  └──────────────┘  └──────────────┘  └──────────────────┘  │
├─────────────────────────────────────────────────────────────┤
│                       DOMAIN LAYER                           │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────────┐  │
│  │   UseCases   │  │   Models     │  │  Repository      │  │
│  │  (Business)  │  │   (Pure)     │  │  (Interfaces)    │  │
│  └──────────────┘  └──────────────┘  └──────────────────┘  │
├─────────────────────────────────────────────────────────────┤
│                        DATA LAYER                            │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────────┐  │
│  │   Entities   │  │     DAOs     │  │  Repository      │  │
│  │   (Room)     │  │   (Room)     │  │  (Implementations)│  │
│  └──────────────┘  └──────────────┘  └──────────────────┘  │
└─────────────────────────────────────────────────────────────┘
```

---

## 2. Estructura del Proyecto

### Árbol de Directorios Principal

```
app/src/main/java/com/antigravity/aegis/
├── AegisApplication.kt          # Punto de entrada Hilt
├── MainActivity.kt              # Activity única (Single Activity)
│
├── data/                        # CAPA DE DATOS
│   ├── datasource/              # Fuentes de datos remotas (futuro)
│   ├── di/                      # Módulos Hilt para inyección
│   ├── local/                   # Base de datos Room
│   │   ├── dao/                 # Data Access Objects
│   │   ├── entity/              # Entidades Room (refactorizadas)
│   │   ├── AegisDatabase.kt     # Configuración de la BD
│   │   └── Converters.kt        # Type Converters para Room
│   ├── model/                   # Entidades de datos (legacy)
│   ├── preferences/             # DataStore para preferencias
│   ├── repository/              # Implementaciones de repositorios
│   ├── security/                # Managers de seguridad y cifrado
│   ├── service/                 # Servicios de dominio
│   └── worker/                  # WorkManager jobs
│
├── domain/                      # CAPA DE DOMINIO
│   ├── expenses/                # Lógica de negocio de gastos
│   ├── inventory/               # Lógica de negocio de inventario
│   ├── model/                   # Modelos puros de dominio
│   ├── reports/                 # Generación de PDFs
│   ├── repository/              # Interfaces de repositorios
│   ├── services/                # Servicios de dominio
│   ├── transfer/                # Import/Export CSV
│   └── usecase/                 # Casos de uso
│
├── presentation/                # CAPA DE PRESENTACIÓN
│   ├── auth/                    # Autenticación y onboarding
│   ├── backup/                  # Pantallas de backup
│   ├── common/                  # Componentes compartidos
│   ├── components/              # Componentes UI reutilizables
│   ├── crm/                     # Módulo CRM completo
│   ├── dashboard/               # Dashboard principal
│   ├── expenses/                # Módulo de gastos
│   ├── feature/                 # Features refactorizadas
│   │   └── clients/             # Feature Clients (Clean Arch)
│   ├── inventory/               # Módulo de inventario
│   ├── mileage/                 # Módulo de kilometraje
│   ├── navigation/              # Navegación Compose
│   ├── reports/                 # Partes de trabajo
│   ├── screens/                 # Pantallas placeholder
│   ├── settings/                # Configuración
│   └── theme/                   # Tema Material 3
│
└── ui/                          # Recursos UI adicionales
    └── theme/                   # Definiciones de tema
```

---

## 3. Capa Data

### 3.1 Base de Datos (`data/local/`)

#### AegisDatabase.kt
```kotlin
// Configuración central de Room con todas las entidades
@Database(
    entities = [
        UserEntity::class,
        UserConfig::class,
        ClientEntity::class,
        ProjectEntity::class,
        TaskEntity::class,
        WorkReportEntity::class,
        QuoteEntity::class,
        BudgetLineEntity::class,
        ExpenseEntity::class,
        ProductEntity::class,
        MileageLogEntity::class,
        DocumentEntity::class,
        BudgetLogEntity::class
    ],
    version = X,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AegisDatabase : RoomDatabase()
```

#### DAOs (`data/local/dao/`)

| Archivo | Propósito | Operaciones Principales |
|---------|-----------|------------------------|
| `ClientDao.kt` | Gestión de clientes | CRUD + búsqueda |
| `CrmDao.kt` | **DAO principal monolítico** | Clientes, Proyectos, Tareas, Reportes, Presupuestos, Gastos, Productos, Kilometraje |
| `ProjectDao.kt` | Proyectos segregado | CRUD proyectos |
| `BudgetDao.kt` | Presupuestos y líneas | Líneas detalladas, logs |
| `ExpenseDao.kt` | Gastos | CRUD + filtros por fecha |
| `DocumentDao.kt` | Documentos adjuntos | Archivos por cliente |
| `UserConfigDao.kt` | Configuración usuario | Preferencias, idioma, tema |
| `UserEntityDao.kt` | Usuarios del sistema | Multi-usuario |

> **Nota:** `CrmDao.kt` es el DAO legacy monolítico. Los nuevos DAOs segregados (`ClientDao`, `ProjectDao`, etc.) se están implementando gradualmente.

### 3.2 Entidades (`data/model/` y `data/local/entity/`)

#### Entidades Principales

| Entidad | Tabla | Descripción |
|---------|-------|-------------|
| `ClientEntity` | `clients` | Clientes (Particulares/Empresas) |
| `ProjectEntity` | `projects` | Proyectos vinculados a clientes |
| `TaskEntity` | `tasks` | Tareas dentro de proyectos |
| `QuoteEntity` | `quotes` | Presupuestos |
| `BudgetLineEntity` | `budget_lines` | Líneas detalladas de presupuesto |
| `WorkReportEntity` | `work_reports` | Partes de trabajo con firma |
| `ExpenseEntity` | `expenses` | Gastos con imagen adjunta |
| `ProductEntity` | `products` | Inventario de productos |
| `MileageLogEntity` | `mileage_logs` | Registros de kilometraje |
| `DocumentEntity` | `documents` | Documentos adjuntos a clientes |
| `UserEntity` | `users` | Usuarios del sistema |
| `UserConfig` | `user_configs` | Configuración por usuario |
| `BudgetLogEntity` | `budget_logs` | Historial de cambios en presupuestos |

#### Ejemplo: ClientEntity
```kotlin
@Entity(tableName = "clients")
data class ClientEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val firstName: String,           // Nombre o Nombre Comercial
    val lastName: String = "",       // Apellidos (solo Particulares)
    val tipoCliente: String,         // "Particular" o "Empresa"
    val razonSocial: String? = null, // Solo Empresas
    val nifCif: String? = null,
    val personaContacto: String? = null,
    val phone: String? = null,
    val email: String? = null,
    val calle: String? = null,
    val numero: String? = null,
    val piso: String? = null,
    val poblacion: String? = null,
    val codigoPostal: String? = null,
    val categoria: String = "Potencial", // "Potencial" o "Activo"
    val notas: String? = null
)
```

### 3.3 Repositorios (`data/repository/`)

| Repositorio | Interfaz en Domain | Descripción |
|-------------|-------------------|-------------|
| `ClientRepositoryImpl` | `ClientRepository` | **Nuevo** - Clientes segregado |
| `CrmRepositoryImpl` | `CrmRepository` | **Legacy** - Monolítico |
| `ProjectRepositoryImpl` | `ProjectRepository` | Proyectos segregado |
| `BudgetRepositoryImpl` | `BudgetRepository` | Presupuestos |
| `ExpenseRepositoryImpl` | `ExpenseRepository` | Gastos |
| `AuthRepositoryImpl` | `AuthRepository` | Autenticación y usuarios |
| `SettingsRepositoryImpl` | `SettingsRepository` | Configuración app |
| `BackupRepositoryImpl` | `BackupRepository` | Backup JSON |
| `AttachmentRepository` | - | Gestión de archivos adjuntos |

### 3.4 Seguridad (`data/security/`)

| Archivo | Propósito |
|---------|-----------|
| `EncryptionKeyManager.kt` | Gestión de claves de cifrado en memoria |
| `BiometricPromptManager.kt` | Wrapper para autenticación biométrica |
| `BiometricCryptoManager.kt` | Cifrado vinculado a biometría |
| `KeyCryptoManager.kt` | Derivación de claves desde PIN |
| `FileCryptoManager.kt` | Cifrado de archivos adjuntos |
| `FileEncryptionManager.kt` | Wrapper para cifrado de archivos |

### 3.5 Módulos DI (`data/di/`)

| Módulo | Provee |
|--------|--------|
| `DatabaseModule.kt` | `AegisDatabase`, todos los DAOs |
| `AuthModule.kt` | `AuthRepository` |
| `CrmModule.kt` | `CrmRepository` (legacy) |
| `ClientModule.kt` | `ClientRepository` (nuevo) |
| `FinanceModule.kt` | `ExpenseRepository`, `BudgetRepository` |
| `BackupModule.kt` | `BackupRepository`, `Gson` |
| `SettingsModule.kt` | `SettingsRepository` |

---

## 4. Capa Domain

### 4.1 Modelos Puros (`domain/model/`)

```kotlin
// Ejemplo: Modelo puro de Cliente
data class Client(
    val id: Int,
    val firstName: String,
    val lastName: String,
    val tipoCliente: ClientType,
    val razonSocial: String?,
    val nifCif: String?,
    val personaContacto: String?,
    val phone: String?,
    val email: String?,
    val calle: String?,
    val numero: String?,
    val piso: String?,
    val poblacion: String?,
    val codigoPostal: String?,
    val categoria: ClientCategory,
    val notas: String?
)

enum class ClientType { PARTICULAR, EMPRESA }
enum class ClientCategory { POTENCIAL, ACTIVO }
```

### 4.2 Interfaces de Repositorio (`domain/repository/`)

```kotlin
// Ejemplo: ClientRepository (interfaz)
interface ClientRepository {
    fun getAllClients(): Flow<List<Client>>
    fun searchClients(query: String): Flow<List<Client>>
    suspend fun insertClient(client: Client): Long
    suspend fun updateClient(client: Client)
    suspend fun deleteClient(client: Client)
}
```

### 4.3 Casos de Uso (`domain/usecase/`)

| UseCase | Propósito | Dependencias |
|---------|-----------|--------------|
| `GetClientsUseCase` | Obtener lista de clientes con búsqueda | `ClientRepository` |
| `AddClientUseCase` | Añadir nuevo cliente | `ClientRepository` |
| `GetProjectRealProfitUseCase` | **Cálculo de rentabilidad real** | `CrmRepository`, `ExpenseRepository` |
| `ConvertBudgetToProjectUseCase` | Convertir presupuesto en proyecto | `BudgetRepository`, `ProjectRepository` |
| `LoseBudgetUseCase` | Marcar presupuesto como perdido | `BudgetRepository` |
| `LoginWithPinUseCase` | Autenticación con PIN | `AuthRepository` |
| `LoginWithBiometricsUseCase` | Autenticación biométrica | `AuthRepository` |
| `EnableBiometricsUseCase` | Habilitar biometría | `AuthRepository` |
| `InitSetupUseCase` | Inicialización primera vez | `AuthRepository` |
| `FinalizeSetupUseCase` | Finalizar setup inicial | `AuthRepository` |
| `CreateBackupUseCase` | Crear backup JSON | `BackupRepository` |
| `RestoreBackupUseCase` | Restaurar desde backup | `BackupRepository` |

#### Ejemplo: GetProjectRealProfitUseCase
```kotlin
// Calcula la rentabilidad real de un proyecto
// considerando:
// - Ingresos facturados
// - Gastos directos asignados
// - Prorrateo de gastos generales
// - Horas trabajadas
class GetProjectRealProfitUseCase @Inject constructor(
    private val crmRepository: CrmRepository,
    private val expenseRepository: ExpenseRepository
) {
    suspend operator fun invoke(projectId: Int): ProjectProfitability? {
        // Lógica compleja de cálculo de rentabilidad
    }
}
```

### 4.4 Servicios de Dominio (`domain/services/`, `domain/reports/`)

| Servicio | Propósito |
|----------|-----------|
| `PdfGenerator.kt` | Generación de PDFs nativos (presupuestos, partes) |
| `PdfGeneratorService.kt` | Servicio inyectable para PDFs |

### 4.5 Transferencia de Datos (`domain/transfer/`)

| Archivo | Propósito |
|---------|-----------|
| `DataTransferManager.kt` | Import/Export CSV con validación |
| `CsvHelper.kt` | Parser y generador de CSV |

---

## 5. Capa Presentation

### 5.1 Arquitectura de ViewModels

```
┌──────────────────────────────────────────────────────────┐
│                      VIEWMODEL                            │
│  ┌─────────────────┐    ┌─────────────────────────────┐  │
│  │   UI State      │    │      Event Handling         │  │
│  │ (StateFlow)     │    │   onAction(event: UiEvent)  │  │
│  └─────────────────┘    └─────────────────────────────┘  │
│           │                         │                     │
│           ▼                         ▼                     │
│  ┌─────────────────────────────────────────────────────┐ │
│  │                    USE CASES                         │ │
│  │   getClientsUseCase, addClientUseCase, etc.         │ │
│  └─────────────────────────────────────────────────────┘ │
└──────────────────────────────────────────────────────────┘
```

### 5.2 Módulo Auth (`presentation/auth/`)

| Archivo | Tipo | Descripción |
|---------|------|-------------|
| `AuthViewModel.kt` | ViewModel | Estado de autenticación, login, setup |
| `SplashScreen.kt` | Screen | Pantalla de carga inicial |
| `LoginScreen.kt` | Screen | Selección de usuario, PIN, biometría |
| `SetupScreen.kt` | Screen | Configuración inicial (seed phrase, perfil) |
| `CreateUserScreen.kt` | Screen | Creación de usuarios adicionales |
| `RecoveryScreen.kt` | Screen | Recuperación con seed phrase |
| `ChangePinScreen.kt` | Screen | Cambio de PIN |

#### Estados de Autenticación
```kotlin
sealed interface AuthState {
    object Loading : AuthState
    object NeedsSetup : AuthState
    object Unauthenticated : AuthState
    object Authenticated : AuthState
    object RequiresPinChange : AuthState
}
```

### 5.3 Módulo CRM (`presentation/crm/`)

| Archivo | Tipo | Descripción |
|---------|------|-------------|
| `CrmViewModel.kt` | ViewModel | **Monolítico** - Gestiona todo el CRM |
| `DashboardScreen.kt` | Screen | Dashboard de proyectos |
| `ClientListScreen.kt` | Screen | Lista de clientes (legacy) |
| `ClientDashboardScreen.kt` | Screen | Dashboard individual de cliente |
| `ClientDetailScreen.kt` | Screen | Detalle de cliente |
| `ClientEditScreen.kt` | Screen | Edición/Creación de cliente |
| `ProjectDetailScreen.kt` | Screen | Detalle de proyecto con rentabilidad |
| `QuoteKanbanScreen.kt` | Screen | Kanban de presupuestos |
| `QuoteKanbanViewModel.kt` | ViewModel | Estado del Kanban |
| `CreateQuoteScreen.kt` | Screen | Crear presupuesto |
| `BudgetEditorScreen.kt` | Screen | Editor de líneas de presupuesto |
| `BudgetViewModel.kt` | ViewModel | Estado del editor |
| `ArchivedProjectsScreen.kt` | Screen | Proyectos archivados |

### 5.4 Feature Clients Refactorizada (`presentation/feature/clients/`)

> **Nota:** Esta es la implementación Clean Architecture del módulo de Clientes.

| Archivo | Tipo | Descripción |
|---------|------|-------------|
| `ClientListViewModel.kt` | ViewModel | ViewModel segregado con UiState |
| `ClientListScreen.kt` | Screen | UI adaptada al nuevo ViewModel |

```kotlin
// Ejemplo: ClientListUiState
data class ClientListUiState(
    val clients: List<Client> = emptyList(),
    val isLoading: Boolean = false,
    val searchQuery: String = "",
    val filterType: ClientType? = null
)
```

### 5.5 Otros Módulos de Presentación

#### Expenses (`presentation/expenses/`)
| Archivo | Descripción |
|---------|-------------|
| `ExpensesScreen.kt` | Lista de gastos con filtros, captura foto |
| `ExpensesViewModel.kt` | Estado y operaciones de gastos |

#### Inventory (`presentation/inventory/`)
| Archivo | Descripción |
|---------|-------------|
| `InventoryScreen.kt` | Lista productos, escáner códigos barras |
| `InventoryViewModel.kt` | Estado y operaciones de inventario |

#### Mileage (`presentation/mileage/`)
| Archivo | Descripción |
|---------|-------------|
| `MileageScreen.kt` | Registro kilometraje con cálculo coste |
| `MileageViewModel.kt` | Estado y operaciones de kilometraje |

#### Reports (`presentation/reports/`)
| Archivo | Descripción |
|---------|-------------|
| `FieldServiceScreen.kt` | Lista de partes de trabajo |
| `CreateReportScreen.kt` | Crear parte con firma |
| `SignatureCanvas.kt` | Componente de firma digital |

#### Settings (`presentation/settings/`)
| Archivo | Descripción |
|---------|-------------|
| `SettingsScreen.kt` | Pantalla de configuración completa |
| `SettingsViewModel.kt` | Estado y operaciones de settings |
| `ProfileConfigScreen.kt` | Configuración de perfil de usuario |

#### Backup (`presentation/backup/`)
| Archivo | Descripción |
|---------|-------------|
| `ImportBackupScreen.kt` | Importación de backup |
| `ImportExportViewModel.kt` | Estado de import/export |

### 5.6 Componentes Reutilizables (`presentation/components/`)

| Componente | Propósito |
|------------|-----------|
| `AegisTopAppBar.kt` | TopAppBar con logo personalizable |
| `BovedaLogo.kt` | Logo de la empresa configurable |
| Otros | Diálogos, campos de formulario, etc. |

---

## 6. Módulos Funcionales

### 6.1 Gestión de Clientes

```
Flujo: ClientListScreen → ClientDashboardScreen → ClientDetailScreen
                                    ↓
                            ClientEditScreen
```

**Características:**
- Clientes Particulares y Empresas
- Búsqueda con mínimo 3 caracteres
- Filtros por tipo
- Categoría auto-calculada (Potencial/Activo)
- Import/Export CSV

### 6.2 Gestión de Proyectos

```
Flujo: DashboardScreen → ProjectDetailScreen → Tasks/Reports/Budget
              ↓
      ArchivedProjectsScreen
```

**Características:**
- Estados: Active, Closed, Archived
- Cálculo de rentabilidad real
- Vinculación con presupuestos
- Partes de trabajo con firma

### 6.3 Presupuestos (Kanban)

```
Flujo: QuoteKanbanScreen → CreateQuoteScreen → BudgetEditorScreen
              ↓
    Estados: Borrador → Enviado → Negociando → Aceptado/Perdido
```

**Características:**
- Kanban visual por estados
- Líneas detalladas (cantidad, precio, IVA)
- Generación de PDF
- Conversión a Proyecto al aceptar

### 6.4 Control de Gastos

**Características:**
- Captura de ticket con cámara
- Categorización (Material, Transporte, etc.)
- Estados (Pendiente, Aprobado, Rechazado)
- Vinculación opcional a proyectos

### 6.5 Inventario

**Características:**
- Escáner de códigos de barras
- Control de stock mínimo
- Alertas de reposición
- Import/Export CSV

### 6.6 Kilometraje

**Características:**
- Registro origen/destino
- Cálculo automático de coste por km
- Configuración de tarifa por vehículo
- Historial mensual

---

## 7. Sistema de Navegación

### 7.1 Definición de Rutas (`navigation/Screen.kt`)

```kotlin
sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Login : Screen("login")
    object CreateUser : Screen("create_user")
    object Dashboard : Screen("dashboard")
    object Projects : Screen("projects")
    object Clients : Screen("clients")
    object ClientDetail : Screen("client_detail")
    object ClientEdit : Screen("client_edit/{clientId}")
    object ProjectDetail : Screen("project_detail")
    object CreateReport : Screen("create_report/{projectId}")
    object EditBudget : Screen("edit_budget/{projectId}/{quoteId}")
    object Budgets : Screen("budgets")
    object WorkReports : Screen("work_reports")
    object Expenses : Screen("expenses")
    object Inventory : Screen("inventory")
    object Mileage : Screen("mileage")
    object Settings : Screen("settings")
    object Recovery : Screen("recovery")
    object ChangePin : Screen("change_pin")
    object ImportBackup : Screen("import_backup")
    // ... más rutas
}
```

### 7.2 Grafo de Navegación (`navigation/NavigationGraph.kt`)

```kotlin
@Composable
fun NavigationGraph(
    navController: NavHostController,
    isDarkTheme: Boolean,
    onThemeToggle: () -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        // Definición de cada composable...
    }
}
```

---

## 8. Inyección de Dependencias

### 8.1 Configuración Hilt

```kotlin
// AegisApplication.kt
@HiltAndroidApp
class AegisApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Inicialización de librerías
    }
}

// MainActivity.kt
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    // Single Activity pattern
}
```

### 8.2 Módulos Principales

#### DatabaseModule
```kotlin
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AegisDatabase
    
    @Provides fun provideClientDao(db: AegisDatabase): ClientDao
    @Provides fun provideCrmDao(db: AegisDatabase): CrmDao
    // ... más DAOs
}
```

#### ClientModule (Nuevo - Clean Architecture)
```kotlin
@Module
@InstallIn(SingletonComponent::class)
abstract class ClientModule {
    @Binds abstract fun bindClientRepository(
        impl: ClientRepositoryImpl
    ): ClientRepository
}
```

---

## 9. Seguridad y Cifrado

### 9.1 Arquitectura de Seguridad

```
┌─────────────────────────────────────────────────────────────┐
│                    CAPA DE SEGURIDAD                         │
├─────────────────────────────────────────────────────────────┤
│  ┌─────────────────┐   ┌─────────────────────────────────┐  │
│  │ EncryptionKey   │   │      BiometricPrompt            │  │
│  │ Manager         │   │      Manager                    │  │
│  │ (In-Memory Key) │   │ (Autenticación Biométrica)      │  │
│  └────────┬────────┘   └─────────────┬───────────────────┘  │
│           │                          │                       │
│           ▼                          ▼                       │
│  ┌─────────────────┐   ┌─────────────────────────────────┐  │
│  │  KeyCrypto      │   │      BiometricCrypto            │  │
│  │  Manager        │   │      Manager                    │  │
│  │ (Derivación PIN)│   │ (Cifrado con Biometría)         │  │
│  └────────┬────────┘   └─────────────────────────────────┘  │
│           │                                                  │
│           ▼                                                  │
│  ┌─────────────────────────────────────────────────────────┐│
│  │              SQLCipher (Base de Datos Cifrada)          ││
│  │              AES-256 Encryption                         ││
│  └─────────────────────────────────────────────────────────┘│
└─────────────────────────────────────────────────────────────┘
```

### 9.2 Flujo de Autenticación

1. **Primera vez:** 
   - Generar seed phrase (12 palabras)
   - Crear PIN maestro
   - Derivar clave de cifrado desde PIN
   - Opcionalmente habilitar biometría

2. **Login normal:**
   - Verificar PIN → Derivar clave → Desbloquear BD
   - O usar biometría → Recuperar clave cifrada → Desbloquear BD

3. **Recuperación:**
   - Introducir seed phrase
   - Reconstruir clave maestra
   - Permitir cambio de PIN

---

## 10. Guía de Contribución

### 10.1 Convenciones de Código

```kotlin
// Nombrado de archivos
FeatureNameScreen.kt      // Pantallas Compose
FeatureNameViewModel.kt   // ViewModels
FeatureNameEntity.kt      // Entidades Room
FeatureNameRepository.kt  // Repositorios
FeatureNameUseCase.kt     // Casos de uso

// Estados UI
data class FeatureUiState(
    val items: List<Item> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

// Eventos UI (opcional)
sealed interface FeatureUiEvent {
    data class ItemClicked(val id: Int) : FeatureUiEvent
    object RefreshRequested : FeatureUiEvent
}
```

### 10.2 Añadir una Nueva Feature

1. **Domain Layer:**
   - Crear modelo puro en `domain/model/`
   - Crear interfaz de repositorio en `domain/repository/`
   - Crear UseCase(s) en `domain/usecase/`

2. **Data Layer:**
   - Crear Entity en `data/local/entity/` o `data/model/`
   - Crear DAO en `data/local/dao/`
   - Crear RepositoryImpl en `data/repository/`
   - Crear módulo DI en `data/di/`

3. **Presentation Layer:**
   - Crear carpeta en `presentation/feature/[nombre]/`
   - Crear ViewModel con UiState
   - Crear Screen(s) Compose
   - Añadir ruta en `navigation/Screen.kt`
   - Añadir composable en `navigation/NavigationGraph.kt`

### 10.3 Testing

```
app/src/test/           # Unit tests
app/src/androidTest/    # Instrumentation tests
```

### 10.4 Build & Run

```bash
# Compilar debug
./gradlew assembleDebug

# Ejecutar tests
./gradlew test

# Limpiar y compilar
./gradlew clean assembleDebug
```

---

## Apéndice A: Inventario Completo de Archivos

### Data Layer (~50 archivos)
- 8 DAOs
- 13 Entidades
- 9 Repositorios
- 7 Módulos DI
- 6 Security Managers
- 2 Workers
- 1 DataStore

### Domain Layer (~29 archivos)
- 2+ Modelos puros
- 8 Interfaces de repositorio
- 12 UseCases
- 2 Servicios PDF
- 2 Transfer managers

### Presentation Layer (~49 archivos)
- 7 Auth screens
- 13 CRM screens/viewmodels
- 2 Dashboard files
- 2 Expenses files
- 2 Inventory files
- 2 Mileage files
- 3 Reports files
- 3 Settings files
- 2 Backup files
- 2 Navigation files
- 4 Components
- 2 Feature/Clients (refactorizado)

---

> **Documento generado automáticamente**  
> Para actualizaciones, consultar el código fuente directamente.
