# Preview-then-move backend files into ./be
# Run this from repository root (PowerShell) after reviewing the list below.
# WARNING: This will move files/folders. Backup or commit changes first.

$items = @(
  'HELP.md','mvnw','mvnw.cmd','pom.xml','README.md','rentaldb.sql',
  'src','target','uploads','test','generated-sources','pom.xml'
)

Write-Host "The following items will be moved into ./be if they exist:" -ForegroundColor Cyan
$items | ForEach-Object { if(Test-Path $_) { Write-Host " - $_" } }

$confirm = Read-Host "Proceed with moving the listed items into ./be ? Type YES to proceed"
if($confirm -ne 'YES'){
    Write-Host "Aborted by user." -ForegroundColor Yellow
    exit 1
}

# Ensure be exists
if(-not (Test-Path -Path './be')){ New-Item -ItemType Directory -Path './be' | Out-Null }

foreach($it in $items){
    if(Test-Path $it){
        try{
            Move-Item -Path $it -Destination './be' -Force
            Write-Host "Moved: $it" -ForegroundColor Green
        } catch {
            Write-Host "Failed to move $it : $_" -ForegroundColor Red
        }
    }
}

Write-Host "Done. Please inspect ./be and update any IDE run configurations if needed." -ForegroundColor Cyan
