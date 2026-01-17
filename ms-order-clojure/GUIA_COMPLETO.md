# GUIA COMPLETO - MS-ORDER-CLOJURE

## 🎯 O que foi implementado?

Convertemos o microserviço `ms-order` de **Kotlin/Spring Boot** para **Clojure nativo**. 

### Arquitetura Original (Kotlin)
- **Framework**: Spring Boot
- **Banco de Dados**: MySQL com JPA/Hibernate
- **Comunicação**: gRPC para produtos
- **Estilo**: Programação Orientada a Objetos com elementos funcionais

### Nova Arquitetura (Clojure)
- **Framework**: Ring + Compojure (puro Clojure)
- **Banco de Dados**: MongoDB (NoSQL)
- **Comunicação**: Mock do gRPC (facilmente substituível)
- **Estilo**: Programação Funcional Pura

---

## 📚 EXPLICAÇÃO DETALHADA DE CADA ARQUIVO

### 1. `deps.edn` - Gerenciador de Dependências

```clojure
{:deps {org.clojure/clojure {:mvn/version "1.11.1"}
        org.mongodb/mongodb-driver-sync {:mvn/version "4.11.1"}
        ring/ring-core {:mvn/version "1.10.0"}
        ...}}
```

**O que é?**
- Similar ao `build.gradle.kts` do Kotlin ou `package.json` do Node.js
- Define as bibliotecas (dependências) do projeto
- `:mvn/version` indica que virá do Maven Central

**Principais bibliotecas:**
- **Clojure**: A linguagem em si
- **MongoDB Driver**: Para conectar ao MongoDB
- **Ring**: Framework HTTP base (como Express.js ou Jetty)
- **Compojure**: Roteamento de URLs (como Spring MVC)
- **Cheshire**: Conversão JSON (como Jackson)

---

### 2. `config.edn` - Configuração da Aplicação

```clojure
{:app {:name "ms-order-clojure"
       :port 8082}
 :mongodb {:uri "mongodb://localhost:27017"
           :database "order_db"}}
```

**O que é EDN?**
- EDN = Extensible Data Notation
- Formato de dados nativo do Clojure (como JSON, mas melhor)
- Mais tipos: keywords (`:name`), símbolos, conjuntos
- Mais seguro que JSON

**Por que usar EDN e não YAML?**
- Mais simples e idiomático em Clojure
- Tipos de dados nativos
- Fácil de ler programaticamente

---

### 3. `models/order.clj` - Modelo de Dados

#### Em Kotlin (orientado a objetos):
```kotlin
@Entity
@Table(name = "orders")
data class Order(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: Long = 0,
    @ElementCollection
    var products: Map<Int,Int> = emptyMap(),
    var price: Double
)
```

#### Em Clojure (funcional):
```clojure
(defn create-order [products price]
  {:products products
   :price price
   :created-at (java.time.Instant/now)})
```

**Diferenças principais:**

1. **Sem Classes**: Clojure usa mapas (hash-maps) simples
2. **Sem Anotações**: Não precisa de `@Entity`, `@Table`, etc.
3. **Imutável**: Uma vez criado, não muda (thread-safe por padrão)
4. **Função pura**: Sempre retorna o mesmo resultado para mesma entrada

**Por que mapas ao invés de classes?**
- Mais flexível: pode adicionar campos facilmente
- Menos boilerplate: não precisa criar classe, getter, setter
- Composição: fácil combinar e transformar dados
- Serialização: JSON/EDN automática

---

### 4. `repository/order_repository.clj` - Acesso ao Banco

#### Conceitos importantes:

**a) Atom - Estado mutável thread-safe**
```clojure
(defonce db-connection (atom nil))
```
- `atom`: container para estado mutável
- Thread-safe automaticamente
- `@db-connection`: lê o valor
- `(reset! db-connection valor)`: atualiza

**b) Conversões Document ↔ Map**
```clojure
(defn document->map [^Document doc]
  (into {} (for [[k v] doc] [(keyword k) v])))
```
- MongoDB usa classe `Document` (Java)
- Clojure prefere mapas nativos
- Convertemos entre os dois

**c) Operações CRUD**

**CREATE:**
```clojure
(defn save-order [order]
  (let [collection (get-collection "orders")
        doc (map->document order)]
    (.insertOne collection doc)
    ...))
```

**READ:**
```clojure
(defn find-all-orders []
  (let [collection (get-collection "orders")
        cursor (.find collection)]
    (mapv document->map cursor)))
```

