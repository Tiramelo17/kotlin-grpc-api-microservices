(ns ms-order.service.order-service
  "Serviço de lógica de negócio para Orders (Pedidos).
   
   Este namespace contém toda a lógica de negócio relacionada a pedidos.
   É a camada intermediária entre o controller (HTTP) e o repository (banco).
   
   Responsabilidades:
   - Validar dados de entrada
   - Calcular preços usando o cliente gRPC
   - Orquestrar operações entre repository e outras dependências
   - Aplicar regras de negócio
   
   Padrão de design: Service Layer Pattern"
  (:require [ms-order.repository.order-repository :as repo]
            [ms-order.grpc.product-client-mock :as product-client]
            [ms-order.models.order :as order-model]
            [ms-order.models.schemas :as schemas]))

(defn create-order
  "Cria um novo pedido.
   
   Fluxo:
   1. Valida o request
   2. Calcula o preço total usando o cliente gRPC (mockado)
   3. Cria o modelo de pedido
   4. Salva no banco de dados
   5. Retorna a resposta formatada
   
   Parâmetros:
   - request: mapa {:products {product-id quantidade}}
   
   Retorna:
   - Response formatada com o pedido criado
   
   Lança exceção se:
   - Request inválido
   - Produtos não encontrados
   - Erro ao salvar no banco"
  [request]
  ;; 1. Valida o request
  (when-not (schemas/valid-create-order-request? request)
    (throw (ex-info "Request inválido para criar pedido"
                    {:type :invalid-request
                     :request request})))
  
  (let [products (:products request)
        ;; 2. Calcula o preço total (simula chamada gRPC)
        total-price (product-client/calculate-total-price products)
        
        ;; Valida se há produtos (opcional, mas boa prática)
        _ (when (zero? total-price)
            (throw (ex-info "Nenhum produto válido encontrado"
                            {:type :no-valid-products
                             :products products})))
        
        ;; 3. Cria o modelo de pedido
        new-order (order-model/create-order products total-price)
        
        ;; 4. Salva no banco de dados
        saved-order (repo/save-order new-order)]
    
    ;; 5. Retorna resposta formatada
    (schemas/create-order-response saved-order)))

(defn find-all-orders
  "Busca todos os pedidos.
   
   Retorna uma lista de pedidos formatada para resposta HTTP.
   
   Esta função é simples: apenas busca do repository e formata."
  []
  (let [orders (repo/find-all-orders)]
    (schemas/find-all-orders-response orders)))

(defn find-order-by-id
  "Busca um pedido específico por ID.
   
   Parâmetros:
   - id: ID do pedido (string ou ObjectId)
   
   Retorna:
   - Response formatada com o pedido
   
   Lança exceção se:
   - Pedido não encontrado"
  [id]
  (if-let [order (repo/find-order-by-id id)]
    (schemas/find-order-response order)
    (throw (ex-info "Pedido não encontrado"
                    {:type :not-found
                     :id id}))))

(defn delete-order-by-id
  "Deleta um pedido por ID.
   
   Parâmetros:
   - id: ID do pedido
   
   Retorna:
   - true se deletou com sucesso
   
   Lança exceção se:
   - Pedido não encontrado"
  [id]
  (let [deleted? (repo/delete-order-by-id id)]
    (if deleted?
      {:success true
       :message "Pedido deletado com sucesso"}
      (throw (ex-info "Pedido não encontrado"
                      {:type :not-found
                       :id id})))))

(defn get-order-summary
  "Retorna um resumo do pedido com detalhes dos produtos.
   
   Esta função demonstra como combinar dados de múltiplas fontes.
   
   Parâmetros:
   - id: ID do pedido
   
   Retorna:
   - Mapa com informações detalhadas do pedido e produtos"
  [id]
  (let [order (repo/find-order-by-id id)
        _ (when-not order
            (throw (ex-info "Pedido não encontrado"
                            {:type :not-found
                             :id id})))
        ;; Busca detalhes dos produtos
        products-details (product-client/get-products-with-quantities 
                          (:products order))]
    {:order order
     :products-details products-details
     :summary {:total-items (reduce + (vals (:products order)))
               :total-price (:price order)}}))
