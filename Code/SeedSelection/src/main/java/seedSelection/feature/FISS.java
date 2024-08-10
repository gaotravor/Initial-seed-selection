package seedSelection.feature;

import seedSelection.SubSet;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class FISS extends SubSet {

    protected String featureFilePath = "";
    protected String clusterFilePath = "";

    protected List<String> nameList = new ArrayList<>();
    protected List<Integer> clusterList = new ArrayList<>();
    protected List<Double[]> featureList= new ArrayList<>();
    public FISS(String clusterFilePath, String featureFilePath, double budget, double originSize){
        super(budget, originSize);
        this.clusterFilePath = clusterFilePath;
        this.featureFilePath = featureFilePath;
    }

    @Override
    public void reduce() throws IOException {

        getFeatureInfo();

        int caseCount = nameList.size();
        DistanceMatrix distanceMatrix = new DistanceMatrix(caseCount);

        // 计算重心
        Double[] center = null; // 重心
        for (Double[] feature : featureList) {
            if(center == null){
                center = new Double[feature.length];
                Arrays.fill(center, 0.0);
            }
            for(int i = 0; i < feature.length; i++){
                center[i] += feature[i];
            }
        }
        for(int i = 0; i < Objects.requireNonNull(center).length; i++){
            center[i] /= caseCount;
        }

        // 初始化候选点
        Map<String, Double> candidate = new HashMap<>();
        for (String key : nameList) {
            candidate.put(key, Double.MAX_VALUE);
        }

        // 选择初始点
        String targetName = "";
        double maxDistance = -1;
        for(int i=0;i<featureList.size();i++){
            double result = DistanceMatrix.calDistance(featureList.get(i), center);
            if( result > maxDistance ){
                maxDistance = result;
                targetName = nameList.get(i);
            }
        }

        while (!(fileNameList.size() > targetSize)) {

            // 选中集增加
            fileNameList.add(targetName);
            // 候选集减少
            candidate.remove(targetName);

            maxDistance = -1;
            String nextTargetName = "";
            for (String name : candidate.keySet()) {
                // 计算 候选点 和 被选点 的最小距离
                Double minDistance = candidate.get(name);
                int rowIndex = nameList.indexOf(name);
                Double[] feature1 = featureList.get(rowIndex);
                int colIndex = nameList.indexOf(targetName);
                Double[] feature2 = featureList.get(colIndex);
                double distance = distanceMatrix.getDistance(rowIndex, colIndex, feature1, feature2);
                if (distance < minDistance) {
                    minDistance = distance;
                }
                candidate.put(name, minDistance);

                distance = minDistance;
                if (distance > maxDistance) {
                    maxDistance = distance;
                    nextTargetName = name;
                }
            }
            targetName = nextTargetName;
        }
    }

    protected void getClusterInfo() throws IOException {
        nameList.clear();
        clusterList.clear();
        // 得到 nameList clusterList 信息
        File clusterFile = new File(clusterFilePath);
        BufferedReader bufferedReader = new BufferedReader(new FileReader(clusterFile));
        String line = bufferedReader.readLine();
        while(line!=null){
            if (!line.startsWith(",")){
                String name = line.split(",")[1];
//                name = name.replace("/",".");
//                name = name.replace("\\",".");
//                name = name.split("src.")[1];
                name = name.replace(".txt","");
                nameList.add(name);
                clusterList.add(Integer.valueOf(line.split(",")[2]));
            }
            line=bufferedReader.readLine();
        }
    }

    protected void getFeatureInfo() throws IOException {
        nameList.clear();
        featureList.clear();
        // 得到 featureList 信息
        File featureFile = new File(featureFilePath);
        BufferedReader bufferedReader = new BufferedReader(new FileReader(featureFile));
        String line = bufferedReader.readLine();
        while(line!=null){
            if (!line.startsWith(",")){
                // 第 0 列是行号 第一列是文件路径
                String[] cells = line.split(",");

                String name = cells[1];
                name = name.replace(".txt","");
                name = name.replace(".java","");
                if (skipClass(name)) {
                    line=bufferedReader.readLine();
                    continue;
                }
                nameList.add(name);

                Double[] feature = new Double[cells.length - 2];
                for(int i = 0; i < feature.length; i++){
                    feature[i] = Double.valueOf(cells[i + 2]);
                }
                featureList.add(feature);
            }
            line=bufferedReader.readLine();
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
