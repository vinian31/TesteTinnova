# API Gerenciamento de Veículos

## Instalação e Configuração

Para instalar os serviços necessários, veja o arquivo DEPLOYMENT.md

### 1. Clonar o Repositório (ou usar workspace existente)

```bash
cd /home/ian/IdeaProjects/teste
```

### 2. Atualizar Credenciais (application.properties)

Edite `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/veiculo_db
spring.datasource.username=veiculo
spring.datasource.password=veiculo123

spring.data.redis.host=localhost
spring.data.redis.port=6379
```

### 3. Construir o Projeto

```bash
./gradlew clean build
```

## 🏃 Executar a Aplicação

```bash
./gradlew bootRun
```

A aplicação estará disponível em: `http://localhost:8080/api`

### Swagger UI
```
http://localhost:8080/api/swagger-ui.html
```

## 🧪 Testes

### Executar Todos os Testes

```bash
./gradlew test
```

### Executar Testes Específicos

```bash
# Testes de Serviço
./gradlew test --tests VeiculoServiceTest

# Testes de Controller
./gradlew test --tests VeiculoControllerTest

# Testes de Repository
./gradlew test --tests VeiculoRepositoryTest

# Testes de Integração
./gradlew test --tests VeiculoIntegrationTest
```

### Cobertura de Testes

```bash
./gradlew test jacocoTestReport
```

## 🔐 Autenticação

### Usuários Criados Automaticamente

Na **primeira execução** da aplicação, dois usuários são criados automaticamente no banco de dados:

| Username | Password | Role | Permissões |
|----------|----------|------|------------|
| `admin` | `admin123` | ADMIN + USER | Acesso total (GET/POST/PUT/PATCH/DELETE) |
| `user` | `user123` | USER | Apenas leitura (GET) |

> **💡 Nota:** Os usuários são criados automaticamente pelo `DataInitializer.java` no startup da aplicação.

### Obter Token JWT

**Endpoint:** `POST /api/auth/login`

**Request (Admin):**
```json
{
  "username": "admin",
  "password": "admin123"
}
```

**Request (User):**
```json
{
  "username": "user",
  "password": "user123"
}
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzUxMiJ9...",
  "type": "Bearer",
  "expiresIn": 86400000
}
```

### Usar Token em Requisições

Adicione o header:
```
Authorization: Bearer <token>
```

## 🧩 Exemplos de Uso

### 1. Criar Veículo (ADMIN)

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

### 2. Listar Veículos com Filtros

```bash
# Listar todos
curl http://localhost:8080/api/veiculos?page=0&size=10

# Filtrar por marca e ano
curl "http://localhost:8080/api/veiculos?marca=Toyota&ano=2023"

# Filtrar por range de preço
curl "http://localhost:8080/api/veiculos?minPreco=30000&maxPreco=60000"

# Ordenação customizada
curl "http://localhost:8080/api/veiculos?sortBy=preco&direction=DESC"
```

### 3. Obter Detalhes de Veículo

```bash
curl http://localhost:8080/api/veiculos/1
```

### 4. Atualizar Veículo (ADMIN)

```bash
curl -X PUT http://localhost:8080/api/veiculos/1 \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "marca": "Honda",
    "modelo": "Civic",
    "ano": 2024,
    "placa": "XYZ-5678",
    "cor": "Branco",
    "preco": 55000.00
  }'
```

### 5. Atualizar Parcialmente (ADMIN)

```bash
curl -X PATCH http://localhost:8080/api/veiculos/1 \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "cor": "Azul"
  }'
```

### 6. Deletar Veículo (ADMIN)

```bash
curl -X DELETE http://localhost:8080/api/veiculos/1 \
  -H "Authorization: Bearer <token>"
```

### 7. Obter Relatório por Marca

```bash
curl http://localhost:8080/api/veiculos/relatorios/por-marca
```

## 📊 Modelos de Dados

### Veiculo

```json
{
  "id": 1,
  "marca": "Toyota",
  "modelo": "Corolla",
  "ano": 2023,
  "placa": "ABC-1234",
  "cor": "Preto",
  "preco": 50000.00,
  "ativo": true,
  "dataCriacao": "2024-01-15T10:30:00",
  "dataAtualizacao": "2024-01-15T10:30:00"
}
```

### ErrorResponse

```json
{
  "status": 409,
  "message": "Já existe um veículo com a placa: ABC-1234",
  "details": null,
  "timestamp": "2024-01-15T10:35:22",
  "path": "/api/veiculos"
}
```

### LoginResponse

```json
{
  "token": "eyJhbGciOiJIUzUxMiJ9...",
  "type": "Bearer",
  "expiresIn": 86400000
}
```

### RelatorioMarcaDTO

```json
[
  {
    "marca": "Toyota",
    "quantidade": 5
  },
  {
    "marca": "Honda",
    "quantidade": 3
  }
]
```

## 🔄 Fluxo de Integração Completo

1. **Autenticar** - Obter token via `/auth/login`
2. **Criar Veículo** - POST `/veiculos` com autenticação (ADMIN)
3. **Listar Veículos** - GET `/veiculos` sem autenticação
4. **Filtrar Veículos** - GET `/veiculos?marca={marca}` sem autenticação
5. **Obter Detalhes** - GET `/veiculos/{id}` sem autenticação
6. **Atualizar** - PUT `/veiculos/{id}` com autenticação (ADMIN)
7. **Atualizar Parcialmente** - PATCH `/veiculos/{id}` com autenticação (ADMIN)
8. **Deletar** - DELETE `/veiculos/{id}` com autenticação (ADMIN)
9. **Relatório** - GET `/veiculos/relatorios/por-marca` sem autenticação

### Filtros Combinados

Os filtros podem ser combinados:
```
GET /veiculos?marca=Toyota&ano=2023&minPreco=40000&maxPreco=60000&page=0&size=10&sortBy=preco&direction=DESC
```