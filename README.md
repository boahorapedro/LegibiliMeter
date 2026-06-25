# LegibiliMeter

Ferramenta de linha de comando que atribui uma **nota de legibilidade [0–10]** a código-fonte
Java. Em vez de calcular métricas isoladas (como PMD ou SonarQube), o LegibiliMeter combina
cinco características estruturais em um **score unificado e interpretável**.


## O que a métrica mede

Legibilidade aqui é o grau com que um trecho de código pode ser compreendido por um
desenvolvedor humano com esforço cognitivo mínimo. A ferramenta **não** avalia correção,
desempenho ou segurança — apenas características estruturais e estilísticas associadas pela
literatura à facilidade de compreensão.

A nota final é uma soma ponderada normalizada de cinco features:

| Feature | Peso | O que mede |
| :--- | :---: | :--- |
| Complexidade Cognitiva | 30% | Esforço para compreender o fluxo de controle de um método (Campbell, 2018) |
| Profundidade de Aninhamento | 25% | Nível máximo de blocos de controle encadeados |
| Comprimento de Identificadores | 20% | Tamanho médio dos nomes declarados |
| Número de Parâmetros | 15% | Parâmetros formais por método |
| Comprimento de Linha | 10% | Largura visual média das linhas não-vazias |

## Arquitetura

O processamento é uma sequência de quatro componentes:

```
arquivo.java → Parser & Loader → Feature Extractor → Scorer → Reporter → nota [0–10]
```

| Componente | Responsabilidade | Estado |
| :--- | :--- | :---: |
| **Parser & Loader** | Lê o arquivo e constrói a AST (JavaParser) | ✅ |
| **Feature Extractor** | Percorre o código e coleta o valor bruto de cada feature | ✅ |
| **Scorer** | Normaliza os valores e aplica a soma ponderada | ✅ |
| **Reporter** | Apresenta a nota com detalhamento (terminal + JSON/CSV) | ✅ |

### Contrato da camada de extração

As cinco features são intercambiáveis por trás de três abstrações:

- **`SourceFile`** — o arquivo sob análise empacotado como `(name, source, ast)`: carrega o
  texto-fonte **original** (necessário para o comprimento de linha) e a **AST** (usada pelas
  demais features).
- **`Feature`** — interface comum: `FeatureResult extract(SourceFile)`. Cada feature tem
  implementação interna distinta, mas a mesma assinatura externa.
- **`FeatureResult`** — valor bruto `(feature, max, mean)`: para features por-método guarda o
  pior método (`max`) e a média entre métodos (`mean`); para features por-arquivo
  `max == mean` (via `FeatureResult.single`).

Adicionar uma nova feature = criar uma classe que implementa `Feature`.

## Tecnologias

- **Java 17**
- **JavaParser 3.25** — construção da AST e extração de features
- **Maven** — build e dependências
- **JUnit 5** — testes unitários

## Build e testes

```bash
mvn test          # compila e roda a suíte de testes
mvn -o test       # offline (após o primeiro build popular o cache)
```

Uso da CLI (pipeline completa: parser → extração → score → relatório):

```bash
# nota no terminal
mvn -q compile exec:java -Dexec.args="caminho/para/Arquivo.java"

# exportando também relatórios estruturados
mvn -q compile exec:java \
    -Dexec.args="caminho/para/Arquivo.java --json saida.json --csv saida.csv"
```

Saída no terminal:

```
============================================================
  LegibiliMeter — InventoryManager.java
============================================================
  Nota final : 6.03 / 10   [BOA LEGIBILIDADE]
------------------------------------------------------------
  Feature                             Bruto    Score   Peso
------------------------------------------------------------
  Complexidade Cognitiva               4.00     0.73    30%
  Profundidade de Aninhamento          2.00     0.67    20%
  Comprimento de Identificadores       6.46     0.62    20%
  Número de Parâmetros                 4.00     0.20    15%
  Comprimento de Linha                35.60     0.64    15%
============================================================
```

> Os pesos refletem a calibração Nível A (ver `eval/DECISOES-CALIBRACAO.md`).

### Relatório de avaliação (scorer × nota da turma)

Além do relatório por arquivo acima, o harness de avaliação compara a nota da
ferramenta com a nota humana da turma e gera relatórios CSV + JSON
automaticamente:

```bash
mvn -q compile exec:java \
    -Dexec.mainClass=br.ufpe.cin.metric.eval.EvaluationRunner \
    -Dexec.args="eval/snippets eval/class-scores.csv eval/final-results"
# → escreve eval/final-results.csv e eval/final-results.json
```

## Estrutura do projeto

```
src/
├── main/java/br/ufpe/cin/metric/
│   ├── Main.java                 # ponto de entrada (CLI) — orquestra a pipeline
│   ├── model/                    # SourceFile, FeatureResult (valor bruto max/mean)
│   ├── parser/JavaFileLoader     # Parser & Loader
│   ├── extractor/                # Feature Extractor
│   │   ├── Feature               # contrato comum
│   │   ├── MethodLevelFeature    # base das features por-método
│   │   ├── FeatureExtractor      # roda as 5 features → mapa de resultados
│   │   └── features/*Feature      # as 5 features
│   ├── scorer/                   # Scorer, ScoreResult, FeatureScore
│   └── reporter/Reporter         # saída terminal + JSON + CSV
└── test/
    ├── java/.../extractor/       # testes unitários por feature
    ├── java/.../scorer/          # testes do scorer
    └── resources/samples/        # amostras de código para os testes
```

## Status

- ✅ **Semana 1** — ambiente, build Maven, parser
- ✅ **Semana 2** — as 5 features implementadas e testadas
- ✅ **Semana 3** — Scorer (normalização + soma ponderada) + pipeline completa (extractor → scorer → reporter)
- ✅ **Semana 4** — testes com casos reais e calibração dos limiares
- ✅ **Semana 5** — avaliação da ferramenta
