package dataProcessing.utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NgramLibrary {
    private Map<Integer, Integer> oneGram = new HashMap<>();
    private Map<List<Integer>, Integer> twoGram = new HashMap<>();
    private Map<List<Integer>, Integer> threeGram = new HashMap<>();

    public NgramLibrary() {

    }

    public void compute(List<JavaAST> asts) {
        for (JavaAST ast : asts) {
            ast.getOneGram().forEach((k, v) -> oneGram.merge(k, v, Integer::sum));
            ast.getTwoGram().forEach((k, v) -> twoGram.merge(k, v, Integer::sum));
            ast.getThreeGram().forEach((k, v) -> threeGram.merge(k, v, Integer::sum));
        }
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


}
