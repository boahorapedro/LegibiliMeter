package br.ufpe.cin.metric.model;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;

import java.util.List;

public record SourceFile(String name, String source, CompilationUnit ast) {

    public static SourceFile of(String name, String source) {
        return new SourceFile(name, source, StaticJavaParser.parse(source));
    }

    public List<String> nonBlankLines() {
        return source.lines()
                .filter(line -> !line.isBlank())
                .toList();
    }
}
