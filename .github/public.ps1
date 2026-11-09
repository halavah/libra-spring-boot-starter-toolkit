
# Libra Toolkit - 普通发布脚本 (PowerShell)

$ErrorActionPreference = "Stop"
function Write-Info ($msg) { Write-Host "[INFO] $msg" -ForegroundColor Cyan }
function Write-Success ($msg) { Write-Host "[SUCCESS] $msg" -ForegroundColor Green }
function Write-Error ($msg) { Write-Host "[ERROR] $msg" -ForegroundColor Red }
function Write-Warning ($msg) { Write-Host "[WARNING] $msg" -ForegroundColor Yellow }

$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$RootDir = Split-Path -Parent $ScriptDir
Set-Location $RootDir

# 1. 自增版本
try {
    $NewVersion = & "$ScriptDir/increment_version.ps1"
    if ($NewVersion -is [array]) { $NewVersion = $NewVersion[-1] }
    if ([string]::IsNullOrWhiteSpace($NewVersion)) { throw "无法获取新版本" }
} catch {
    Write-Error "版本递增失败: $_"
    exit 1
}

$CurrentBranch = git rev-parse --abbrev-ref HEAD

# 2. 全局构建验证
Write-Info "正在构建所有模块..."
cmd /c mvn clean install -DskipTests
if ($LASTEXITCODE -ne 0) { Write-Error "构建失败"; exit 1 }

# 3. 提交代码 (Root)
Write-Info "提交代码..."
git add .
git diff --staged --quiet
if ($LASTEXITCODE -ne 0) {
    git commit -m "Release v$NewVersion"
    git push origin "$CurrentBranch"
}

# 4. 单独发布 Core 模块
Write-Info "正在发布 Core 模块到 GitHub Packages..."
# 切换到 core 目录执行 deploy，或者使用 -f
cmd /c mvn deploy -f libra-toolkit-core/pom.xml -DskipTests
if ($LASTEXITCODE -ne 0) { Write-Error "部署失败"; exit 1 }

# 5. Tag & Release
$TagName = "v$NewVersion"
if (-not (git rev-parse "$TagName" 2>$null)) {
    git tag -a "$TagName" -m "Release $NewVersion"
    git push origin "$TagName"
}

Write-Info "创建 GitHub Release..."
gh release create "$TagName" --title "$TagName" --notes "Release v$NewVersion" --target "$CurrentBranch"

Write-Success "发布完成! ($NewVersion)"
