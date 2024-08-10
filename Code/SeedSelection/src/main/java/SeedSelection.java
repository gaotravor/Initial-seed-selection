import seedSelection.coverage.CISS_M;
import seedSelection.coverage.CISS_P;
import seedSelection.feature.FISS;
import seedSelection.prefuzz.PISS;
import utils.BitMap;
import utils.DTPlatform;
import ilog.concert.IloException;

import java.io.*;
import java.util.*;

/**
 * 生成 SubSet 使用的类
 */
public class SeedSelection {
    public static String covRootPath;
    public static String bugRootPath;
    public static String featureRootPath;

    public static void main(String[] args) throws IOException, ClassNotFoundException, IloException {
        String projectListArg = args[0];
        String budgetListArg = args[1];
        String methodListArg = args[2];
        String rootPath = System.getProperty("user.dir")+DTPlatform.FILE_SEPARATOR+".."+DTPlatform.FILE_SEPARATOR+"..";

        List<String> projectList = splitString(projectListArg);
        List<String> budgetList = splitString(budgetListArg);
        List<String> methodList = splitString(methodListArg);
        for (String project : projectList) {
            String outPath = rootPath+DTPlatform.FILE_SEPARATOR+"Results"+DTPlatform.FILE_SEPARATOR+"subset"+DTPlatform.FILE_SEPARATOR+project;
            new File(outPath).mkdirs();
            List<Integer> budgets = new ArrayList<>();
            for (String budget : budgetList) {
                budgets.add(Integer.parseInt(budget));
            }
            int originSize = 0;
            File inputFile = new File(rootPath+DTPlatform.FILE_SEPARATOR+"Data"+DTPlatform.FILE_SEPARATOR+"benchmarks"+DTPlatform.FILE_SEPARATOR+project+DTPlatform.FILE_SEPARATOR+"testcases.txt");
            BufferedReader bufferedReader = new BufferedReader(new FileReader(inputFile));
            String line = bufferedReader.readLine();
            while(line!=null){
                originSize++;
                line=bufferedReader.readLine();
            }
            int maxBudget = 0;
            for (Integer budget : budgets) {
                if(budget>maxBudget){
                    maxBudget = budget;
                }
            }

            long startTime = 0;
            long endTime = 0;

            bugRootPath = rootPath+DTPlatform.FILE_SEPARATOR+"Data"+DTPlatform.FILE_SEPARATOR+"bugInfo"+DTPlatform.FILE_SEPARATOR+project;
            featureRootPath = rootPath+DTPlatform.FILE_SEPARATOR+"Data"+DTPlatform.FILE_SEPARATOR+"featureInfo"+DTPlatform.FILE_SEPARATOR+project;
            covRootPath = rootPath+DTPlatform.FILE_SEPARATOR+"Data"+DTPlatform.FILE_SEPARATOR+"covInfo"+DTPlatform.FILE_SEPARATOR+project+DTPlatform.FILE_SEPARATOR+"branchBitMap";
            for (String method : methodList) {
                startTime = System.currentTimeMillis();
                if (method.equals("PISS")) {
                    writeFile(PISS(originSize,maxBudget),originSize,budgets,outPath,"HotSet");
                } else if (method.equals("FISSTFIDF")) {
                    writeFile(FISS("TF_IDF", originSize,maxBudget),originSize,budgets,outPath,"FISSTFIDF");
                } else if (method.equals("FISSCFG")) {
                    writeFile(FISS("3gramCFG", originSize,maxBudget),originSize,budgets,outPath,"FISSCFG");
                } else if (method.equals("FISSAST")) {
                    writeFile(FISS("3gramAST", originSize,maxBudget),originSize,budgets,outPath,"FISSAST");
                } else if (method.equals("FISSCodeBERT")) {
                    writeFile(FISS("CodeBERT", originSize,maxBudget),originSize,budgets,outPath,"FISSCodeBERT");
                } else if (method.equals("FISSCodeT5")) {
                    writeFile(FISS("CodeT5", originSize,maxBudget),originSize,budgets,outPath,"FISSCodeT5");
                } else if (method.equals("FISSPLBART")) {
                    writeFile(FISS("PLBART", originSize,maxBudget),originSize,budgets,outPath,"FISSPLBART");
                } else if (method.equals("FISSInferCode")) {
                    writeFile(FISS("InferCode", originSize,maxBudget),originSize,budgets,outPath,"FISSInferCode");
                } else if (method.equals("CISSM")) {
                    writeFile(CISS_m(originSize,maxBudget),originSize,budgets,outPath,"CISSM");
                } else if (method.equals("CISSP")) {
                    writeFile(CISS_p(originSize,maxBudget),originSize,budgets,outPath,"CISSP");
                }
                endTime = System.currentTimeMillis();
                System.out.println(method+" Time:"+(endTime-startTime)/1000);
            }
        }
    }

