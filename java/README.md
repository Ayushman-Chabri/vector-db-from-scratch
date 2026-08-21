# Your-OWN-AI (Java)

A vector database built from scratch — three interchangeable search algorithms
(BruteForce, KD-Tree, HNSW), a hand-rolled JSON layer, a zero-dependency REST
API, and a local RAG pipeline that talks to Ollama — ported from a
[C++ original](https://github.com/perryvegehan/Your-OWN-AI) to idiomatic Java.

Nothing here comes from a library. The distance functions, the search
algorithms (including HNSW — the same multilayer graph algorithm real vector
databases like Pinecone, Weaviate, and Milvus use), the JSON parser, and the
HTTP server are all implemented directly, using only the JDK standard library.

## Why this exists

Most "I built a RAG app" portfolio projects wire together an embedding API, a
hosted vector DB, and an LLM API — three levels of abstraction away from
understanding what's actually happening. This project goes the other
direction: implement the vector search engine itself, including the exact
graph algorithm that makes modern vector search fast, then build the RAG
pipeline on top of it.

## Architecture

```
┌─────────────────────────────────────────────────────────┐
│  frontend/index.html  (vanilla HTML/CSS/JS, zero deps)   │
└───────────────────────────┬─────────────────────────────┘
                             │ REST (JSON over HTTP)
┌───────────────────────────▼─────────────────────────────┐
│  VectorDbServer  (com.sun.net.httpserver, JDK built-in)  │
│  12 routes: search · insert · delete · items · stats ·   │
│  benchmark · hnsw-info · doc/insert · doc/list ·          │
│  doc/delete · doc/ask · compare                          │
└───────────────────────────┬─────────────────────────────┘
                             │
        ┌────────────────────┼─────────────────────┐
        ▼                    ▼                      ▼
┌───────────────┐   ┌────────────────┐    ┌──────────────────┐
│   VectorDB      │   │   DocumentDB   │    │  GenericLlmClient │
│  BruteForce      │   │  (RAG storage) │    │  (OpenAI-schema   │
│  KD-Tree         │   │  + TextChunker │    │  compatible: any  │
│  HNSW (from      │   │                │    │  provider)        │
│  scratch)         │   └───────┬────────┘    └──────────────────┘
└───────────────────┘           │
                                  ▼
                          ┌───────────────┐
                          │  OllamaClient   │
                          │  (embed + gen)  │
                          └───────────────┘
```

## Search algorithms

| Algorithm | Complexity | Role |
|---|---|---|
| **BruteForce** | O(N·d) | Ground-truth oracle — exhaustive linear scan, used to verify the other two are correct |
| **KD-Tree** | O(log N) avg | Binary space partitioning, exact results, degrades in high dimensions |
| **HNSW** | O(log N) | Multilayer probabilistic graph, approximate but fast at scale — same algorithm production vector databases use |

All three implement the same `knn(query, k, distFn) → List<SearchResult>`
shape, so they're interchangeable and directly comparable. `VectorDB.benchmark()`
and the frontend's "Compare Algorithms" panel run identical queries against
all three and report real timing.

## Requirements

- JDK 25+ (built and tested on Eclipse Temurin 25.0.1)
- Maven 3.9+
- [Ollama](https://ollama.com), with two models pulled:
  ```
  ollama pull nomic-embed-text
  ollama pull llama3.2
  ```
  (Only required for the Documents/Ask AI/Compare tabs — Search and
  Benchmark work without Ollama running.)
## Docker

```bash
docker build -t vector-db-from-scratch .
docker run -p 8080:8080 --add-host=host.docker.internal:host-gateway vector-db-from-scratch
```

The container reaches Ollama on the host via `host.docker.internal`
(configurable through `OLLAMA_HOST`/`OLLAMA_PORT` env vars, both consumed
by `Main.java` at startup). `--add-host` ensures this resolves correctly
on native Linux Docker Engine as well as Docker Desktop.

## Running it

```bash
cd java
mvn compile
mvn exec:java
```

The server starts on `http://localhost:8080`, pre-loaded with 20 demo
vectors across 4 categories (CS, math, food, sports). Then open
`frontend/index.html` directly in a browser — no build step, no dev server.

## Running tests

```bash
cd java
mvn test
```

Tests are split into two kinds:
- **Deterministic, always run**: distance math, all three search algorithms
  (including cross-checks that KD-Tree and HNSW agree with BruteForce on
  identical queries), JSON round-trips, HTTP route integration tests against
  a real server on an OS-assigned port.
- **Live-dependency, gated with `Assumptions.assumeTrue`**: a few Ollama
  integration tests skip (not fail) if Ollama isn't running locally, so the
  suite stays green in any environment.

`/doc/insert`, `/doc/ask`, and `/compare` are deliberately left out of the
automated suite — they require real Ollama round-trips (and, for `/compare`,
a live third-party API key), and are better verified manually via the
frontend or curl.

## REST API

| Method | Endpoint | Description |
|---|---|---|
| GET | `/status` | Health check |
| GET | `/items` | List all vectors |
| GET | `/stats` | Vector/doc counts, dims, Ollama status |
| GET | `/search?v=f1,f2,...&k=5&metric=cosine&algo=hnsw` | k-NN search |
| GET | `/benchmark?v=f1,f2,...&k=5&metric=cosine` | Time all 3 algorithms on one query |
| GET | `/hnsw-info` | Graph structure (layers, nodes, edges) |
| POST | `/insert` | `{"label":..., "embedding":[...]}` |
| DELETE | `/delete/:id` or `/delete?id=` | Remove a vector |
| POST | `/doc/insert` | `{"title":..., "text":...}` — chunks, embeds via Ollama, indexes |
| GET | `/doc/list` | List documents (with word count + preview) |
| DELETE | `/doc/delete/:id` or `/doc/delete?id=` | Remove a document |
| POST | `/doc/ask` | `{"question":..., "k":3}` — full RAG: embed → retrieve → generate |
| POST | `/compare` | `{"question":..., "k":3, "providers":[{"name","endpoint","apiKey","model"}]}` — local RAG vs any number of external LLM providers, timed |

```bash
curl "http://localhost:8080/search?v=0.9,0.85,0.72,0.68,0.12,0.08,0.15,0.10,0.05,0.08,0.06,0.09,0.07,0.11,0.08,0.06&k=3&metric=cosine&algo=hnsw"

curl -X POST http://localhost:8080/doc/ask \
  -H "Content-Type: application/json" \
  -d '{"question":"What is dynamic programming?","k":3}'
```

## The Compare feature

`/compare` runs one question against your local RAG pipeline (HNSW retrieval
+ Ollama `llama3.2`) alongside any number of external providers you add from
the frontend — each just `{name, endpoint, API key, model}`. It works with
any provider speaking the OpenAI-compatible chat completions schema (OpenAI,
Groq, Together, Mistral, DeepSeek, Fireworks, OpenRouter, and others), which
covers most hosted LLM APIs without needing a bespoke client per provider.
Keys are held only in browser memory for the duration of the request — never
persisted or logged server-side.

## Project structure

```
java/
├── pom.xml
├── frontend/
│   └── index.html                 # custom UI, zero build step
└── src/main/java/com/yourownai/
    ├── Main.java
    ├── model/       VectorItem
    ├── distance/     Distance, DistFn, DistFnFactory
    ├── index/        BruteForce, KDTree, HNSW, SearchResult, GraphInfo
    ├── db/           VectorDB, DocumentDB, TextChunker, Demo
    ├── json/         JsonWriter, JsonParser (hand-rolled, no library)
    ├── ollama/       OllamaClient
    ├── llm/          GenericLlmClient (any OpenAI-schema provider)
    └── server/       VectorDbServer + 13 route handlers (JDK HttpServer)
```

## Design notes worth knowing

- **`List<Float>` over `float[]`** for vector storage — arrays don't get
  correct `equals()`/`hashCode()` for free, which would break the `VectorItem`
  record's auto-generated methods. Flagged as a future perf optimization.
- **All distance math in `double`**, not `float` — `Math.sqrt` returns
  `double`, and computing in the wider type avoids compounding rounding
  error before any final narrowing.
- **JDK's built-in `HttpServer`** instead of a framework — zero dependencies,
  matches the original's `httplib.h` philosophy, and its context-prefix
  matching (`/delete` also catches `/delete/5`) meant a full path-parameter
  router was unnecessary.
- **Hand-rolled JSON**, not Jackson/Gson — preserves the "from scratch"
  learning goal; a library would be the production-realistic choice.

## Known issue: Docker Desktop on Windows + `localhost`

If `http://localhost:8080` hangs indefinitely in the browser while
`docker ps` shows the container running, this is a known Docker Desktop
IPv6-loopback routing quirk on some Windows setups — Windows resolves
`localhost` to `::1` (IPv6) first, which doesn't route through Docker
Desktop's port mapping reliably in some configurations. Use
`http://127.0.0.1:8080` instead (the frontend's default now points there).
