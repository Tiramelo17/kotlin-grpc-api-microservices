(ns ms-order.service.order-service-test
  "Testes unitários para o serviço de pedidos.
   
   Em Clojure, testes são escritos usando clojure.test.
   É simples e integrado à linguagem.
   
   Estrutura de um teste:
   (deftest nome-do-teste
     (testing 'descrição do que está sendo testado'
       (is (= expected actual))))"
  (:require [clojure.test :refer :all]
            [ms-order.models.schemas :as schemas]
            [ms-order.grpc.product-client-mock :as mock]))

;; ============================================================================
;; Testes de Validação de Schemas
;; ============================================================================

(deftest test-valid-create-order-request
  (testing "Valida request válido para criar pedido"
    (let [valid-request {:products {1 2, 3 1}}]
      (is (true? (schemas/valid-create-order-request? valid-request))
          "Request com produtos válidos deve ser aceito")))
  
  (testing "Rejeita request inválido"
    (let [invalid-requests [{:products {}}  ; vazio
                           {}  ; sem :products
                           {:products nil}  ; products nil
                           {:products {1 0}}  ; quantidade 0
                           {:products {1 -1}}]]  ; quantidade negativa
      (doseq [req invalid-requests]
        (is (false? (schemas/valid-create-order-request? req))
            (str "Request inválido deve ser rejeitado: " req))))))

;; ============================================================================
;; Testes do Cliente gRPC Mock
;; ============================================================================

(deftest test-get-product
  (testing "Busca produto por ID"
    (let [product (mock/get-product 1)]
      (is (map? product) "Deve retornar um mapa")
      (is (= 1 (:id product)) "ID deve ser 1")
      (is (= "Laptop" (:name product)) "Nome deve ser Laptop")
      (is (= 1500.0 (:price product)) "Preço deve ser 1500.0")))
  
  (testing "Retorna nil para produto inexistente"
    (is (nil? (mock/get-product 999))
        "Produto inexistente deve retornar nil")))

(deftest test-calculate-total-price
  (testing "Calcula preço total com um produto"
    (let [products {1 1}  ; 1 Laptop
          expected 1500.0
          actual (mock/calculate-total-price products)]
      (is (= expected actual)
          "1 Laptop deve custar 1500.0")))
  
  (testing "Calcula preço total com múltiplos produtos"
    (let [products {1 2, 3 1}  ; 2 Laptops + 1 Teclado
          expected 3075.0  ; (1500 * 2) + (75 * 1)
          actual (mock/calculate-total-price products)]
      (is (= expected actual)
          "2 Laptops + 1 Teclado deve custar 3075.0")))
  
  (testing "Calcula preço total com todos os produtos"
    (let [products {1 1, 2 1, 3 1, 4 1, 5 1}
          expected 2000.0  ; 1500 + 25 + 75 + 300 + 100
          actual (mock/calculate-total-price products)]
      (is (= expected actual)
          "Soma de todos os produtos deve ser 2000.0")))
  
  (testing "Retorna 0.0 para produtos inexistentes"
    (let [products {999 10}
          expected 0.0
          actual (mock/calculate-total-price products)]
      (is (= expected actual)
          "Produtos inexistentes devem resultar em 0.0"))))

(deftest test-validate-products-stock
  (testing "Valida estoque suficiente"
    (let [products {1 5}  ; 5 Laptops (estoque: 10)
          result (mock/validate-products-stock products)]
      (is (true? (:valid? result))
          "Deve ser válido quando há estoque")
      (is (empty? (:errors result))
          "Não deve ter erros")))
  
  (testing "Detecta estoque insuficiente"
    (let [products {1 20}  ; 20 Laptops (estoque: 10)
          result (mock/validate-products-stock products)]
      (is (false? (:valid? result))
          "Deve ser inválido quando não há estoque")
      (is (not (empty? (:errors result)))
          "Deve ter erros")
      (is (= 1 (count (:errors result)))
          "Deve ter exatamente 1 erro")
      (let [error (first (:errors result))]
        (is (= 1 (:product-id error)))
        (is (= 20 (:requested error)))
        (is (= 10 (:available error)))))))

;; ============================================================================
;; Testes de Response Schemas
;; ============================================================================

(deftest test-create-order-response
  (testing "Cria response de pedido criado"
    (let [order {:id "abc123"
                 :products {1 2, 3 1}
                 :price 3075.0}
          response (schemas/create-order-response order)]
      (is (= "abc123" (:id response)))
      (is (= {1 2, 3 1} (:products response)))
      (is (= 3075.0 (:price response))))))

(deftest test-find-order-response
  (testing "Cria response de busca de pedido"
    (let [order {:id "abc123"
                 :products {1 2}
                 :price 3000.0
                 :created-at #inst "2024-01-17T12:00:00.000Z"}
          response (schemas/find-order-response order)]
      (is (= "abc123" (:id response)))
      (is (= {1 2} (:products response)))
      (is (= 3000.0 (:price response)))
      (is (string? (:created-at response))
          "created-at deve ser convertido para string"))))

(deftest test-find-all-orders-response
  (testing "Cria response de lista de pedidos"
    (let [orders [{:id "1" :products {1 1} :price 100.0 :created-at #inst "2024-01-17"}
                  {:id "2" :products {2 1} :price 200.0 :created-at #inst "2024-01-17"}]
          response (schemas/find-all-orders-response orders)]
      (is (vector? response) "Deve retornar um vetor")
      (is (= 2 (count response)) "Deve ter 2 pedidos")
      (is (= "1" (:id (first response))))
      (is (= "2" (:id (second response)))))))

;; ============================================================================
;; Testes de Integração (Simulados)
;; ============================================================================

(deftest test-integration-create-and-calculate
  (testing "Fluxo completo: validar, calcular e criar response"
    (let [request {:products {1 2, 2 1}}  ; 2 Laptops + 1 Mouse
          _ (is (true? (schemas/valid-create-order-request? request))
                "Request deve ser válido")
          
          total-price (mock/calculate-total-price (:products request))
          _ (is (= 3025.0 total-price)  ; (1500 * 2) + (25 * 1)
                "Preço total deve ser calculado corretamente")
          
          order {:id "test123"
                 :products (:products request)
                 :price total-price}
          
          response (schemas/create-order-response order)
          _ (is (= "test123" (:id response)))
          _ (is (= {1 2, 2 1} (:products response)))
          _ (is (= 3025.0 (:price response)))]
      (is true "Fluxo completo executado com sucesso"))))

;; ============================================================================
;; Helpers para executar testes
;; ============================================================================

(comment
  ;; Para executar no REPL:
  (run-tests)
  
  ;; Para executar teste específico:
  (test-calculate-total-price)
  
  ;; Para executar com detalhes:
  (run-tests 'ms-order.service.order-service-test))
