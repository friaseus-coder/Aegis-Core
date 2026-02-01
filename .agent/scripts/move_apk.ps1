# Script para mover y renombrar el APK generado
# Cumple la regla definida en agents.md

$projectRoot = "c:\Users\Usuario\Documents\Javier Frias\Antigravity\aegis core"
$buildDir = "$projectRoot\app\build\outputs\apk\debug"
$targetDir = "$projectRoot\apks"
$appName = "aegis-core"
$version = "v1.0.0" # Esto podría leerse dinámicamente si se deseara, hardcoded por ahora para cumplir ejemplo
$flavor = "debug"

# Crear directorio de destino si no existe
if (!(Test-Path -Path $targetDir)) {
    New-Item -ItemType Directory -Path $targetDir | Out-Null
    Write-Host "Directorio creado: $targetDir"
}

# Buscar el APK generado
$sourceFile = "$buildDir\app-debug.apk"

if (Test-Path -Path $sourceFile) {
    # Definir nombre de destino
    $date = Get-Date -Format "yyyyMMdd"
    $targetFile = "$targetDir\$appName-$flavor-$version-$date.apk"
    
    # Mover (o copiar) el archivo
    Copy-Item -Path $sourceFile -Destination $targetFile -Force
    
    Write-Host "APK movido correctamente a: $targetFile"
} else {
    Write-Host "Error: No se encontró el APK en $sourceFile. Asegúrate de compilar primero."
}
