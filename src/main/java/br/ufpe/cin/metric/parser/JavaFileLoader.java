package br.ufpe.cin.metric.parser;

import br.ufpe.cin.metric.model.SourceFile;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class JavaFileLoader {

    public static CompilationUnit load(String filePath)
            throws FileNotFoundException {
        return StaticJavaParser.parse(new File(filePath));
    }

    public static SourceFile loadSource(String filePath) throws IOException {
        Path path = Path.of(filePath);
        String source = Files.readString(path);
        return new SourceFile(path.getFileName().toString(), source, StaticJavaParser.parse(source));
    }
}
