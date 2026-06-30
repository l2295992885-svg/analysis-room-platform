# Backend

Spring Boot backend scaffold for the Analysis Room Platform.

## Database

The first-stage backend uses MySQL 8.x and Flyway migrations.

Default development connection:

```text
url: jdbc:mysql://127.0.0.1:3306/analysis_room_platform_dev
username: root
password: <set APP_DATASOURCE_PASSWORD locally>
```

Override with environment variables:

```powershell
$env:APP_DATASOURCE_URL="jdbc:mysql://127.0.0.1:3306/analysis_room_platform_dev?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true&createDatabaseIfNotExist=true"
$env:APP_DATASOURCE_USERNAME="root"
$env:APP_DATASOURCE_PASSWORD="<local development password>"
```

Flyway runs automatically on application startup when `APP_FLYWAY_ENABLED` is `true`.

Development administrator account:

```text
username: admin
password: Admin@123456
```

This password is only for local development and must be changed before any shared or production environment is used.

## Run

```powershell
mvn spring-boot:run
```

If port `8080` is in use:

```powershell
mvn spring-boot:run "-Dspring-boot.run.arguments=--server.port=18080"
```

To start without running Flyway, useful when MySQL is not available:

```powershell
mvn spring-boot:run "-Dspring-boot.run.arguments=--server.port=18080 --spring.flyway.enabled=false"
```

To enable local-only write guard verification endpoints:

```powershell
mvn spring-boot:run "-Dspring-boot.run.arguments=--server.port=18080 --spring.profiles.active=dev"
```

## Verify

Health check:

```powershell
Invoke-RestMethod -Uri http://localhost:18080/api/health
```

Login:

```powershell
$login = Invoke-RestMethod `
  -Method Post `
  -Uri http://localhost:18080/api/auth/login `
  -ContentType "application/json" `
  -Body '{"username":"admin","password":"Admin@123456"}'

$token = $login.data.token
```

Authenticated APIs:

```powershell
Invoke-RestMethod -Uri http://localhost:18080/api/auth/profile -Headers @{ Authorization = "Bearer $token" }
Invoke-RestMethod -Uri http://localhost:18080/api/auth/menus -Headers @{ Authorization = "Bearer $token" }
Invoke-RestMethod -Uri http://localhost:18080/api/auth/permissions -Headers @{ Authorization = "Bearer $token" }
```

System management read-only APIs:

```powershell
$headers = @{ Authorization = "Bearer $token" }

Invoke-RestMethod -Uri "http://localhost:18080/api/system/users?pageNo=1&pageSize=20" -Headers $headers
Invoke-RestMethod -Uri http://localhost:18080/api/system/users/1 -Headers $headers
Invoke-RestMethod -Uri "http://localhost:18080/api/system/roles?pageNo=1&pageSize=20" -Headers $headers
Invoke-RestMethod -Uri http://localhost:18080/api/system/roles/1 -Headers $headers
Invoke-RestMethod -Uri http://localhost:18080/api/system/depts/tree -Headers $headers
Invoke-RestMethod -Uri http://localhost:18080/api/system/menus/tree -Headers $headers
Invoke-RestMethod -Uri http://localhost:18080/api/system/permissions -Headers $headers
```

The first implementation only allows users with the `SUPER_ADMIN` role to access `/api/system/**`.

Local-only write guard and operation log verification, available only with `dev` or `test` profile:

```powershell
$headers = @{ Authorization = "Bearer $token" }
$body = @{
  username = "tester"
  password = "plain-password"
  token = "plain-token"
  Authorization = "Bearer plain-token"
  nested = @{ newPassword = "new-plain-password"; credential = "plain-credential" }
} | ConvertTo-Json -Depth 5

Invoke-RestMethod `
  -Method Post `
  -Uri http://localhost:18080/api/dev/write-guard/success `
  -Headers $headers `
  -ContentType "application/json" `
  -Body $body

Invoke-WebRequest `
  -UseBasicParsing `
  -Method Post `
  -Uri http://localhost:18080/api/dev/write-guard/fail `
  -Headers $headers `
  -ContentType "application/json" `
  -Body $body
```

The second request intentionally returns `422` and writes a `FAILED` operation log.

Unauthorized write guard check:

```powershell
Invoke-WebRequest `
  -UseBasicParsing `
  -Method Post `
  -Uri http://localhost:18080/api/dev/write-guard/success `
  -ContentType "application/json" `
  -Body '{}'
```

Verify Flyway and system tables in MySQL:

```sql
SHOW TABLES LIKE 'sys_%';
SELECT version, description, success FROM flyway_schema_history ORDER BY installed_rank;
SELECT id, username, status FROM sys_user;
SELECT id, role_key, status FROM sys_role;
SELECT module_title, business_type, operation_status, request_params, response_body
FROM sys_operation_log
ORDER BY id DESC
LIMIT 5;
SELECT COUNT(*) AS sensitive_log_rows
FROM sys_operation_log
WHERE COALESCE(request_params, '') LIKE '%plain-password%'
   OR COALESCE(request_params, '') LIKE '%plain-token%'
   OR COALESCE(request_params, '') LIKE '%Authorization%'
   OR COALESCE(response_body, '') LIKE '%plain-password%'
   OR COALESCE(response_body, '') LIKE '%plain-token%'
   OR COALESCE(response_body, '') LIKE '%Authorization%';
```
