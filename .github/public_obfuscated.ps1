
# Libra Toolkit - 混淆发布脚本 (PowerShell)

$ErrorActionPreference = "Stop"
function Write-Info ($msg) { Write-Host "[INFO] $msg" -ForegroundColor Cyan }
function Write-Success ($msg) { Write-Host "[SUCCESS] $msg" -ForegroundColor Green }
function Write-Error ($msg) { Write-Host "[ERROR] $msg" -ForegroundColor Red }

$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$RootDir = Split-Path -Parent $ScriptDir
Set-Location $RootDir

# 1. 自增版本
try {
    $NewVersion = & "$ScriptDir/increment_version.ps1"
    if ($NewVersion -is [array]) { $NewVersion = $NewVersion[-1] }
} catch {
    Write-Error "版本递增失败: $_"
    exit 1
}

$CurrentBranch = git rev-parse --abbrev-ref HEAD

# 2. 全局构建 (Package 阶段触发混淆)
Write-Info "构建并混淆..."
cmd /c mvn clean package -DskipTests
if ($LASTEXITCODE -ne 0) { Write-Error "构建失败"; exit 1 }

# 验证混淆包
$ObfuscatedJar = "libra-toolkit-core/target/libra-spring-boot-starter-toolkit-$NewVersion-obfuscated.jar"
if (-not (Test-Path $ObfuscatedJar)) {
    Write-Error "未找到混淆包: $ObfuscatedJar"
    exit 1
}

# 3. 提交
Write-Info "提交代码..."
git add .
git diff --staged --quiet
if ($LASTEXITCODE -ne 0) {
    git commit -m "Release v$NewVersion (Obfuscated)"
    git push origin "$CurrentBranch"
}

# 4. 发布 Core
Write-Info "发布 Core 模块..."
cmd /c mvn deploy -f libra-toolkit-core/pom.xml -DskipTests
if ($LASTEXITCODE -ne 0) { Write-Error "发布失败"; exit 1 }

# 5. Tag & Release
$TagName = "v$NewVersion"
if (-not (git rev-parse "$TagName" 2>$null)) {
    git tag -a "$TagName" -m "Release $NewVersion"
    git push origin "$TagName"
}

Write-Info "创建 Release 并上传混淆包..."
gh release create "$TagName" "$ObfuscatedJar" --title "$TagName" --notes "Release v$NewVersion (Obfuscated)" --target "$CurrentBranch" --latest

Write-Success "混淆发布完成! ($NewVersion)"
