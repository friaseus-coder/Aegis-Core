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

*Manual Reportes v1.0 - Aegis Core*