**DELETE:**
```clojure
(defn delete-order-by-id [id]
  (let [object-id (ObjectId. id)
        filter (Document. "_id" object-id)]
    (.deleteOne collection filter)))
```

**Comparação com JPA (Kotlin):**
- JPA: `orderRepository.save(order)` - automático
- Clojure: Chamadas explícitas ao driver MongoDB
- Vantagem JPA: Menos código
- Vantagem Clojure: Mais controle, sem "magia"

---

### 5. `grpc/product_client_mock.clj` - Cliente gRPC Mockado

#### Em Kotlin (real):
```kotlin
@Component
class ProductClientGrpc {
    @Autowired
    private lateinit var channel: ManagedChannel
    
    fun getProducts(products: Map<Int,Int>): ProductsResponse {
        return client.getProducts(...)
    }
}
```

#### Em Clojure (mock):
```clojure
(def mock-products
  {1 {:id 1 :name "Laptop" :price 1500.0}
   2 {:id 2 :name "Mouse" :price 25.0}
   ...})

(defn calculate-total-price [products-map]
  (let [products-with-qty (get-products-with-quantities products-map)]
    (reduce (fn [total {:keys [product quantity]}]
              (+ total (* (:price product) quantity)))
            0.0
            products-with-qty)))
```

**Por que mock?**
- Focar no essencial: implementação em Clojure
- gRPC em Clojure é possível mas adiciona complexidade
- Pode ser substituído facilmente depois

**Como calcular preço total:**
1. Busca cada produto pelo ID
2. Multiplica preço × quantidade
3. Soma tudo usando `reduce`

**Reduce explicado:**
```clojure
(reduce + [1 2 3 4])  ; => 10
; Funciona assim: (+ (+ (+ 1 2) 3) 4)
```

---

### 6. `service/order_service.clj` - Lógica de Negócio

**Fluxo de criar pedido:**

```clojure
(defn create-order [request]
  ;; 1. Valida request
  (when-not (valid-create-order-request? request)
    (throw (ex-info "Request inválido" {...})))
  
  ;; 2. Calcula preço total
  (let [products (:products request)
        total-price (product-client/calculate-total-price products)
        
        ;; 3. Cria modelo
        new-order (order-model/create-order products total-price)
        
        ;; 4. Salva no banco
        saved-order (repo/save-order new-order)]
    
    ;; 5. Retorna response formatada
    (schemas/create-order-response saved-order)))
```

**Let binding explicado:**
```clojure
(let [x 10
      y 20
      z (+ x y)]  ; z = 30
  z)  ; => 30
```
- Define variáveis locais temporárias
- Similar a `const` em JavaScript
- Valores são imutáveis dentro do `let`

**Threading com `let`:**
- Cada linha pode usar valores das anteriores
- Mais legível que aninhar funções

---

### 7. `controller/order_controller.clj` - API HTTP

#### Anatomia de um Handler:

```clojure
(defn create-order-handler [request]
  (try
    (let [order-request (:body request)
          result (service/create-order order-request)]
      (response/response result))
    (catch Exception e
      (response/status
       (response/response {:error (.getMessage e)})
       400))))
```

**Request em Ring:**
```clojure
{:uri "/orders"
 :request-method :post
 :headers {"content-type" "application/json"}
 :body {:products {1 2, 3 1}}  ; já parseado pelo middleware
 :params {...}}
```

**Response em Ring:**
```clojure
{:status 200
 :headers {"Content-Type" "application/json"}
 :body {:id "123" :products {1 2} :price 100.0}}
```

#### Roteamento com Compojure:

```clojure
(defroutes app-routes
  (GET "/orders" [] find-all-orders-handler)
  (POST "/orders" [] create-order-handler)
  (GET "/orders/:id" [] find-order-by-id-handler)
  (DELETE "/orders/:id" [] delete-order-by-id-handler))
```

**Simples e declarativo:**
- Sem anotações `@GetMapping`, `@PostMapping`
- Rotas definidas como dados
- Ordem importa (primeira que bater, executa)

#### Middleware Stack:

```clojure
(def app
  (-> app-routes
      wrap-json-response
      wrap-json-request
      wrap-keyword-params
      wrap-params))
```

**Threading macro `->` explicado:**
```clojure
; Isso:
(-> x
    (f a)
    (g b))

; É equivalente a:
(g (f x a) b)
```

