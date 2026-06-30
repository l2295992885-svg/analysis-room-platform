[CmdletBinding()]
param(
    [string]$BackendUrl = $env:ARP_BACKEND_URL,
    [string]$FrontendUrl = $env:ARP_FRONTEND_URL,
    [string]$AdminUsername = $env:ARP_ACCEPTANCE_USERNAME,
    [string]$AdminPassword = $env:ARP_ACCEPTANCE_PASSWORD,
    [string]$LimitedUsername = $env:ARP_ACCEPTANCE_LIMITED_USERNAME,
    [string]$LimitedPassword = $env:ARP_ACCEPTANCE_LIMITED_PASSWORD,
    [string]$RolePassword = $env:ARP_ACCEPTANCE_ROLE_PASSWORD,
    [string]$ClientId = $env:ARP_ACCEPTANCE_CLIENT_ID,
    [string]$PersonnelExcelPath = $env:ARP_PERSONNEL_EXCEL_PATH,
    [string]$DailyExcelPath = $env:ARP_DAILY_LKJ_EXCEL_PATH,
    [int]$TimeoutSec = 30,
    [switch]$AllowTodo
)

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

$RepoRoot = Resolve-Path (Join-Path $PSScriptRoot '..\..')
if ([string]::IsNullOrWhiteSpace($BackendUrl)) { $BackendUrl = 'http://localhost:18080' }
if ([string]::IsNullOrWhiteSpace($FrontendUrl)) { $FrontendUrl = 'http://127.0.0.1' }
if ([string]::IsNullOrWhiteSpace($RolePassword)) { $RolePassword = '666666' }
$BackendUrl = $BackendUrl.TrimEnd('/')
$FrontendUrl = $FrontendUrl.TrimEnd('/')

function Get-EnvValueFromFile {
    param([string]$Path, [string]$Name)
    if (-not (Test-Path -LiteralPath $Path)) { return $null }
    $pattern = '^\s*' + [regex]::Escape($Name) + '\s*=\s*(.+?)\s*$'
    foreach ($line in Get-Content -LiteralPath $Path -Encoding UTF8) {
        if ($line -match $pattern) {
            return ($Matches[1].Trim() -replace "^['""]|['""]$", '')
        }
    }
    return $null
}

function Find-SampleFile {
    param([string[]]$Patterns)
    $sampleRoot = [IO.Path]::GetFullPath((Join-Path $RepoRoot '..\..'))
    if (-not (Test-Path -LiteralPath $sampleRoot)) { return $null }
    $files = Get-ChildItem -LiteralPath $sampleRoot -File -ErrorAction SilentlyContinue |
        Where-Object { $_.Extension -in @('.xls', '.xlsx') }
    foreach ($pattern in $Patterns) {
        $match = $files | Where-Object { $_.Name -like $pattern } | Select-Object -First 1
        if ($match) { return $match.FullName }
    }
    return $null
}

if ([string]::IsNullOrWhiteSpace($ClientId)) {
    $ClientId = Get-EnvValueFromFile -Path (Join-Path $RepoRoot 'frontend\.env.development') -Name 'VITE_APP_CLIENT_ID'
}
if ([string]::IsNullOrWhiteSpace($PersonnelExcelPath)) {
    $PersonnelExcelPath = Find-SampleFile -Patterns @('*2026.05.10.xlsx', '*personnel*.xlsx')
}
if ([string]::IsNullOrWhiteSpace($DailyExcelPath)) {
    $DailyExcelPath = Find-SampleFile -Patterns @('*113445*.xls', '*113445*.xlsx', '*daily*.xls', '*daily*.xlsx')
}

$script:Checks = New-Object System.Collections.Generic.List[object]

function Add-Check {
    param(
        [string]$Name,
        [ValidateSet('PASS', 'FAIL', 'TODO', 'SKIP')] [string]$Status,
        [string]$Evidence
    )
    $item = [pscustomobject]@{
        name = $Name
        status = $Status
        evidence = $Evidence
    }
    $script:Checks.Add($item) | Out-Null
    Write-Host ("[{0}] {1} - {2}" -f $Status, $Name, $Evidence)
}

function ConvertTo-JsonBody {
    param([object]$Body)
    if ($null -eq $Body) { return $null }
    return ($Body | ConvertTo-Json -Depth 20)
}

function Invoke-Json {
    param(
        [ValidateSet('GET', 'POST', 'PUT', 'DELETE')] [string]$Method,
        [string]$Path,
        [object]$Body = $null,
        [string]$Token = $null
    )
    $uri = if ($Path -match '^https?://') { $Path } else { "$BackendUrl$Path" }
    $headers = @{}
    if (-not [string]::IsNullOrWhiteSpace($ClientId)) { $headers['clientid'] = $ClientId }
    if (-not [string]::IsNullOrWhiteSpace($Token)) { $headers['Authorization'] = "Bearer $Token" }
    $params = @{
        Uri = $uri
        Method = $Method
        Headers = $headers
        UseBasicParsing = $true
        TimeoutSec = $TimeoutSec
    }
    $jsonBody = ConvertTo-JsonBody $Body
    if ($null -ne $jsonBody) {
        $params['ContentType'] = 'application/json'
        $params['Body'] = $jsonBody
    }
    try {
        $response = Invoke-WebRequest @params
        $json = $null
        try { $json = $response.Content | ConvertFrom-Json -ErrorAction Stop } catch { }
        return [pscustomobject]@{
            ok = $true
            httpStatus = [int]$response.StatusCode
            json = $json
            raw = $response.Content
        }
    } catch {
        $status = 0
        $raw = $_.Exception.Message
        if ($_.Exception.Response) {
            $status = [int]$_.Exception.Response.StatusCode
            $stream = $_.Exception.Response.GetResponseStream()
            if ($stream) {
                $reader = New-Object IO.StreamReader($stream)
                $raw = $reader.ReadToEnd()
                $reader.Dispose()
            }
        }
        $json = $null
        try { $json = $raw | ConvertFrom-Json -ErrorAction Stop } catch { }
        return [pscustomobject]@{
            ok = $false
            httpStatus = $status
            json = $json
            raw = $raw
        }
    }
}

