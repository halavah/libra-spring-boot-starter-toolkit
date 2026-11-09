# Libra Toolkit - 构建与自动化脚本（.github/）

> 本目录为工程级自动化脚本统一目录（同 bin/ 迁入 + 原 Git 运维工具箱合并），支持 Linux/Mac (Shell) 和 Windows (PowerShell)。

本目录包含了项目构建、版本管理和发布流程的自动化脚本，全面支持 **Linux/Mac** (Shell) 和 **Windows** (PowerShell)。

## 🚀 快速开始

推荐使用项目根目录下的 **主控脚本**，它提供了交互式菜单和参数化调用两种模式。

### 入口脚本
- **Linux/Mac**: `./start.sh`
- **Windows**: 双击 `start.bat` (或终端运行 `start.ps1`)

### 使用方式
1. **交互模式**：直接运行脚本，选择菜单选项。
2. **命令模式**：
   - `./start.sh 1` 或 `.\start.ps1 1` → 执行 **普通发布** (不含混淆)
   - `./start.sh 2` 或 `.\start.ps1 2` → 执行 **混淆发布** (含 ProGuard 混淆)

---

## 🛠️ 脚本清单

所有核心脚本均位于 `bin/` 目录下，并成对提供（.sh 和 .ps1），确保跨平台一致性。

### 1. 版本管理 (`increment_version`)
- **文件**: `bin/increment_version.sh` / `bin/increment_version.ps1`
- **功能**:
  - 自动读取 `pom.xml` 中的版本号。
  - 递增 Patch 版本位 (例如 `1.0.2` -> `1.0.3`)。
  - 同步更新父工程 (`pom.xml`) 及所有子模块 (`libra-toolkit-core`, `libra-toolkit-demo`) 的版本配置。

### 2. 普通发布 (`public`)
- **文件**: `bin/public.sh` / `bin/public.ps1`
- **功能**: 执行标准化的构建和发布流程。
- **流程**:
  1. 调用 `increment_version` 更新版本。
  2. 执行 `mvn clean install -DskipTests` 进行构建。
  3. 自动提交代码更改并推送到 Git 远程分支。
  4. 执行 `mvn deploy` 发布构件到 GitHub Packages。
  5. 创建 Git Tag (例如 `v1.0.3`) 并推送。
  6. 创建 GitHub Release 并附带发布日志。

### 3. 混淆发布 (`public_obfuscated`)
- **文件**: `bin/public_obfuscated.sh` / `bin/public_obfuscated.ps1`
- **功能**: 在普通发布的基础上，增加了代码混淆步骤，适用于生产环境发布。
- **额外流程**:
  - 检查 `libra-toolkit-core/proguard.conf` 配置文件。
  - 验证混淆后的 JAR 包 (`-obfuscated.jar`) 是否生成。
  - 将混淆后的 JAR 包作为附件上传到 GitHub Release。

---

## 📋 环境要求

运行上述脚本需要以下环境支持：
1. **Java & Maven**: 已配置在系统 PATH 中。
2. **Git**: 用于版本控制操作。
3. **GitHub CLI (`gh`)**: 用于自动化创建 Release 和上传附件 (必须)。
4. **PowerShell 5.1+** (仅 Windows): 用于运行 `.ps1` 脚本。

## ⚠️ 注意事项
1. 脚本会自动跳过单元测试 (`-DskipTests`) 以确保发布流程的稳定性，请在发布前确保本地测试通过。
2. 确保你的 `settings.xml` 中已正确配置 GitHub Packages 的认证信息。
