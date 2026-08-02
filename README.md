<p align="center">
  <img src="https://img.shields.io/badge/Java-21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white" alt="Java 21"/>
  <img src="https://img.shields.io/badge/Spring%20Boot-4.0-6DB33F?style=for-the-badge&logo=springboot&logoColor=white" alt="Spring Boot 4"/>
  <img src="https://img.shields.io/badge/MongoDB-Atlas-47A248?style=for-the-badge&logo=mongodb&logoColor=white" alt="MongoDB"/>
  <img src="https://img.shields.io/badge/Maven-Build-C71A36?style=for-the-badge&logo=apachemaven&logoColor=white" alt="Maven"/>
  <img src="https://img.shields.io/badge/Lombok-Boilerplate-BC4521?style=for-the-badge&logo=lombok&logoColor=white" alt="Lombok"/>
</p>

<h1 align="center">Coworking Room Reservation</h1>
<p align="center">API REST para reserva de salas de coworking, construída com Spring Boot e MongoDB  com validação de conflito de horários.</p>

---

## Índice

1. [Sobre o projeto](#sobre-o-projeto)
2. [O que é um banco NoSQL](#o-que-é-um-banco-nosql)
3. [SQL vs NoSQL: as diferenças que importam aqui](#sql-vs-nosql-as-diferenças-que-importam-aqui)
4. [MongoDB na prática: os conceitos usados neste projeto](#mongodb-na-prática-os-conceitos-usados-neste-projeto)
5. [Modelagem de dados do projeto](#modelagem-de-dados-do-projeto)
6. [A regra de negócio: sobreposição de horários](#a-regra-de-negócio-sobreposição-de-horários)
7. [Endpoints da API](#endpoints-da-api)
8. [Como rodar o projeto](#como-rodar-o-projeto)

---

## Sobre o projeto

O **Coworking Room Reservation** é uma API que permite cadastrar salas de um espaço de coworking e reservar essas salas em intervalos de tempo, impedindo que duas reservas se sobreponham na mesma sala.

O projeto foi escolhido justamente para praticar um problema clássico de banco de dados — sobreposição de intervalos — resolvido usando um banco **orientado a documentos** (MongoDB), em vez de um banco relacional tradicional.

---

## O que é um banco NoSQL

**NoSQL** significa *"Not Only SQL"* — não é que esses bancos rejeitem o SQL, é que eles resolvem o problema de armazenar e consultar dados de formas diferentes das tabelas relacionais tradicionais (como MySQL ou PostgreSQL).

Bancos NoSQL surgiram para resolver cenários onde o modelo relacional (tabelas fixas, com colunas e relacionamentos rígidos) se torna limitante — como aplicações que precisam de:

- **Escala horizontal** (adicionar mais servidores em vez de aumentar um servidor só)
- **Flexibilidade de schema** (dados que mudam de formato com frequência)
- **Alta performance de leitura/escrita** em grande volume

Existem 4 grandes categorias de bancos NoSQL:

| Categoria | Exemplo | Como guarda os dados |
|---|---|---|
| **Documento** | MongoDB, CouchDB | Documentos JSON/BSON, parecidos com objetos |
| **Chave-valor** | Redis, DynamoDB | Pares simples de chave → valor |
| **Colunar** | Cassandra, HBase | Dados organizados por colunas, não por linhas |
| **Grafo** | Neo4j | Nós e relacionamentos, ideal pra redes complexas |

Este projeto usa o **MongoDB**, que é da categoria **orientada a documentos** — a mais parecida com a forma como já pensamos em objetos no Java.

---

## SQL vs NoSQL: as diferenças que importam aqui

| Conceito relacional (SQL) | Equivalente no MongoDB (NoSQL) |
|---|---|
| Banco de dados (`database`) | Banco de dados (`database`) |
| Tabela (`table`) | Coleção (`collection`) |
| Linha (`row`) | Documento (`document`) |
| Coluna (`column`) | Campo (`field`) |
| Chave primária (`PRIMARY KEY`) | `_id` |
| `JOIN` entre tabelas | Sem `JOIN` nativo — dados geralmente são aninhados ou duplicados de propósito |
| Schema fixo, validado na criação da tabela | Schema flexível — cada documento pode ter campos diferentes |

A diferença mais importante na prática: em um banco relacional, todas as linhas de uma tabela **precisam** seguir a mesma estrutura de colunas. No MongoDB, cada documento dentro de uma coleção pode ter campos diferentes — o schema é aplicado pela aplicação (no nosso caso, pelas classes Java), não pelo banco.

---

## MongoDB na prática: os conceitos usados neste projeto

### 1. Documentos e Coleções

Cada registro no MongoDB é um **documento** — um objeto no formato **BSON** (uma versão binária do JSON, mais eficiente para o banco processar). Documentos ficam agrupados em **coleções**.

No projeto, temos duas coleções:
- `salas` → documentos representando cada sala do coworking
- `reservas` → documentos representando cada reserva feita

Isso é definido na anotação `@Document`:

```java
@Document(collection = "salas")
public class Sala { ... }
```

### 2. `_id` — a chave primária do MongoDB

Todo documento tem um campo `_id`, que funciona como chave primária. Se você não informar um valor, o MongoDB gera automaticamente um `ObjectId` — um identificador único de 12 bytes.

```java
@Id
private String id;
```

### 3. Ausência de `JOIN` — e por que isso muda a modelagem

Bancos relacionais são otimizados para separar dados em várias tabelas e juntá-las na consulta (`JOIN`). O MongoDB não tem `JOIN` nativo eficiente — a filosofia geral é modelar pensando em **"o que costuma ser lido junto, fica salvo junto"**.

No nosso caso, `Reserva` não guarda o objeto `Sala` inteiro dentro dela — guarda só a referência (`salaId`), e a aplicação (na camada de `Service`) é responsável por buscar a sala quando precisar. Essa é uma modelagem por **referência**, uma das duas formas de relacionar dados no MongoDB (a outra é por **documento aninhado/embedded**, que faria sentido se, por exemplo, quiséssemos guardar um histórico de reservas dentro do próprio documento da sala).

### 4. Índices (`@Indexed`)

Assim como em bancos relacionais, índices no MongoDB aceleram buscas, evitando que o banco precise varrer todos os documentos da coleção.

```java
@Indexed
private String salaId;
```

Como toda consulta de conflito de horário filtra por `salaId`, indexar esse campo evita que o MongoDB precise escanear a coleção `reservas` inteira a cada nova reserva.

### 5. Queries no MongoDB (`@Query`)

O MongoDB usa uma sintaxe própria de consulta, baseada em documentos JSON, bem diferente do SQL. No projeto, a busca por conflito de horário é feita assim:

```java
@Query("{ 'salaId': ?0, 'status': 'CONFIRMED', "
     + "'dataInicio': { $lt: ?2 }, 'dataFinal': { $gt: ?1 } }")
List<Reserva> findOverlapping(String salaId, LocalDateTime inicio, LocalDateTime fim);
```

Repare na diferença de sintaxe: em vez de `WHERE data_inicio < ? AND data_final > ?` (SQL), o MongoDB usa operadores como `$lt` (*less than*) e `$gt` (*greater than*) dentro de um documento de consulta.

### 6. Replica Set — por que sua conexão mostra vários hosts

Ao rodar o projeto conectado ao MongoDB Atlas, você deve ter reparado nos logs que a aplicação se conecta a **três servidores diferentes**, não só um:

```
Discovered replica set primary ac-btzotvv-shard-00-02...
```

Isso é um **Replica Set** — um grupo de servidores que mantêm cópias idênticas dos dados. Um deles é o `PRIMARY` (recebe escritas), os outros são `SECONDARY` (réplicas, usadas para leitura e para assumir o lugar do primary automaticamente se ele cair). É um dos principais motivos pelos quais bancos NoSQL como o MongoDB são escolhidos para aplicações que precisam de alta disponibilidade.

---

## Modelagem de dados do projeto

### Coleção `salas`

```json
{
  "_id": "64f1a2b3c4d5e6f7a8b9c0d1",
  "nome": "Sala Azul",
  "capacidade": 6,
  "recursos": ["tv", "quadro branco"],
  "ativa": true
}
```

### Coleção `reservas`

```json
{
  "_id": "64f1a2b3c4d5e6f7a8b9c0d2",
  "salaId": "64f1a2b3c4d5e6f7a8b9c0d1",
  "userNome": "Maria",
  "dataInicio": "2026-08-01T10:00:00",
  "dataFinal": "2026-08-01T11:00:00",
  "status": "CONFIRMED"
}
```

Repare que `reservas` não contém os dados da sala em si — só o `salaId`, que referencia o documento em `salas`. Isso evita duplicação de dados que mudam com frequência (como a lista de recursos da sala) dentro de cada reserva.

---

## A regra de negócio: sobreposição de horários

O núcleo do projeto é impedir que duas reservas da mesma sala se sobreponham. A lógica matemática usada é a fórmula clássica de intersecção de intervalos:

```
sobrepõe = (inícioA < fimB)  E  (fimA > inícioB)
```

Fluxo, na camada de `Service`:

1. Confirma que a sala existe (`SalaRepository`)
2. Valida que a data final é depois da data de início
3. Consulta reservas que se sobrepõem ao intervalo pedido (`ReservaRepository.findOverlapping`)
4. Se encontrar alguma → lança `ConflictException` (HTTP 409)
5. Se não encontrar → salva a reserva com status `CONFIRMED`

---

## Endpoints da API

| Método | Endpoint | Descrição |
|---|---|---|
| `POST` | `/api/reserva` | Cria uma nova reserva (valida conflito de horário) |
| `GET` | `/api/reserva/sala/{salaId}` | Lista reservas confirmadas de uma sala |
| `DELETE` | `/api/reserva/{id}` | Cancela uma reserva |

> Documentação interativa disponível em `/swagger-ui.html` quando a aplicação está rodando.

---

## Como rodar o projeto

### Pré-requisitos
- Java 21
- Um cluster MongoDB (local via Docker, ou gratuito no [MongoDB Atlas](https://www.mongodb.com/cloud/atlas))

### Configuração

Defina a variável de ambiente `MONGODB_URI` com sua connection string:

```
mongodb+srv://usuario:senha@cluster.mongodb.net/coworking?appName=room-reservation
```

### Executar

```bash
./mvnw spring-boot:run
```

A aplicação sobe em `http://localhost:8080`.
