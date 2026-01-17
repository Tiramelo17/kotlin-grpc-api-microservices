(ns ms-order.core
  "Namespace principal da aplicação ms-order-clojure.
   
   Este é o ponto de entrada da aplicação. Contém a função -main
   que é executada quando iniciamos o servidor.
   
   Responsabilidades:
   - Carregar configurações
   - Inicializar banco de dados
   - Iniciar servidor HTTP
   - Gerenciar shutdown gracioso
   
   Para executar:
   $ clojure -M:run
   
   Ou direto:
   $ clojure -M -m ms-order.core"
  (:require [ring.adapter.jetty :as jetty]
            [ms-order.controller.order-controller :refer [app]]
            [ms-order.repository.order-repository :as repo]
            [clojure.java.io :as io]
            [clojure.edn :as edn])
  (:gen-class))  ;; Necessário para criar um JAR executável

;; ============================================================================
;; Configuração
;; ============================================================================

(defn load-config
  "Carrega configurações do arquivo config.edn.
   
   EDN (Extensible Data Notation) é o formato de dados nativo do Clojure.
   É similar ao JSON mas mais poderoso:
   - Suporta mais tipos de dados (keywords, symbols, sets, etc)
   - É mais legível
   - É extensível
   
   Retorna um mapa com as configurações."
  []
  (try
    (let [config-file (io/resource "config.edn")]
      (if config-file
        (edn/read-string (slurp config-file))
        (do
          (println "⚠ Arquivo config.edn não encontrado, usando configurações padrão")
          {:app {:name "ms-order-clojure" :port 8082}
           :mongodb {:uri "mongodb://localhost:27017" :database "order_db"}})))
    (catch Exception e
      (println "⚠ Erro ao carregar config.edn:" (.getMessage e))
      {:app {:name "ms-order-clojure" :port 8082}
       :mongodb {:uri "mongodb://localhost:27017" :database "order_db"}})))

;; ============================================================================
;; Servidor HTTP
;; ============================================================================

;; Atom para manter referência ao servidor (para poder pará-lo)
(defonce server (atom nil))

(defn start-server!
  "Inicia o servidor HTTP usando Jetty.
   
   Jetty é um servidor HTTP/servlet container em Java.
   Ring fornece um adapter para usar Jetty facilmente.
   
   Parâmetros:
   - port: porta do servidor (padrão 8082)
   
   Opções do Jetty:
   - :port -> porta do servidor
   - :join? false -> não bloqueia a thread principal
   - :host -> IP para bind (0.0.0.0 aceita conexões externas)
   
   Retorna o servidor iniciado."
  [port]
  (println "========================================")
  (println "🚀 Iniciando servidor HTTP...")
  (println "   Porta:" port)
  (println "========================================")
  
  (let [jetty-server (jetty/run-jetty 
                      app  ;; Nossa aplicação (definida no controller)
                      {:port port
                       :join? false  ;; Não bloqueia
                       :host "0.0.0.0"})]  ;; Aceita conexões externas
    
    (println "✓ Servidor HTTP iniciado!")
    (println)
    (println "📍 Endpoints disponíveis:")
    (println "   GET    http://localhost:" port "/health")
    (println "   GET    http://localhost:" port "/orders")
    (println "   POST   http://localhost:" port "/orders")
    (println "   GET    http://localhost:" port "/orders/:id")
    (println "   GET    http://localhost:" port "/orders/:id/summary")
    (println "   DELETE http://localhost:" port "/orders/:id")
    (println)
    (println "========================================")
    
    ;; Salva referência ao servidor
    (reset! server jetty-server)
    jetty-server))

(defn stop-server!
  "Para o servidor HTTP graciosamente.
   
   Fecha todas as conexões e libera a porta."
  []
  (when-let [s @server]
    (println "🛑 Parando servidor HTTP...")
    (.stop s)
    (reset! server nil)
    (println "✓ Servidor parado")))

;; ============================================================================
;; Inicialização da aplicação
;; ============================================================================

(defn init!
  "Inicializa todos os componentes da aplicação.
   
   Ordem de inicialização:
   1. Carregar configurações
   2. Conectar ao banco de dados
   3. Iniciar servidor HTTP
   
   Esta função orquestra todo o startup da aplicação."
  []
  (println)
  (println "╔════════════════════════════════════════╗")
  (println "║   MS-ORDER-CLOJURE                     ║")
  (println "║   Microserviço de Pedidos em Clojure   ║")
  (println "╚════════════════════════════════════════╝")
  (println)
  
  ;; 1. Carregar configurações
  (let [config (load-config)
        app-config (:app config)
        db-config (:mongodb config)]
    
    (println "📋 Configurações carregadas:")
    (println "   App:" (:name app-config))
    (println "   MongoDB URI:" (:uri db-config))
    (println "   MongoDB Database:" (:database db-config))
    (println)
    
    ;; 2. Conectar ao banco de dados
    (try
      (repo/connect! db-config)
      (catch Exception e
        (println "❌ Erro ao conectar ao MongoDB:" (.getMessage e))
        (println "   Verifique se o MongoDB está rodando em" (:uri db-config))
        (println)
        (throw e)))
    
    ;; 3. Iniciar servidor HTTP
    (start-server! (:port app-config))
    
    (println "✅ Aplicação iniciada com sucesso!")
    (println)))

(defn shutdown!
  "Desliga a aplicação graciosamente.
   
   Fecha recursos na ordem inversa da inicialização:
   1. Servidor HTTP
   2. Banco de dados"
  []
  (println)
  (println "🔄 Desligando aplicação...")
  (stop-server!)
  (repo/disconnect!)
  (println "✅ Aplicação encerrada")
  (println))

;; ============================================================================
;; Função main (ponto de entrada)
;; ============================================================================

(defn -main
  "Função principal da aplicação.
   
   Esta é a função que é executada quando rodamos:
   $ clojure -M:run
   
   Ou quando criamos um JAR e executamos:
   $ java -jar ms-order-clojure.jar
   
   O prefixo - (hífen) indica que é uma função pública
   que pode ser chamada pela JVM.
   
   O & args permite receber argumentos da linha de comando."
  [& args]
  (try
    ;; Registra shutdown hook para cleanup gracioso
    ;; Quando o processo recebe SIGTERM/SIGINT, executa shutdown!
    (.addShutdownHook 
     (Runtime/getRuntime)
     (Thread. shutdown!))
    
    ;; Inicializa a aplicação
    (init!)
    
    ;; Mantém a aplicação rodando
    ;; (o servidor roda em outra thread)
    
    (catch Exception e
      (println "❌ Erro ao iniciar aplicação:" (.getMessage e))
      (.printStackTrace e)
      (System/exit 1))))

;; Para desenvolvimento no REPL (Read-Eval-Print Loop)
;; Você pode executar estas funções manualmente:
(comment
  ;; Iniciar
  (init!)
  
  ;; Testar endpoints (usando curl ou httpie no terminal)
  ;; curl http://localhost:8082/health
  ;; curl -X POST http://localhost:8082/orders -H "Content-Type: application/json" -d '{"products": {"1": 2, "2": 1}}'
  
  ;; Parar
  (shutdown!)
  )
