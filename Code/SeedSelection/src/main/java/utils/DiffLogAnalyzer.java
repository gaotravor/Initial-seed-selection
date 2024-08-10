package utils;


import org.junit.Test;

import java.io.*;
import java.util.*;

/**
 * 概念说明
 * rootPath:包含子文件夹 03results 的根路径
 * path:指向一个确定的 diffrence.log 文件的路径
 * diffLog:读取 diffrence.log 后的 File 类型文件
 * diff:一个 diff 的全部内容
 * diffDetail:将一个diff拆分为 className  crashMess  [JVMInfo  output] * n 的格式
 */
public class DiffLogAnalyzer {
    public static List<String> methodList = new ArrayList<>();
    public static List<String> benchmarkList = new ArrayList<>();
    public static List<String> randomSeedList = new ArrayList<>();

    public static String OpenJDKVersion;

    static {
//        methodList.add("JavaTailor");
//        methodList.add("VECT");
        methodList.add("budget");

        benchmarkList.add("HotspotTests-Java");
//        benchmarkList.add("Openj9Test-Test");
//        benchmarkList.add("CollectProject");

        randomSeedList.add("1");
        randomSeedList.add("2");
        randomSeedList.add("3");
        randomSeedList.add("4");
        randomSeedList.add("5");

    }

