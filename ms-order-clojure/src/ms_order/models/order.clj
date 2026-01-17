(ns ms-order.models.order
  "Namespace para o modelo de Order (Pedido).
   
   Em Clojure, não usamos classes como em Kotlin/Java. Em vez disso, usamos
   mapas (hash-maps) simples para representar dados. Isso torna o código
   mais flexível e idiomático em Clojure.
   
   Um Order é representado como:
   {:id 123
    :products {1 2, 3 1}  ;; map de product-id -> quantidade
    :price 150.50}
   
   Funções neste namespace:
   - create-order: cria um novo pedido
   - order->map: converte um pedido para um mapa (para JSON)
   - map->order: converte um mapa para um pedido (do banco de dados)")

(defn create-order
  "Cria um novo Order (pedido).
   
   Parâmetros:
   - products: mapa de product-id para quantidade {1 2, 3 1}
   - price: preço total do pedido
   
   Retorna um mapa representando o pedido.
   
   Em Clojure, funções são cidadãos de primeira classe e usamos
   mapas imutáveis para representar dados."
  [products price]
  {:products products
   :price price
   :created-at (java.time.Instant/now)})

(defn create-order-with-id
  "Cria um Order com ID (normalmente vindo do banco de dados).
   
   O ID é gerado pelo MongoDB automaticamente quando salvamos um documento."
  [id products price]
  {:id id
   :products products
   :price price
   :created-at (java.time.Instant/now)})

(defn order->map
  "Converte um Order para um mapa simples para serialização JSON.
   
   Esta função prepara o pedido para ser enviado como resposta HTTP.
   Converte tipos especiais (como ObjectId do MongoDB) para strings."
  [order]
  (-> order
      (update :id str)  ;; Converte ObjectId para string
      (update :created-at str)))  ;; Converte Instant para string

(defn map->order
  "Converte um mapa (normalmente do banco de dados) para um Order.
   
   Esta função é útil quando recuperamos dados do MongoDB."
  [m]
  {:id (:_id m)
   :products (:products m)
   :price (:price m)
   :created-at (:created-at m)})
