# reset-db.ps1 - 重置数据库并重新导入 book_manager.sql
# 使用方式：在项目根目录执行 .\reset-db.ps1

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  即将重置数据库（MySQL + Redis）" -ForegroundColor Yellow
Write-Host "  警告：所有数据将被清除！" -ForegroundColor Red
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

$confirmation = Read-Host "确认继续？(y/N)"
if ($confirmation -ne "y" -and $confirmation -ne "Y") {
    Write-Host "已取消" -ForegroundColor Gray
    exit
}

Write-Host ""
Write-Host "[1/3] 停止容器并删除数据卷..." -ForegroundColor Cyan
docker compose down -v

if ($LASTEXITCODE -ne 0) {
    Write-Host "错误：docker compose down 失败" -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "[2/3] 重新创建并启动容器..." -ForegroundColor Cyan
docker compose up -d

if ($LASTEXITCODE -ne 0) {
    Write-Host "错误：docker compose up 失败" -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "[3/3] 等待数据库就绪..." -ForegroundColor Cyan

# 轮询等待 MySQL 可用
$maxAttempts = 30
$attempt = 0
$ready = $false
while ($attempt -lt $maxAttempts) {
    $attempt++
    $result = docker compose exec mysql mysql -uroot -proot -e "SELECT 1" 2>&1
    if ($LASTEXITCODE -eq 0) {
        $ready = $true
        break
    }
    Write-Host "   等待 MySQL 启动中... ($attempt/$maxAttempts)" -ForegroundColor Gray
    Start-Sleep -Seconds 2
}

if (-not $ready) {
    Write-Host "错误：MySQL 启动超时" -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Green
Write-Host "  数据库重置完成！" -ForegroundColor Green
Write-Host "  MySQL : root@localhost:3306 / book_manager" -ForegroundColor Green
Write-Host "  Redis : localhost:6379" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green