**Ordem dos middlewares:**
1. Request chega → `wrap-params` (parseia query params)
2. → `wrap-keyword-params` (converte para keywords)
3. → `wrap-json-request` (parseia JSON do body)
4. → `app-routes` (executa handler)
5. → `wrap-json-response` (converte response para JSON)
6. → Response sai

---

### 8. `core.clj` - Inicialização

**Função `-main`:**
```clojure
(defn -main [& args]
  (.addShutdownHook (Runtime/getRuntime) (Thread. shutdown!))
  (init!))
```

**O que acontece:**
1. Registra shutdown hook (cleanup ao terminar)
2. Chama `init!`

**Função `init!`:**
```clojure
(defn init! []
  (let [config (load-config)]
    (repo/connect! (:mongodb config))
    (start-server! (:port (:app config)))))
```

**Ordem de inicialização:**
1. Carrega config.edn
2. Conecta ao MongoDB
3. Inicia servidor HTTP (Jetty)

**Jetty configuração:**
```clojure
(jetty/run-jetty app
  {:port 8082
   :join? false  ; Não bloqueia a thread
   :host "0.0.0.0"})  ; Aceita conexões externas
```

---

## 🔄 FLUXO COMPLETO DE UMA REQUISIÇÃO

### Exemplo: Criar um pedido

**1. Cliente faz requisição HTTP:**
```bash
curl -X POST http://localhost:8082/orders \
  -H "Content-Type: application/json" \
  -d '{"products": {"1": 2, "3": 1}}'
```

**2. Request chega no servidor Jetty**
```clojure
{:request-method :post
 :uri "/orders"
 :headers {"content-type" "application/json"}
 :body "{\"products\": {\"1\": 2, \"3\": 1}}"}
```

**3. Passa pelos middlewares:**
- `wrap-params`: extrai query params (nenhum neste caso)
- `wrap-keyword-params`: converte params para keywords
- `wrap-json-request`: parseia JSON → `{:products {"1" 2, "3" 1}}`

**4. Compojure roteia para handler:**
```clojure
(POST "/orders" [] create-order-handler)
```

**5. Handler processa:**
```clojure
(defn create-order-handler [request]
  (let [order-request (:body request)  ; {"products": {"1": 2}}
        products (convert-keys order-request)  ; {1 2, 3 1}
        result (service/create-order {:products products})]
    (response/response result)))
```

**6. Service executa lógica:**
```clojure
(defn create-order [request]
  (let [products (:products request)  ; {1 2, 3 1}
        total-price (product-client/calculate-total-price products)  ; 3075.0
        new-order (order-model/create-order products total-price)
        saved-order (repo/save-order new-order)]
    (schemas/create-order-response saved-order)))
```

**7. Repository salva no MongoDB:**
```clojure
(defn save-order [order]
  (let [doc (map->document order)]
    (.insertOne collection doc)
    ; MongoDB gera ID automaticamente
    (assoc order :id generated-id)))
```

**8. Response sobe a stack:**
```clojure
{:id "507f1f77bcf86cd799439011"
 :products {1 2, 3 1}
 :price 3075.0}
```

**9. Middleware converte para JSON:**
```json
{
  "id": "507f1f77bcf86cd799439011",
  "products": {"1": 2, "3": 1},
  "price": 3075.0
}
```

**10. Cliente recebe resposta!**

---

## 💡 CONCEITOS-CHAVE DO CLOJURE

### 1. Imutabilidade
```clojure
(def x {:a 1 :b 2})
(assoc x :c 3)  ; Retorna NOVO mapa: {:a 1 :b 2 :c 3}
x  ; ORIGINAL não mudou: {:a 1 :b 2}
```

**Vantagens:**
- Thread-safe por padrão
- Sem efeitos colaterais inesperados
- Mais fácil raciocinar sobre o código

### 2. Funções Puras
```clojure
; Pura: sempre retorna o mesmo resultado
(defn add [a b]
  (+ a b))

; Impura: depende de estado externo ou tem side-effects
(defn save-to-db [data]
  (.insertOne collection data))  ; side-effect!
```

**Como lidar com impureza?**
- Isole side-effects nas bordas (repository, I/O)
- Mantenha o core (models, service) puro

### 3. Composição de Funções
```clojure
; Threading macro
(-> order
    (assoc :price 100.0)
    (update :products #(assoc % 1 5))
    repo/save-order
    schemas/create-order-response)

; Equivalente imperativo:
order1 = assoc(order, :price, 100.0)
order2 = update(order1, :products, ...)
order3 = repo/save-order(order2)
result = schemas/create-order-response(order3)
```

