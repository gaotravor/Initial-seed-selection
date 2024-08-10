package utils;

import ilog.concert.IloException;
import ilog.concert.IloLinearNumExpr;
import ilog.concert.IloNumVar;
import ilog.cplex.IloCplex;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ILPSolver {
    private static IloCplex cplex;
    private static final List<IloNumVar> vars = new ArrayList<>();
    private static IloLinearNumExpr targetConstraint;
    private static final List<IloLinearNumExpr> constraints = new ArrayList<>();
    private static int TARGET_MODE = 0;

    public static int SIZE_TARGET = 0;
    public static int TIME_TARGET = 1;

    public static void main(String[] args) throws IOException, ClassNotFoundException, IloException {
        String bugRootPath = "D:\\Project\\VECT++\\实验结果\\covInfo\\";
        List<BitMap> bitMapList = new ArrayList<>();
        File file = new File(bugRootPath + "lineBitMap");
        for (File listFile : Objects.requireNonNull(file.listFiles())) {
            String fileName = listFile.getName().split("@")[0];
            double exeTime = Long.parseLong(listFile.getName().split("@")[1].split("\\.")[0]);
            BitMap bitMap = new BitMap(listFile.getAbsolutePath(),fileName,exeTime);
            bitMapList.add(bitMap);
        }
        if(initAndConstruct(bitMapList, ILPSolver.SIZE_TARGET)){
            double result = cplex.getObjValue();  // 获取解（目标函数最大值）
            System.out.println("目标函数最小值为："+result);
            for(int i =0;i<vars.size();i++){
                if(cplex.getValue(vars.get(i)) == 1.0){
                    System.out.println(bitMapList.get(i).getFileName());
                }
            }
        }
    }

    static {
        System.load("E:\\Program Files\\IBM\\ILOG\\CPLEX_Studio1263\\cplex\\bin\\x64_win64\\cplex1263.dll");
        try {
            cplex = new IloCplex();
            targetConstraint = cplex.linearNumExpr();
        } catch (IloException e) {
            e.printStackTrace();
        }
    }

    /**
     * 初始化环境并且按照最小化目标构造 ILP 求解器
     * @param target 最小化目标
     * @return 判断是否有解
     */
    public static boolean initAndConstruct(List<BitMap> bitMaps, int target) throws IloException {
        init(bitMaps.size(), target);
        construct(bitMaps);
        boolean isSolve = cplex.solve();
        return isSolve;
    }

    /**
     * 初始化求解器环境
     * @param size 总测试用例数目
     * @param target 最小化目标
     */
    public static void init(int size, int target) throws IloException {
        reset();

        for(int i=0; i<size; i++){
            IloNumVar x = cplex.boolVar();
            vars.add(x);
        }
        TARGET_MODE = target;
    }

    /**
     * 构造整个 ILP 求解器
     */
    public static void construct(List<BitMap> bitMaps) throws IloException {
        constraintConstruct(bitMaps);
        // 构造覆盖约束

        BitMap allBitMap = null;
        for (BitMap bitMap : bitMaps) {
            if(allBitMap == null){
                allBitMap = new BitMap(bitMap.getBits().size(),"allBitMap",0);
            }
            allBitMap = BitMap.add(bitMap,allBitMap,"allBitMap",0);
        }

        for(int i = 0; i< bitMaps.size();i++){
            BitMap bitMap = bitMaps.get(i);
            for(int j = 0; j < bitMap.getBits().size() * 32;j++){
                while (constraints.size() <= j){
                    constraints.add(cplex.linearNumExpr());
                }
                if(bitMap.getBit(j)){
                    constraints.get(j).addTerm(1,vars.get(i));
                }
            }
        }
        for(int j = 0; j < Objects.requireNonNull(allBitMap).getBits().size() * 32; j++){
            if(allBitMap.getBit(j)){
                cplex.addGe(constraints.get(j),1);
            }else {
                cplex.addGe(constraints.get(j),0);
            }
        }
    }

    /**
     * 根据设置的 target 生成对应的约束
     */
    private static void constraintConstruct(List<BitMap> bitMaps) throws IloException {
        if(TARGET_MODE == SIZE_TARGET){
            sizeConstraint(bitMaps);
        }
        if(TARGET_MODE == TIME_TARGET){
            timeConstraint(bitMaps);
        }
    }

    /**
     * 要求最小化 测试用例 数目
     */
    private static void sizeConstraint(List<BitMap> bitMaps) throws IloException {
        int size = bitMaps.size();
        for(int i=0;i<size;i++){
            targetConstraint.addTerm(1,vars.get(i));
        }
        cplex.addMinimize(targetConstraint);
    }

    /**
     * 要求最小化 总时间
     */
    private static void timeConstraint(List<BitMap> bitMaps) throws IloException {
        int size = bitMaps.size();

        for(int i=0;i<size;i++){
            targetConstraint.addTerm(bitMaps.get(i).getExeTime(), vars.get(i));
        }

        cplex.addMinimize(targetConstraint);
    }

    public static IloCplex getCplex() {
        return cplex;
    }

    public static List<IloNumVar> getVars() {
        return vars;
    }

    private static void reset() throws IloException {
        cplex = new IloCplex();
        targetConstraint.clear();
        vars.clear();
        constraints.clear();
        TARGET_MODE = 0;
    }
}
