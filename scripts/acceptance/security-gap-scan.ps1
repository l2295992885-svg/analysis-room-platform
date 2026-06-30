[CmdletBinding()]
param(
    [string]$RepoRoot = '',
    [string]$OutputPath = '',
    [switch]$ShowFindings
)

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

if ([string]::IsNullOrWhiteSpace($RepoRoot)) {
    $RepoRoot = (Resolve-Path (Join-Path $PSScriptRoot '..\..')).Path
} else {
    $RepoRoot = (Resolve-Path $RepoRoot).Path
}

$excludeFragments = @(
    '\.git\',
    '\node_modules\',
    '\target\',
    '\dist\',
    '\logs\',
    '\coverage\',
    '\playwright-report\'
)

$includeExtensions = @('.yml', '.yaml', '.properties', '.env', '.ts', '.vue', '.java', '.sql', '.md', '.ps1')
$patterns = [ordered]@{
    'credential-keywords' = '(?i)\b(password|token|authorization|cookie|secret|credential|access[-_]?key|secret[-_]?key)\b'
    'url-token-risk' = '(?i)([?&]Authorization=Bearer|Authorization=Bearer\s*\+|[?&](access[-_]?token|token|authorization)=.*getToken\()'
    'private-key-marker' = 'BEGIN (RSA |OPENSSH |EC |DSA )?PRIVATE KEY'
}

function Test-Excluded {
    param([string]$Path)
    $normalized = $Path -replace '/', '\'
    foreach ($fragment in $excludeFragments) {
        if ($normalized -like "*$fragment*") { return $true }
    }
    return $false
}

function Redact-Line {
    param([string]$Line)
    $value = $Line
    $value = $value -replace '(?i)(password|token|authorization|cookie|secret|credential|access[-_]?key|secret[-_]?key)(\s*[:=]\s*)([^,\s''"]+)', '$1$2<redacted>'
    $value = $value -replace '(?i)(password|token|authorization|cookie|secret|credential|access[-_]?key|secret[-_]?key)(\s*[:=]\s*)([''"])(.*?)([''"])', '$1$2$3<redacted>$5'
    $value = $value -replace '(?i)(Bearer\s+)[A-Za-z0-9._\-]+', '$1<redacted>'
    if ($value.Length -gt 240) { return $value.Substring(0, 240) + '...' }
    return $value
}

$findings = New-Object System.Collections.Generic.List[object]
$files = Get-ChildItem -LiteralPath $RepoRoot -Recurse -File -Force |
    Where-Object { $includeExtensions -contains $_.Extension -and -not (Test-Excluded $_.FullName) }

foreach ($file in $files) {
    $lineNo = 0
    foreach ($line in Get-Content -LiteralPath $file.FullName -Encoding UTF8 -ErrorAction SilentlyContinue) {
        $lineNo++
        foreach ($key in $patterns.Keys) {
            if ($line -match $patterns[$key]) {
                $findings.Add([pscustomobject]@{
                    category = $key
                    file = $file.FullName.Substring($RepoRoot.Length).TrimStart('\')
                    line = $lineNo
                    excerpt = Redact-Line $line
                }) | Out-Null
            }
        }
    }
}

if (-not [string]::IsNullOrWhiteSpace($OutputPath)) {
    $findings | ConvertTo-Json -Depth 6 | Set-Content -LiteralPath $OutputPath -Encoding UTF8
}

$summary = $findings | Group-Object category | Select-Object Name, Count
if ($ShowFindings) {
    $findings | Sort-Object category, file, line | Format-Table -AutoSize
} else {
    $summary | Format-Table -AutoSize
}
Write-Host ("Security scan findings: {0}" -f $findings.Count)
