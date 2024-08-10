package dataProcessing;

import utils.DTPlatform;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Unit;
import soot.options.Options;
import soot.toolkits.graph.*;
import soot.util.dot.DotGraph;

import java.io.*;
import java.util.*;

// dot -Tpng -o output.png input.dot
// 提取 CFG 的 NGram 特征
public class CFGFeature {
    public static LinkedList<String> excludePathList;

    public static void extract(String rootPath, String project) throws IOException {
        if (project.contains("HotspotTests-Java") || project.contains("Openj9Test-Test")){
            simpleProject(rootPath,project);
        }else if (project.contains("CollectProject")){
            CollectProject(rootPath,project);
        }else {
            System.out.println("You need to select the appropriate function entry");
            System.exit(0);
        }
    }

    public static void simpleProject(String rootPath, String project) throws IOException {

        int n = 3;

        String benchmarkPath = rootPath + DTPlatform.FILE_SEPARATOR + "benchmarks"+ DTPlatform.FILE_SEPARATOR + project;
        String tmpFilePath = rootPath + DTPlatform.FILE_SEPARATOR + "tmp"+DTPlatform.FILE_SEPARATOR+project+DTPlatform.FILE_SEPARATOR+"CFG";
        new File(tmpFilePath).mkdirs();

        Map<String, Map<String,Integer>> name2ngram = constructNGram(project,benchmarkPath,tmpFilePath,n);

        Set<String> ngramKey = new HashSet<>();
        for (String key : name2ngram.keySet()) {
            ngramKey.addAll(name2ngram.get(key).keySet());
        }
        // 保存ngram
        String outPath = rootPath+DTPlatform.FILE_SEPARATOR+"featureInfo"+DTPlatform.FILE_SEPARATOR+project;
        new File(outPath).mkdirs();
        File outFile = new File(outPath+DTPlatform.FILE_SEPARATOR+n+"gramCFGVectors.csv");
        BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(outFile));
        // 获得name2ngram的value的size
        int size = ngramKey.size() + 1;
        for(int i=0;i<size;i++){
            bufferedWriter.write(","+i);
        }
        bufferedWriter.newLine();
        int index = 0;
        for (String key : name2ngram.keySet()) {
            bufferedWriter.write(index+++",");
            bufferedWriter.write(key);
            Map<String, Integer> temp = name2ngram.get(key);
            for (String ngram : ngramKey) {
                bufferedWriter.write(","+temp.get(ngram));
            }
            bufferedWriter.newLine();
        }
        bufferedWriter.flush();
        bufferedWriter.close();
    }

    public static void CollectProject(String rootPath, String project) throws IOException{
        String CollectProjectPath = rootPath + DTPlatform.FILE_SEPARATOR + "benchmarks" + DTPlatform.FILE_SEPARATOR + project;
        Map<String, Map<String,Integer>> name2ngram = new HashMap<>();
        Set<String> ngramKey = new HashSet<>();
        int n = 3;
        for (File subProject : Objects.requireNonNull(new File(CollectProjectPath).listFiles())) {
            String benchmarkPath = subProject.getAbsolutePath();
            String tmpFilePath = rootPath + DTPlatform.FILE_SEPARATOR + "tmp"+DTPlatform.FILE_SEPARATOR+project+DTPlatform.FILE_SEPARATOR+subProject.getName()+DTPlatform.FILE_SEPARATOR+"CFG";
            new File(tmpFilePath).mkdirs();

            Map<String, Map<String,Integer>> tempMap = constructNGram(subProject.getName(),benchmarkPath,tmpFilePath,n);
            for (String key : tempMap.keySet()) {
                name2ngram.put(key,tempMap.get(key));
            }
        }
        for (String key : name2ngram.keySet()) {
            ngramKey.addAll(name2ngram.get(key).keySet());
        }
        for (String className : name2ngram.keySet()) {
            Map<String, Integer> temp = name2ngram.get(className);
            for (String key : ngramKey) {
                if(!temp.containsKey(key)){
                    temp.put(key,0);
                }
            }
            name2ngram.put(className,temp);
        }
        // 保存ngram
        String outPath = rootPath+DTPlatform.FILE_SEPARATOR+"featureInfo"+DTPlatform.FILE_SEPARATOR+project;
        new File(outPath).mkdirs();
        File outFile = new File(outPath+DTPlatform.FILE_SEPARATOR+n+"gramCFGVectors.csv");
        BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(outFile));
        // 获得name2ngram的value的size
        int size = ngramKey.size() + 1;
        for(int i=0;i<size;i++){
            bufferedWriter.write(","+i);
        }
        bufferedWriter.newLine();
        int index = 0;
        for (String key : name2ngram.keySet()) {
            bufferedWriter.write(index+++",");
            bufferedWriter.write(key);
            Map<String, Integer> temp = name2ngram.get(key);
            for (String ngram : ngramKey) {
                bufferedWriter.write(","+temp.get(ngram));
            }
            bufferedWriter.newLine();
        }
        bufferedWriter.flush();
        bufferedWriter.close();

    }
    /**
     * 构造className 到 CFG的映射, Map<String, Map<String,Integer>>: Map<className, Map<nGram, count>>
     * @param benchmarkPath 需要解析的项目根目录
     * @param tmpFilePath 生成的CFG文件的存放路径
     * @param n n-gram的n
     */
    public static Map<String, Map<String,Integer>> constructNGram(String project, String benchmarkPath, String tmpFilePath, int n) throws IOException {
        Set<String> ngramKey = new HashSet<>();
        // key : classname value : ngram
        Map<String, Map<String,Integer>> name2ngram = new HashMap<>();

        // 设置Soot选项，指定要分析的类和类路径
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(System.getProperty("java.class.path"));
        stringBuilder.append(System.getProperty("path.separator"));
        stringBuilder.append(benchmarkPath+DTPlatform.FILE_SEPARATOR+"out"+DTPlatform.FILE_SEPARATOR+"production"+DTPlatform.FILE_SEPARATOR+ project);
        stringBuilder.append(System.getProperty("path.separator"));
        stringBuilder.append("libs"+DTPlatform.FILE_SEPARATOR+"rt.jar");
        for (File file : Objects.requireNonNull(new File(benchmarkPath + DTPlatform.FILE_SEPARATOR + "lib").listFiles())) {
            stringBuilder.append(System.getProperty("path.separator"));
            stringBuilder.append(file.getAbsolutePath());
        }

        Scene.v().setSootClassPath(stringBuilder.toString());
        Options.v().set_soot_classpath(stringBuilder.toString());
        Options.v().set_java_version(Options.java_version_8);
        Options.v().set_wrong_staticness(Options.wrong_staticness_ignore);

        Options.v().set_ignore_resolving_levels(true);
        Options.v().set_whole_program(true);
        Options.v().set_app(true);
        Options.v().set_exclude(excludeList());
        Options.v().set_allow_phantom_refs(true);

        File inputFile = new File(benchmarkPath+ DTPlatform.FILE_SEPARATOR+"testcases.txt");
        BufferedReader bufferedReader = new BufferedReader(new FileReader(inputFile));
        String line = bufferedReader.readLine();
        System.out.println("target project: "+project);
        while(line!=null){
            System.out.println(line);
            // 加载类
            SootClass sootClass;
            try {
                sootClass = Scene.v().forceResolve(line, SootClass.BODIES);
            }catch (Exception e){
                System.out.println("ClassNotFindError: "+project+" "+line);
                line = bufferedReader.readLine();
                continue;
            }
            // 添加分析的类到Scene中
            Scene.v().loadNecessaryClasses();
            // 设置分析的入口点方法
            for (SootMethod method : sootClass.getMethods()) {
                CompleteUnitGraph unitGraph = null;
                try {
                    unitGraph = (CompleteUnitGraph) generateUnitCFG(method);
                    CompleteBlockGraph blockGraph = (CompleteBlockGraph) generateBlockCFG(method);
                    saveCFG(blockGraph,tmpFilePath+DTPlatform.FILE_SEPARATOR+line+"_"+method.getName()+"blockGraph.dot");
                    saveCFG(unitGraph,tmpFilePath+DTPlatform.FILE_SEPARATOR+line+"_"+method.getName()+"unitGraph.dot");
                }catch (Exception e){
                    //TODO: RegisterNatives 中的main函数无法load，可能需要特殊处理
                    e.printStackTrace();
                    continue;
                }

                Map<String,Integer> ngramMap =  CFG2Ngram(n,unitGraph);

                ngramKey.addAll(ngramMap.keySet());

                if(name2ngram.containsKey(line)){
                    Map<String,Integer> temp = name2ngram.get(line);
                    for (String key : ngramMap.keySet()) {
                        if(temp.containsKey(key)){
                            temp.put(key,temp.get(key)+ngramMap.get(key));
                        }else {
                            temp.put(key,ngramMap.get(key));
                        }
                    }
                    name2ngram.put(line,temp);
                }else {
                    name2ngram.put(line,ngramMap);
                }
            }
            line=bufferedReader.readLine();
        }
        bufferedReader.close();

        for (String className : name2ngram.keySet()) {
            Map<String, Integer> temp = name2ngram.get(className);
            for (String key : ngramKey) {
                if(!temp.containsKey(key)){
                    temp.put(key,0);
                }
            }
            name2ngram.put(className,temp);
        }
        return name2ngram;
    }
    /**
     * 生成方法的控制流图
     * @param sootMethod 要生成控制流图的方法
     * @return 方法的控制流图
     */
    public static DirectedGraph<Unit> generateUnitCFG(SootMethod sootMethod) {
        sootMethod.retrieveActiveBody();
        return new CompleteUnitGraph(sootMethod.getActiveBody());
    }
    public static DirectedGraph<Block> generateBlockCFG(SootMethod sootMethod) {
        sootMethod.retrieveActiveBody();
        return new CompleteBlockGraph(sootMethod.getActiveBody());
    }

    /**
     * 保存控制流图
     * @param graph 控制流图
     * @param path 保存路径
     */
    public static void saveCFG(DirectedGraph graph, String path){
        DotGraph unitDotGraph = new DotGraph("unitGraph");
        for (Object unit : graph){
            unitDotGraph.drawNode(unit.toString());
        }
        for (Object unit : graph){

            for (Object succ : graph.getSuccsOf(unit)){
                unitDotGraph.drawEdge(unit.toString(), succ.toString());
            }
        }
        unitDotGraph.plot(path);
    }

    /**
     * 从控制流图中提取 ngram
     * @param ngram ngram的长度
     * @param unitGraph 控制流图
     * @return ngramID 2 count 的映射
     */
    public static Map<String,Integer> CFG2Ngram(int ngram, UnitGraph unitGraph){
        Map<String,Integer> ngramMap = new HashMap<>();
        List<List<Unit>> ngramList = new ArrayList<>();
        for (Unit unit : unitGraph) {

            List<List<Unit>> succList = getNSuss(unitGraph,unit, ngram - 1);
            if (succList.size() == 0){
                ngramList.add(new ArrayList<>());
                ngramList.get(ngramList.size()-1).add(unit);
            }
            for (List<Unit> units : succList) {
                ngramList.add(new ArrayList<>());
                ngramList.get(ngramList.size()-1).add(unit);
                ngramList.get(ngramList.size()-1).addAll(units);
            }
        }
        for (int i = 0; i < ngramList.size(); i++) {
            if(ngramList.get(i).size() < ngram){
                ngramList.remove(i);
                i--;
            }
        }

        for (List<Unit> units : ngramList) {
            StringBuilder ngramStr = new StringBuilder();
            for (Unit unit : units) {
                ngramStr.append(unit.getClass().toString().replace("class soot.jimple.internal.","")).append(",");
            }
            ngramStr = new StringBuilder(ngramStr.substring(0,ngramStr.length()-1));
            if (ngramMap.containsKey(ngramStr.toString())){
                ngramMap.put(ngramStr.toString(),ngramMap.get(ngramStr.toString())+1);
            }else {
                ngramMap.put(ngramStr.toString(),1);
            }
        }
        return ngramMap;
    }

    /**
     * 获取n个后继
     * @param nSucc 后继个数
     * @param unit 当前单元
     * @param unitGraph 单元图
     * @return n个后继
     */
    public static List<List<Unit>> getNSuss(UnitGraph unitGraph, Unit unit, int nSucc){
        List<List<Unit>> succList = new ArrayList<>();
        if (nSucc == 0){
            return succList;
        }
        for (Unit succ : unitGraph.getSuccsOf(unit)) {
            if (nSucc > 1){
                List<List<Unit>> tempList = getNSuss(unitGraph,succ,nSucc-1);
                for (List<Unit> units : tempList) {
                    succList.add(new ArrayList<>());
                    succList.get(succList.size()-1).add(succ);
                    succList.get(succList.size()-1).addAll(units);
                }
            }else {
                succList.add(new ArrayList<>());
                succList.get(succList.size()-1).add(succ);
            }
        }
        return succList;
    }

    private static LinkedList<String> excludeList() {

        if(excludePathList == null) {

            excludePathList = new LinkedList<String> ();
            excludePathList.add("java.");
            excludePathList.add("javax.");
            excludePathList.add("sun.");
            excludePathList.add("sunw.");
            excludePathList.add("com.sun.");
            excludePathList.add("com.ibm.");
            excludePathList.add("com.apple.");
            excludePathList.add("apple.awt.");
        }
        return excludePathList;
    }
}
