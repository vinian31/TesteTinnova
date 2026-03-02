# 🚀 Guia de Deployment - API Gerenciamento de Veículos

## 1 Configurar Banco de Dados PostgreSQL

### Linux/Mac

```bash
# Instalar PostgreSQL (se não tiver)
# macOS com Homebrew:
brew install postgresql@14

# Ubuntu/Debian:
sudo apt-get install postgresql postgresql-contrib

# Iniciar o serviço
sudo service postgresql start

# Criar banco de dados
sudo -u postgres createdb veiculo_db

# Criar usuário (opcional)
sudo -u postgres psql
CREATE USER veiculo WITH PASSWORD 'veiculo123';
ALTER ROLE veiculo WITH CREATEDB;
GRANT ALL PRIVILEGES ON DATABASE veiculo_db TO veiculo;
\q
```

### Windows

```bash
# Instalar PostgreSQL usando o instalador
# Link: https://www.postgresql.org/download/windows/

# Após instalação, abrir pgAdmin 4 ou usar psql
# Criar banco:
CREATE DATABASE veiculo_db;
CREATE USER veiculo WITH PASSWORD 'veiculo123';
ALTER ROLE veiculo WITH CREATEDB;
GRANT ALL PRIVILEGES ON DATABASE veiculo_db TO veiculo;
```

### Docker

```bash
docker run -d \
  --name postgres-veiculo \
  -e POSTGRES_DB=veiculo_db \
  -e POSTGRES_USER=veiculo \
  -e POSTGRES_PASSWORD=veiculo123 \
  -p 5432:5432 \
  postgres:14-alpine
```

## 2 Configurar Redis

### Linux/Mac

```bash
# macOS com Homebrew:
brew install redis
brew services start redis

# Ubuntu/Debian:
sudo apt-get install redis-server
sudo service redis-server start
```

### Windows

```bash
# Download: https://github.com/microsoftarchive/redis/releases
# Ou usar WSL2 com instrução Linux
```

### Docker

```bash
docker run -d \
  --name redis-veiculo \
  -p 6379:6379 \
  redis:7-alpine
```

## 3 Configurar a Aplicação

### Editar application.properties

```bash
# Arquivo: src/main/resources/application.properties

# Banco de Dados
spring.datasource.url=jdbc:postgresql://localhost:5432/veiculo_db
spring.datasource.username=veiculo
spring.datasource.password=veiculo123

# Redis
spring.data.redis.host=localhost
spring.data.redis.port=6379

# JWT
app.jwt.secret=MyVerySecureJWTSecretKeyForTinnovaAPITokenGeneration2026WithMinimum256Bits!@#$%
app.jwt.expiration=86400000

# Porta da aplicação (opcional)
server.port=8080
```

## 4 Construir o Projeto

```bash
cd /home/ian/IdeaProjects/teste

# Build completo
./gradlew clean build

# Build sem testes (mais rápido)
./gradlew clean build -x test
```

## 5 Executar a Aplicação

### Opção 1: Via Gradle

```bash
./gradlew bootRun
```

### Opção 2: Via JAR construído

```bash
java -jar build/libs/teste-0.0.1-SNAPSHOT.jar
```

### Opção 3: Via IDE

- Abrir a classe `TesteApplication.java`
- Clicar em "Run" ou `Shift+F10`

## 6 Testar a API

### Acessar Swagger UI

```
http://localhost:8080/api/swagger-ui.html
```

### Obter Token (via cURL)

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "admin123"
  }'
```

### Resposta Esperada

```json
{
  "token": "eyJhbGciOiJIUzUxMiJ9...",
  "type": "Bearer",
  "expiresIn": 86400000
}
```

### Criar Veículo com Token

```bash
curl -X POST http://localhost:8080/api/veiculos \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "marca": "Toyota",
    "modelo": "Corolla",
    "ano": 2023,
    "placa": "ABC-1234",
    "cor": "Preto",
    "preco": 50000.00
  }'
```

### Listar Veículos

```bash
curl http://localhost:8080/api/veiculos
```

## Usuários de Teste

### Admin
- Username: `admin`
- Password: `admin123`
- Role: `ADMIN`

### Usuário Regular
- Username: `user`
- Password: `user123`
- Role: `USER`

> **Nota**: Esses usuários são criados automaticamente no primeiro acesso. Verifique `VeiculoIntegrationTest.java` para o script de inicialização.

## Rodar Testes

```bash
# Todos os testes
./gradlew test

# Teste específico
./gradlew test --tests VeiculoServiceTest

# Sem executar testes
./gradlew build -x test
```

## Logs

### Ver logs em tempo real

```bash
# Se executando via Gradle
./gradlew bootRun

# Se executando via JAR, redirecionar para arquivo
java -jar build/libs/teste-0.0.1-SNAPSHOT.jar > app.log 2>&1 &
```

### Nível de logs em application.properties

```properties
logging.level.root=INFO
logging.level.org.tinnova.teste=DEBUG
logging.level.org.springframework.security=DEBUG
```