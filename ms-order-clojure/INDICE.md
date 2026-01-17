# 📑 ÍNDICE DE NAVEGAÇÃO - MS-ORDER-CLOJURE

## 🎯 Por Onde Começar?

### Se você quer...

#### 1️⃣ **Entender o que foi feito**
→ Comece pelo **[SUMARIO.md](SUMARIO.md)**  
📄 Resumo executivo de toda a implementação (13KB)

#### 2️⃣ **Executar a aplicação rapidamente**
→ Siga o **[TUTORIAL.md](TUTORIAL.md)**  
📘 Passo a passo completo desde instalação até testes (14KB)

#### 3️⃣ **Aprender Clojure do zero**
→ Leia o **[GUIA_COMPLETO.md](GUIA_COMPLETO.md)**  
📚 Explicação detalhada de cada conceito e arquivo (18KB)

#### 4️⃣ **Comparar com Kotlin**
→ Veja **[COMPARACAO.md](COMPARACAO.md)**  
📊 Comparação lado a lado de código, métricas e filosofias (15KB)

#### 5️⃣ **Testar e validar**
→ Use o **[TESTES.md](TESTES.md)**  
🧪 Guia completo de testes manuais e automatizados (10KB)

#### 6️⃣ **Visão geral rápida**
→ Leia o **[README.md](README.md)**  
📖 Introdução, tecnologias e conceitos básicos (9KB)

---

## 📁 Estrutura de Arquivos

### 📚 Documentação (6 arquivos - 70KB)

| Arquivo | Tamanho | Descrição | Quando Ler |
|---------|---------|-----------|------------|
| **[SUMARIO.md](SUMARIO.md)** | 13KB | Resumo executivo completo | Primeiro contato |
| **[README.md](README.md)** | 9KB | Visão geral e introdução | Início |
| **[TUTORIAL.md](TUTORIAL.md)** | 14KB | Passo a passo de execução | Para executar |
| **[GUIA_COMPLETO.md](GUIA_COMPLETO.md)** | 18KB | Explicação detalhada | Para aprender |
| **[COMPARACAO.md](COMPARACAO.md)** | 15KB | Kotlin vs Clojure | Para comparar |
| **[TESTES.md](TESTES.md)** | 10KB | Guia de testes | Para validar |

### 💻 Código-fonte (8 arquivos - 31KB)

#### Core e Configuração

| Arquivo | Tamanho | Descrição |
|---------|---------|-----------|
| **[core.clj](src/ms_order/core.clj)** | 7.3KB | Ponto de entrada, inicialização, main |
| **[config.edn](resources/config.edn)** | 201B | Configuração da aplicação |
| **[deps.edn](deps.edn)** | 1.1KB | Dependências do projeto |

#### Models (Dados)

| Arquivo | Tamanho | Descrição |
|---------|---------|-----------|
| **[order.clj](src/ms_order/models/order.clj)** | 2.0KB | Modelo de Order (pedido) |
| **[schemas.clj](src/ms_order/models/schemas.clj)** | 1.9KB | Validação e schemas de request/response |

#### Repository (Banco de Dados)

| Arquivo | Tamanho | Descrição |
|---------|---------|-----------|
| **[order_repository.clj](src/ms_order/repository/order_repository.clj)** | 5.6KB | Operações MongoDB (CRUD) |

#### Service (Lógica de Negócio)

| Arquivo | Tamanho | Descrição |
|---------|---------|-----------|
| **[order_service.clj](src/ms_order/service/order_service.clj)** | 4.2KB | Lógica de negócio de pedidos |

#### Controller (API HTTP)

| Arquivo | Tamanho | Descrição |
|---------|---------|-----------|
| **[order_controller.clj](src/ms_order/controller/order_controller.clj)** | 6.8KB | Handlers HTTP e rotas |

#### gRPC Mock

