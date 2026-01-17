(ns ms-order.controller.order-controller
  "Controller HTTP para endpoints de Orders (Pedidos).
   
   Este namespace define os handlers HTTP usando Ring e Compojure.
   
   Ring é a biblioteca base para servidores HTTP em Clojure:
   - Request: mapa com informações da requisição HTTP
   - Response: mapa com {:status 200 :headers {...} :body ...}
   
   Compojure fornece roteamento conveniente:
   - GET, POST, DELETE: macros para definir rotas
   - defroutes: agrupa múltiplas rotas
   
   Estrutura de um handler:
   (defn handler [request]
     {:status 200
      :headers {'Content-Type' 'application/json'}
      :body {...}})
   
   O middleware ring-json automaticamente converte:
   - Request JSON -> Clojure map
   - Clojure map -> Response JSON"
  (:require [compojure.core :refer [defroutes GET POST DELETE]]
            [compojure.route :as route]
            [ring.middleware.json :refer [wrap-json-request wrap-json-response]]
            [ring.middleware.params :refer [wrap-params]]
            [ring.middleware.keyword-params :refer [wrap-keyword-params]]
            [ring.util.response :as response]
            [ms-order.service.order-service :as service]))

;; ============================================================================
;; Handlers (funções que processam requisições HTTP)
;; ============================================================================

(defn create-order-handler
  "Handler para criar um novo pedido.
   
   Endpoint: POST /orders
   Body: {\"products\": {\"1\": 2, \"3\": 1}}
   
   O middleware wrap-json-request já converteu o JSON para mapa Clojure.
   O body está em (:body request)."
  [request]
  (try
    (let [order-request (:body request)
          ;; Converte chaves de string para números
          ;; JSON não suporta números como chaves, então vem como strings
          products (into {} (map (fn [[k v]] [(Integer/parseInt (name k)) v])
                                 (:products order-request)))
          ;; Chama o serviço
          result (service/create-order {:products products})]
      ;; Retorna resposta de sucesso
      (response/response result))
    (catch Exception e
      ;; Tratamento de erros
      (let [data (ex-data e)]
        (response/status
         (response/response {:error (.getMessage e)
                             :type (:type data)
                             :details data})
         (case (:type data)
           :invalid-request 400
           :no-valid-products 400
           :not-found 404
           500))))))

(defn find-all-orders-handler
  "Handler para buscar todos os pedidos.
   
   Endpoint: GET /orders
   
   Retorna lista de todos os pedidos."
  [request]
  (try
    (let [result (service/find-all-orders)]
      (response/response result))
    (catch Exception e
      (response/status
       (response/response {:error (.getMessage e)})
       500))))

(defn find-order-by-id-handler
  "Handler para buscar um pedido por ID.
   
   Endpoint: GET /orders/:id
   
   O ID vem nos path params: (:params request)"
  [request]
  (try
    (let [id (get-in request [:params :id])
          result (service/find-order-by-id id)]
      (response/response result))
    (catch Exception e
      (let [data (ex-data e)]
        (response/status
         (response/response {:error (.getMessage e)
                             :type (:type data)})
         (if (= (:type data) :not-found) 404 500))))))

(defn delete-order-by-id-handler
  "Handler para deletar um pedido por ID.
   
   Endpoint: DELETE /orders/:id
   
   Retorna 204 No Content em sucesso."
  [request]
  (try
    (let [id (get-in request [:params :id])
          result (service/delete-order-by-id id)]
      (response/status (response/response result) 200))
    (catch Exception e
      (let [data (ex-data e)]
        (response/status
         (response/response {:error (.getMessage e)
                             :type (:type data)})
         (if (= (:type data) :not-found) 404 500))))))

(defn get-order-summary-handler
  "Handler para obter resumo detalhado do pedido.
   
   Endpoint: GET /orders/:id/summary
   
   Retorna informações completas incluindo detalhes dos produtos."
  [request]
  (try
    (let [id (get-in request [:params :id])
          result (service/get-order-summary id)]
      (response/response result))
    (catch Exception e
      (let [data (ex-data e)]
        (response/status
         (response/response {:error (.getMessage e)
                             :type (:type data)})
         (if (= (:type data) :not-found) 404 500))))))

(defn health-check-handler
  "Handler de health check para monitoramento.
   
   Endpoint: GET /health
   
   Retorna status da aplicação."
  [request]
  (response/response {:status "UP"
                      :service "ms-order-clojure"
                      :timestamp (str (java.time.Instant/now))}))

;; ============================================================================
;; Definição de Rotas
;; ============================================================================

(defroutes app-routes
  "Define todas as rotas da aplicação.
   
   Compojure usa uma DSL (Domain Specific Language) para rotas:
   - (GET '/path' [] handler) -> rota GET
   - (POST '/path' [] handler) -> rota POST
   - (DELETE '/path' [] handler) -> rota DELETE
   - :id é um path parameter
   
   Rotas são testadas em ordem até encontrar uma que corresponda."
  
  ;; Health check
  (GET "/health" [] health-check-handler)
  
  ;; Rotas de pedidos
  (GET "/orders" [] find-all-orders-handler)
  (POST "/orders" [] create-order-handler)
  (GET "/orders/:id" [] find-order-by-id-handler)
  (GET "/orders/:id/summary" [] get-order-summary-handler)
  (DELETE "/orders/:id" [] delete-order-by-id-handler)
  
  ;; Rota padrão para 404
  (route/not-found 
   (response/response {:error "Rota não encontrada"
                       :status 404})))

;; ============================================================================
;; Middleware Stack
;; ============================================================================

(def app
  "Define a aplicação completa com todos os middlewares.
   
   Middlewares em Ring são funções que transformam handlers:
   - wrap-json-response: converte mapas Clojure para JSON
   - wrap-json-request: converte JSON para mapas Clojure
   - wrap-keyword-params: converte params para keywords
   - wrap-params: parseia query params e form params
   
   A ordem é importante! São aplicados de baixo para cima:
   1. wrap-params (extrai params)
   2. wrap-keyword-params (converte para keywords)
   3. wrap-json-request (parseia JSON do body)
   4. wrap-json-response (serializa response para JSON)
   5. app-routes (nossas rotas)"
  (-> app-routes
      (wrap-json-response {:pretty true})  ;; JSON formatado
      wrap-json-request
      wrap-keyword-params
      wrap-params))