    public static List<String> splitString(String input) {
        List<String> result = new ArrayList<>();
        if (input.startsWith("[") && input.endsWith("]")) {
            String trimmedInput = input.substring(1, input.length() - 1);
            String[] splitArray = trimmedInput.split(",");
            for (String element : splitArray) {
                result.add(element.trim());
            }
        }
        return result;
    }
    public static List<String> PISS(double originSize, double maxBudget) throws IOException, ClassNotFoundException, IloException {
        BitMap allBitMap = null;
        List<BitMap> bitMapList = new ArrayList<>();
        File file = new File(bugRootPath + DTPlatform.FILE_SEPARATOR + "bitmap");
        for (File listFile : Objects.requireNonNull(file.listFiles())) {
            String fileName = listFile.getName().split("@")[0];
            if (skipClass(fileName)) {
                continue;
            }
            double exeTime = 0;
            if (listFile.getName().contains("@")){
                exeTime = Long.parseLong(listFile.getName().split("@")[1].split("\\.")[0]);
            }
            BitMap bitMap = new BitMap(listFile.getAbsolutePath(),fileName,exeTime);
            bitMapList.add(bitMap);
            if(allBitMap == null){
                allBitMap = new BitMap(bitMap.getBits().size(),"allBitMap",0);
            }
            allBitMap = BitMap.add(allBitMap,bitMap,"allBitMap",0);
        }

        PISS subset = new PISS(bitMapList,maxBudget,originSize);
        subset.reduce();

        assert allBitMap != null;
        System.out.println(allBitMap.hitCount());
        System.out.println(subset.getAllBitMap().hitCount());
        System.out.println(subset.getSumTime());

        return subset.getFileNameList();
    }
    public static List<String> CISS_m(double originSize, double maxBudget) throws IOException, ClassNotFoundException {
        BitMap allBitMap = null;
        List<BitMap> bitMapList = new ArrayList<>();
        File file = new File(covRootPath);
        for (File listFile : Objects.requireNonNull(file.listFiles())) {
            String fileName = listFile.getName().split("@")[0];
            if (skipClass(fileName)) {
                continue;
            }
            double exeTime = Long.parseLong(listFile.getName().split("@")[1].split("\\.")[0]);
            BitMap bitMap = new BitMap(listFile.getAbsolutePath(),fileName,exeTime);
            bitMapList.add(bitMap);
            if(allBitMap == null){
                allBitMap = new BitMap(bitMap.getBits().size(),"allBitMap",0);
            }
            allBitMap = BitMap.add(allBitMap,bitMap,"allBitMap",0);
        }
        CISS_M subset = new CISS_M(bitMapList,maxBudget,originSize);
        subset.reduce();
        assert allBitMap != null;
        System.out.println(allBitMap.hitCount());
        System.out.println(subset.getAllBitMap().hitCount());
        System.out.println(subset.getSumTime());

        return subset.getFileNameList();
    }
    public static List<String> CISS_p(double originSize, double maxBudget) throws IOException, ClassNotFoundException {
        BitMap allBitMap = null;
        List<BitMap> bitMapList = new ArrayList<>();
        File file = new File(covRootPath);
        for (File listFile : Objects.requireNonNull(file.listFiles())) {
            String fileName = listFile.getName().split("@")[0];
            if (skipClass(fileName)) {
                continue;
            }
            double exeTime = Long.parseLong(listFile.getName().split("@")[1].split("\\.")[0]);
            BitMap bitMap = new BitMap(listFile.getAbsolutePath(),fileName,exeTime);
            bitMapList.add(bitMap);
            if(allBitMap == null){
                allBitMap = new BitMap(bitMap.getBits().size(),"allBitMap",0);
            }
            allBitMap = BitMap.add(allBitMap,bitMap,"allBitMap",0);
        }
        CISS_P subset = new CISS_P(bitMapList, maxBudget, originSize);
        subset.reduce();

        assert allBitMap != null;
        System.out.println(allBitMap.hitCount());
        System.out.println(subset.getAllBitMap().hitCount());
        System.out.println(subset.getSumTime());

        return subset.getFileNameList();
    }
    public static List<String> FISS(String modelName, double originSize, double maxBudget) throws IOException, ClassNotFoundException, IloException {
        FISS subset = new FISS("",featureRootPath+DTPlatform.FILE_SEPARATOR+modelName+"Vectors.csv", maxBudget, originSize );
        subset.reduce();
        return subset.getFileNameList();
    }
    private static void writeFile(List<String> fileNameList,double originSize, List<Integer> budgets, String outPath, String method) throws IOException {
        for (Integer budget : budgets) {
            int targetSize = (int) (originSize * budget / 100);
            if (targetSize > fileNameList.size()) {
                targetSize = fileNameList.size();
            }
            List<String> subList = fileNameList.subList(0, targetSize);
            File file= new File(outPath+DTPlatform.FILE_SEPARATOR+method+"_"+budget+".txt");
            FileWriter fileWriter = new FileWriter(file);
            for (String line : subList) {
                fileWriter.write(line.replace(".bitmap", "") + "\n");
            }
            fileWriter.close();
        }
    }

