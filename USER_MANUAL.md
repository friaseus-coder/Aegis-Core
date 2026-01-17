# Manual de Usuario - Aegis Core

Bienvenido a **Aegis Core**, su sistema integral de gestión empresarial segura. Este manual le guiará a través de sus cinco módulos principales.

## Tabla de Contenidos
1.  [Inicio y Seguridad](#1-inicio-y-seguridad)
2.  [Módulo 1: Project Hub (CRM)](#2-módulo-1-project-hub-crm)
3.  [Módulo 2: Field Service (Reportes)](#3-módulo-2-field-service-reportes)
4.  [Módulo 3: Presupuestos (Kanban)](#4-módulo-3-presupuestos-kanban)
5.  [Módulo 4: Gastos y Tickets (OCR)](#5-módulo-4-gastos-y-tickets-ocr)
6.  [Módulo 5: Inventario (Scanner)](#6-módulo-5-inventario-scanner)
7.  [Módulo 7: Registro de Kilometraje](#7-módulo-7-registro-de-kilometraje)

---

## 1. Inicio y Seguridad

### Primer Acceso
Al abrir la aplicación por primera vez, deberá configurar su seguridad:
1.  Cree un **PIN Maestro** de 6 dígitos.
2.  (Opcional) Active el acceso biométrico (Huella/Cara).
3.  **IMPORTANTE**: Anote su "Frase de Recuperación" (12 palabras). Es la única forma de recuperar sus datos si olvida el PIN.

### Copias de Seguridad
Puede exportar toda su base de datos desde el menú de Ajustes -> Backup. Se generará un archivo `.boveda` que puede guardar en Google Drive o enviarse por email.

---

## 2. Módulo 1: Project Hub (CRM)

Gestione sus clientes y proyectos activos.

*   **Dashboard**: Vista rápida de proyectos en curso.
*   **Crear**: Use el botón **+** para añadir un nuevo Cliente o Proyecto.
*   **Tareas**: Dentro de cada proyecto, puede añadir tareas y marcarlas como "Completadas" para seguir el progreso del trabajo.

---

## 3. Módulo 2: Field Service (Reportes)

Herramienta diseñada para técnicos en campo.

1.  **Nuevo Reporte**: Seleccione un proyecto.
2.  **Detalles**: Describa el trabajo realizado.
3.  **Fotos**: Toque el icono de cámara para adjuntar evidencias del antes/después.
4.  **Firma**: Pida al cliente que firme directamente en la pantalla usando el dedo.
5.  **Enviar**: Al guardar, toque el botón "Compartir PDF" para enviar el parte firmado por WhatsApp o Email al cliente.

---

## 4. Módulo 3: Presupuestos (Kanban)

Gestione sus oportunidades de venta de forma visual.

*   **Vista de Tablero**: Sus presupuestos se organizan en columnas: *Borrador*, *Enviado*, *Ganado*, *Perdido*.
*   **Crear**: Toque el botón **+** para crear un nuevo presupuesto para un cliente.
*   **Mover**: Use el menú (tres puntos) en cada tarjeta para moverla de estado (ej. de "Borrador" a "Enviado").
*   **Compartir**: Desde el mismo menú, seleccione "Compartir PDF" para generar y enviar el presupuesto formal.

---

## 5. Módulo 4: Gastos y Tickets (OCR)

Digitalice sus gastos y olvídese de los papeles.

1.  **Escanear**: Toque el botón de **Cámara** en la esquina inferior.
2.  **Captura**: Tome una foto clara del ticket o factura.
3.  **Revisión Inteligente**: La app intentará leer automáticamente la **Fecha** y el **Total**. Verifique los datos y añada el nombre del comercio.
4.  **Exportar Trimestre**: Al final del trimestre, pulse el botón "Exportar Trimestre". Se creará un archivo ZIP con un Excel resumen y todas las fotos de los tickets, listo para enviar a su gestoría.

---

## 6. Módulo 5: Inventario (Scanner)

Control de stock en tiempo real usando códigos de barras.

*   **Pestaña Scan**:
    *   Apunte con la cámara a cualquier código de barras.
    *   **Producto Nuevo**: Si no existe, rellene el formulario (Nombre, Precio) para darlo de alta.
    *   **Producto Existente**: Si ya existe, aparecerán botones grandes **+1** / **-1** para ajustar el stock rápidamente.
*   **Pestaña Lista**:
    *   Vea todo su inventario.
    *   **Alerta de Stock**: Los productos con pocas unidades aparecerán resaltados en **ROJO** con la etiqueta "LOW STOCK".

---

## 7. Módulo 7: Registro de Kilometraje

Lleve un control de sus viajes y costes deducibles.

1.  **Configuración**: Pulse el icono de engranaje para establecer su **precio por kilómetro** (ej. 0.19€).
2.  **Nuevo Viaje**:
    *   Introduzca Origen y Destino.
    *   Introduzca los kilómetros del odómetro al inicio y al final.
    *   La app calculará la distancia y el coste automáticamente.
3.  **Historial**: Verá una lista de todos sus viajes debajo de la calculadora.
4.  **Exportar**: Pulse "Export Annual CSV" para descargar un informe completo en formato Excel/CSV.
