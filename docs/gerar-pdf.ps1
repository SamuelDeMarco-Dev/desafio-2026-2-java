# Gera o PDF do guia a partir do HTML usando o Microsoft Edge (headless).
# Uso:  powershell -ExecutionPolicy Bypass -File docs\gerar-pdf.ps1
# Nao instala nada: usa o Edge (ou Chrome) ja presente no Windows.

$ErrorActionPreference = 'Stop'
$aqui   = Split-Path -Parent $MyInvocation.MyCommand.Path
$html   = Join-Path $aqui 'GUIA-INSTALACAO-E-USO.html'
$pdf    = Join-Path $aqui 'GUIA-INSTALACAO-E-USO.pdf'

$navegadores = @(
  "$env:ProgramFiles\Microsoft\Edge\Application\msedge.exe",
  "${env:ProgramFiles(x86)}\Microsoft\Edge\Application\msedge.exe",
  "$env:ProgramFiles\Google\Chrome\Application\chrome.exe",
  "${env:ProgramFiles(x86)}\Google\Chrome\Application\chrome.exe"
)
$navegador = $navegadores | Where-Object { Test-Path $_ } | Select-Object -First 1
if (-not $navegador) { throw "Edge ou Chrome nao encontrado." }

$uri = ([System.Uri]$html).AbsoluteUri
$perfil = Join-Path $env:TEMP ("edge-pdf-" + [guid]::NewGuid())
if (Test-Path $pdf) { Remove-Item $pdf }

& $navegador --headless=new --disable-gpu --no-sandbox --user-data-dir=$perfil `
  --no-pdf-header-footer --run-all-compositor-stages-before-draw `
  --virtual-time-budget=5000 "--print-to-pdf=$pdf" $uri 2>$null
Start-Sleep -Seconds 2
Remove-Item $perfil -Recurse -Force -ErrorAction SilentlyContinue

if (Test-Path $pdf) { Write-Host "PDF gerado: $pdf" }
else { throw "Falha ao gerar o PDF." }