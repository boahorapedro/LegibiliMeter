# Análise (notebook)

`analise_legibilimeter.ipynb` lê os CSVs de `eval/` e fundamenta a validação da
ferramenta contra a avaliação da turma (correlação, resíduos, smoke test e efeito
da calibração).

## Pré-requisitos

```bash
pip install pandas numpy matplotlib scipy jupyter
```

## Como rodar

Antes, garanta que os CSVs estão atualizados (gerados pelos harnesses Java):

```bash
# da raiz do projeto
mvn -q compile
# correlação com a turma → eval/final-results.csv (+ .json)
mvn -q exec:java -Dexec.mainClass=br.ufpe.cin.metric.eval.EvaluationRunner \
    -Dexec.args="eval/snippets eval/class-scores.csv eval/final-results"
# smoke test → eval/control-results.csv
mvn -q exec:java -Dexec.mainClass=br.ufpe.cin.metric.eval.ControlRunner \
    -Dexec.args="eval/control eval/control-results.csv"
```

Depois abra o notebook:

```bash
jupyter notebook analysis/analise_legibilimeter.ipynb
```

O caminho dos CSVs se ajusta sozinho: funciona rodando da raiz do projeto ou de
dentro de `analysis/`.

## Entradas (de `eval/`)

- `final-results.csv` — nota da ferramenta × média da turma, por snippet (+ resíduo)
- `class-scores.csv` — média/mediana da turma por snippet
- `control-results.csv` — resultado do smoke test nos exemplos de controle