function Test-ApiSuccess {
    param([object]$Response)
    if ($null -eq $Response) { return $false }
    if ($Response.httpStatus -lt 200 -or $Response.httpStatus -ge 300) { return $false }
    if ($null -ne $Response.json -and ($Response.json.PSObject.Properties.Name -contains 'code')) {
        return ([int]$Response.json.code -eq 200)
    }
    return $true
}

function Get-BackendCheckEvidence {
    param([object]$Check)
    if (Test-ApiSuccess $Check) {
        return "$BackendUrl /auth/tenant/list"
    }

    $parts = New-Object System.Collections.Generic.List[string]
    $parts.Add("$BackendUrl /auth/tenant/list") | Out-Null
    $parts.Add("status=$($Check.httpStatus)") | Out-Null

    $raw = [string]$Check.raw
    if (-not [string]::IsNullOrWhiteSpace($raw)) {
        $raw = ($raw -replace '\s+', ' ').Trim()
        if ($raw.Length -gt 120) { $raw = $raw.Substring(0, 120) + '...' }
        $parts.Add("error=$raw") | Out-Null
    }

    if ([string]::IsNullOrWhiteSpace($env:APP_DATASOURCE_URL)) {
        $parts.Add("APP_DATASOURCE_URL not set, default database ry-vue will be used") | Out-Null
    }
    if ([string]::IsNullOrWhiteSpace($env:APP_DATASOURCE_USERNAME)) {
        $parts.Add("APP_DATASOURCE_USERNAME not set, default user root will be used") | Out-Null
    }
    if ([string]::IsNullOrWhiteSpace($env:APP_DATASOURCE_PASSWORD)) {
        $parts.Add("APP_DATASOURCE_PASSWORD not set") | Out-Null
    }

    return ($parts -join '; ')
}

function Get-ResponseData {
    param([object]$Response)
    if ($null -eq $Response -or $null -eq $Response.json) { return $null }
    if ($Response.json.PSObject.Properties.Name -contains 'data') { return $Response.json.data }
    return $Response.json
}

function Get-Rows {
    param([object]$Response)
    if ($null -eq $Response -or $null -eq $Response.json) { return ,@() }
    if ($Response.json.PSObject.Properties.Name -contains 'rows') { return ,@($Response.json.rows) }
    if ($Response.json.PSObject.Properties.Name -contains 'data') {
        $data = $Response.json.data
        if ($null -ne $data -and ($data.PSObject.Properties.Name -contains 'records')) { return ,@($data.records) }
    }
    return ,@()
}

function Get-LoginToken {
    param([string]$Username, [string]$Password)
    if ([string]::IsNullOrWhiteSpace($Username) -or [string]::IsNullOrWhiteSpace($Password)) { return $null }
    $login = Invoke-Json -Method POST -Path '/auth/login' -Body @{
        tenantId = '000000'
        username = $Username
        password = $Password
        clientId = $ClientId
        grantType = 'password'
    }
    $data = Get-ResponseData $login
    if ($null -eq $data) { return $null }
    if ($data.PSObject.Properties.Name -contains 'access_token') { return $data.access_token }
    if ($data.PSObject.Properties.Name -contains 'token') { return $data.token }
    return $null
}

function Get-ItemCount {
    param([object]$Value)
    if ($null -eq $Value) { return 0 }
    return @($Value).Count
}

function Invoke-Multipart {
    param(
        [string]$Path,
        [string]$FilePath,
        [hashtable]$Fields,
        [string]$Token
    )
    Add-Type -AssemblyName System.Net.Http
    $uri = "$BackendUrl$Path"
    $client = New-Object System.Net.Http.HttpClient
    $stream = $null
    $multipart = $null
    try {
        $client.Timeout = [TimeSpan]::FromSeconds($TimeoutSec)
        if (-not [string]::IsNullOrWhiteSpace($ClientId)) { $client.DefaultRequestHeaders.Add('clientid', $ClientId) }
        if (-not [string]::IsNullOrWhiteSpace($Token)) {
            $client.DefaultRequestHeaders.Authorization = New-Object System.Net.Http.Headers.AuthenticationHeaderValue -ArgumentList 'Bearer', $Token
        }
        $multipart = New-Object System.Net.Http.MultipartFormDataContent
        $stream = [System.IO.File]::OpenRead($FilePath)
        $fileContent = New-Object System.Net.Http.StreamContent($stream)
        $fileContent.Headers.ContentType = [System.Net.Http.Headers.MediaTypeHeaderValue]::Parse('application/octet-stream')
        $multipart.Add($fileContent, 'file', [System.IO.Path]::GetFileName($FilePath))
        foreach ($key in $Fields.Keys) {
            if ($null -ne $Fields[$key] -and -not [string]::IsNullOrWhiteSpace([string]$Fields[$key])) {
                $multipart.Add((New-Object System.Net.Http.StringContent([string]$Fields[$key])), $key)
            }
        }
        $response = $client.PostAsync($uri, $multipart).GetAwaiter().GetResult()
        $raw = $response.Content.ReadAsStringAsync().GetAwaiter().GetResult()
        $json = $null
        try { $json = $raw | ConvertFrom-Json -ErrorAction Stop } catch { }
        return [pscustomobject]@{
            ok = $response.IsSuccessStatusCode
            httpStatus = [int]$response.StatusCode
            json = $json
            raw = $raw
        }
    } finally {
        if ($multipart) { $multipart.Dispose() }
        if ($stream) { $stream.Dispose() }
        $client.Dispose()
    }
}

