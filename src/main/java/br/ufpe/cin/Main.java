package br.ufpe.cin;

import br.ufpe.cin.parser.JavaFileLoader;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;

public class Main {

    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            System.out.println("Uso: java Main <caminho-do-arquivo.java>");
            return;
        }

        CompilationUnit cu = JavaFileLoader.load(args[0]);

        System.out.println("Arquivo carregado com sucesso!");
        System.out.println("Métodos encontrados:");

        cu.findAll(MethodDeclaration.class)
                .forEach(m -> System.out.println("  - " + m.getNameAsString()));
    }
}