### 4. Destructuring
```clojure
; Map destructuring
(let [{:keys [id products price]} order]
  (println id products price))

; Mesmo que:
(let [id (:id order)
      products (:products order)
      price (:price order)]
  (println id products price))
```

### 5. Higher-Order Functions
```clojure
; map: aplica função a cada elemento
(map inc [1 2 3])  ; => (2 3 4)

; filter: filtra elementos
(filter even? [1 2 3 4])  ; => (2 4)

; reduce: acumula valores
(reduce + [1 2 3 4])  ; => 10
```

---

## 📊 COMPARAÇÃO: KOTLIN vs CLOJURE

### Criar um pedido

**Kotlin:**
```kotlin
@Service
class OrderService {
    @Autowired
    private lateinit var orderRepository: OrderRepository
    
    @Autowired
    private lateinit var productClientGrpc: ProductClientGrpc
    
    fun createNewOrder(request: CreateOrderRequest): CreateOrderResponse {
        return Order()
            .apply {
                this.products = request.products
                this.price = calcTotalPrice(request.products)
            }
            .let { orderRepository.save(it) }
            .let { mapper.toCreateOrderResponse(it) }
    }
    
    private fun calcTotalPrice(products: Map<Int,Int>): Double {
        return productClientGrpc.getProducts(products)
            .productsList
            .map { it.price * products[it.id]!! }
            .reduce { acc, price -> acc + price }
    }
}
```

**Clojure:**
```clojure
(defn create-order [request]
  (let [products (:products request)
        total-price (calculate-total-price products)
        new-order (order-model/create-order products total-price)
        saved-order (repo/save-order new-order)]
    (schemas/create-order-response saved-order)))

(defn calculate-total-price [products-map]
  (let [products-with-qty (get-products-with-quantities products-map)]
    (reduce (fn [total {:keys [product quantity]}]
              (+ total (* (:price product) quantity)))
            0.0
            products-with-qty)))
```

**Análise:**
- **Linhas de código**: Clojure ~15 vs Kotlin ~20
- **Anotações**: Clojure 0 vs Kotlin 3 (`@Service`, `@Autowired`, `@Autowired`)
- **Mutabilidade**: Clojure 100% imutável vs Kotlin misto
- **Dependências**: Clojure explícitas vs Kotlin injetadas
- **Estilo**: Clojure funcional puro vs Kotlin OO + funcional

---

## 🚀 COMANDOS ÚTEIS

### Iniciar MongoDB
```bash
docker-compose up -d mongodb
```

### Verificar MongoDB
```bash
# Via CLI
docker exec -it ms-order-mongodb mongosh
> use order_db
> db.orders.find()

# Via UI (Mongo Express)
# Abra: http://localhost:8081
# User: admin, Pass: admin
```

### Executar aplicação
```bash
cd ms-order-clojure
clojure -M:run
```

### Testar endpoints
```bash
# Health check
curl http://localhost:8082/health

# Criar pedido
curl -X POST http://localhost:8082/orders \
  -H "Content-Type: application/json" \
  -d '{"products": {"1": 2, "2": 1}}'

# Listar pedidos
curl http://localhost:8082/orders | jq

# Buscar por ID (substitua o ID)
curl http://localhost:8082/orders/65a7f1234bcf86cd799439011 | jq

# Deletar
curl -X DELETE http://localhost:8082/orders/65a7f1234bcf86cd799439011
```

---

## 🎓 RESUMO: O QUE VOCÊ APRENDEU

### Sobre Clojure:
1. ✅ Sintaxe básica: funções, mapas, keywords
2. ✅ Namespaces e require
3. ✅ Threading macros (`->`, `let`)
4. ✅ Imutabilidade e atoms
5. ✅ Higher-order functions (map, filter, reduce)
6. ✅ Destructuring
7. ✅ Tratamento de exceções

### Sobre o Ecossistema:
1. ✅ **Ring**: Framework HTTP base
2. ✅ **Compojure**: Roteamento
3. ✅ **MongoDB**: Driver Java/Clojure
4. ✅ **Cheshire**: JSON
5. ✅ **EDN**: Formato de dados

### Sobre Arquitetura:
1. ✅ Arquitetura em camadas
2. ✅ Separação de responsabilidades
3. ✅ Repository pattern
4. ✅ Service layer
5. ✅ RESTful API

---

**Sucesso na sua jornada com Clojure! 🎉**