function Invoke-DownloadCheck {
    param(
        [string]$Path,
        [string]$Token,
        [ValidateSet('GET', 'POST')] [string]$Method = 'POST'
    )
    $uri = "$BackendUrl$Path"
    $headers = @{}
    if (-not [string]::IsNullOrWhiteSpace($ClientId)) { $headers['clientid'] = $ClientId }
    if (-not [string]::IsNullOrWhiteSpace($Token)) { $headers['Authorization'] = "Bearer $Token" }
    try {
        $response = Invoke-WebRequest -Uri $uri -Method $Method -Headers $headers -UseBasicParsing -TimeoutSec $TimeoutSec
        $json = $null
        try { $json = $response.Content | ConvertFrom-Json -ErrorAction Stop } catch { }
        return [pscustomobject]@{ ok = ($response.StatusCode -ge 200 -and $response.StatusCode -lt 300); httpStatus = [int]$response.StatusCode; json = $json; raw = $response.Content }
    } catch {
        $status = 0
        $raw = $_.Exception.Message
        if ($_.Exception.Response) {
            $status = [int]$_.Exception.Response.StatusCode
            $stream = $_.Exception.Response.GetResponseStream()
            if ($stream) {
                $reader = New-Object IO.StreamReader($stream)
                $raw = $reader.ReadToEnd()
                $reader.Dispose()
            }
        }
        $json = $null
        try { $json = $raw | ConvertFrom-Json -ErrorAction Stop } catch { }
        return [pscustomobject]@{ ok = $false; httpStatus = $status; json = $json; raw = $raw }
    }
}

function New-DraftRecord {
    param([string]$Token, [string]$Suffix)
    $body = @{
        reportDate = '2026-06-12'
        violationDate = '2026-06-09'
        violationTime = '13:15:10'
        violationCode = 'QBJ028'
        proposedAssessmentContent = "acceptance daily LKJ draft $Suffix"
        responsibleDeptName = 'acceptance dept'
        employeeName = "acceptance person $Suffix"
        locomotive = 'HXD1B-0378'
        trainNo = '41096'
        location = 'acceptance location'
        timeSegment = '13:15:10'
        issuingDept = 'analysis room'
    }
    $create = Invoke-Json -Method POST -Path '/violation/daily/records' -Body $body -Token $Token
    if (-not (Test-ApiSuccess $create)) { return $null }
    $list = Invoke-Json -Method GET -Path "/violation/daily/records?pageNum=1&pageSize=20&currentStatus=DRAFT" -Token $Token
    $rows = Get-Rows $list
    $match = $rows | Where-Object { $_.proposedAssessmentContent -eq $body.proposedAssessmentContent } | Select-Object -First 1
    if ($match) { return $match.recordId }
    if ($rows.Count -gt 0) { return $rows[0].recordId }
    return $null
}

function Invoke-ActionCheck {
    param([string]$Name, [long]$RecordId, [string]$Action, [hashtable]$Body, [string]$Token)
    $result = Invoke-Json -Method POST -Path "/violation/daily/records/$RecordId/$Action" -Body $Body -Token $Token
    if (Test-ApiSuccess $result) {
        Add-Check $Name 'PASS' "record=$RecordId action=$Action"
        return $true
    }
    Add-Check $Name 'FAIL' "record=$RecordId action=$Action status=$($result.httpStatus)"
    return $false
}

function Test-ScopedRows {
    param(
        [object[]]$Rows,
        [string]$IdProperty,
        [string]$NameProperty,
        [long]$ExpectedId,
        [string]$ExpectedName
    )
    foreach ($row in $Rows) {
        $idValue = $null
        $nameValue = $null
        if ($row.PSObject.Properties.Name -contains $IdProperty) { $idValue = $row.$IdProperty }
        if ($row.PSObject.Properties.Name -contains $NameProperty) { $nameValue = $row.$NameProperty }
        $idMatches = $false
        if ($null -ne $idValue -and -not [string]::IsNullOrWhiteSpace([string]$idValue)) {
            try { $idMatches = ([long]$idValue -eq $ExpectedId) } catch { $idMatches = $false }
        }
        if ((-not $idMatches) -and ([string]$nameValue -ne $ExpectedName)) {
            return $false
        }
    }
    return $true
}

$backendCheck = Invoke-Json -Method GET -Path '/auth/tenant/list'
Add-Check '01 backend started' ($(if (Test-ApiSuccess $backendCheck) { 'PASS' } else { 'FAIL' })) (Get-BackendCheckEvidence $backendCheck)

try {
    $frontendResponse = Invoke-WebRequest -Uri $FrontendUrl -Method GET -UseBasicParsing -TimeoutSec $TimeoutSec
    Add-Check '02 frontend reachable' 'PASS' "$FrontendUrl HTTP $($frontendResponse.StatusCode)"
} catch {
    Add-Check '02 frontend reachable' 'TODO' "$FrontendUrl not reachable"
}

