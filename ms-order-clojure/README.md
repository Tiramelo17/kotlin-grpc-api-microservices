# MS-ORDER-CLOJURE

Microserviço de pedidos (orders) implementado em **Clojure nativo**, convertido do projeto original em Kotlin/Spring Boot.

## 📚 Sobre o Projeto

Este projeto é uma conversão do microserviço `ms-order` de Kotlin para Clojure, mantendo a mesma funcionalidade mas usando o ecossistema nativo do Clojure.

### Tecnologias Utilizadas

- **Clojure 1.11.1**: Linguagem funcional baseada na JVM
- **MongoDB**: Banco de dados NoSQL (substituindo MySQL/JPA)
- **Ring**: Biblioteca base para servidores HTTP
- **Compojure**: Roteamento HTTP
- **Cheshire**: Serialização JSON
- **Jetty**: Servidor HTTP embutido

## 🏗️ Arquitetura

O projeto segue uma arquitetura em camadas:

```
src/ms_order/
├── core.clj                    # Ponto de entrada, inicialização
├── models/
│   ├── order.clj              # Modelo de dados Order
│   └── schemas.clj            # Schemas de Request/Response
├── repository/
│   └── order_repository.clj   # Operações com MongoDB
├── service/
│   └── order_service.clj      # Lógica de negócio
├── controller/
│   └── order_controller.clj   # Handlers HTTP e rotas
└── grpc/
    └── product_client_mock.clj # Mock do cliente gRPC
```

## 🎓 Conceitos de Clojure Explicados

### 1. **Namespaces** (similar a packages em Java/Kotlin)

```clojure
(ns ms-order.core
  (:require [ring.adapter.jetty :as jetty]))
```

- `ns` declara um namespace (como `package` em Java)
- `:require` importa outros namespaces (como `import`)
- `:as` cria um alias (como `import ... as ...` em Python)

### 2. **Funções**

```clojure
(defn create-order [products price]
  {:products products
   :price price})
```

- `defn` define uma função
- Parâmetros entre colchetes `[]`
- Última expressão é o retorno (sem `return`)

### 3. **Mapas** (Hash-maps)

```clojure
{:id 1
 :products {1 2, 3 1}
 :price 150.0}
```

- Estrutura de dados principal em Clojure
- Keywords começam com `:` (como symbols)
- Imutáveis por padrão

### 4. **Threading Macros** (`->` e `->>`)

```clojure
(-> order
    (update :id str)
    (assoc :price 100.0))
```

- `->` (thread-first): passa o resultado como primeiro argumento
- Similar a method chaining em OO
- Torna código mais legível

### 5. **Atoms** (Estado mutável thread-safe)

```clojure
(defonce db-connection (atom nil))
(reset! db-connection new-value)
@db-connection  ;; dereferencia
```

- `atom`: referência mutável com atualizações atômicas
- `reset!`: define novo valor
- `@` (deref): lê o valor

### 6. **Destructuring**

```clojure
(let [{:keys [id products price]} order]
  (println id products price))
```

- Extrai valores de mapas diretamente
- `:keys` extrai valores usando keywords

## 🚀 Como Executar

### Pré-requisitos

1. **Clojure CLI Tools** instalado
   ```bash
   # macOS
   brew install clojure
   
   # Linux
   curl -O https://download.clojure.org/install/linux-install-1.11.1.1413.sh
   chmod +x linux-install-1.11.1.1413.sh
   sudo ./linux-install-1.11.1.1413.sh
   ```

2. **MongoDB** rodando
   ```bash
   # Docker
   docker run -d -p 27017:27017 --name mongodb mongo:latest
   
   # Ou MongoDB instalado localmente
   mongod --dbpath /path/to/data
   ```

### Executar a aplicação

```bash
cd ms-order-clojure

# Método 1: Usando alias do deps.edn
clojure -M:run

# Método 2: Direto
clojure -M -m ms-order.core
```

A aplicação estará disponível em: `http://localhost:8082`

## 📡 API Endpoints

### 1. Health Check
```bash
GET /health
```

**Resposta:**
```json
{
  "status": "UP",
  "service": "ms-order-clojure",
  "timestamp": "2024-01-17T12:00:00Z"
}
```

### 2. Criar Pedido
```bash
POST /orders
Content-Type: application/json

{
  "products": {
    "1": 2,
    "3": 1
  }
}
```

**Resposta:**
```json
{
  "id": "507f1f77bcf86cd799439011",
  "products": {
    "1": 2,
    "3": 1
  },
  "price": 3075.0
}
```

### 3. Buscar Todos os Pedidos
```bash
GET /orders
```

### 4. Buscar Pedido por ID
```bash
GET /orders/{id}
```

### 5. Buscar Resumo do Pedido
```bash
GET /orders/{id}/summary
```

**Resposta:**
```json
{
  "order": {
    "id": "507f1f77bcf86cd799439011",
    "products": {"1": 2, "3": 1},
    "price": 3075.0
  },
  "products-details": [
    {
      "product": {"id": 1, "name": "Laptop", "price": 1500.0},
      "quantity": 2
    },
    {
      "product": {"id": 3, "name": "Teclado", "price": 75.0},
      "quantity": 1
    }
  ],
  "summary": {
    "total-items": 3,
    "total-price": 3075.0
  }
}
```

