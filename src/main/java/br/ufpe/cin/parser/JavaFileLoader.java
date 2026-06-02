package br.ufpe.cin.parser;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;

import java.io.File;
import java.io.FileNotFoundException;

public class JavaFileLoader {
    public static CompilationUnit load(String filePath)
            throws FileNotFoundException {
        return StaticJavaParser.parse(new File(filePath));
    }
}
