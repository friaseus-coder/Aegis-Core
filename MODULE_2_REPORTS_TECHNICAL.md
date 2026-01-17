# Módulo 2: Partes de Trabajo (Field Service) - Documentación Técnica

## Descripción General
Este módulo permite a los operarios crear "Partes de Trabajo" in situ, incluyendo la firma digital del cliente y fotografías del trabajo realizado. Genera automáticamente un documento PDF como entregable.

## Arquitectura y Componentes

### 1. Capa de Datos (Data Layer)
*   **Base de Datos**:
    *   `AegisDatabase`: Actualizada a versión 3.
    *   **Entidades**:
        *   `WorkReportEntity`: Vinculado a `ProjectEntity`. Almacena la descripción, timestamp (`date`), ruta del archivo de firma (`signaturePath`) y rutas de fotos (`photoPaths`).
    *   **DAO / Repositorio**:
        *   Métodos añadidos para insertar y recuperar reportes por ID de proyecto.

### 2. Funcionalidades Clave (Domain/Utils)
*   **Captura de Firma (`SignatureCanvas`)**:
    *   Implementado como una `AndroidView` personalizada embebida en Compose.
    *   Captura eventos de toque (`MotionEvent`) para dibujar trazos (`Path`) en un `Canvas`.
    *   Exporta el resultado como un `Bitmap` para su almacenamiento.
*   **Generación de PDF (`PdfGenerator`)**:
    *   Utiliza la API nativa `android.graphics.pdf.PdfDocument`.
    *   Maqueta el documento dibujando texto y mapas de bits (logo, firma) en un Canvas de tamaño A4.
    *   Guarda el archivo resultante en el almacenamiento interno de la app (`context.filesDir/reports/`).
*   **Cámara**:
    *   Utiliza `ActivityResultContracts.TakePicturePreview` (para prototipado rápido) o `TakePicture` (con `FileProvider`) para capturar imágenes y recibir un Bitmap/Uri.

### 3. Capa de Presentación (UI)
*   **`CreateReportScreen`**:
    *   Formulario con campo de texto para descripción.
    *   Botón para lanzar la cámara.
    *   Área de firma interactiva.
    *   Botón "Finish & Sign" que orquesta el guardado en BD y la generación del PDF.
*   **Integración**:
    *   Accesible desde `ProjectDetailScreen`.

## Flujo de Datos
1.  Usuario abre `CreateReportScreen`.
2.  Usuario dibuja firma -> `SignatureView` mantiene el Path.
3.  Usuario pulsa Guardar -> `CrmViewModel` recibe Bitmap de la firma.
4.  `CrmViewModel` -> `CrmRepository` (Guarda Entity) -> `PdfGenerator` (Crea PDF en disco).

## Dependencias Nuevas
*   Permiso de Cámara (declarado en Manifest).
