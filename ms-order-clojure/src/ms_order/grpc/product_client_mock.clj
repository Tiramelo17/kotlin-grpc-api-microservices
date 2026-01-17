(ns ms-order.grpc.product-client-mock
  "Mock do cliente gRPC para buscar produtos.
   
   Este namespace simula as chamadas gRPC para o serviço de produtos.
   Em uma implementação real, usaríamos uma biblioteca gRPC do Clojure
   para fazer chamadas reais.
   
   Por enquanto, retornamos dados mockados (simulados) para permitir
   que o serviço de pedidos funcione sem depender do serviço real
   de produtos.
   
   Estrutura de um produto mockado:
   {:id 1
    :name 'Produto Exemplo'
    :price 50.0
    :stock 100}")

;; Base de dados mockada de produtos
;; Em produção, isso viria de uma chamada gRPC real
(def mock-products
  "Produtos simulados para demonstração.
   
   Este é um mapa onde a chave é o ID do produto e o valor
   é um mapa com as informações do produto."
  {1 {:id 1 :name "Laptop" :price 1500.0 :stock 10}
   2 {:id 2 :name "Mouse" :price 25.0 :stock 50}
   3 {:id 3 :name "Teclado" :price 75.0 :stock 30}
   4 {:id 4 :name "Monitor" :price 300.0 :stock 15}
   5 {:id 5 :name "Headset" :price 100.0 :stock 25}})

(defn get-product
  "Busca um produto mockado por ID.
   
   Parâmetros:
   - product-id: ID do produto
   
   Retorna o produto ou nil se não encontrado.
   
   Em uma implementação real, isso faria uma chamada gRPC
   ao serviço de produtos usando algo como:
   (grpc/call product-service :GetProduct {:id product-id})"
  [product-id]
  (get mock-products product-id))

(defn get-products
  "Busca múltiplos produtos de uma vez.
   
   Parâmetros:
   - product-ids: coleção de IDs de produtos
   
   Retorna uma sequência de produtos encontrados.
   
   Ignora produtos não encontrados (nil)."
  [product-ids]
  (->> product-ids
       (map get-product)
       (remove nil?)))

(defn get-products-with-quantities
  "Busca produtos com suas quantidades.
   
   Parâmetros:
   - products-map: mapa de {product-id quantidade}
                   exemplo: {1 2, 3 1}
   
   Retorna uma sequência de mapas com produto e quantidade:
   [{:product {...} :quantity 2}
    {:product {...} :quantity 1}]
   
   Esta função simula o que seria uma chamada gRPC batch
   para buscar vários produtos de uma vez."
  [products-map]
  (for [[product-id quantity] products-map]
    (when-let [product (get-product product-id)]
      {:product product
       :quantity quantity})))

(defn calculate-total-price
  "Calcula o preço total de um pedido.
   
   Parâmetros:
   - products-map: mapa de {product-id quantidade}
                   exemplo: {1 2, 3 1}
   
   Retorna o preço total somando (preço do produto * quantidade).
   
   Esta é a lógica de negócio principal que interage com o
   cliente gRPC mockado para obter os preços dos produtos."
  [products-map]
  (let [products-with-qty (get-products-with-quantities products-map)]
    ;; reduce é como fold/reduce em outras linguagens
    ;; acumula valores aplicando uma função
    (reduce (fn [total {:keys [product quantity]}]
              (+ total (* (:price product) quantity)))
            0.0  ;; valor inicial
            (remove nil? products-with-qty))))

(defn validate-products-stock
  "Valida se há estoque suficiente para todos os produtos.
   
   Parâmetros:
   - products-map: mapa de {product-id quantidade}
   
   Retorna {:valid? true/false
            :errors [...]}
   
   Esta função seria útil em uma implementação real para
   validar antes de criar o pedido."
  [products-map]
  (let [products-with-qty (get-products-with-quantities products-map)
        errors (for [{:keys [product quantity]} products-with-qty
                     :when (and product (> quantity (:stock product)))]
                 {:product-id (:id product)
                  :requested quantity
                  :available (:stock product)
                  :message (str "Estoque insuficiente para produto " (:id product))})]
    {:valid? (empty? errors)
     :errors errors}))
