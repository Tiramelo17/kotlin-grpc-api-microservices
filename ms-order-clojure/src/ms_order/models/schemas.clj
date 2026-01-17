(ns ms-order.models.schemas
  "Namespace para schemas de Request e Response.
   
   Em Clojure, schemas são geralmente apenas especificações de como
   os dados devem ser estruturados. Podemos usar a biblioteca 'spec'
   para validação formal, mas aqui vamos manter simples.
   
   Schemas definidos:
   - CreateOrderRequest: {:products {1 2, 3 1}}
   - CreateOrderResponse: {:id 123, :products {1 2}, :price 100.0}
   - FindOrderResponse: {:id 123, :products {1 2}, :price 100.0, :created-at ...}")

;; Request para criar um novo pedido
;; Exemplo: {:products {1 2, 3 1}}
;; Significa: produto 1 com quantidade 2, produto 3 com quantidade 1

(defn valid-create-order-request?
  "Valida se um request para criar pedido é válido.
   
   Um request válido deve ter:
   - :products como um mapa não-vazio
   - chaves do mapa devem ser números (product IDs)
   - valores devem ser números positivos (quantidades)"
  [request]
  (and (map? request)
       (map? (:products request))
       (not (empty? (:products request)))
       (every? number? (keys (:products request)))
       (every? pos? (vals (:products request)))))

;; Response ao criar um pedido
(defn create-order-response
  "Cria uma resposta após criar um pedido.
   
   Inclui o ID gerado, produtos e preço total."
  [order]
  {:id (str (:id order))
   :products (:products order)
   :price (:price order)})

;; Response ao buscar um pedido
(defn find-order-response
  "Cria uma resposta ao buscar um pedido.
   
   Inclui todas as informações do pedido incluindo data de criação."
  [order]
  {:id (str (:id order))
   :products (:products order)
   :price (:price order)
   :created-at (str (:created-at order))})

;; Lista de pedidos response
(defn find-all-orders-response
  "Cria uma resposta com lista de todos os pedidos."
  [orders]
  (mapv find-order-response orders))