| Arquivo | Tamanho | Descrição |
|---------|---------|-----------|
| **[product_client_mock.clj](src/ms_order/grpc/product_client_mock.clj)** | 3.9KB | Mock do cliente gRPC de produtos |

### 🧪 Testes (1 arquivo - 7KB)

| Arquivo | Tamanho | Descrição |
|---------|---------|-----------|
| **[order_service_test.clj](test/ms_order/service/order_service_test.clj)** | 7.1KB | Testes unitários de exemplo |

### ⚙️ Infraestrutura (2 arquivos)

| Arquivo | Descrição |
|---------|-----------|
| **[docker-compose.yml](docker-compose.yml)** | MongoDB + Mongo Express |
| **[.gitignore](.gitignore)** | Arquivos ignorados pelo git |

---

## 🗺️ Fluxo de Leitura Recomendado

### 🏃 Rápido (30 minutos)
1. **SUMARIO.md** (5 min) - Entenda o que foi feito
2. **README.md** (10 min) - Visão geral
3. **TUTORIAL.md** (15 min) - Execute e teste

### 📖 Completo (2-3 horas)
1. **SUMARIO.md** (5 min) - Resumo
2. **README.md** (10 min) - Introdução
3. **TUTORIAL.md** (30 min) - Execute passo a passo
4. **GUIA_COMPLETO.md** (60 min) - Aprenda cada conceito
5. **COMPARACAO.md** (30 min) - Compare com Kotlin
6. **TESTES.md** (20 min) - Valide tudo

### 🎓 Aprendizado Profundo (1 semana)
**Dia 1-2:** Documentação completa  
**Dia 3-4:** Leitura de código com REPL aberto  
**Dia 5:** Modificações e experimentos  
**Dia 6:** Testes e validações  
**Dia 7:** Criar seu próprio endpoint

---

## 📊 Estatísticas do Projeto

### Métricas Gerais
- **Total de arquivos:** 17
- **Total de linhas:** 4.266
- **Linhas de código:** ~600
- **Linhas de documentação:** ~2.000
- **Linhas de comentários:** ~1.666

### Por Categoria

| Categoria | Arquivos | Linhas | % do Total |
|-----------|----------|--------|------------|
| 📚 Documentação | 6 | ~2.000 | 47% |
| 💻 Código | 8 | ~600 | 14% |
| 💬 Comentários | - | ~1.666 | 39% |
| 🧪 Testes | 1 | ~200 | - |
| ⚙️ Config | 2 | ~50 | - |

### Linguagem da Documentação
- 🇧🇷 **100% em Português Brasileiro**
- 📝 ~15.000 palavras
- 📖 ~80 páginas de conteúdo
- 💡 50+ exemplos de código
- 🎯 100% dos conceitos explicados

---

## 🎯 Referência Rápida de Arquivos

### Preciso entender...

#### Conceitos de Clojure
- **Sintaxe básica** → GUIA_COMPLETO.md (seção: Conceitos-Chave)
- **Namespaces** → GUIA_COMPLETO.md (deps.edn)
- **Mapas e keywords** → models/order.clj
- **Funções** → Qualquer arquivo .clj
- **Threading macros** → controller/order_controller.clj
- **Atoms** → repository/order_repository.clj
- **Higher-order functions** → grpc/product_client_mock.clj

#### Como fazer...

#### Criar um pedido
- **Código** → service/order_service.clj (create-order)
- **API** → controller/order_controller.clj (create-order-handler)
- **Exemplo** → TUTORIAL.md (seção 5.2)

#### Conectar ao MongoDB
- **Código** → repository/order_repository.clj (connect!)
- **Config** → resources/config.edn
- **Tutorial** → TUTORIAL.md (Passo 2)

#### Calcular preço
- **Mock** → grpc/product_client_mock.clj (calculate-total-price)
- **Service** → service/order_service.clj (create-order)
- **Explicação** → GUIA_COMPLETO.md (seção do mock)

