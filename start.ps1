
# Libra Toolkit - 管理脚本入口
# 功能：提供交互式菜单或直接参数调用，选择执行不同的发布流程
# 用法：
#   ./start.ps1       -> 进入交互菜单（默认选项 1）
#   ./start.ps1 1     ->直接执行普通发布
#   ./start.ps1 2     ->直接执行混淆发布

param(
    [string]$InputChoice = ""
)

$ErrorActionPreference = "Stop"

function Write-Menu {
    Clear-Host
    Write-Host "========================================" -ForegroundColor Cyan
    Write-Host "      Libra Toolkit - 发布菜单" -ForegroundColor Cyan
    Write-Host "========================================" -ForegroundColor Cyan
    Write-Host "1. 普通发布 (Public Release)" -ForegroundColor Green
    Write-Host "   - 自动递增版本"
    Write-Host "   - 编译并发布到 GitHub"
    Write-Host ""
    Write-Host "2. 混淆发布 (Obfuscated Release)" -ForegroundColor Green
    Write-Host "   - 自动递增版本"
    Write-Host "   - 执行 ProGuard 混淆"
    Write-Host "   - 发布混淆包到 GitHub"
    Write-Host ""
    Write-Host "0. 退出" -ForegroundColor Yellow
    Write-Host "========================================" -ForegroundColor Cyan
}

function Invoke-Action ($Option) {
    if ([string]::IsNullOrWhiteSpace($Option)) {
        return $false
    }

    switch ($Option) {
        "1" {
            Write-Host "`n正在启动普通发布流程..." -ForegroundColor Yellow
            & "$PSScriptRoot/.github/public.ps1"
            return $true
        }
        "2" {
            Write-Host "`n正在启动混淆发布流程..." -ForegroundColor Yellow
            & "$PSScriptRoot/.github/public_obfuscated.ps1"
            return $true
        }
        "0" {
            Write-Host "退出..."
            exit 0
        }
        default {
            Write-Host "无效选项: $Option" -ForegroundColor Red
            return $false
        }
    }
}

# 模式 1：命令行参数模式
if (-not [string]::IsNullOrWhiteSpace($InputChoice)) {
    $result = Invoke-Action $InputChoice
    if ($result) {
        exit $LASTEXITCODE
    } else {
        exit 1
    }
}

# 模式 2：交互模式
while ($true) {
    Write-Menu
    $Choice = Read-Host "请输入选项 (默认: 1)"

    # 默认处理
    if ([string]::IsNullOrWhiteSpace($Choice)) {
        $Choice = "1"
    }

    $result = Invoke-Action $Choice

    if ($result) {
        if ($LASTEXITCODE -eq 0) {
            Write-Host "`n按任意键返回菜单..." -NoNewline
            [void][System.Console]::ReadKey($true)
        } else {
            Write-Host "`n执行出错 ($LASTEXITCODE)，按任意键返回..." -ForegroundColor Red
            [void][System.Console]::ReadKey($true)
        }
    } else {
        Start-Sleep -Seconds 1
    }
}
