2. Resumen de Arquitectura y Seguridad (El "Cómo")
Este documento resume el plan técnico para construir el ecosistema "Bóveda Modular". Define cómo lo haremos, qué tecnologías usaremos y cómo funcionará la seguridad.
A. Stack Tecnológico Principal
Para construir una app nativa, robusta y moderna, usaremos el stack recomendado por Google (Jetpack):
●	Lenguaje: 100% Kotlin.
●	UI: Jetpack Compose (el sistema de UI moderno, en lugar de XML).
●	Arquitectura: MVVM (Model-View-ViewModel).
●	Inyección de Dependencias: Hilt (para gestionar y proveer las clases).
●	Asincronía: Corrutinas de Kotlin y Flow (para gestionar la base de datos y tareas en segundo plano).
B. El Núcleo: La "Bóveda" (Base de Datos Encriptada)
Esta es la base de todo el ecosistema.
●	Tecnología: Room (la capa de Google sobre SQLite) + SQLCipher (el motor de encriptación).
●	Funcionamiento: Room gestiona las tablas (Clientes, Proyectos, Gastos), pero SQLCipher encripta el fichero físico .db en el disco usando AES-256.
●	Resultado: Si la app está cerrada, los datos son un bloque ilegible. Si roban el móvil, los datos están seguros.
C. Arquitectura de Seguridad y Acceso
1.	PIN Maestro / Biometría:
○	Onboarding: La primera vez, la app fuerza al usuario a crear un PIN de 6 dígitos.
○	Gestión del PIN: El PIN nunca se guarda. Se guarda un Hash del PIN en EncryptedSharedPreferences (un almacén seguro de Android) solo para verificar el login.
○	La Llave: El PIN real se guarda en la memoria RAM (en EncryptionKeyManager) solo mientras la app está abierta, y se usa para "abrir" la Bóveda de SQLCipher.
○	Acceso Diario: El usuario usa su Huella Dactilar (androidx.biometric) o su PIN para desbloquear la app.
2.	El "Seguro de Vida" (Backup/Restauración):
○	Objetivo: Proteger al usuario si el móvil se rompe o se pierde.
○	Exportación: La app genera un JSON con todos los datos de la Bóveda.
○	Seguridad del Backup: Este JSON se encripta con AES-256 usando una contraseña de backup (creada por el usuario en el momento).
○	Fichero Final: El resultado es un fichero .boveda (un JSON encriptado, no un texto plano), que es 100% seguro.
○	Plan B (Anti-olvido): La app también genera un "Kit de Recuperación" (palabras aleatorias) que permite desencriptar el backup si el usuario olvida la contraseña.
○	Lugar Seguro: La app usa el Storage Access Framework de Android para que el usuario guarde su .boveda en un lugar seguro (ej. Google Drive, OneDrive) sin que la app necesite permisos de internet.
○	Recordatorio: La app detecta cuándo fue el último backup y le recuerda amigablemente al usuario que cree uno nuevo (ej. mensualmente).
D. La "Bóveda Modular" (La Compra)
●	Desafío: No podemos tener una sola base de datos "gigante" si el usuario solo compra la "App de Gastos".
●	Solución: La "Bóveda" es un sistema inteligente.
1.	La Fundación (Seguridad, PIN, Backup) es la base de todas las apps.
2.	Cuando el usuario compra e instala el Módulo 2 (Gastos), la Bóveda ejecuta una migración de Room y "añade" las tablas Gasto y CategoriaGasto a la base de datos encriptada.
3.	Si luego compra el Módulo 3 (Inventario), la Bóveda ejecuta otra migración y añade las tablas Producto y Proveedor.
●	Resultado: La Bóveda del usuario crece con los módulos que compra, manteniendo siempre una única base de datos encriptada y un único sistema de backup.
