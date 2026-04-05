# 📋 Módulo CRM - Manual de Usuario

> Hub de Proyectos: Gestión de Clientes, Proyectos y Tareas

---

## Introducción

El Hub de Proyectos le permite organizar su trabajo gestionando **clientes**, **proyectos** y **listas de tareas** de forma segura y sencilla.

---

## Guía de Uso

### 1. Acceso al Dashboard

Una vez que inicia sesión con su PIN o Biometría, accederá al **Dashboard**:

| Sección | Descripción |
|---------|-------------|
| **Active Projects** | Proyectos en curso para acceso rápido |
| **Manage Clients** | Acceso a la agenda de clientes |

---

### 2. Gestionar Clientes

#### Crear un nuevo Cliente

```
Dashboard → Manage Clients → [+] → Rellenar datos → Add
```

| Campo | Obligatorio |
|-------|-------------|
| Nombre | ✅ Sí |
| Email | ❌ No |
| Teléfono | ❌ No |
| Notas | ❌ No |

#### Ver detalles
Pulse sobre cualquier cliente para ver su información y proyectos asociados.

---

### 3. Gestionar Proyectos

#### Crear un nuevo Proyecto

```
Cliente → [+] → Nombre del proyecto → Guardar
```

**Ejemplo**: "Instalación Eléctrica Sala B"

El proyecto aparecerá en:
- Lista del cliente
- Dashboard (si está Activo)

#### Estados del Proyecto
| Estado | Significado |
|--------|-------------|
| **Activo** | Trabajo en curso |
| **Pausado** | Trabajo detenido temporalmente |
| **Completado** | Trabajo finalizado |
| **Proyecto Cerrado** | Facturación finalizada, no admite más costes generales |

> [!TIP]
> **Cierre de Proyecto**: Solo puede cerrar un proyecto si está en estado **Ganado** (desde Presupuestos) o **Completado**. Una vez cerrado, el sistema le avisará si intenta imputarle nuevos costes.

---

### 4. Gestionar Tareas

#### Añadir Tareas
1. Entre en el detalle de un Proyecto
2. Pulse el botón **+**
3. Escriba la descripción de la tarea

#### Completar Tareas
- ☑️ Marque el checkbox para completar
- ☐ Desmarque para volver a pendiente

---

### 5. Sesiones de Seguimiento (Novedad v1.2.1)

Las sesiones permiten registrar intervenciones específicas con el cliente (reuniones, coaching, mentorías, instalaciones).

#### Registrar una Sesión
1. Entre en el **Detalle del Proyecto**.
2. Deslice hasta la sección **Sesiones**.
3. Pulse **Añadir Sesión** y complete:
   - **Lugar y Duración**: (Ej: "Oficina Cliente", "1h 30m").
   - **Notas**: Resumen de lo tratado durante la sesión.
   - **Ejercicios**: Puntos clave o tareas para el cliente.
   - **Próxima Sesión**: Fecha de la siguiente cita.

#### Sincronización con Calendario
Si asigna una fecha de **Próxima Sesión**, Aegis Core creará automáticamente un evento en su **Google Calendar** con toda la información relevante.

---

### 6. Informe Integral de Cliente

Desde la ficha del **Cliente**, puede pulsar el icono de PDF (📄) para generar un informe que consolida:
- Resumen de todos los proyectos del cliente.
- Historial detallado de todas las sesiones realizadas.
- Notas acumuladas y ejercicios pendientes.

## Flujo de Navegación

```
Dashboard
    │
    ├──→ Active Projects ──→ Project Detail ──→ Tasks
    │
    └──→ Manage Clients ──→ Client Detail ──→ Projects
```

---

*Manual CRM v1.2.1 - Aegis Core - Abril 2026*
