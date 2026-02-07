Regla de Compilación: Siempre que generes una APK o un Bundle de Android, muévelos automáticamente a la carpeta /apks/. Si la carpeta no existe, créala. No dejes los archivos en la carpeta de build por defecto de Gradle/Flutter. El fichero tiene que tener el nombre de la versión, por ejemplo: app-release-v1.0.0.apk

# **🛡️ Protocolo de Desarrollo de Nuevas Features (i18n Strict Mode)**

**Instrucción Maestra:**

Cada vez que generes código para una nueva funcionalidad, pantalla o componente en **Aegis Core**, debes adherirte estrictamente al **Protocolo de Internacionalización (i18n)**. Está **prohibido** entregar código con cadenas de texto literales (hardcoded strings) en la capa de presentación.

## **1\. Reglas de Implementación de Textos**

### **A. Capa UI (Jetpack Compose)**

* **Regla:** Nunca escribas "Guardar" o "Save".  
* **Implementación:** Debes crear la clave en XML y usar stringResource(R.string.feature\_key).  
* **Excepción:** Solo se permiten literales para datos de prueba (previewData) dentro de funciones @Preview.

### **B. Capa ViewModel**

* **Regla:** Los ViewModels **nunca** deben importar android.content.Context ni usar R.string directamente para formatear texto.  
* **Implementación:** Usa la clase sellada UiText para emitir mensajes a la UI.  
  // Correcto  
  \_uiState.update { it.copy(error \= UiText.StringResource(R.string.auth\_login\_error)) }  
  // Incorrecto  
  \_uiState.update { it.copy(error \= "Error al iniciar sesión") }

### **C. Archivos de Recursos (XML)**

Cada vez que introduzcas un nuevo texto, **debes generar el código XML para DOS archivos simultáneamente**:

1. res/values/strings.xml (Inglés \- Default)  
2. res/values-es/strings.xml (Español)

## **2\. Convención de Nombres (Naming Convention)**

Usa snake\_case y prefijos semánticos. No uses nombres genéricos como title o button.

**Estructura:** \[modulo\]\_\[pantalla\]\_\[elemento\]\_\[descripcion\]

**Ejemplos:**

* ❌ save\_button (Muy genérico)  
* ✅ crm\_client\_edit\_btn\_save  
* ✅ inventory\_dashboard\_label\_stock\_count  
* ✅ settings\_profile\_error\_invalid\_email

## **3\. Flujo de Trabajo Requerido**

Cuando te solicite una nueva funcionalidad (ej: "Crea la pantalla de Detalles del Proveedor"), tu respuesta debe seguir este orden:

1. **Paso 1: Definición de Strings:** Lista primero el bloque XML con las nuevas claves que necesitarás, tanto en Inglés como en Español.  
2. **Paso 2: Implementación UI/ViewModel:** Genera el código Kotlin utilizando esas claves desde el primer momento.

**Nota para la IA:** Si ves que estoy pidiendo algo rápido, asume que este protocolo sigue activo. La calidad del código depende de que la aplicación sea 100% traducible desde el día 1\.