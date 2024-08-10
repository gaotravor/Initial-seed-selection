package dataProcessing.utils;

import org.eclipse.jdt.core.dom.*;

import java.util.*;

public class NGramVisitor extends ASTVisitor {

    private int maxDepth = 0;
    private int currentDepth = 0;
    private Map<ASTNode, ASTNode> parentMap = new HashMap<>();
    private List<ASTNode> leafNodes = new ArrayList<>();


    @Override
    public boolean preVisit2(ASTNode node) {
        // get depth
        currentDepth++;
        if (currentDepth > maxDepth) {
            maxDepth = currentDepth;
        }

        // record parent
        if (node.getParent() != null) {
            parentMap.put(node, node.getParent());
        }


        return true;
    }

    @Override
    public void postVisit(ASTNode node) {
        currentDepth--;
    }

    private List<ASTNode> getPath(ASTNode node) {
        List<ASTNode> path = new ArrayList<>();
        while (node != null) {
            path.add(node);
            node = parentMap.get(node);
        }
        Collections.reverse(path);
        return path;
    }

    public Map<ASTNode, List<ASTNode>> getPaths() {
        Map<ASTNode, List<ASTNode>> paths = new HashMap<>();
        for (ASTNode leafNode : leafNodes) {
            paths.put(leafNode, getPath(leafNode));
        }
        return paths;
    }

    @Override
    public boolean visit(SimpleName node) {
        leafNodes.add(node);
        return true;
    }

    @Override
    public boolean visit(BooleanLiteral node) {
        leafNodes.add(node);
        return true;
    }

    @Override
    public boolean visit(CharacterLiteral node) {
        leafNodes.add(node);

        return true;
    }

    @Override
    public boolean visit(StringLiteral node) {
        leafNodes.add(node);

        return true;
    }

    @Override
    public boolean visit(NumberLiteral node) {
        leafNodes.add(node);

        return true;
    }

    @Override
    public boolean visit(NullLiteral node) {
        leafNodes.add(node);

        return true;
    }

    @Override
    public boolean visit(TypeLiteral node) {
        leafNodes.add(node);
        return true;
    }


    public int getMaxDepth() {
        return maxDepth;
    }

    public List<ASTNode> getLeafNodes() {
        return leafNodes;
    }



}