    private static boolean skipClass(String className){
        List<String> skipList = new ArrayList<>();

        // skipClass for CollectProject
        skipList.add("edu.ntnu.texasai.PrintOpponentsModel");
        skipList.add("edu.ntnu.texasai.PrintPreFlop");
        skipList.add("cn.ponfee.commons.boolm.GuavaBloomFilterTest");
        skipList.add("com.pancm.utils.QrCodeCreateUtil");
        skipList.add("org.jetlang.examples.nio.MulticastSend");
        skipList.add("com.thealgorithms.sorts.BucketSort");
        skipList.add("com.github.davidmoten.rx.AdHoc");
        skipList.add("cn.ponfee.commons.jce.PBECryptorTest");
        skipList.add("com.vgrazi.javaconcurrentanimated.study.ReactorStudy");
        skipList.add("com.doinb.jdbc.JDBCDemo");
        skipList.add("com.doinb.executor.MyThreadPoolExecutorDemo");
        skipList.add("jsr166tests.loops.FinalLongTest");
        skipList.add("cn.ponfee.commons.json.JacksonIgnore");
        skipList.add("com.github.davidmoten.rx.internal.operators.TransformerOnBackpressureBufferRequestLimiting");
        skipList.add("jsr166tests.jtreg.util.IdentityHashMap.ToArray");
        skipList.add("cn.ponfee.commons.event.EventBusTest");
        skipList.add("ch3.s1.TimeLock");
        skipList.add("cn.ponfee.commons.json.BooleanPojoTest");
        skipList.add("cn.ponfee.commons.jce.security.DSASignerTest");

        return skipList.contains(className);
    }

}
