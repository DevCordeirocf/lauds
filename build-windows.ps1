$ErrorActionPreference = "Stop"

$appName = "GeradorLaudos"
$jarName = "gerador-laudos-1.0-SNAPSHOT.jar"
$targetJar = Join-Path $PSScriptRoot "target\$jarName"
$distDir = Join-Path $PSScriptRoot "dist"
$appDir = Join-Path $distDir $appName

Write-Host "Gerando JAR com Maven..."
mvn package

if (-not (Test-Path $targetJar)) {
    throw "JAR nao encontrado em: $targetJar"
}

if (Test-Path $appDir) {
    Write-Host "Removendo pacote anterior..."
    Remove-Item -LiteralPath $appDir -Recurse -Force
}

Write-Host "Criando executavel Windows com runtime Java embutido..."
jpackage `
    --type app-image `
    --name $appName `
    --input (Join-Path $PSScriptRoot "target") `
    --main-jar $jarName `
    --main-class lauds.Main `
    --dest $distDir `
    --java-options "-Dfile.encoding=UTF-8"

Write-Host ""
Write-Host "Pronto. Execute:"
Write-Host "  $appDir\$appName.exe"
Write-Host ""
Write-Host "Para testar em outro PC, copie a pasta inteira:"
Write-Host "  $appDir"
