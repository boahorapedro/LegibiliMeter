package br.ufpe.cin.metric.extractor;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;

import java.util.ArrayList;
import java.util.List;

/**
 * Feature 4: Comprimento Médio de Identificadores.
 *
 * <p>Média (em caracteres) dos nomes que o autor declara no arquivo: tipos
 * (classes/enums/records), métodos, parâmetros e variáveis/campos. É uma feature
 * por-arquivo, então {@code max == mean}. Limiares (ver roteiro):
 * &lt; 8 curtos demais | 8-15 ideal | &gt; 15 longos demais.
 *
 * <p>Decisão de definição: medimos identificadores <em>declarados</em>, não todo
 * {@code SimpleName} da AST — usos de tipos da JDK e nomes de métodos chamados não
 * são escolhas do autor e enviesariam a média. Revisar na calibração (Semana 4) se
 * o grupo preferir a contagem ampla.
 */
public class IdentifierLengthFeature implements Feature {

    public static final String NAME = "identifierLength";

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public FeatureResult extract(SourceFile file) {
        List<String> names = new ArrayList<>();
        file.ast().findAll(TypeDeclaration.class).forEach(t -> names.add(t.getNameAsString()));
        file.ast().findAll(MethodDeclaration.class).forEach(m -> names.add(m.getNameAsString()));
        file.ast().findAll(Parameter.class).forEach(p -> names.add(p.getNameAsString()));
        file.ast().findAll(VariableDeclarator.class).forEach(v -> names.add(v.getNameAsString()));

        if (names.isEmpty()) {
            return FeatureResult.single(NAME, 0);
        }

        double average = names.stream()
                .mapToInt(String::length)
                .average()
                .orElse(0);
        return FeatureResult.single(NAME, average);
    }
}
