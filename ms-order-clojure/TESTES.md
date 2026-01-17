# TESTES E VALIDAÇÃO

## Como testar a aplicação

### Pré-requisitos

1. **Instalar Clojure:**
   ```bash
   # Linux
   curl -O https://download.clojure.org/install/linux-install-1.11.1.1413.sh
   chmod +x linux-install-1.11.1.1413.sh
   sudo ./linux-install-1.11.1.1413.sh
   
   # macOS
   brew install clojure/tools/clojure
   
   # Windows
   # Baixe e instale: https://github.com/clojure/tools.deps.alpha/wiki/clj-on-Windows
   ```

2. **Verificar instalação:**
   ```bash
   clojure --version
   # Deve retornar: Clojure CLI version 1.11.1.1413
   ```

3. **Iniciar MongoDB:**
   ```bash
   cd ms-order-clojure
   docker-compose up -d mongodb
   ```

### Executar a aplicação

```bash
cd ms-order-clojure
clojure -M:run
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

### Testes manuais com curl

#### 1. Health Check
```bash
curl http://localhost:8082/health
```

**Resposta esperada:**
```json
{
  "status": "UP",
  "service": "ms-order-clojure",
  "timestamp": "2024-01-17T12:00:00.000Z"
}
```

#### 2. Criar um pedido (Laptop + Teclado)
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

**Resposta esperada:**
```json
{
  "id": "65a7f1234bcf86cd799439011",
  "products": {
    "1": 2,
    "3": 1
  },
  "price": 3075.0
}
```

**Cálculo:**
- Produto 1 (Laptop): 1500.0 × 2 = 3000.0
- Produto 3 (Teclado): 75.0 × 1 = 75.0
- **Total: 3075.0** ✓

#### 3. Listar todos os pedidos
```bash
curl http://localhost:8082/orders | jq
```

**Resposta esperada:**
```json
[
  {
    "id": "65a7f1234bcf86cd799439011",
    "products": {
      "1": 2,
      "3": 1
    },
    "price": 3075.0,
    "created-at": "2024-01-17T12:00:00.000Z"
  }
]
```

#### 4. Buscar pedido por ID
```bash
# Substitua ID_DO_PEDIDO pelo ID retornado no passo 2
curl http://localhost:8082/orders/65a7f1234bcf86cd799439011 | jq
```

#### 5. Buscar resumo detalhado
```bash
curl http://localhost:8082/orders/65a7f1234bcf86cd799439011/summary | jq
```

**Resposta esperada:**
```json
{
  "order": {
    "id": "65a7f1234bcf86cd799439011",
    "products": {
      "1": 2,
      "3": 1
    },
    "price": 3075.0,
    "created-at": "2024-01-17T12:00:00.000Z"
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

#### 6. Deletar pedido
```bash
curl -X DELETE http://localhost:8082/orders/65a7f1234bcf86cd799439011
```

**Resposta esperada:**
```json
{
  "success": true,
  "message": "Pedido deletado com sucesso"
}
```

### Verificar dados no MongoDB

```bash
# Acessar MongoDB via CLI
docker exec -it ms-order-mongodb mongosh

# Dentro do mongosh:
> use order_db
> db.orders.find().pretty()

# Ou usar Mongo Express (interface web)
# Abrir: http://localhost:8081
# Usuário: admin
# Senha: admin
```

## Testes de erro

### 1. Pedido inválido (produtos vazios)
```bash
curl -X POST http://localhost:8082/orders \
  -H "Content-Type: application/json" \
  -d '{"products": {}}'
```

**Resposta esperada (400 Bad Request):**
```json
{
  "error": "Request inválido para criar pedido",
  "type": "invalid-request"
}
```

### 2. Buscar pedido inexistente
```bash
curl http://localhost:8082/orders/000000000000000000000000
```

**Resposta esperada (404 Not Found):**
```json
{
  "error": "Pedido não encontrado",
  "type": "not-found",
  "id": "000000000000000000000000"
}
```

### 3. Deletar pedido inexistente
```bash
curl -X DELETE http://localhost:8082/orders/000000000000000000000000
```

**Resposta esperada (404 Not Found):**
```json
{
  "error": "Pedido não encontrado",
  "type": "not-found",
  "id": "000000000000000000000000"
}
```

## Testes automatizados (opcional)

Para criar testes automatizados em Clojure, você pode usar `clojure.test`:

### Exemplo de teste (test/ms_order/service/order_service_test.clj):

```clojure
(ns ms-order.service.order-service-test
  (:require [clojure.test :refer :all]
            [ms-order.service.order-service :as service]
            [ms-order.grpc.product-client-mock :as mock]))

(deftest test-calculate-total-price
  (testing "Calcula preço total corretamente"
    (let [products {1 2, 3 1}  ; 2 Laptops + 1 Teclado
          expected 3075.0]
      (is (= expected (mock/calculate-total-price products))))))

(deftest test-create-order
  (testing "Cria pedido com sucesso"
    (let [request {:products {1 2, 3 1}}
          result (service/create-order request)]
      (is (map? result))
      (is (contains? result :id))
      (is (= (:products result) {1 2, 3 1}))
      (is (= (:price result) 3075.0)))))

(deftest test-invalid-request
  (testing "Rejeita request inválido"
    (let [request {:products {}}]
      (is (thrown? Exception (service/create-order request))))))
```

### Executar testes:
```bash
clojure -M:test
```

## Checklist de validação

- [ ] MongoDB está rodando e acessível
- [ ] Aplicação inicia sem erros
- [ ] Endpoint /health retorna status UP
- [ ] POST /orders cria pedido com ID válido
- [ ] Preço é calculado corretamente
- [ ] GET /orders lista todos os pedidos
- [ ] GET /orders/:id busca pedido específico
- [ ] GET /orders/:id/summary retorna detalhes completos
- [ ] DELETE /orders/:id remove pedido
- [ ] Erros 400 para requisições inválidas
- [ ] Erros 404 para recursos não encontrados
- [ ] Dados persistem no MongoDB
- [ ] Logs aparecem no console corretamente

## Troubleshooting

### Erro: "Connection refused" ao conectar MongoDB
```bash
# Verificar se MongoDB está rodando
docker ps | grep mongodb

# Iniciar MongoDB se não estiver rodando
docker-compose up -d mongodb

# Verificar logs do MongoDB
docker logs ms-order-mongodb
```

### Erro: "Port 8082 already in use"
```bash
# Encontrar processo usando a porta
lsof -i :8082

# Matar o processo (substitua PID)
kill -9 <PID>

# Ou mudar a porta no config.edn
```

### Erro: "ClassNotFoundException" ou "NoClassDefFoundError"
```bash
# Limpar cache do Clojure
rm -rf .cpcache/

# Baixar dependências novamente
clojure -P

# Tentar executar novamente
clojure -M:run
```

### Erro ao parsear JSON
```bash
# Verificar Content-Type
curl -X POST http://localhost:8082/orders \
  -H "Content-Type: application/json" \  # Importante!
  -d '{"products": {"1": 2}}'

# Verificar sintaxe JSON
echo '{"products": {"1": 2}}' | jq  # Deve retornar JSON formatado
```

## Performance

### Testes de carga (opcional)

Usar `wrk` ou `ab` para testes de carga:

```bash
# Instalar wrk
sudo apt-get install wrk

# Teste de carga GET
wrk -t4 -c100 -d30s http://localhost:8082/health

# Teste de carga POST (criar script.lua)
wrk -t4 -c100 -d30s -s post.lua http://localhost:8082/orders
```

### Métricas esperadas

- **Health check**: < 5ms
- **GET /orders** (poucos registros): < 50ms
- **POST /orders**: < 100ms
- **GET /orders/:id**: < 20ms
- **DELETE /orders/:id**: < 50ms

---

## Conclusão

Se todos os testes passarem, a conversão do microserviço de Kotlin para Clojure foi bem-sucedida! 🎉

A aplicação agora:
- ✅ Usa Clojure nativo (sem Spring Boot)
- ✅ Conecta ao MongoDB (ao invés de MySQL)
- ✅ Implementa API REST completa
- ✅ Mock do gRPC funcional
- ✅ Código funcional e idiomático em Clojure
