# Запуск JavaFX-приложения "Автомойка" (КР2).
# Перед запуском должен быть доступен PostgreSQL с базой из КР №1
# (параметры подключения — в src/main/resources/database.properties).
#
# Использование:  powershell -ExecutionPolicy Bypass -File .\run.ps1

$ErrorActionPreference = "Stop"
Set-Location $PSScriptRoot

# 1) если Maven есть в PATH — используем его
$mvn = (Get-Command mvn -ErrorAction SilentlyContinue)
if ($mvn) {
    & mvn javafx:run
    exit $LASTEXITCODE
}

# 2) иначе берём Maven и JDK, встроенные в IntelliJ IDEA
$idea = Get-ChildItem "C:\Program Files\JetBrains" -Directory -Filter "IntelliJ IDEA*" -ErrorAction SilentlyContinue |
        Sort-Object Name -Descending | Select-Object -First 1
if ($idea) {
    $bundledMvn = Join-Path $idea.FullName "plugins\maven-plugin\lib\maven3\bin\mvn.cmd"
    $jbr = Join-Path $idea.FullName "jbr"
    if ((Test-Path $bundledMvn) -and (Test-Path $jbr)) {
        $env:JAVA_HOME = $jbr
        & $bundledMvn javafx:run
        exit $LASTEXITCODE
    }
}

Write-Host "Не найден Maven. Установите Maven 3.9+ и Java 21, либо запустите проект из IntelliJ IDEA:" -ForegroundColor Yellow
Write-Host "  правый клик по carwash.fx.CarWashApp -> Run,  или вкладка Maven -> Plugins -> javafx:run"
exit 1
