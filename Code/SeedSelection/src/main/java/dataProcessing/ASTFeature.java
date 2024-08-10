package dataProcessing;

import dataProcessing.utils.JavaAST;
import dataProcessing.utils.NGramVisitor;
import dataProcessing.utils.NgramLibrary;
import utils.DTPlatform;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ASTFeature {

    public static void extract(String rootPath,String project) throws IOException {
        int n = 3;

        ASTParser parser = ASTParser.newParser(AST.JLS8);
        String codePath = rootPath + DTPlatform.FILE_SEPARATOR + "tmp"+ DTPlatform.FILE_SEPARATOR +project+DTPlatform.FILE_SEPARATOR+"code";
        new File(codePath).mkdirs();
        String featurePath = rootPath + DTPlatform.FILE_SEPARATOR+"featureInfo"+DTPlatform.FILE_SEPARATOR+project;
        new File(featurePath).mkdirs();



        List<JavaAST> ASTList = new ArrayList<>();
        NgramLibrary library = new NgramLibrary();
        for (File javaFile : Objects.requireNonNull(new File(codePath).listFiles())) {
            String sourceCode = Files.readString(Path.of(javaFile.getAbsolutePath()), StandardCharsets.UTF_8);
            parser.setSource(sourceCode.toCharArray());
            CompilationUnit compilationUnit = (CompilationUnit) parser.createAST(null);
            NGramVisitor visitor = new NGramVisitor();
            compilationUnit.accept(visitor);
            JavaAST ast = new JavaAST(javaFile.getName().replace(".java",""), javaFile.getName().replace(".java",""), visitor.getMaxDepth());
            ast.buildNgrams(visitor.getPaths());
            ASTList.add(ast);
        }

        library.compute(ASTList);
        System.out.println(library.getOneGram().size());
        System.out.println(library.getTwoGram().size());
        System.out.println(library.getThreeGram().size());

        StringBuilder nGramResult = new StringBuilder();
        for (int i=0;i<=library.getThreeGram().size();i++){
            nGramResult.append(",").append(i);
        }
        nGramResult.append("\n");
        for(int index = 0;index<ASTList.size();index++){
            JavaAST javaAST = ASTList.get(index);
            nGramResult.append(index);
            nGramResult.append(",").append(javaAST.getOriginClassName());
            for (List<Integer> list : library.getThreeGram().keySet()) {
                if (javaAST.getThreeGram().containsKey(list)){
                    nGramResult.append(",").append(javaAST.getThreeGram().get(list));
                }else {
                    nGramResult.append(",").append(0);
                }
            }
            nGramResult.append("\n");
        }
        FileWriter fileWriter = new FileWriter(featurePath+DTPlatform.FILE_SEPARATOR+"3gramASTVectors.csv");
        fileWriter.write(nGramResult.toString());
        fileWriter.close();

    }
}
