# TUTORIAL PASSO A PASSO

## 🚀 Como executar o ms-order-clojure

Este guia irá te ajudar a executar o microserviço Clojure do zero.

---

## Pré-requisitos

### 1. Instalar Java (JDK 11+)

```bash
# Verificar se Java está instalado
java -version

# Se não estiver instalado:
# Ubuntu/Debian
sudo apt-get update
sudo apt-get install openjdk-17-jdk

# macOS
brew install openjdk@17

# Windows
# Baixe de: https://adoptium.net/
```

### 2. Instalar Clojure

#### Linux
```bash
curl -O https://download.clojure.org/install/linux-install-1.11.1.1413.sh
chmod +x linux-install-1.11.1.1413.sh
sudo ./linux-install-1.11.1.1413.sh

# Verificar instalação
clojure --version
```

#### macOS
```bash
brew install clojure/tools/clojure

# Verificar instalação
clojure --version
```

#### Windows
```bash
# Usar PowerShell como Administrador
iwr -useb download.clojure.org/install/win-install-1.11.1.1413.ps1 | iex

# Verificar instalação
clojure --version
```

### 3. Instalar Docker (para MongoDB)

```bash
# Linux
curl -fsSL https://get.docker.com -o get-docker.sh
sudo sh get-docker.sh

# macOS
brew install --cask docker

# Windows
# Baixe Docker Desktop: https://www.docker.com/products/docker-desktop
```

---

## 📦 Passo 1: Clonar ou navegar para o projeto

```bash
# Se ainda não tem o repositório
git clone https://github.com/Tiramelo17/kotlin-grpc-api-microservices.git
cd kotlin-grpc-api-microservices/ms-order-clojure

# Ou se já tem
cd ms-order-clojure
```

---

## 🗄️ Passo 2: Iniciar MongoDB

```bash
# Iniciar MongoDB com Docker Compose
docker-compose up -d mongodb

# Verificar se está rodando
docker ps

# Deve mostrar algo como:
# CONTAINER ID   IMAGE        STATUS         PORTS
# abc123...      mongo:7.0    Up 10 seconds  0.0.0.0:27017->27017/tcp
```

**Troubleshooting:**
```bash
# Se der erro de porta ocupada
sudo lsof -i :27017
# Mate o processo ou mude a porta no docker-compose.yml

# Ver logs do MongoDB
docker logs ms-order-mongodb

# Parar e remover containers
docker-compose down
```

---

## 🔧 Passo 3: Baixar dependências

```bash
# O Clojure vai baixar as dependências na primeira execução
# Mas você pode forçar o download antecipado:
clojure -P

# Isso baixa todas as dependências definidas em deps.edn
# Pode demorar alguns minutos na primeira vez
```

**O que está sendo baixado:**
- Clojure core libraries
- MongoDB Java Driver
- Ring (servidor HTTP)
- Compojure (roteamento)
- Cheshire (JSON)
- E suas dependências transitivas

**Cache:**
- As dependências ficam em `~/.m2/repository/`
- Não precisa baixar novamente em próximas execuções

---

## ▶️ Passo 4: Executar a aplicação

```bash
# Método 1: Usando o alias definido em deps.edn
clojure -M:run

# Método 2: Direto
clojure -M -m ms-order.core
```

**Saída esperada:**
```
╔════════════════════════════════════════╗
║   MS-ORDER-CLOJURE                     ║
║   Microserviço de Pedidos em Clojure   ║
╚════════════════════════════════════════╝

📋 Configurações carregadas:
   App: ms-order-clojure
   MongoDB URI: mongodb://localhost:27017
   MongoDB Database: order_db

✓ Conectado ao MongoDB: mongodb://localhost:27017 / order_db
========================================
🚀 Iniciando servidor HTTP...
   Porta: 8082
========================================
✓ Servidor HTTP iniciado!

📍 Endpoints disponíveis:
   GET    http://localhost:8082/health
   GET    http://localhost:8082/orders
   POST   http://localhost:8082/orders
   GET    http://localhost:8082/orders/:id
   GET    http://localhost:8082/orders/:id/summary
   DELETE http://localhost:8082/orders/:id

========================================
✅ Aplicação iniciada com sucesso!
```

**Parar a aplicação:**
- Pressione `Ctrl+C`

---

## 🧪 Passo 5: Testar os endpoints

### 5.1 Health Check (verificar se está funcionando)

```bash
curl http://localhost:8082/health
```

**Resposta esperada:**
```json
{
  "status": "UP",
  "service": "ms-order-clojure",
  "timestamp": "2024-01-17T15:30:00.000Z"
}
```

✅ **Se você viu isso, está funcionando!**

---

### 5.2 Criar seu primeiro pedido

```bash
curl -X POST http://localhost:8082/orders \
  -H "Content-Type: application/json" \
  -d '{
    "products": {
      "1": 2,
      "3": 1
    }
  }'
```