    public static void main(String[] args) throws IOException {

        for (String method : methodList) {
            String version = "OpenJDK8";
            for (String Benchmark : benchmarkList) {
                for (String randomSeed : randomSeedList) {
                    Map<String, List<Integer>> strategyToUniqueBug = new TreeMap<>();
                    // System.setOut 到控制台
                    System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out)));

                    File rootFile = new File("Z:\\JVM_Testing\\SeedSelector\\Results\\RQ1\\"+method+"\\"+version+"\\"+Benchmark+"\\"+randomSeed);
                    int exeTime = 24;

                    for (File listFile : Objects.requireNonNull(rootFile.listFiles())) {
                        if(!listFile.isDirectory()){
                            continue;
                        }

                        String strategy = listFile.getAbsolutePath().replace("\\","_").split(Benchmark+"_"+randomSeed+"_")[1];

                        try {
                            int time0 = getExeTime(new File(listFile.getAbsolutePath()+"\\03results"))/6;
//                            System.out.println(Benchmark);
//                            System.out.println(strategy);
//                            System.out.println("执行时间："+ time0);
                            if (time0 < 20){
                                System.out.println(strategy);
                                System.out.println("执行时间："+ time0);
                                throw new Exception();
                            }
                        }catch (Exception e){
                            System.out.println("删除："+listFile.getAbsolutePath());
                            deleteAll(listFile);
                            continue;
                        }




                        strategyToUniqueBug.put(strategy,new ArrayList<>());

                        List<Set<String>> uniqueCrashSetList = getUniqueBug(listFile,exeTime); // List中每一个元素为一个UC集合，下标代表时间
                        for (Set<String> uniqueCrashSet : uniqueCrashSetList) {
                            Set<String> tempSet = new HashSet<>();
                            for (String crash : uniqueCrashSet) {
                                Map<String, List<String>> detailMess = analysisCrashMess(crash);
                                StringBuilder simpleCrashMess = simpleCrashMess(detailMess);
                                tempSet.add(simpleCrashMess.toString());
                            }
                            uniqueCrashSet.clear();
                            uniqueCrashSet.addAll(tempSet);
                            strategyToUniqueBug.get(strategy).add(uniqueCrashSet.size());
                        }
                        System.out.println("+++++++++++++++++++++++++++++++++");
                    }
                    System.setOut(new PrintStream(new FileOutputStream(new File(rootFile.getAbsolutePath()+DTPlatform.FILE_SEPARATOR+"result.csv"))));
                    // 对 map 排序
                    System.out.print(rootFile.getAbsolutePath()+",");
                    for (int i=1;i<=exeTime;i++){
                        System.out.print(i+",");
                    }
                    System.out.println();

                    for (String key : strategyToUniqueBug.keySet()) {
                        System.out.print(key+",");
                        for (Integer integer : strategyToUniqueBug.get(key)) {
                            System.out.print(integer+",");
                        }
                        System.out.println();
                    }
                    System.out.println();


                }
            }
        }

    }

    @Test
    public void byPath() throws IOException {
        // System.setOut 到控制台
        System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out)));

        File rootFile = new File("Z:\\JVM_Testing\\SeedSelector\\Results\\RQ3\\FindBug\\CFG50");
        int exeTime = 24;

        for (File listFile : Objects.requireNonNull(rootFile.listFiles())) {
            if(!listFile.isDirectory()){
                continue;
            }
            if (listFile.getName().contains("OpenJDK8")){
                OpenJDKVersion = "OpenJDK8";
            }else if (listFile.getName().contains("OpenJDK11")){
                OpenJDKVersion = "OpenJDK11";
            }else if (listFile.getName().contains("OpenJDK17")){
                OpenJDKVersion = "OpenJDK17";
            }else {
                continue;
            }

            Map<String,List<String>> diff2classList = new TreeMap<>();
            new File(listFile.getAbsolutePath()+"\\diff2classList.txt").delete();

            File diffLog;
            diffLog = DiffLogAnalyzer.getAllFile(listFile.getAbsolutePath());
            if (diffLog == null){
                return ;
            }

            List<StringBuilder> diffs = DiffLogAnalyzer.clearTimeOutDiff(DiffLogAnalyzer.getInfoForEachDiff(diffLog));

            diffs.sort(new Comparator<StringBuilder>() {
                @Override
                public int compare(StringBuilder o1, StringBuilder o2) {
                    List<StringBuilder> diffDetail1 = DiffLogAnalyzer.getDetailForDiff(o1);
                    List<StringBuilder> diffDetail2 = DiffLogAnalyzer.getDetailForDiff(o2);
                    String time1 = diffDetail1.get(0).toString().split("@")[1].replace(".class", "");
                    String time2 = diffDetail2.get(0).toString().split("@")[1].replace(".class", "");
                    return time1.compareTo(time2);
                }
            });

            long firstHit = 0;
            for (StringBuilder diff : diffs) {
                List<StringBuilder> diffDetail = DiffLogAnalyzer.getDetailForDiff(diff);
                String className = diffDetail.get(0).toString();
                String crashMess = diffDetail.get(1).toString();
                if (diff.toString().contains("FileNotFoundException")){
                    continue;
                }
                if (diff.toString().contains("FileAlreadyExistsException")){
                    continue;
                }
                crashMess.replace("ArrayIndexOutOfBoundsException","IndexOutOfBoundsException");
                Map<String, List<String>> detailMess = analysisCrashMess(crashMess);
                StringBuilder simpleCrashMess = simpleCrashMess(detailMess);
                if (diff2classList.containsKey(simpleCrashMess.toString())){
                    diff2classList.get(simpleCrashMess.toString()).add(className);
                }else {
                    diff2classList.put(simpleCrashMess.toString(),new ArrayList<>());
                    diff2classList.get(simpleCrashMess.toString()).add(className);
                }
            }
            System.setOut(new PrintStream(new FileOutputStream(new File(listFile.getAbsolutePath()+"\\diff2classList.txt"))));
            for (String key : diff2classList.keySet()) {
                System.out.println("record start");
                System.out.println(key);
                for (String s : diff2classList.get(key)) {
                    System.out.println(s);
                }
                System.out.println();
            }
        }
    }


    public static Integer getExeTime(File resultDir) throws IOException {
        if (!resultDir.exists()) {
            return 0;
        }
        int sum = 0;
        for (File timestamp : Objects.requireNonNull(resultDir.listFiles())) {
            for (File file : Objects.requireNonNull(timestamp.listFiles())) {
                File diffTimeLog = new File(file.getAbsolutePath()+"\\DiffAndSelectTime.txt");
                // 获得diffTimeLog的行数
                int lineNum = 0;
                BufferedReader bufferedReader = new BufferedReader(new FileReader(diffTimeLog));
                String line = bufferedReader.readLine();
                while(line!=null){
                    lineNum++;
                    line = bufferedReader.readLine();
                }
                bufferedReader.close();
                sum += lineNum;
            }
        }
        return sum;
    }
    public static Set<String> getUniqueBug(File rootDir) throws IOException {

        new File(rootDir.getAbsolutePath()+"\\CorrectingCommit.txt").delete();
        new File(rootDir.getAbsolutePath()+"\\uniqueCrash.txt").delete();

        File diffLog;
        diffLog = DiffLogAnalyzer.getAllFile(rootDir.getAbsolutePath());
        if (diffLog == null){
            return null;
        }

        List<StringBuilder> diffs = DiffLogAnalyzer.clearTimeOutDiff(DiffLogAnalyzer.getInfoForEachDiff(diffLog));

        diffs.sort(new Comparator<StringBuilder>() {
            @Override
            public int compare(StringBuilder o1, StringBuilder o2) {
                List<StringBuilder> diffDetail1 = DiffLogAnalyzer.getDetailForDiff(o1);
                List<StringBuilder> diffDetail2 = DiffLogAnalyzer.getDetailForDiff(o2);
                String time1 = diffDetail1.get(0).toString().split("@")[1].replace(".class", "");
                String time2 = diffDetail2.get(0).toString().split("@")[1].replace(".class", "");
                return time1.compareTo(time2);
            }
        });

        long startTime = Long.MAX_VALUE;
        for (File file : Objects.requireNonNull(new File(rootDir.getAbsolutePath() + DTPlatform.FILE_SEPARATOR + "03results").listFiles())) {
            if(file.isDirectory()){
                long time = Long.parseLong(file.getName());
                if( time < startTime){
                    startTime = time;
                }
            }
        }


        int crashCount = 0;
        int checkSumCount = 0;
        int skipSize = 0;
        Set<String> uniqueCrash = new HashSet<>();
        Set<String> uniqueCheck = new HashSet<>();
        long firstHit = 0;
        for (StringBuilder diff : diffs) {
            List<StringBuilder> diffDetail = DiffLogAnalyzer.getDetailForDiff(diff);
            String crashMess = diffDetail.get(1).toString();
            if (diff.toString().contains("ClassFormatError")){
                continue;
            }
            if (diff.toString().contains("FileNotFoundException")){
                continue;
            }
            if (diff.toString().contains("FileAlreadyExistsException")){
                continue;
            }
            if (diff.toString().contains("getThrowableException")){
                continue;
            }
            for (StringBuilder stringBuilder : diffDetail) {
                if (stringBuilder.toString().contains("Uninitialized object exists on backward branch")) {
                    crashMess = "hotspot_old [VerifyError] hotspot [] openj9_old [] openj9 [] bisheng_old [] bisheng []";
                    break;
                }

            }

            crashMess.replace("ArrayIndexOutOfBoundsException","IndexOutOfBoundsException");


            crashMess = crashMess + " ";

            boolean isCrash = true;
            if(crashMess.equals("hotspot_old [] hotspot [] openj9_old [] openj9 [] bisheng_old [] bisheng [] ") || crashMess.equals("hotspot_old [] hotspot [] openj9_old [] openj9 [] ")){
                isCrash = false;
                String checkMess = "hotspot_old [FIRST] hotspot [SECOND] openj9_old [THIRD] openj9 [FORTH] bisheng_old [FIFTH] bisheng [SIXTH] ";

//                        StringBuilder checkMess = new StringBuilder();
                String[] jvms = new String[]{"hotspot_old","hotspot","openj9_old","openj9","bisheng_old","bisheng"};
                boolean skipFlag = false;
                String placeholder = "";
                for(int i= 2;i<diffDetail.size();i++){
                    String line = diffDetail.get(i).toString();
                    if(i%2 == 0){
                        for (String jvm : jvms) {
                            if (line.contains(jvm)){
                                placeholder = checkMess.split(jvm+" \\[")[1].split("]")[0];
                                break;
                            }
                        }
                    }else {
                        if(!line.contains("my_check_sum_value:")){
                            skipFlag =true;
                            break;
                        }
                        try {
                            checkMess = checkMess.replace(placeholder,line.split("my_check_sum_value:")[1].split("\n")[0]);
                        }catch (Exception e){
                            skipFlag = true;
                            skipSize++;
                            break;
                        }

                    }
                }
                if(skipFlag){
                    continue;
                }
                crashMess = checkMess;
            }

            if(crashMess.contains("FIFTH")){
                crashMess = crashMess.split("bisheng_old")[0];
            }

            if(isCrash){
                Map<String, List<String>> detailMess = analysisCrashMess(crashMess);
                StringBuilder simpleCrashMess = simpleCrashMess(detailMess);
                crashCount++;
                if(!uniqueCrash.contains(simpleCrashMess.toString())){
                    long hitTime = Long.parseLong(diffDetail.get(0).toString().split("@")[1].replace(".class", ""));
                    if(firstHit == 0){
                        firstHit = hitTime;
                    }
//                        System.out.println((hitTime - firstHit)/1000/60);
                    uniqueCrash.add(simpleCrashMess.toString());
                    File uniqueCrashFile = new File(rootDir.getAbsolutePath()+"\\uniqueCrash.txt");
                    FileWriter fileWriter = new FileWriter(uniqueCrashFile,true);
                    fileWriter.write(diffDetail.get(0).toString()+"\n");
                    fileWriter.write(crashMess+"\n");
                    fileWriter.flush();
                    fileWriter.close();
                }


            }else {
                checkSumCount++;
                if(!uniqueCheck.contains(crashMess)){
                    uniqueCheck.add(crashMess);
                    File uniqueChecksumName = new File(rootDir.getAbsolutePath()+"\\CorrectingCommit.txt");
                    FileWriter fileWriter = new FileWriter(uniqueChecksumName,true);
                    fileWriter.write(diffDetail.get(0).toString()+"\n");
                    fileWriter.flush();
                    fileWriter.close();
                }
            }


        }

        return uniqueCrash;
    }
    public static List<Set<String>> getUniqueBug(File rootDir,int exeTime) throws IOException {

        File diffLog;
        diffLog = DiffLogAnalyzer.getAllFile(rootDir.getAbsolutePath());
        if (diffLog == null){
            return null;
        }

        List<StringBuilder> allDiffList = DiffLogAnalyzer.clearTimeOutDiff(DiffLogAnalyzer.getInfoForEachDiff(diffLog));

        allDiffList.sort(new Comparator<StringBuilder>() {
            @Override
            public int compare(StringBuilder o1, StringBuilder o2) {
                List<StringBuilder> diffDetail1 = DiffLogAnalyzer.getDetailForDiff(o1);
                List<StringBuilder> diffDetail2 = DiffLogAnalyzer.getDetailForDiff(o2);
                String time1 = diffDetail1.get(0).toString().split("@")[1].replace(".class", "");
                String time2 = diffDetail2.get(0).toString().split("@")[1].replace(".class", "");
                return time1.compareTo(time2);
            }
        });

        long startTime = Long.MAX_VALUE;
        for (File file : Objects.requireNonNull(new File(rootDir.getAbsolutePath() + DTPlatform.FILE_SEPARATOR + "03results").listFiles())) {
            if(file.isDirectory()){
                long time = Long.parseLong(file.getName());
                if( time < startTime){
                    startTime = time;
                }
            }
        }
        List<List<StringBuilder>> diffsList = new ArrayList<>(); // 将diffs按时间分成多个list
        for(long i=1;i<=exeTime;i++){
            diffsList.add(new ArrayList<>());
            for (StringBuilder diff : allDiffList) {
                List<StringBuilder> diffDetail = DiffLogAnalyzer.getDetailForDiff(diff);
                String time = diffDetail.get(0).toString().split("@")[1].replace(".class", "");
                if(Long.parseLong(time) < startTime + i *60*60*1000){
                    diffsList.get((int) (i-1)).add(diff);
                }
            }
        }

        List<Set<String>> uniqueCrashSetList = new ArrayList<>();

        for (List<StringBuilder> diffs : diffsList) {
            new File(rootDir.getAbsolutePath()+"\\CorrectingCommit.txt").delete();
            new File(rootDir.getAbsolutePath()+"\\uniqueCrash.txt").delete();
            int crashCount = 0;
            int checkSumCount = 0;
            int skipSize = 0;
            Set<String> uniqueCrash = new HashSet<>();
            Set<String> uniqueCheck = new HashSet<>();
            long firstHit = 0;
            for (StringBuilder diff : diffs) {

                List<StringBuilder> diffDetail = DiffLogAnalyzer.getDetailForDiff(diff);
                String crashMess = diffDetail.get(1).toString();

                if (diff.toString().contains("ClassFormatError")){
                    continue;
                }
                if (diff.toString().contains("FileNotFoundException")){
                    continue;
                }
                if (diff.toString().contains("FileAlreadyExistsException")){
                    continue;
                }
                for (StringBuilder stringBuilder : diffDetail) {
                    if (stringBuilder.toString().contains("Uninitialized object exists on backward branch")) {
                        crashMess = "hotspot_old [VerifyError] hotspot [] openj9_old [] openj9 [] bisheng_old [] bisheng []";
                        break;
                    }
                }
                crashMess.replace("ArrayIndexOutOfBoundsException","IndexOutOfBoundsException");


                crashMess = crashMess + " ";

                boolean isCrash = true;
                if(crashMess.equals("hotspot_old [] hotspot [] openj9_old [] openj9 [] bisheng_old [] bisheng [] ") || crashMess.equals("hotspot_old [] hotspot [] openj9_old [] openj9 [] ")){
                    isCrash = false;
                    String checkMess = "hotspot_old [FIRST] hotspot [SECOND] openj9_old [THIRD] openj9 [FORTH] bisheng_old [FIFTH] bisheng [SIXTH] ";

//                        StringBuilder checkMess = new StringBuilder();
                    String[] jvms = new String[]{"hotspot_old","hotspot","openj9_old","openj9","bisheng_old","bisheng"};
                    boolean skipFlag = false;
                    String placeholder = "";
                    for(int i= 2;i<diffDetail.size();i++){
                        String line = diffDetail.get(i).toString();
                        if(i%2 == 0){
                            for (String jvm : jvms) {
                                if (line.contains(jvm)){
                                    placeholder = checkMess.split(jvm+" \\[")[1].split("]")[0];
                                    break;
                                }
                            }
                        }else {
                            if(!line.contains("my_check_sum_value:")){
                                skipFlag =true;
                                break;
                            }
                            try {
                                checkMess = checkMess.replace(placeholder,line.split("my_check_sum_value:")[1].split("\n")[0]);
                            }catch (Exception e){
                                skipFlag = true;
                                skipSize++;
                                break;
                            }

                        }
                    }
                    if(skipFlag){
                        continue;
                    }
                    crashMess = checkMess;
                }

                if(crashMess.contains("FIFTH")){
                    crashMess = crashMess.split("bisheng_old")[0];
                }

                if(isCrash){
                    Map<String, List<String>> detailMess = analysisCrashMess(crashMess);
                    StringBuilder simpleCrashMess = simpleCrashMess(detailMess);
                    crashCount++;
                    if(!uniqueCrash.contains(simpleCrashMess.toString())){
                        long hitTime = Long.parseLong(diffDetail.get(0).toString().split("@")[1].replace(".class", ""));
                        if(firstHit == 0){
                            firstHit = hitTime;
                        }
//                        System.out.println((hitTime - firstHit)/1000/60);
                        uniqueCrash.add(simpleCrashMess.toString());
                        File uniqueCrashFile = new File(rootDir.getAbsolutePath()+"\\uniqueCrash.txt");
                        FileWriter fileWriter = new FileWriter(uniqueCrashFile,true);
                        fileWriter.write(diffDetail.get(0).toString()+"\n");
                        fileWriter.write(crashMess+"\n");
                        fileWriter.flush();
                        fileWriter.close();
                    }

                }else {
                    checkSumCount++;
                    if(!uniqueCheck.contains(crashMess)){
                        uniqueCheck.add(crashMess);
                        File uniqueChecksumName = new File(rootDir.getAbsolutePath()+"\\CorrectingCommit.txt");
                        FileWriter fileWriter = new FileWriter(uniqueChecksumName,true);
                        fileWriter.write(diffDetail.get(0).toString()+"\n");
                        fileWriter.flush();
                        fileWriter.close();
                    }
                }


            }
            uniqueCrashSetList.add(uniqueCrash);
        }
        return uniqueCrashSetList;
    }

    /**
     * 指定单个diffLog的位置，返回文件
     * @param path
     * @return
     */
    public static File getFile(String path){
        return new File(path);
    }

    /**
     * 合并 rootPath 下 03results 文件夹中的所有 diffLog，重新保存在 rootPath 下
     * @param rootPath
     * @return
     * @throws IOException
     */
    public static File getAllFile(String rootPath) throws IOException {
        File results = new File(rootPath+"\\03results");
        File allDiff = new File(rootPath+"\\difference.log");
        if (!results.exists()){
            if (allDiff.exists()){
                return allDiff;
            }else {
                return null;
            }
        }
        allDiff.delete();
        allDiff.createNewFile();
        FileWriter fileWriter = new FileWriter(allDiff,true);
        for (File timeStamp : Objects.requireNonNull(results.listFiles())) {
            if (!timeStamp.isDirectory()){
                continue;
            }
            File timeFile = new File(Objects.requireNonNull(timeStamp.listFiles())[0].getAbsolutePath()+"\\difference.log");
            BufferedReader bufferedReader = new BufferedReader(new FileReader(timeFile));
            String line = bufferedReader.readLine();
            while (line!=null){
                fileWriter.write(line+"\n");
                line = bufferedReader.readLine();
            }
        }
        fileWriter.flush();
        fileWriter.close();
        return allDiff;
    }

    /**
     * 将 diffLog 文件中的每一个 diff 提取出来
     * @param diffLog
     * @return
     * @throws IOException
     */
    public static List<StringBuilder> getInfoForEachDiff(File diffLog) throws IOException {
        List<StringBuilder> diffs = new ArrayList<>();
        BufferedReader bufferedReader = new BufferedReader(new FileReader(diffLog));
        String line = bufferedReader.readLine();
        while (line != null){
            if(line.contains("Difference found:")){
                diffs.add(new StringBuilder(""));
            }
            diffs.get(diffs.size() - 1).append(line).append("\n");
            line = bufferedReader.readLine();
        }
        return diffs;
    }

    /**
     * 将一个 diff 拆分为 className  crashMess  [JVMInfo  output] * n 的格式
     * @param diff
     * @return
     */
    public static List<StringBuilder> getDetailForDiff(StringBuilder diff){
        List<StringBuilder> diffDetail = new ArrayList<>(); // className  crashMess  [JVMInfo  output] * n

        String[] lines = diff.toString().split("\n");
        String[] temp = lines[0].split("-");
        diffDetail.add(new StringBuilder(temp[temp.length - 1]));
        diffDetail.add(new StringBuilder(lines[1]));
        for (int i = 2;i< lines.length;i++){
            String line = lines[i];
            if(line.contains("======")){
                line = line.replace("=","");
                diffDetail.add(new StringBuilder(line));
                diffDetail.add(new StringBuilder());
                continue;
            }
            if(line.contains("Target") || line.equals("")){
                continue;
            }
            diffDetail.get(diffDetail.size() - 1).append(line).append("\n");
        }
        return diffDetail;
    }

    /**
     * 删除所有 diff 记录中的 TimeOut 类型的差异
     * @param diffs
     * @return
     */
    public static List<StringBuilder> clearTimeOutDiff(List<StringBuilder> diffs){
        List<StringBuilder> noTimeoutDiffs = new ArrayList<>();
        for (StringBuilder diff : diffs) {
            if(!diff.toString().contains("TIMEOUT") && !diff.toString().contains("OutOfMemoryError")){
                noTimeoutDiffs.add(diff);
            }
        }
        return noTimeoutDiffs;
    }

    /**
     * 将 crashMess 分隔开
     * @param crashMess
     * @return
     */
    public static Map<String, List<String>> analysisCrashMess(String crashMess){
        Map<String, List<String>> result = new HashMap<>();
        List<String> jvms = new ArrayList<>();
        jvms.add("hotspot");
        jvms.add("openj9");
        jvms.add("bisheng");
        jvms.add("hotspot_old");
        jvms.add("openj9_old");
        jvms.add("bisheng_old");
//        if (OpenJDKVersion.equals("OpenJDK8")){
//            jvms.add("Hotspot-openjdk8-jdk_linux_8_hotspot");
//            jvms.add("OpenJ9-openjdk8-jdk_linux_8_openj9");
//            jvms.add("Bisheng-openjdk8-jdk_linux_8_bisheng");
//            jvms.add("OpenJ9_-Xfuture-openjdk8-jdk_linux_8_openj9");
//        }else if (OpenJDKVersion.equals("OpenJDK11")){
//            jvms.add("Hotspot-openjdk11-jdk_linux_11_hotspot");
//            jvms.add("OpenJ9-openjdk11-jdk_linux_11_openj9");
//            jvms.add("Bisheng-openjdk11-jdk_linux_11_bisheng");
//            jvms.add("OpenJ9_-Xfuture-openjdk11-jdk_linux_11_openj9");
//        } else if (OpenJDKVersion.equals("OpenJDK17")){
//            jvms.add("Hotspot-openjdk17-jdk_linux_17_hotspot");
//            jvms.add("OpenJ9-openjdk17-jdk_linux_17_openj9");
//            jvms.add("Bisheng-openjdk17-jdk_linux_17_bisheng");
//            jvms.add("OpenJ9_-Xfuture-openjdk17-jdk_linux_17_openj9");
//        }
        String jvm = "";
        for (String key : crashMess.split("\\["+"|"+"]"+"|"+",")) {
            key = key.strip();
            if (Objects.equals(key, ""))
                continue;
            if(jvms.contains(key)){
                result.put(key,new ArrayList<>());
                jvm = key;
            }else {
                result.get(jvm).add(key);
            }
        }
        return result;
    }

    /**
     * 删除 crashMess 中每个JVM都报告的exception
     * @param detailMess
     * @return
     */
    public static StringBuilder simpleCrashMess(Map<String, List<String>> detailMess){
        StringBuilder simpleCrashMess = new StringBuilder();

        List<String> sameCrash = null;
        for (String key : detailMess.keySet()) {
            if (sameCrash==null){
                sameCrash = new ArrayList<>(detailMess.get(key));
            }else {
                sameCrash.retainAll(detailMess.get(key));
            }
        }

        List<String> keySet = new ArrayList<>();
        keySet.add("hotspot_old");
        keySet.add("hotspot");
        keySet.add("openj9_old");
        keySet.add("openj9");
        keySet.add("bisheng_old");
        keySet.add("bisheng");
//        if (OpenJDKVersion.equals("OpenJDK8")){
//            keySet.add("Hotspot-openjdk8-jdk_linux_8_hotspot");
//            keySet.add("OpenJ9-openjdk8-jdk_linux_8_openj9");
//            keySet.add("Bisheng-openjdk8-jdk_linux_8_bisheng");
//            keySet.add("OpenJ9_-Xfuture-openjdk8-jdk_linux_8_openj9");
//        } else if (OpenJDKVersion.equals("OpenJDK11")){
//            keySet.add("Hotspot-openjdk11-jdk_linux_11_hotspot");
//            keySet.add("OpenJ9-openjdk11-jdk_linux_11_openj9");
//            keySet.add("Bisheng-openjdk11-jdk_linux_11_bisheng");
//            keySet.add("OpenJ9_-Xfuture-openjdk11-jdk_linux_11_openj9");
//        } else if (OpenJDKVersion.equals("OpenJDK17")){
//            keySet.add("Hotspot-openjdk17-jdk_linux_17_hotspot");
//            keySet.add("OpenJ9-openjdk17-jdk_linux_17_openj9");
//            keySet.add("Bisheng-openjdk17-jdk_linux_17_bisheng");
//            keySet.add("OpenJ9_-Xfuture-openjdk17-jdk_linux_17_openj9");
//        }

        for (String key : keySet) {
            simpleCrashMess.append(key+" [");
            detailMess.get(key).removeAll(sameCrash);
            StringBuilder output = new StringBuilder();

            for (int i = 0; i < detailMess.get(key).size(); i++) {
                output.append(detailMess.get(key).get(i));
                if (i < detailMess.get(key).size() - 1) {
                    output.append(",");
                }
            }

            simpleCrashMess.append(output);
            simpleCrashMess.append("] ");
        }
        return simpleCrashMess;
    }

    public static void deleteAll(File file){
        if(file.isFile()){
            file.delete();
        }else{
            File[] files = file.listFiles();
            for (File f : files) {
                deleteAll(f);
            }
            file.delete();
        }
    }

}
