
# Libra Toolkit - 版本自动递增脚本
# 功能：独立更新 Core 和 Demo 的版本号

$ErrorActionPreference = "Stop"

# 颜色定义
function Write-Info ($msg) { Write-Host "[INFO] $msg" -ForegroundColor Cyan }
function Write-Success ($msg) { Write-Host "[SUCCESS] $msg" -ForegroundColor Green }
function Write-Warning ($msg) { Write-Host "[WARNING] $msg" -ForegroundColor Yellow }
function Write-Error ($msg) { Write-Host "[ERROR] $msg" -ForegroundColor Red }

# 确保在根目录
$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$RootDir = Split-Path -Parent $ScriptDir
Set-Location $RootDir

Write-Info "正在读取当前版本号..."

# 我们以 Core 模块的版本为准
$CorePom = Join-Path $RootDir "libra-toolkit-core/pom.xml"
if (-not (Test-Path $CorePom)) {
    Write-Error "找不到 $CorePom"
    exit 1
}

$Content = Get-Content $CorePom -Raw
# 提取 <version> (简单正则)
if ($Content -match "<artifactId>libra-spring-boot-starter-toolkit</artifactId>\s*<version>(.+?)</version>") {
    $CurrentVersion = $matches[1]
} else {
    # 尝试更宽泛的匹配
    if ($Content -match "<version>(.+?)</version>") {
         # 需要小心不要匹配到 parent 的 version
         # 假设 core pom 中第一个 version 是 parent 的，第二个是自己的（或者就在 artifactId 后面）
         # 此处简化逻辑，建议使用 maven 命令获取最准确
         $CurrentVersion = cmd /c "mvn help:evaluate -f libra-toolkit-core/pom.xml -Dexpression=project.version -q -DforceStdout" 2>$null
    }
}

if ([string]::IsNullOrWhiteSpace($CurrentVersion)) {
    Write-Error "无法解析当前版本号"
    exit 1
}

Write-Info "当前版本: $CurrentVersion"

# 解析版本号
try {
    $VersionParts = $CurrentVersion.Split('.')
    if ($VersionParts.Count -ne 3) { throw "版本号格式必须为 X.Y.Z" }
    $Major = $VersionParts[0]
    $Minor = $VersionParts[1]
    $Patch = [int]$VersionParts[2]
} catch {
    Write-Error "版本号解析失败: $_"
    exit 1
}

# 递增 Patch 版本
$NewPatch = $Patch + 1
$NewVersion = "$Major.$Minor.$NewPatch"

Write-Info "新版本: $NewVersion"

function Update-PomVersion ($FilePath, $OldVer, $NewVer) {
    if (Test-Path $FilePath) {
        Write-Info "更新 $FilePath ..."
        $FileContent = Get-Content $FilePath -Raw
        # 注意：这里我们只替换项目自身的 version，而不是 parent 的
        # 因为 parent 现在是 spring-boot，版本是固定的 (2.5.14)

        # 策略：替换 <version>OldVer</version> 为 <version>NewVer</version>
        # 这可能会误伤依赖的版本，如果版本号相同。但在我们的项目中，OldVer 是特定的 1.0.x，风险较低。
         $NewContent = $FileContent -replace "<version>$OldVer</version>", "<version>$NewVer</version>"

        Set-Content -Path $FilePath -Value $NewContent -Encoding utf8
    }
}

# 更新 Core
Update-PomVersion "libra-toolkit-core/pom.xml" $CurrentVersion $NewVersion

# 更新 Demo (Demo 版本通常保持一致)
Update-PomVersion "libra-toolkit-demo/pom.xml" $CurrentVersion $NewVersion

# 更新 Root Aggregator (虽然它只是 Aggregator，但也保持版本一致比较好)
Update-PomVersion "pom.xml" $CurrentVersion $NewVersion

Write-Success "版本号已更新: $CurrentVersion -> $NewVersion"
return $NewVersion