### 6. Deletar Pedido
```bash
DELETE /orders/{id}
```

## 🔧 Exemplos de Uso (curl)

```bash
# 1. Verificar health
curl http://localhost:8082/health

# 2. Criar um pedido
curl -X POST http://localhost:8082/orders \
  -H "Content-Type: application/json" \
  -d '{"products": {"1": 2, "2": 1}}'

# 3. Listar todos os pedidos
curl http://localhost:8082/orders

# 4. Buscar pedido específico (use o ID retornado no passo 2)
curl http://localhost:8082/orders/507f1f77bcf86cd799439011

# 5. Buscar resumo completo
curl http://localhost:8082/orders/507f1f77bcf86cd799439011/summary

# 6. Deletar pedido
curl -X DELETE http://localhost:8082/orders/507f1f77bcf86cd799439011
```

## 📦 Produtos Mockados

O serviço usa produtos mockados (simulados) já que a conexão gRPC real foi temporariamente substituída:

| ID | Nome    | Preço    | Estoque |
|----|---------|----------|---------|
| 1  | Laptop  | 1500.0   | 10      |
| 2  | Mouse   | 25.0     | 50      |
| 3  | Teclado | 75.0     | 30      |
| 4  | Monitor | 300.0    | 15      |
| 5  | Headset | 100.0    | 25      |

## 🎯 Comparação: Kotlin vs Clojure

### Kotlin (Spring Boot + JPA)
```kotlin
@Service
class OrderService {
    @Autowired
    private lateinit var orderRepository: OrderRepository
    
    fun createNewOrder(request: CreateOrderRequest): CreateOrderResponse {
        return Order()
            .apply {
                this.products = request.products
                this.price = calcTotalPrice(request.products)
            }
            .let { orderRepository.save(it) }
            .let { mapper.toCreateOrderResponse(it) }
    }
}
```

### Clojure (Funcional Puro)
```clojure
(defn create-order [request]
  (let [products (:products request)
        total-price (product-client/calculate-total-price products)
        new-order (order-model/create-order products total-price)
        saved-order (repo/save-order new-order)]
    (schemas/create-order-response saved-order)))
```

**Diferenças principais:**
- ✅ Clojure: Sem anotações, sem injeção de dependências implícita
- ✅ Clojure: Funções puras, mais fácil de testar
- ✅ Clojure: Imutabilidade por padrão
- ✅ Clojure: Menos boilerplate, mais conciso
- ✅ Clojure: Threading macros (`let`, `->`) para composição

## 🔍 Estrutura de Dados

### Order (Pedido)
```clojure
{:id #object[org.bson.types.ObjectId "507f1f77bcf86cd799439011"]
 :products {1 2, 3 1}  ;; {product-id quantidade}
 :price 3075.0
 :created-at #inst "2024-01-17T12:00:00.000Z"}
```

### MongoDB Document
```json
{
  "_id": ObjectId("507f1f77bcf86cd799439011"),
  "products": {"1": 2, "3": 1},
  "price": 3075.0,
  "created-at": ISODate("2024-01-17T12:00:00.000Z")
}
```

## 🧪 Desenvolvimento no REPL

Clojure tem um REPL (Read-Eval-Print Loop) poderoso para desenvolvimento interativo:

```bash
# Iniciar REPL
clojure

# No REPL:
user=> (require '[ms-order.core :as core])
user=> (core/init!)  ; Inicia a aplicação
user=> (core/shutdown!)  ; Para a aplicação
```

## 📝 Próximos Passos

Para evoluir este projeto:

1. **Adicionar testes**
   - Usar `clojure.test` para testes unitários
   - Testar funções puras facilmente

2. **Implementar cliente gRPC real**
   - Usar biblioteca como `protojure` ou `gRPC-clojure`
   - Substituir o mock atual

3. **Adicionar validação de schemas**
   - Usar `clojure.spec` para validação formal
   - Gerar documentação automática

4. **Configuração avançada**
   - Usar `aero` para múltiplos ambientes
   - Externalize configurações sensíveis

5. **Observabilidade**
   - Adicionar logging estruturado
   - Métricas com Prometheus
   - Tracing distribuído

## 🤝 Diferenças do Original

| Aspecto | Kotlin (Original) | Clojure (Este) |
|---------|-------------------|----------------|
| Framework | Spring Boot | Ring + Compojure |
| Banco | MySQL + JPA | MongoDB + Driver nativo |
| ORM | Hibernate | Sem ORM (queries diretas) |
| Estilo | OOP + Funcional | Funcional puro |
| Config | application.yml | config.edn |
| DI | Spring @Autowired | Passagem explícita |
| gRPC | Cliente real | Mock (temporário) |

## 📚 Recursos para Aprender Clojure

- [Clojure.org](https://clojure.org/) - Site oficial
- [Clojure for the Brave and True](https://www.braveclojure.com/) - Livro gratuito
- [ClojureDocs](https://clojuredocs.org/) - Documentação com exemplos
- [4Clojure](http://www.4clojure.com/) - Exercícios práticos

## 📄 Licença

Este projeto segue a mesma licença do projeto original.

---

**Nota**: Este é um projeto educacional para demonstrar a conversão de um microserviço de Kotlin/Spring para Clojure nativo, mantendo a funcionalidade mas adotando o paradigma funcional.
