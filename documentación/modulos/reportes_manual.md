# 📋 Módulo Reportes - Manual de Usuario

> Partes de Trabajo: Documentación con firma digital

---

## Introducción

La herramienta de Partes de Trabajo permite documentar intervenciones en campo, recoger la **firma digital** del cliente y generar documentación automática en PDF.

---

## Guía de Uso

### 1. Iniciar un Parte de Trabajo

Los partes siempre están asociados a un **proyecto existente**.

```
Dashboard → Proyecto → "Create Work Report"
```

---

### 2. Rellenar el Parte

| Campo | Descripción |
|-------|-------------|
| **Descripción** | Trabajo realizado (ej: "Sustitución de cableado") |
| **📷 Attach Photo** | Abrir cámara para foto adjunta |

---

### 3. Firma del Cliente

En la parte inferior encontrará un recuadro blanco:

- ✍️ Pida al cliente que firme con el dedo
- 🔄 **"Clear Signature"** para borrar y reintentar

---

### 4. Finalizar y Guardar

Al pulsar **"Finish & Sign"**:

| Acción | Resultado |
|--------|-----------|
| Guardar registro | Base de datos segura |
| Generar PDF | Documento con datos + firma |
| Navegación | Vuelve al proyecto |

---

### 5. Consultar Partes Anteriores

En la pantalla del proyecto, sección **"Work Reports"**:
- Historial de intervenciones
- Fecha y número de identificación

---

### 6. Informe Integral de Cliente (Nuevo v1.2.1)

A diferencia de los partes de trabajo, que son para una intervención única, el **Informe Integral** consolida toda la relación con el cliente.

#### Cómo generarlo:
1. Vaya al **Dashboard del Cliente**.
2. Pulse el botón de **PDF** (📄) en la barra superior.
3. El sistema generará un documento con:
   - Resumen de estado de **todos los proyectos**.
   - Historial de todas las **sesiones de seguimiento**.
   - Notas y ejercicios acumulados.

> [!TIP]
> Use este informe para reuniones de cierre de mes o revisiones de objetivos con su cliente.

---

## Flujo Visual

```
┌─────────────────────────────────────────────┐
│                NUEVO PARTE                   │
├─────────────────────────────────────────────┤
│  📝 Descripción del trabajo                 │
│  ─────────────────────────────              │
│                                             │
│  📷 [Adjuntar Foto]                         │
│                                             │
├─────────────────────────────────────────────┤
│  ✍️  FIRMA DEL CLIENTE                      │
│  ┌─────────────────────────────────────┐    │
│  │                                     │    │
│  │         [Área de firma]             │    │
│  │                                     │    │
│  └─────────────────────────────────────┘    │
│           [Clear Signature]                 │
├─────────────────────────────────────────────┤
│         [🔒 Finish & Sign]                  │
└─────────────────────────────────────────────┘
```

---

*Manual Reportes v1.2.1 - Aegis Core - Abril 2026*
