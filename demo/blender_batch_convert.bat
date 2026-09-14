@echo off
chcp 65001 >nul 2>&1
title Blender 批量转换 OBJ → 二进制 PLY

:: -------------------------- 需修改的路径 --------------------------
:: 1. Blender 可执行文件路径（务必用英文路径，且加双引号）
set "BLENDER_PATH=D:\tools\blender-2.93.5-windows-x64\blender.exe"
:: 2. 目标文件夹（你的 OBJ 所在目录）
set "TARGET_DIR=D:\github\g3d\demo\resource\res\models"
:: -----------------------------------------------------------------

:: 检查 Blender 是否存在
if not exist "%BLENDER_PATH%" (
    echo 【错误】未找到 Blender 可执行文件！
    echo 路径：%BLENDER_PATH%
    echo 请检查路径是否正确（不要包含中文/特殊字符），按任意键退出...
    pause >nul
    exit /b 1
)

:: 检查目标文件夹
if not exist "%TARGET_DIR%" (
    echo 【错误】目标文件夹不存在！
    echo 路径：%TARGET_DIR%
    pause >nul
    exit /b 1
)

:: 【关键修复】用双引号包裹 Blender 路径，避免空格/中文导致的识别错误
echo 【开始转换】Blender 后台运行中...
echo 目标文件夹：%TARGET_DIR%
echo ------------------------------
"%BLENDER_PATH%" --background --python "%~dp0blender_obj2ply.py" -- "%TARGET_DIR%"

echo ------------------------------
echo 【转换结束】请查看上述日志确认结果
echo 按任意键退出...
pause >nul
exit /b 0