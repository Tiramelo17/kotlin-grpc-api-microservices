(ns ms-order.repository.order-repository
  "Repository para operações de banco de dados com Orders (Pedidos).
   
   Este namespace encapsula todas as interações com o MongoDB.
   Usa o driver MongoDB oficial para Java/Clojure.
   
   Conceitos importantes:
   
   1. MongoClient: Conexão com o banco de dados
   2. MongoDatabase: Representa um banco de dados específico
   3. MongoCollection: Representa uma coleção (similar a uma tabela SQL)
   4. Document: Representa um documento BSON (similar a JSON)
   
   Operações CRUD:
   - save-order: Salva um novo pedido
   - find-all-orders: Busca todos os pedidos
   - find-order-by-id: Busca pedido por ID
   - delete-order-by-id: Deleta pedido por ID"
  (:import [com.mongodb MongoClient MongoClientSettings ServerAddress]
           [com.mongodb.client MongoCollection MongoDatabase]
           [org.bson Document]
           [org.bson.types ObjectId]
           [java.util ArrayList]))

;; Atom é uma forma de manter estado mutável em Clojure de forma thread-safe
;; Aqui usamos para armazenar a conexão com o banco de dados
(defonce db-connection (atom nil))

(defn connect!
  "Estabelece conexão com o MongoDB.
   
   Parâmetros:
   - config: mapa com {:uri '...' :database '...'}
   
   Esta função cria um MongoClient e armazena no atom db-connection.
   O atom garante que a conexão seja thread-safe e possa ser
   atualizada de forma segura."
  [config]
  (let [mongo-uri (:uri config "mongodb://localhost:27017")
        db-name (:database config "order_db")
        ;; Cria cliente MongoDB
        client (MongoClient. (ServerAddress. "localhost" 27017))
        ;; Obtém o banco de dados
        database (.getDatabase client db-name)]
    ;; Armazena a conexão no atom
    (reset! db-connection {:client client :database database})
    (println "✓ Conectado ao MongoDB:" mongo-uri "/" db-name)))

(defn get-collection
  "Obtém uma coleção do MongoDB.
   
   Uma coleção é similar a uma tabela em bancos SQL.
   Retorna um MongoCollection<Document>."
  [collection-name]
  (when-let [db (:database @db-connection)]
    (.getCollection db collection-name Document)))

(defn document->map
  "Converte um Document do MongoDB para um mapa Clojure.
   
   Document é a classe do driver MongoDB que representa documentos BSON.
   Convertemos para mapa Clojure para trabalhar de forma idiomática."
  [^Document doc]
  (when doc
    (into {}
          (for [[k v] doc]
            [(keyword k) v]))))

(defn map->document
  "Converte um mapa Clojure para um Document do MongoDB.
   
   Remove chaves nil e converte keywords para strings."
  [m]
  (let [doc (Document.)]
    (doseq [[k v] m]
      (when (and k v (not= k :id))  ; não salva :id como campo separado
        (.append doc (name k) v)))
    doc))

(defn save-order
  "Salva um novo Order no MongoDB.
   
   Parâmetros:
   - order: mapa com {:products {...} :price 100.0}
   
   Retorna o pedido salvo com o :id gerado pelo MongoDB.
   
   O MongoDB gera automaticamente um _id do tipo ObjectId."
  [order]
  (let [collection (get-collection "orders")
        doc (map->document order)
        ;; Adiciona timestamp se não existir
        _ (when-not (.containsKey doc "created-at")
            (.append doc "created-at" (java.time.Instant/now)))
        ;; Insere no banco
        result (.insertOne collection doc)
        ;; Obtém o ID gerado
        inserted-id (.getInsertedId result)]
    ;; Retorna o pedido com o ID
    (assoc order :id (.getValue inserted-id))))

(defn find-all-orders
  "Busca todos os pedidos do MongoDB.
   
   Retorna uma sequência de mapas representando os pedidos.
   
   Usa .find() sem filtros para buscar todos os documentos."
  []
  (let [collection (get-collection "orders")
        ;; find() retorna um cursor iterável
        cursor (.find collection)]
    ;; Converte cada Document para mapa
    (mapv (fn [doc]
            (-> doc
                document->map
                (assoc :id (:_id (document->map doc)))))
          cursor)))

(defn find-order-by-id
  "Busca um pedido específico por ID.
   
   Parâmetros:
   - id: pode ser ObjectId ou string
   
   Retorna o pedido encontrado ou nil.
   
   MongoDB usa ObjectId para IDs, então convertemos string para ObjectId."
  [id]
  (let [collection (get-collection "orders")
        ;; Converte string para ObjectId se necessário
        object-id (if (instance? ObjectId id)
                    id
                    (ObjectId. id))
        ;; Cria filtro de busca
        filter (Document. "_id" object-id)
        ;; Busca o documento
        doc (.find collection filter)]
    ;; Retorna o primeiro resultado
    (when-let [result (first doc)]
      (-> result
          document->map
          (assoc :id (:_id (document->map result)))))))

(defn delete-order-by-id
  "Deleta um pedido por ID.
   
   Parâmetros:
   - id: pode ser ObjectId ou string
   
   Retorna true se deletou, false se não encontrou.
   
   Usa deleteOne() do MongoDB para remover o documento."
  [id]
  (let [collection (get-collection "orders")
        ;; Converte string para ObjectId se necessário
        object-id (if (instance? ObjectId id)
                    id
                    (ObjectId. id))
        ;; Cria filtro de exclusão
        filter (Document. "_id" object-id)
        ;; Executa deleção
        result (.deleteOne collection filter)]
    ;; Verifica se deletou algo
    (> (.getDeletedCount result) 0)))

(defn disconnect!
  "Fecha a conexão com o MongoDB.
   
   Boa prática liberar recursos quando a aplicação termina."
  []
  (when-let [client (:client @db-connection)]
    (.close client)
    (reset! db-connection nil)
    (println "✓ Desconectado do MongoDB")))
