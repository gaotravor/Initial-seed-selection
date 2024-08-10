package dataProcessing.utils;

import org.eclipse.jdt.core.dom.ASTNode;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class JavaAST {
    private final String originClassName;
    private final String javaPath;
    private final int depth;

    private Map<Integer, Integer> oneGram = new HashMap<>();
    private Map<List<Integer>, Integer> twoGram = new HashMap<>();
    private Map<List<Integer>, Integer> threeGram = new HashMap<>();

    public JavaAST(String originClassName, String javaPath, int depth) {
        this.originClassName = originClassName;
        this.javaPath = javaPath;
        this.depth = depth;
    }

    public void buildNgrams(Map<ASTNode, List<ASTNode>> paths) {
        for (ASTNode astNode : paths.keySet()) {
            List<ASTNode> path = paths.get(astNode);

            List<Integer> one = IntStream.range(0, path.size())
                    .mapToObj(i -> path.get(i).getNodeType())
                    .collect(Collectors.toList());

            List<List<Integer>> two = IntStream.range(0, path.size() - 1)
                    .mapToObj(i -> Arrays.asList(path.get(i).getNodeType(), path.get(i+1).getNodeType()))
                    .collect(Collectors.toList());

            List<List<Integer>> three = IntStream.range(0, path.size() - 2)
                    .mapToObj(i -> Arrays.asList(path.get(i).getNodeType(), path.get(i+1).getNodeType(), path.get(i+2).getNodeType()))
                            .collect(Collectors.toList());

            one.forEach(integer -> {
                oneGram.compute(integer, (k, v) -> v == null? 1 : v + 1);
            });

            two.forEach(list -> {
                twoGram.compute(list, (k, v) -> v == null? 1 : v + 1);
            });

            three.forEach(list -> {
                threeGram.compute(list, (k, v) -> v == null? 1 : v + 1);
            });
        }
    }

    public String getOriginClassName() {
        return originClassName;
    }

    public String getJavaPath() {
        return javaPath;
    }

    public int getDepth() {
        return depth;
    }

    public Map<Integer, Integer> getOneGram() {
        return oneGram;
    }

    public Map<List<Integer>, Integer> getTwoGram() {
        return twoGram;
    }

    public Map<List<Integer>, Integer> getThreeGram() {
        return threeGram;
    }

    @Override
    public String toString() {
        return "JavaAST{ " + "\n" +
                "originClassName: " + originClassName + "\n" +
                "javaPath: " + javaPath + "\n" +
                "}";
    }


}
