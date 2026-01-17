# Documentación Técnica: Aegis Core

Este documento detalla la arquitectura, tecnologías y módulos implementados en el proyecto **Aegis Core**.

## 1. Stack Tecnológico

El proyecto está construido sobre el ecosistema moderno de Android recomendado por Google (Modern Android Development):

*   **Lenguaje**: [Kotlin 1.9.20](https://kotlinlang.org/)
*   **UI Toolkit**: [Jetpack Compose](https://developer.android.com/jetpack/compose) (Material Design 3).
*   **Inyección de Dependencias**: [Hilt](https://dagger.dev/hilt/).
*   **Base de Datos**: [Room](https://developer.android.com/training/data-storage/room) con soporte para [SQLCipher](https://www.zetetic.net/sqlcipher/).
*   **Seguridad**:
    *   `androidx.security:security-crypto`: Para `EncryptedSharedPreferences`.
    *   `androidx.biometric`: Para autenticación con huella/rostro.
    *   `PBKDF2` + `AES-GCM`: Para encriptación manual de claves y ficheros.
*   **Inteligencia Artificial (On-Device)**:
    *   **Google ML Kit Text Recognition**: Para OCR en tickets.
    *   **Google ML Kit Barcode Scanning**: Para escáner de inventario.
*   **Cámara**: [CameraX](https://developer.android.com/training/camerax) para análisis de imágenes en tiempo real y captura.
*   **Serialización**: [Gson](https://github.com/google/gson) (para Backups JSON) y exportación CSV.
*   **Navegación**: [Navigation Compose](https://developer.android.com/guide/navigation/navigation-compose).

## 2. Arquitectura (Clean Architecture)

El proyecto sigue una arquitectura limpia dividida en capas:

### Estructura de Paquetes (`com.antigravity.aegis`)

*   **`data/`**: Capa de Datos.
    *   `local/`: Room DB. Entidades: `ClientEntity`, `ProjectEntity`, `QuoteEntity`, `ExpenseEntity`, `ProductEntity`, `MileageLogEntity`.
    *   `di/`: Módulos Hilt (`DatabaseModule`, `AppModule`).
*   **`domain/`**: Reglas de Negocio.
    *   `repository/`: Interface `CrmRepository`.
    *   `reports/`: `PdfGenerator` (Android PDF Document).
    *   `expenses/`: `OcrManager` (ML Kit wrapper), `ExportManager` (Zip/CSV).
    *   `inventory/`: `BarcodeAnalyzer` (CameraX ImageAnalysis).
    *   `mileage/`: Logic in ViewModel (Calculator & Export).
*   **`presentation/`**: UI (Compose).
    *   `crm/`: Project Hub y Presupuestos (`QuoteKanbanScreen`).
    *   `field_service/`: Reportes de Trabajo (`WorkReportScreen`, `SignatureCanvas`).
    *   `expenses/`: Gastos (`ExpensesScreen` con OCR).
    *   `inventory/`: Inventario (`InventoryScreen` con Scanner).
    *   `mileage/`: Kilometraje (`MileageScreen`, `MileageViewModel`).

## 3. Módulos del Sistema

### Módulo 1: Project Hub (CRM)
Gestión relacional básica de clientes y proyectos.
*   **Entidades**: `Client`, `Project`, `Task`.
*   **Funcionalidad**: Dashboard de proyectos activos, lista de tareas pendientes.

### Módulo 2: Field Service (Work Reports)
Generación de partes de trabajo firmados.
*   **Firma Digital**: Implementada con `Canvas` en Compose, capturando la ruta (Path) del dedo.
*   **PDF**: Generación nativa usando `android.graphics.pdf.PdfDocument`.
*   **Cámara**: Captura de fotos adjuntas usando `ActivityResultContracts.TakePicture`.

### Módulo 3: Presupuestos (Quotes)
Gestión visual de oportunidades de venta.
*   **Kanban**: Tablero visual con estados (Draft, Sent, Won, Lost).
*   **PDF Generator**: Reutiliza `PdfGenerator` para crear presupuestos formales.
*   **Interacción**: Drag & Drop simulado mediante menús contextuales.

### Módulo 4: Gastos (OCR)
Digitalización de tickets con IA.
*   **Smart Scan**: Captura una imagen y la procesa con **ML Kit Text Recognition**.
*   **RegEx Parsing**: Algoritmos para extraer automáticamente la Fecha (`\d{2}/\d{2}/\d{4}`) y el Importe Total (búsqueda de keyword "TOTAL").
*   **Exportación**: Genera un ZIP que contiene un CSV y las imágenes de los tickets del trimestre actual.

### Módulo 5: Inventario (Scanner)
Gestión de stock en tiempo real.
*   **Real-time Analysis**: Usa `CameraX.ImageAnalysis` para procesar frames de video sin necesidad de disparar la foto.
*   **ML Kit Barcode**: Decodifica códigos de barras (EAN-13, QR, UPC, etc.) en milisegundos.
*   **Lógica Reactiva**: 
    *   Si el producto existe -> Muestra controles rápidos (+1/-1).
    *   Si es nuevo -> Abre formulario de alta.
*   **Alertas**: Highlight visual para productos con stock bajo mínimo.

### Módulo 7: Registro de Kilometraje
Calculadora y registro de viajes deducibles.
*   **Calculadora**: Input de Odómetro Inicio/Fin con cálculo automático de distancia y coste.
*   **Configuración**: Precio por Km configurable en `UserConfig`.
*   **Exportación**: Generación de CSV anual con todos los viajes.

## 4. Modelo de Seguridad (The "Vault")

*   **Zero-Knowledge Local**: La Master Key nunca se guarda en plano.
*   **SQLCipher**: Base de datos encriptada con AES-256.
*   **Key Wrapping**: La clave maestra está protegida por el PIN del usuario y el Keystore de Android.

## 5. Sistema de Backup (.boveda)

Exportación segura de todos los datos en formato JSON comprimido y encriptado con una clave personalizada por el usuario, permitiendo la portabilidad entre dispositivos.