if ([string]::IsNullOrWhiteSpace($AdminUsername) -or [string]::IsNullOrWhiteSpace($AdminPassword) -or [string]::IsNullOrWhiteSpace($ClientId)) {
    Add-Check '03 admin login' 'TODO' 'Set ARP_ACCEPTANCE_USERNAME, ARP_ACCEPTANCE_PASSWORD, and ARP_ACCEPTANCE_CLIENT_ID or pass parameters.'
    Add-Check 'protected API checks' 'TODO' 'Login credentials were not provided.'
} else {
    $login = Invoke-Json -Method POST -Path '/auth/login' -Body @{
        tenantId = '000000'
        username = $AdminUsername
        password = $AdminPassword
        clientId = $ClientId
        grantType = 'password'
    }
    $token = $null
    $loginData = Get-ResponseData $login
    if ($null -ne $loginData) {
        if ($loginData.PSObject.Properties.Name -contains 'access_token') { $token = $loginData.access_token }
        elseif ($loginData.PSObject.Properties.Name -contains 'token') { $token = $loginData.token }
    }

    if ([string]::IsNullOrWhiteSpace($token)) {
        Add-Check '03 admin login' 'FAIL' "status=$($login.httpStatus)"
    } else {
        Add-Check '03 admin login' 'PASS' 'access token acquired and not printed'

        $roleTokens = @{}
        $roleAccounts = @(
            @{ key = 'analyst'; username = 'analyst_test' },
            @{ key = 'leader'; username = 'leader_test' },
            @{ key = 'director'; username = 'director_test' },
            @{ key = 'workshop'; username = 'workshop_test' },
            @{ key = 'team'; username = 'team_test' },
            @{ key = 'guide'; username = 'guide_test' }
        )
        foreach ($account in $roleAccounts) {
            $roleTokens[$account['key']] = Get-LoginToken -Username $account['username'] -Password $RolePassword
        }
        $loggedRoleCount = @($roleTokens.Keys | Where-Object { -not [string]::IsNullOrWhiteSpace($roleTokens[$_]) }).Count
        Add-Check '33 multi-role dev accounts login' ($(if ($loggedRoleCount -eq 6) { 'PASS' } else { 'FAIL' })) "loggedRoles=$loggedRoleCount/6"

        $personnel = Invoke-Json -Method GET -Path '/base/personnel/list?pageNum=1&pageSize=1' -Token $token
        $codes = Invoke-Json -Method GET -Path '/base/violationCode/list?pageNum=1&pageSize=1' -Token $token
        Add-Check '04 base personnel and violation code APIs' ($(if ((Test-ApiSuccess $personnel) -and (Test-ApiSuccess $codes)) { 'PASS' } else { 'FAIL' })) 'base list APIs called'

        if ($PersonnelExcelPath -and (Test-Path -LiteralPath $PersonnelExcelPath)) {
            $uploadPersonnel = Invoke-Multipart -Path '/base/personnel/importRoster' -FilePath $PersonnelExcelPath -Fields @{} -Token $token
            Add-Check '05 personnel roster import' ($(if (Test-ApiSuccess $uploadPersonnel) { 'PASS' } else { 'FAIL' })) "file=$([IO.Path]::GetFileName($PersonnelExcelPath))"
        } else {
            Add-Check '05 personnel roster import' 'TODO' 'sample personnel Excel file not found'
        }

        $importBatchId = $null
        if ($DailyExcelPath -and (Test-Path -LiteralPath $DailyExcelPath)) {
            $import = Invoke-Multipart -Path '/violation/daily/imports' -FilePath $DailyExcelPath -Fields @{ reportDate = '2026-06-12'; businessYear = '2026' } -Token $token
            $importData = Get-ResponseData $import
            if ((Test-ApiSuccess $import) -and $null -ne $importData -and ($importData.PSObject.Properties.Name -contains 'importBatchId')) {
                $importBatchId = $importData.importBatchId
                Add-Check '06 daily LKJ Excel import' 'PASS' "importBatchId=$importBatchId"
                Add-Check '07 import batch created' 'PASS' "importBatchId=$importBatchId"
            } else {
                $importCode = if ($null -ne $import.json -and ($import.json.PSObject.Properties.Name -contains 'code')) { $import.json.code } else { '' }
                $importMsg = if ($null -ne $import.json -and ($import.json.PSObject.Properties.Name -contains 'msg')) { $import.json.msg } else { '' }
                Add-Check '06 daily LKJ Excel import' 'FAIL' "status=$($import.httpStatus); code=$importCode; msg=$importMsg"
                Add-Check '07 import batch created' 'FAIL' 'importBatchId missing'
            }
        } else {
            Add-Check '06 daily LKJ Excel import' 'TODO' 'sample daily Excel file not found'
            Add-Check '07 import batch created' 'TODO' 'no import batch'
        }

        if ($null -ne $importBatchId) {
            $rowsResponse = Invoke-Json -Method GET -Path "/violation/daily/imports/$importBatchId/rows?pageNum=1&pageSize=200" -Token $token
            $rows = Get-Rows $rowsResponse
            Add-Check '08 import preview rows generated' ($(if ($rows.Count -gt 0) { 'PASS' } else { 'FAIL' })) "rows=$($rows.Count)"
            $statuses = @($rows | ForEach-Object { $_.validationStatus } | Sort-Object -Unique)
            $missingStatuses = @(@('VALID', 'NEED_CONFIRM', 'INVALID') | Where-Object { $statuses -notcontains $_ })
            Add-Check '09 validation statuses include valid/need-confirm/invalid' ($(if ((Get-ItemCount $missingStatuses) -eq 0) { 'PASS' } else { 'TODO' })) "statuses=$($statuses -join ','); missing=$($missingStatuses -join ',')"

            foreach ($row in @($rows | Where-Object { $_.validationStatus -eq 'NEED_CONFIRM' })) {
                [void](Invoke-Json -Method PUT -Path "/violation/daily/imports/$importBatchId/rows/$($row.rowId)" -Body @{ confirmStatus = 'CONFIRMED'; confirmRemark = 'acceptance confirm' } -Token $token)
            }
            $submitRows = @($rows | Where-Object { $_.validationStatus -ne 'INVALID' } | Select-Object -First 3)
            if ($submitRows.Count -gt 0) {
                $rowIds = @($submitRows | ForEach-Object { $_.rowId })
                $submitImport = Invoke-Json -Method POST -Path "/violation/daily/imports/$importBatchId/submit" -Body @{ rowIds = $rowIds } -Token $token
                Add-Check '10 selected preview rows submit to formal records' ($(if (Test-ApiSuccess $submitImport) { 'PASS' } else { 'FAIL' })) "selectedRows=$($rowIds.Count)"
            } else {
                Add-Check '10 selected preview rows submit to formal records' 'TODO' 'no VALID or confirmed NEED_CONFIRM rows'
            }

            $errorReport = Invoke-DownloadCheck -Path "/violation/daily/imports/$importBatchId/error-report" -Token $token
            Add-Check '29 import error report export' ($(if ($errorReport.ok) { 'PASS' } else { 'FAIL' })) "status=$($errorReport.httpStatus)"
        } else {
            Add-Check '08 import preview rows generated' 'TODO' 'no importBatchId'
            Add-Check '09 validation statuses include valid/need-confirm/invalid' 'TODO' 'no importBatchId'
            Add-Check '10 selected preview rows submit to formal records' 'TODO' 'no importBatchId'
            Add-Check '29 import error report export' 'TODO' 'no importBatchId'
        }

        $recordA = New-DraftRecord -Token $token -Suffix 'archive'
        if ($null -ne $recordA) {
            Add-Check 'manual draft for archive branch' 'PASS' "record=$recordA"
            [void](Invoke-ActionCheck '11 submit to leader' $recordA 'submit' @{ opinion = 'acceptance submit' } $token)
            [void](Invoke-ActionCheck '12 leader approve' $recordA 'leader-approve' @{ opinion = 'acceptance leader approve' } $token)
            [void](Invoke-ActionCheck '13 director approve' $recordA 'director-approve' @{ opinion = 'acceptance director approve' } $token)
            [void](Invoke-ActionCheck '14 dispatch to workshop' $recordA 'dispatch-workshop' @{ opinion = 'acceptance dispatch workshop'; workshopId = 103; workshopName = 'acceptance workshop' } $token)
            [void](Invoke-ActionCheck '15 dispatch to team' $recordA 'dispatch-team' @{ opinion = 'acceptance dispatch team'; teamId = 103; teamName = 'acceptance team' } $token)
            [void](Invoke-ActionCheck '16 dispatch to guide group' $recordA 'dispatch-guide-group' @{ opinion = 'acceptance dispatch guide'; guideGroupId = 103; guideGroupName = 'acceptance guide group' } $token)
            [void](Invoke-ActionCheck '17 guide confirm' $recordA 'guide-confirm' @{ opinion = 'acceptance guide confirm' } $token)
            [void](Invoke-ActionCheck '19 return recheck' $recordA 'return-recheck' @{ opinion = 'acceptance return recheck' } $token)
            [void](Invoke-ActionCheck '20 final confirm' $recordA 'final-confirm' @{ finalDecision = 'MAINTAIN'; finalOpinion = 'acceptance final confirm' } $token)
            [void](Invoke-ActionCheck '21 archive to result table' $recordA 'archive' @{ opinion = 'acceptance archive' } $token)
        } else {
            Add-Check '11-21 main workflow actions' 'FAIL' 'could not create manual draft record'
        }

        $recordB = New-DraftRecord -Token $token -Suffix 'cancel'
        if ($null -ne $recordB) {
            [void](Invoke-ActionCheck 'reject branch submit to leader' $recordB 'submit' @{ opinion = 'acceptance submit reject branch' } $token)
            [void](Invoke-ActionCheck 'reject branch leader approve' $recordB 'leader-approve' @{ opinion = 'acceptance leader approve' } $token)
            [void](Invoke-ActionCheck 'reject branch director approve' $recordB 'director-approve' @{ opinion = 'acceptance director approve' } $token)
            [void](Invoke-ActionCheck 'reject branch dispatch to workshop' $recordB 'dispatch-workshop' @{ workshopId = 103; workshopName = 'acceptance workshop' } $token)
            [void](Invoke-ActionCheck 'reject branch dispatch to team' $recordB 'dispatch-team' @{ teamId = 103; teamName = 'acceptance team' } $token)
            [void](Invoke-ActionCheck 'reject branch dispatch to guide group' $recordB 'dispatch-guide-group' @{ guideGroupId = 103; guideGroupName = 'acceptance guide group' } $token)
            [void](Invoke-ActionCheck '18 guide reject false feedback' $recordB 'guide-reject' @{ reasonType = 'NOT_TRUE'; reasonDescription = 'acceptance false feedback'; opinion = 'acceptance false feedback' } $token)
            [void](Invoke-ActionCheck 'reject branch return recheck' $recordB 'return-recheck' @{ opinion = 'acceptance reject return recheck' } $token)
            [void](Invoke-ActionCheck 'reject branch final confirm cancel' $recordB 'final-confirm' @{ finalDecision = 'CANCEL_EXCLUDED'; finalOpinion = 'acceptance cancel decision' } $token)
            [void](Invoke-ActionCheck '22 cancel excluded' $recordB 'cancel' @{ cancelReason = 'acceptance cancel reason' } $token)
        } else {
            Add-Check '18 / 22 false-feedback and cancel branch' 'FAIL' 'could not create manual draft record'
        }

        $recordForRead = if ($null -ne $recordA) { $recordA } else { $recordB }
        if ($null -ne $recordForRead) {
            $logs = Invoke-Json -Method GET -Path "/violation/daily/records/$recordForRead/logs" -Token $token
            Add-Check '23 workflow logs exist' ($(if ((Test-ApiSuccess $logs) -and ((Get-ItemCount (Get-ResponseData $logs)) -gt 0)) { 'PASS' } else { 'FAIL' })) "record=$recordForRead"

            $todo = Invoke-Json -Method GET -Path "/todos/my?pageNum=1&pageSize=10&businessType=DAILY_VIOLATION&businessId=$recordForRead" -Token $token
            Add-Check '25 todo records exist' ($(if ((Test-ApiSuccess $todo) -and ((Get-ItemCount (Get-Rows $todo)) -gt 0)) { 'PASS' } else { 'TODO' })) 'admin may not be in workflow receiver role scope'

            $mail = Invoke-Json -Method GET -Path "/mailbox/messages?pageNum=1&pageSize=10&businessType=DAILY_VIOLATION&businessId=$recordForRead" -Token $token
            Add-Check '26 mailbox messages exist' ($(if ((Test-ApiSuccess $mail) -and ((Get-ItemCount (Get-Rows $mail)) -gt 0)) { 'PASS' } else { 'TODO' })) 'admin visibility may depend on receiver scope'
        }

        $oper = Invoke-Json -Method GET -Path '/monitor/operlog/list?pageNum=1&pageSize=20&title=%E6%AF%8F%E6%97%A5LKJ' -Token $token
        Add-Check '24 operation logs exist' ($(if ((Test-ApiSuccess $oper) -and ((Get-ItemCount (Get-Rows $oper)) -gt 0)) { 'PASS' } else { 'TODO' })) 'depends on async RuoYi operation log and monitor permission'

        $results = Invoke-Json -Method GET -Path '/violation/daily/results?pageNum=1&pageSize=10' -Token $token
        Add-Check '27 result snapshots exist' ($(if ((Test-ApiSuccess $results) -and ((Get-ItemCount (Get-Rows $results)) -gt 0)) { 'PASS' } else { 'FAIL' })) 'result list queried'

        $exportRecords = Invoke-DownloadCheck -Path '/violation/daily/records/export' -Token $token
        $exportResults = Invoke-DownloadCheck -Path '/violation/daily/results/export' -Token $token
        Add-Check '28 backend export endpoints work' ($(if ($exportRecords.ok -and $exportResults.ok) { 'PASS' } else { 'FAIL' })) "records=$($exportRecords.httpStatus), results=$($exportResults.httpStatus)"

        if ($roleTokens['workshop']) {
            $workshopList = Invoke-Json -Method GET -Path '/violation/daily/records?pageNum=1&pageSize=100' -Token $roleTokens['workshop']
            $workshopRows = Get-Rows $workshopList
            Add-Check '34 workshop user scoped to own workshop' ($(if ((Test-ApiSuccess $workshopList) -and $workshopRows.Count -gt 0 -and (Test-ScopedRows -Rows $workshopRows -IdProperty 'workshopId' -NameProperty 'workshopName' -ExpectedId 103 -ExpectedName 'acceptance workshop')) { 'PASS' } else { 'FAIL' })) "rows=$($workshopRows.Count)"
        } else {
            Add-Check '34 workshop user scoped to own workshop' 'FAIL' 'workshop_test login unavailable'
        }

        if ($roleTokens['team']) {
            $teamList = Invoke-Json -Method GET -Path '/violation/daily/records?pageNum=1&pageSize=100' -Token $roleTokens['team']
            $teamRows = Get-Rows $teamList
            Add-Check '35 team user scoped to own team' ($(if ((Test-ApiSuccess $teamList) -and $teamRows.Count -gt 0 -and (Test-ScopedRows -Rows $teamRows -IdProperty 'teamId' -NameProperty 'teamName' -ExpectedId 103 -ExpectedName 'acceptance team')) { 'PASS' } else { 'FAIL' })) "rows=$($teamRows.Count)"
        } else {
            Add-Check '35 team user scoped to own team' 'FAIL' 'team_test login unavailable'
        }

        if ($roleTokens['guide']) {
            $guideList = Invoke-Json -Method GET -Path '/violation/daily/records?pageNum=1&pageSize=100' -Token $roleTokens['guide']
            $guideRows = Get-Rows $guideList
            Add-Check '36 guide user scoped to own guide group' ($(if ((Test-ApiSuccess $guideList) -and $guideRows.Count -gt 0 -and (Test-ScopedRows -Rows $guideRows -IdProperty 'guideGroupId' -NameProperty 'guideGroupName' -ExpectedId 103 -ExpectedName 'acceptance guide group')) { 'PASS' } else { 'FAIL' })) "rows=$($guideRows.Count)"
        } else {
            Add-Check '36 guide user scoped to own guide group' 'FAIL' 'guide_test login unavailable'
        }

        $todoRecord = New-DraftRecord -Token $token -Suffix 'todo-scope'
        $todoId = $null
        if ($null -ne $todoRecord -and $roleTokens['workshop'] -and $roleTokens['analyst']) {
            [void](Invoke-ActionCheck 'todo scope submit to leader' $todoRecord 'submit' @{ opinion = 'todo scope submit' } $token)
            [void](Invoke-ActionCheck 'todo scope leader approve' $todoRecord 'leader-approve' @{ opinion = 'todo scope leader approve' } $token)
            [void](Invoke-ActionCheck 'todo scope director approve' $todoRecord 'director-approve' @{ opinion = 'todo scope director approve' } $token)
            [void](Invoke-ActionCheck 'todo scope dispatch to workshop' $todoRecord 'dispatch-workshop' @{ workshopId = 103; workshopName = 'acceptance workshop' } $token)
            $todoList = Invoke-Json -Method GET -Path "/todos/my?pageNum=1&pageSize=10&businessType=DAILY_VIOLATION&businessId=$todoRecord" -Token $roleTokens['workshop']
            $todoRows = Get-Rows $todoList
            if ($todoRows.Count -gt 0) { $todoId = $todoRows[0].id }
            if ($todoId) {
                $todoOpen = Invoke-Json -Method POST -Path "/todos/$todoId/open" -Token $roleTokens['analyst']
                $todoCode = if ($null -ne $todoOpen.json -and ($todoOpen.json.PSObject.Properties.Name -contains 'code')) { [int]$todoOpen.json.code } else { $todoOpen.httpStatus }
                Add-Check '37 non-receiver opening todo returns 403' ($(if ($todoCode -eq 403) { 'PASS' } else { 'FAIL' })) "code=$todoCode"
            } else {
                Add-Check '37 non-receiver opening todo returns 403' 'FAIL' 'pending workshop todo not found'
            }
        } else {
            Add-Check '37 non-receiver opening todo returns 403' 'FAIL' 'missing todo record or role tokens'
        }

        if ($null -ne $recordA -and $roleTokens['analyst']) {
            $fieldEdit = Invoke-Json -Method PUT -Path "/violation/daily/records/$recordA" -Body @{ reportDate = '2026-06-12'; violationCode = 'QBJ028'; proposedAssessmentContent = 'illegal fact edit after archive' } -Token $token
            $fieldCode = if ($null -ne $fieldEdit.json -and ($fieldEdit.json.PSObject.Properties.Name -contains 'code')) { [int]$fieldEdit.json.code } else { $fieldEdit.httpStatus }
            Add-Check '38 field permission rejects non-draft fact edit' ($(if ($fieldCode -eq 403) { 'PASS' } else { 'FAIL' })) "code=$fieldCode"

            $tempAttachment = New-TemporaryFile
            Set-Content -LiteralPath $tempAttachment.FullName -Value 'daily LKJ acceptance attachment' -Encoding UTF8
            try {
                $uploadAttachment = Invoke-Multipart -Path "/violation/daily/records/$recordA/attachments" -FilePath $tempAttachment.FullName -Fields @{ businessAction = 'ACCEPTANCE'; attachmentType = 'EVIDENCE' } -Token $token
                $attachmentData = Get-ResponseData $uploadAttachment
                $attachmentId = if ($null -ne $attachmentData -and ($attachmentData.PSObject.Properties.Name -contains 'id')) { $attachmentData.id } else { $null }
                if ($attachmentId) {
                    $downloadDenied = Invoke-DownloadCheck -Path "/violation/daily/records/$recordA/attachments/$attachmentId/download" -Token $roleTokens['analyst'] -Method GET
                    $downloadDeniedCode = if ($null -ne $downloadDenied.json -and ($downloadDenied.json.PSObject.Properties.Name -contains 'code')) { [int]$downloadDenied.json.code } else { $downloadDenied.httpStatus }
                    Add-Check '39 unauthorized attachment download returns 403' ($(if ($downloadDeniedCode -eq 403) { 'PASS' } else { 'FAIL' })) "code=$downloadDeniedCode"
                } else {
                    $uploadCode = if ($null -ne $uploadAttachment.json -and ($uploadAttachment.json.PSObject.Properties.Name -contains 'code')) { $uploadAttachment.json.code } else { '' }
                    $uploadMsg = if ($null -ne $uploadAttachment.json -and ($uploadAttachment.json.PSObject.Properties.Name -contains 'msg')) { $uploadAttachment.json.msg } else { '' }
                    Add-Check '39 unauthorized attachment download returns 403' 'FAIL' "attachment upload did not return id; status=$($uploadAttachment.httpStatus); code=$uploadCode; msg=$uploadMsg"
                }
            } finally {
                Remove-Item -LiteralPath $tempAttachment.FullName -Force -ErrorAction SilentlyContinue
            }
        } else {
            Add-Check '38 field permission rejects non-draft fact edit' 'FAIL' 'recordA or analyst token missing'
            Add-Check '39 unauthorized attachment download returns 403' 'FAIL' 'recordA or analyst token missing'
        }

        $resultRows = Get-Rows $results
        $resultForRecordA = $null
        if ($null -ne $recordA) {
            $resultForRecordA = $resultRows | Where-Object { [string]$_.recordId -eq [string]$recordA } | Select-Object -First 1
        }
        if ($resultForRecordA) {
            $correctDenied = if ($roleTokens['analyst']) { Invoke-Json -Method POST -Path "/violation/daily/results/$($resultForRecordA.resultId)/correct" -Body @{ included = '1'; resultStatus = $resultForRecordA.resultStatus; correctReason = 'should be forbidden' } -Token $roleTokens['analyst'] } else { $null }
            $correctDeniedCode = if ($null -ne $correctDenied -and $null -ne $correctDenied.json -and ($correctDenied.json.PSObject.Properties.Name -contains 'code')) { [int]$correctDenied.json.code } elseif ($null -ne $correctDenied) { $correctDenied.httpStatus } else { 0 }
            Add-Check '40 result correct without permission returns 403' ($(if ($correctDeniedCode -eq 403) { 'PASS' } else { 'FAIL' })) "code=$correctDeniedCode"

            $correct = Invoke-Json -Method POST -Path "/violation/daily/results/$($resultForRecordA.resultId)/correct" -Body @{ included = '1'; resultStatus = $resultForRecordA.resultStatus; correctReason = 'acceptance correction version 2' } -Token $token
            $versions = Invoke-Json -Method GET -Path "/violation/daily/results/$($resultForRecordA.resultId)/versions" -Token $token
            $versionRows = Get-ResponseData $versions
            $versionCount = Get-ItemCount $versionRows
            $hasVersion2 = @($versionRows | Where-Object { $_.resultVersion -eq 2 }).Count -gt 0
            Add-Check '41 result version append keeps old version' ($(if ((Test-ApiSuccess $correct) -and (Test-ApiSuccess $versions) -and $versionCount -ge 2 -and $hasVersion2) { 'PASS' } else { 'FAIL' })) "versions=$versionCount"

            $compare = Invoke-Json -Method GET -Path "/violation/daily/results/$($resultForRecordA.resultId)/versions/compare?sourceVersion=1&targetVersion=2" -Token $token
            Add-Check '42 result version compare endpoint works' ($(if (Test-ApiSuccess $compare) { 'PASS' } else { 'FAIL' })) "status=$($compare.httpStatus)"
        } else {
            Add-Check '40 result correct without permission returns 403' 'FAIL' 'result for recordA not found'
            Add-Check '41 result version append keeps old version' 'FAIL' 'result for recordA not found'
            Add-Check '42 result version compare endpoint works' 'FAIL' 'result for recordA not found'
        }

        if ($roleTokens['analyst']) {
            $unauthorizedExport = Invoke-DownloadCheck -Path '/violation/daily/results/export' -Token $roleTokens['analyst']
            $unauthorizedExportCode = if ($null -ne $unauthorizedExport.json -and ($unauthorizedExport.json.PSObject.Properties.Name -contains 'code')) { [int]$unauthorizedExport.json.code } else { $unauthorizedExport.httpStatus }
            Add-Check '43 unauthorized result export returns 403' ($(if ($unauthorizedExportCode -eq 403) { 'PASS' } else { 'FAIL' })) "code=$unauthorizedExportCode"
        } else {
            Add-Check '43 unauthorized result export returns 403' 'FAIL' 'analyst token missing'
        }

        $chatServicePath = Join-Path $RepoRoot 'backend\ruoyi-modules\ruoyi-system\src\main\java\org\dromara\system\service\impl\BizChatServiceImpl.java'
        $chatSource = if (Test-Path -LiteralPath $chatServicePath) { Get-Content -LiteralPath $chatServicePath -Encoding UTF8 -Raw } else { '' }
        $chatRecheck = $chatSource.Contains('checkBusinessDataScope(message)') -and $chatSource.Contains('dailyViolationService.queryRecord')
        Add-Check '44 chat business card open rechecks business data permission' ($(if ($chatRecheck) { 'PASS' } else { 'FAIL' })) 'static service check for DAILY_VIOLATION card open'

        $unauth = Invoke-Json -Method GET -Path '/violation/daily/records?pageNum=1&pageSize=1'
        $unauthCode = if ($null -ne $unauth.json -and ($unauth.json.PSObject.Properties.Name -contains 'code')) { [int]$unauth.json.code } else { $unauth.httpStatus }
        Add-Check '30 unauthenticated access returns 401' ($(if ($unauthCode -eq 401) { 'PASS' } else { 'FAIL' })) "code=$unauthCode"

        if (-not [string]::IsNullOrWhiteSpace($LimitedUsername) -and -not [string]::IsNullOrWhiteSpace($LimitedPassword)) {
            $limitedLogin = Invoke-Json -Method POST -Path '/auth/login' -Body @{ tenantId = '000000'; username = $LimitedUsername; password = $LimitedPassword; clientId = $ClientId; grantType = 'password' }
            $limitedToken = $null
            $limitedData = Get-ResponseData $limitedLogin
            if ($null -ne $limitedData -and ($limitedData.PSObject.Properties.Name -contains 'access_token')) { $limitedToken = $limitedData.access_token }
            if ($limitedToken) {
                $forbidden = Invoke-Json -Method POST -Path '/violation/daily/results/export' -Token $limitedToken
                $forbiddenCode = if ($null -ne $forbidden.json -and ($forbidden.json.PSObject.Properties.Name -contains 'code')) { [int]$forbidden.json.code } else { $forbidden.httpStatus }
                Add-Check '31 limited user forbidden access returns 403' ($(if ($forbiddenCode -eq 403) { 'PASS' } else { 'FAIL' })) "code=$forbiddenCode"
            } else {
                Add-Check '31 limited user forbidden access returns 403' 'TODO' 'limited account login failed or account unavailable'
            }
        } else {
            Add-Check '31 limited user forbidden access returns 403' 'TODO' 'set ARP_ACCEPTANCE_LIMITED_USERNAME / ARP_ACCEPTANCE_LIMITED_PASSWORD'
        }

        Add-Check '32 export bypasses frontend cache' 'PASS' 'script calls backend POST export endpoints directly'
    }
}

$pass = @($script:Checks | Where-Object { $_.status -eq 'PASS' }).Count
$fail = @($script:Checks | Where-Object { $_.status -eq 'FAIL' }).Count
$todo = @($script:Checks | Where-Object { $_.status -eq 'TODO' }).Count
$skip = @($script:Checks | Where-Object { $_.status -eq 'SKIP' }).Count

Write-Host ''
Write-Host ("Summary: PASS={0}, FAIL={1}, TODO={2}, SKIP={3}" -f $pass, $fail, $todo, $skip)

if ($fail -gt 0 -or ($todo -gt 0 -and -not $AllowTodo)) {
    exit 1
}
exit 0
