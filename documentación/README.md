# 📚 Documentación de Aegis Core

> **Aegis Core** - Sistema de gestión empresarial seguro y 100% offline

## Índice de Documentos

### 🎯 Visión y Arquitectura
| Documento | Descripción |
|-----------|-------------|
| [Visión del Producto](vision.md) | Misión, modelo de negocio y ecosistema modular |
| [Arquitectura y Seguridad](arquitectura.md) | Stack tecnológico, multi-usuario y modelo de seguridad "Bóveda" |

### 📖 Manuales de Usuario
| Documento | Descripción |
|-----------|-------------|
| [Manual General](manual_usuario.md) | Guía completa de todos los módulos |
| [CRM - Manual](modulos/crm_manual.md) | Hub de Proyectos: Clientes, Proyectos y Tareas |
| [Reportes - Manual](modulos/reportes_manual.md) | Partes de trabajo con firma digital |

### 🔧 Documentación Técnica
| Documento | Descripción |
|-----------|-------------|
| [Documentación Técnica](documentacion_tecnica.md) | Arquitectura Clean, auth multi-usuario e i18n |
| [CRM - Técnico](modulos/crm_tecnico.md) | Implementación del módulo CRM |
| [Reportes - Técnico](modulos/reportes_tecnico.md) | Implementación de Field Service |

---

## Características Principales

- **🔒 Seguridad**: Base de datos encriptada con AES-256 (SQLCipher)
- **👥 Multi-Usuario**: Sistema de usuarios con roles (Admin, User, Guest)
- **🌐 Multiidioma**: Soporte para Español e Inglés con cambio dinámico
- **📴 100% Offline**: Funcionalidad completa sin conexión a internet
- **🧩 Modular**: Compra solo los módulos que necesitas
- **📱 Nativo Android**: Jetpack Compose + Material Design 3

---

## Flujo de la Aplicación

```
┌─────────────────────────────────────────────────────────────┐
│                     FLUJO DE AEGIS CORE                      │
├─────────────────────────────────────────────────────────────┤
│                                                              │
│   SplashScreen → LoginScreen → Dashboard → Módulos          │
│                       │                                      │
│                       ├── Si no hay usuarios → CreateUser   │
│                       └── Si hay usuarios → Seleccionar+PIN │
│                                                              │
└─────────────────────────────────────────────────────────────┘
```

---

## Módulos Disponibles

| Módulo | Estado | Descripción |
|--------|--------|-------------|
| **Hub de Proyectos** | ✅ Implementado | CRM con clientes, proyectos y tareas |
| **Partes de Trabajo** | ✅ Implementado | Firma digital y generación de PDF |
| **Presupuestos** | ✅ Implementado | Kanban de ofertas comerciales |
| **Gastos** | ✅ Implementado | OCR para digitalizar tickets |
| **Kilometraje** | ✅ Implementado | Registro de viajes con cálculo de costes |
| **Inventario** | ❌ No disponible | Módulo en desarrollo / No accesible |
| **Control Horario** | ❌ No disponible | Módulo en desarrollo / No accesible |

---

# Aegis Core v1.2.1 - Abril 2026

### Changelog v1.2.1
- ✅ **Gestión de Sesiones**: Control de intervenciones integrado en cada proyecto (Notas, Duración, Ejercicios).
- ✅ **Sincronización Calendar**: Agenda automática de próximas citas en Google Calendar.
- ✅ **Informe de Cliente**: Generación de PDF integral con historial de proyectos y sesiones.
- ✅ **Cierre de Proyectos**: Nuevo estado "Proyecto Cerrado" con restricción inteligente de costes.
- ✅ **UX CRM**: Filtrado selectivo de proyectos raíz en la vista de cliente.

### Changelog v1.2.0
- ✅ Alineación de documentación con funcionalidades reales
- ✅ Mejora de i18n en todos los módulos operativos
- ✅ Depuración de referencias a módulos residuales (Bóveda)
- ✅ Consolidación de CRM y Partes de Trabajo

### Changelog v1.1.0
- ✅ Sistema multi-usuario implementado
- ✅ Soporte para Español e Inglés
- ✅ Cambio dinámico de idioma en pantalla de login
- ✅ Flujo de autenticación mejorado
- ✅ Documentación técnica actualizada

---

*© 2026 Antigravity - Todos los derechos reservados*