#### Criar endpoints HTTP
- **Rotas** → controller/order_controller.clj (defroutes)
- **Handlers** → controller/order_controller.clj (funções *-handler)
- **Middleware** → controller/order_controller.clj (def app)

#### Executar testes
- **Código** → test/ms_order/service/order_service_test.clj
- **Tutorial** → TESTES.md (Testes automatizados)
- **Executar** → `clojure -M:test`

---

## 🔍 Busca Rápida

### Encontrar por conceito

| Conceito | Arquivo | Linha/Seção |
|----------|---------|-------------|
| Imutabilidade | GUIA_COMPLETO.md | Conceitos-Chave #1 |
| Threading Macros | GUIA_COMPLETO.md | Conceitos-Chave #3 |
| REPL | TUTORIAL.md | Passo 7 |
| MongoDB CRUD | repository/order_repository.clj | save-order, find-all-orders |
| HTTP Handlers | controller/order_controller.clj | *-handler functions |
| Validação | models/schemas.clj | valid-create-order-request? |
| Mock gRPC | grpc/product_client_mock.clj | Todo o arquivo |
| Startup | core.clj | init! function |
| Configuração | resources/config.edn | Todo o arquivo |

### Encontrar por funcionalidade

| Funcionalidade | Localização |
|----------------|-------------|
| Criar pedido | POST /orders → controller → service → repository |
| Listar pedidos | GET /orders → controller → service → repository |
| Buscar por ID | GET /orders/:id → controller → service → repository |
| Deletar pedido | DELETE /orders/:id → controller → service → repository |
| Health check | GET /health → controller |
| Cálculo de preço | service/order_service.clj + grpc mock |

---

## 🚀 Comandos Rápidos

```bash
# Executar aplicação
clojure -M:run

# Executar testes
clojure -M:test

# REPL interativo
clojure

# Baixar dependências
clojure -P

# Limpar cache
rm -rf .cpcache

# Iniciar MongoDB
docker-compose up -d mongodb

# Parar MongoDB
docker-compose down

# Ver logs MongoDB
docker logs ms-order-mongodb

# Acessar MongoDB shell
docker exec -it ms-order-mongodb mongosh
```

---

## 📞 Contatos e Links

### Documentação Externa
- [Clojure.org](https://clojure.org/) - Site oficial
- [ClojureDocs](https://clojuredocs.org/) - Documentação com exemplos
- [Ring Documentation](https://github.com/ring-clojure/ring/wiki)
- [Compojure Wiki](https://github.com/weavejester/compojure/wiki)

### Este Projeto
- Repositório: `kotlin-grpc-api-microservices`
- Branch: `ms-order-clojure`
- Linguagem: Clojure 1.11.1
- Porta: 8082

---

## ✅ Checklist de Uso

### Primeira vez
- [ ] Ler SUMARIO.md
- [ ] Ler README.md
- [ ] Instalar pré-requisitos (Java, Clojure, Docker)
- [ ] Seguir TUTORIAL.md passo a passo
- [ ] Criar primeiro pedido
- [ ] Explorar MongoDB

### Aprendizado
- [ ] Ler GUIA_COMPLETO.md
- [ ] Entender cada arquivo .clj
- [ ] Experimentar no REPL
- [ ] Modificar código
- [ ] Ver resultados

### Validação
- [ ] Executar todos os testes do TESTES.md
- [ ] Verificar health check
- [ ] Criar, listar, buscar, deletar pedidos
- [ ] Inspecionar dados no MongoDB
- [ ] Executar testes automatizados

### Comparação
- [ ] Ler COMPARACAO.md
- [ ] Comparar com código Kotlin original
- [ ] Entender diferenças de paradigma
- [ ] Avaliar trade-offs

---

## 🎓 Conclusão

Este índice serve como **ponto de navegação central** para toda a documentação e código do projeto ms-order-clojure.

**Dica:** Salve este arquivo nos seus favoritos para referência rápida!

---

**Happy Learning! 🚀**

*Última atualização: Janeiro 2024*