**O que significa:**
- `"1": 2` → 2 unidades do produto 1 (Laptop - R$ 1500 cada)
- `"3": 1` → 1 unidade do produto 3 (Teclado - R$ 75)

**Resposta esperada:**
```json
{
  "id": "65a7f1f8bcf86cd799439011",
  "products": {
    "1": 2,
    "3": 1
  },
  "price": 3075.0
}
```

✅ **Cálculo:** (1500 × 2) + (75 × 1) = **3075.0**

**IMPORTANTE:** Copie o `id` retornado! Vamos usar nos próximos testes.

---

### 5.3 Listar todos os pedidos

```bash
curl http://localhost:8082/orders | jq
```

**Resposta esperada:**
```json
[
  {
    "id": "65a7f1f8bcf86cd799439011",
    "products": {
      "1": 2,
      "3": 1
    },
    "price": 3075.0,
    "created-at": "2024-01-17T15:32:00.000Z"
  }
]
```

**Nota:** `jq` formata o JSON. Se não tiver instalado:
```bash
# Linux
sudo apt-get install jq

# macOS
brew install jq

# Ou simplesmente omita o | jq
curl http://localhost:8082/orders
```

---

### 5.4 Buscar pedido específico por ID

```bash
# Substitua SEU_ID_AQUI pelo ID copiado no passo 5.2
curl http://localhost:8082/orders/65a7f1f8bcf86cd799439011 | jq
```

**Resposta esperada:**
```json
{
  "id": "65a7f1f8bcf86cd799439011",
  "products": {
    "1": 2,
    "3": 1
  },
  "price": 3075.0,
  "created-at": "2024-01-17T15:32:00.000Z"
}
```

---

### 5.5 Buscar resumo detalhado do pedido

```bash
curl http://localhost:8082/orders/65a7f1f8bcf86cd799439011/summary | jq
```

**Resposta esperada:**
```json
{
  "order": {
    "id": "65a7f1f8bcf86cd799439011",
    "products": {
      "1": 2,
      "3": 1
    },
    "price": 3075.0,
    "created-at": "2024-01-17T15:32:00.000Z"
  },
  "products-details": [
    {
      "product": {
        "id": 1,
        "name": "Laptop",
        "price": 1500.0,
        "stock": 10
      },
      "quantity": 2
    },
    {
      "product": {
        "id": 3,
        "name": "Teclado",
        "price": 75.0,
        "stock": 30
      },
      "quantity": 1
    }
  ],
  "summary": {
    "total-items": 3,
    "total-price": 3075.0
  }
}
```

✅ **Aqui você vê os detalhes completos dos produtos!**

---

### 5.6 Criar mais pedidos para testar

**Pedido 2: Mouse barato**
```bash
curl -X POST http://localhost:8082/orders \
  -H "Content-Type: application/json" \
  -d '{"products": {"2": 5}}'
```
Resultado: 5 × 25 = **125.0**

**Pedido 3: Setup completo**
```bash
curl -X POST http://localhost:8082/orders \
  -H "Content-Type: application/json" \
  -d '{"products": {"1": 1, "4": 2, "5": 1}}'
```
Resultado: (1500 × 1) + (300 × 2) + (100 × 1) = **2200.0**

---

### 5.7 Deletar um pedido

```bash
curl -X DELETE http://localhost:8082/orders/65a7f1f8bcf86cd799439011
```

**Resposta esperada:**
```json
{
  "success": true,
  "message": "Pedido deletado com sucesso"
}
```

**Verificar se foi deletado:**
```bash
curl http://localhost:8082/orders
# O pedido não deve aparecer mais na lista
```

---

## 🔍 Passo 6: Inspecionar o MongoDB

### Via MongoDB Shell (CLI)

```bash
# Acessar o MongoDB container
docker exec -it ms-order-mongodb mongosh

# Dentro do mongosh:
test> use order_db
order_db> db.orders.find().pretty()

# Ver quantos pedidos existem
order_db> db.orders.countDocuments()

# Buscar pedidos com preço > 1000
order_db> db.orders.find({ price: { $gt: 1000 } }).pretty()

# Sair
order_db> exit
```

### Via Mongo Express (Interface Web)

```bash
# Iniciar Mongo Express (se ainda não iniciou)
docker-compose up -d mongo-express

# Abrir no navegador:
# http://localhost:8081

# Credenciais:
# Username: admin
# Password: admin

# Navegar: order_db → orders
```

---

## 🧑‍💻 Passo 7: Desenvolvimento Interativo (REPL)

Uma das grandes vantagens do Clojure é o **REPL** (Read-Eval-Print Loop).

