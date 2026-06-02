package br.ufpe.cin.metric.extractor;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;

import java.util.List;

/**
 * Arquivo Java sob análise: nome, texto-fonte original e AST.
 *
 * <p>Features baseadas em estrutura usam {@link #ast()}; features de apresentação
 * visual (ex.: comprimento de linha) usam o texto original, pois a AST não preserva
 * a formatação real do arquivo.
 */
public record SourceFile(String name, String source, CompilationUnit ast) {

    /** Cria a partir de um trecho de código, parseando o fonte. Útil em testes. */
    public static SourceFile of(String name, String source) {
        return new SourceFile(name, source, StaticJavaParser.parse(source));
    }

    /** Linhas não-vazias do fonte original, preservando a formatação real. */
    public List<String> nonBlankLines() {
        return source.lines()
                .filter(line -> !line.isBlank())
                .toList();
    }
}
