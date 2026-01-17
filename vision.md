1. Resumen de la Visión del Producto (El "Qué")
Este documento resume la visión comercial del ecosistema de aplicaciones "Bóveda Modular" que hemos diseñado.
A. Misión y Modelo de Negocio
●	Misión: Devolver a los autónomos y pequeñas empresas el control de sus datos con un ecosistema de aplicaciones 100% offline, rápido, privado y de pago único.
●	Modelo de Negocio: Pago Único por Módulo ("Bóveda Modular"). Los usuarios compran solo las apps que necesitan. No hay suscripciones para la funcionalidad offline.
●	Argumento de Venta: "El Ecosistema de Negocios 100% Offline y Totalmente Encriptado".
B. Arquitectura Central (La "Bóveda")
●	Offline ("Local-First"): Toda la funcionalidad principal funciona sin conexión a internet.
●	Seguridad No Negociable: Todos los datos de todas las apps se guardan en una única base de datos "Bóveda" encriptada (AES-256) en el dispositivo del usuario. La seguridad no es un "extra", es la base.
●	Acceso: La Bóveda está protegida por un PIN Maestro o Huella Dactilar.
●	"Seguro de Vida": La Bóveda incluye un sistema robusto de Backup/Restauración (un fichero .boveda encriptado con contraseña + un Kit de Recuperación) y un recordatorio para animar al usuario a guardar sus datos en un lugar seguro (ej. Google Drive).
C. El Ecosistema de 6 Módulos (Las Apps)
El ecosistema se compone de 6 aplicaciones modulares que comparten la misma Bóveda encriptada:
1.	App 1: Hub de Proyectos (El Cerebro):
○	La app central. Gestiona Proyectos, Tareas y Clientes.
○	Sirve como el "panel de control" que unifica y enlaza los datos de los otros módulos.
2.	App 2: Informes Pro (La Original):
○	Un potente motor para crear checklists e informes.
○	Función Clave: Un Editor de Formularios que permite al usuario crear sus propias plantillas (para una canguro o un vendedor de pisos).
○	Incluye captura de firmas y anotación de fotos.
3.	App 3: Presupuestos (El CRM-Lite):
○	Para crear y gestionar presupuestos (no facturas).
○	Función Clave: Un CRM de seguimiento con una Vista Kanban visual para arrastrar presupuestos ("Oportunidad" -> "Enviado" -> "Aceptado").
○	Usa "Intents" de Android para interactuar con el Calendario y el Email sin pedir contraseñas.
4.	App 4: Gastos y Tickets (El Contable):
○	Para registrar gastos de negocio y adjuntar tickets.
○	Función Clave: OCR (ML Kit) que escanea la foto del ticket y rellena automáticamente el importe y la fecha.
○	Exporta un .zip para el contable (con un Excel y todas las fotos de los tickets).
5.	App 5: Inventario (El Almacén):
○	Gestor de stock (entradas, salidas, stock mínimo).
○	Función Clave: Usa la cámara del móvil como Escáner de Códigos de Barras para buscar o añadir productos.
6.	App 6: Control Horario (El Consultor):
○	Un "Time Tracker" para registrar horas por cliente o proyecto.
○	Función Clave: Un cronómetro simple y la capacidad de generar informes de horas (PDF/Excel) para adjuntar a las facturas.