```bash
# Iniciar REPL
clojure

# No REPL:
user=> (require '[ms-order.core :as core])
user=> (core/init!)  ; Inicia a aplicação

# Testar funções diretamente:
user=> (require '[ms-order.grpc.product-client-mock :as mock])
user=> (mock/get-product 1)
;; => {:id 1, :name "Laptop", :price 1500.0, :stock 10}

user=> (mock/calculate-total-price {1 2, 3 1})
;; => 3075.0

# Parar aplicação:
user=> (core/shutdown!)
user=> (System/exit 0)
```

---

## 📝 Passo 8: Modificar o código e testar

### Exemplo: Adicionar novo produto mockado

1. **Abrir arquivo:**
   ```bash
   nano src/ms_order/grpc/product_client_mock.clj
   # Ou use seu editor favorito
   ```

2. **Adicionar novo produto:**
   ```clojure
   (def mock-products
     {1 {:id 1 :name "Laptop" :price 1500.0 :stock 10}
      2 {:id 2 :name "Mouse" :price 25.0 :stock 50}
      3 {:id 3 :name "Teclado" :price 75.0 :stock 30}
      4 {:id 4 :name "Monitor" :price 300.0 :stock 15}
      5 {:id 5 :name "Headset" :price 100.0 :stock 25}
      6 {:id 6 :name "Webcam" :price 200.0 :stock 20}})  ; NOVO!
   ```

3. **Reiniciar aplicação:**
   ```bash
   # Ctrl+C para parar
   # clojure -M:run para iniciar novamente
   ```

4. **Testar novo produto:**
   ```bash
   curl -X POST http://localhost:8082/orders \
     -H "Content-Type: application/json" \
     -d '{"products": {"6": 2}}'
   ```

✅ **Resultado esperado:** price = 400.0 (200 × 2)

---

## 🎯 Exercícios Práticos

### Exercício 1: Criar pedido grande
Crie um pedido com todos os 5 produtos (1 unidade de cada).

<details>
<summary>Solução</summary>

```bash
curl -X POST http://localhost:8082/orders \
  -H "Content-Type: application/json" \
  -d '{"products": {"1": 1, "2": 1, "3": 1, "4": 1, "5": 1}}'
```

**Preço esperado:** 1500 + 25 + 75 + 300 + 100 = **2000.0**
</details>

### Exercício 2: Testar erro
Tente criar um pedido sem produtos.

<details>
<summary>Solução</summary>

```bash
curl -X POST http://localhost:8082/orders \
  -H "Content-Type: application/json" \
  -d '{"products": {}}'
```

**Resposta esperada:** 400 Bad Request com mensagem de erro
</details>

### Exercício 3: Verificar MongoDB
Use mongosh ou Mongo Express para ver todos os pedidos criados.

---

## 🐛 Troubleshooting

### Problema: "Connection refused" ao acessar MongoDB
```bash
# Verificar se MongoDB está rodando
docker ps | grep mongodb

# Se não estiver, iniciar:
docker-compose up -d mongodb

# Ver logs:
docker logs ms-order-mongodb
```

### Problema: "Port 8082 already in use"
```bash
# Encontrar processo usando a porta
lsof -i :8082

# Matar processo (substitua PID)
kill -9 <PID>

# Ou mudar porta em resources/config.edn
```

### Problema: Erro ao iniciar Clojure
```bash
# Limpar cache
rm -rf .cpcache/

# Baixar dependências novamente
clojure -P

# Tentar novamente
clojure -M:run
```

### Problema: JSON malformado
```bash
# Validar JSON antes de enviar
echo '{"products": {"1": 2}}' | jq

# Se jq retornar erro, corrigir o JSON
```

---

## ✅ Checklist Final

- [ ] Java instalado (java -version)
- [ ] Clojure instalado (clojure --version)
- [ ] Docker instalado e rodando
- [ ] MongoDB iniciado (docker ps)
- [ ] Dependências baixadas (clojure -P)
- [ ] Aplicação iniciada sem erros
- [ ] Health check retorna 200
- [ ] POST /orders cria pedido
- [ ] GET /orders lista pedidos
- [ ] GET /orders/:id busca pedido
- [ ] GET /orders/:id/summary retorna detalhes
- [ ] DELETE /orders/:id remove pedido
- [ ] Dados aparecem no MongoDB

---

## 🎉 Parabéns!

Se você chegou até aqui e todos os testes passaram, você:

1. ✅ Instalou e configurou ambiente Clojure
2. ✅ Executou um microserviço Clojure real
3. ✅ Testou API REST completa
4. ✅ Interagiu com MongoDB
5. ✅ Entendeu o fluxo de uma aplicação Clojure

**Próximos passos:**
- Explore o código em `src/ms_order/`
- Leia `GUIA_COMPLETO.md` para entender cada conceito
- Modifique o código e veja o resultado
- Compare com o código Kotlin original
- Experimente criar seus próprios endpoints!

---

**Dúvidas ou problemas?** Revise os logs da aplicação e do MongoDB! 🚀
