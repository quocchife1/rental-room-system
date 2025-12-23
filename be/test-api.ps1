#!/usr/bin/env pwsh

$BASE_URL = "http://localhost:8080"

Write-Host "========== API TESTING ==========" -ForegroundColor Cyan
Write-Host ""

# Test 1: Login admin
Write-Host "1. Testing Login (admin/123456)..." -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri "$BASE_URL/api/auth/login" `
        -Method POST `
        -ContentType "application/json" `
        -Body @{
            username = "admin"
            password = "123456"
        } | ConvertTo-Json
    Write-Host "✓ Login successful:" -ForegroundColor Green
    Write-Host $response
    Write-Host ""
} catch {
    Write-Host "✗ Login failed: $_" -ForegroundColor Red
    Write-Host ""
}

# Test 2: Check AuditLog
Write-Host "2. Checking AuditLog for recent actions..." -ForegroundColor Yellow
try {
    $url = "$BASE_URL/api/audit-logs?page=1`&size=10"
    $auditResponse = Invoke-RestMethod -Uri $url `
        -Method GET `
        -ContentType "application/json" 
    Write-Host "✓ AuditLog retrieved:" -ForegroundColor Green
    if ($auditResponse.data) {
        $auditResponse.data | ForEach-Object {
            Write-Host "  - Action: $($_.action) | Actor: $($_.actorId) | Role: $($_.actorRole) | Time: $($_.createdAt)"
        }
    } else {
        Write-Host "  (No audit logs yet)"
    }
    Write-Host ""
} catch {
    Write-Host "✗ AuditLog check failed: $_" -ForegroundColor Red
    Write-Host ""
}

# Test 3: Register new guest
Write-Host "3. Registering new guest..." -ForegroundColor Yellow
try {
    $regResponse = Invoke-RestMethod -Uri "$BASE_URL/api/auth/register/guest" `
        -Method POST `
        -ContentType "application/json" `
        -Body @{
            username = "testguest"
            password = "123456"
            email = "testguest@test.com"
            phone = "0909123456"
            fullName = "Test Guest"
        } | ConvertTo-Json
    Write-Host "✓ Guest registered:" -ForegroundColor Green
    Write-Host $regResponse
    Write-Host ""
} catch {
    Write-Host "✗ Guest registration failed: $_" -ForegroundColor Red
    Write-Host ""
}

# Test 4: Check AuditLog again
Write-Host "4. Checking AuditLog again (should show REGISTER_GUEST)..." -ForegroundColor Yellow
try {
    $url = "$BASE_URL/api/audit-logs?page=1`&size=10"
    $auditResponse = Invoke-RestMethod -Uri $url `
        -Method GET `
        -ContentType "application/json" 
    Write-Host "✓ AuditLog retrieved:" -ForegroundColor Green
    if ($auditResponse.data) {
        $auditResponse.data | ForEach-Object {
            Write-Host "  - Action: $($_.action) | Actor: $($_.actorId) | Role: $($_.actorRole)"
        }
    }
    Write-Host ""
} catch {
    Write-Host "✗ AuditLog check failed: $_" -ForegroundColor Red
    Write-Host ""
}

Write-Host "========== TEST COMPLETE ==========" -ForegroundColor Cyan
