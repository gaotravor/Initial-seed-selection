package dataProcessing;

import utils.DTPlatform;
import utils.DiffLogAnalyzer;

import java.io.*;
import java.util.*;

/**
 * 分析diffLog，提取 uniqueBug
 */
public class Processing4PISS {
    private static String diffInfoPath = "";
    private static final List<String> uniqueBugList = new ArrayList<>();

    /**
     * 初始化环境，分析 info 文件，并且返回覆盖率信息
     * @param diffInfoPath
     * @return
     * @throws IOException
     */
    public static String initAndAnalysis(String diffInfoPath) throws IOException {
        initEnv(diffInfoPath);
        analysisDiffInfo();
        return "";
    }

    /**
     * 设置新的 info 文件路径，并且重置全部信息
     * @param diffInfoPath
     */
    public static void initEnv(String diffInfoPath) {
        reset();
        Processing4PISS.diffInfoPath = diffInfoPath;
    }

    /**
     * 根据 diffInfoPath 设置的路径，按测试用例提取 uniqueBug 并且为每一个 测试用例 创建一个文件
     * @throws IOException
     */
    public static void analysisDiffInfo() throws IOException {

        File allBugClassFile = new File(diffInfoPath+DTPlatform.FILE_SEPARATOR+"allBugClass.txt");
        allBugClassFile.delete();

        Set<String> allBugClassSet = new HashSet<>();

        File inputFile = new File(diffInfoPath+ DTPlatform.FILE_SEPARATOR+"testcases.txt");
        BufferedReader bufferedReader = new BufferedReader(new FileReader(inputFile));
        String line = bufferedReader.readLine();
        new File(diffInfoPath+DTPlatform.FILE_SEPARATOR+"info").mkdirs();
        while(line!=null){
            File f = new File(diffInfoPath+DTPlatform.FILE_SEPARATOR+"info"+DTPlatform.FILE_SEPARATOR+line+".info");
            f.delete();
            f.createNewFile();
            line=bufferedReader.readLine();
        }


        File file = DiffLogAnalyzer.getFile(diffInfoPath+DTPlatform.FILE_SEPARATOR+"difference.log");
        String lastFileName = "";
        File uniqueBugFile = null;
        for (StringBuilder stringBuilder : DiffLogAnalyzer.clearTimeOutDiff(DiffLogAnalyzer.getInfoForEachDiff(file))) {
            List<StringBuilder> diffDetail = DiffLogAnalyzer.getDetailForDiff(stringBuilder);

            String fileNameOfDiffDetail = diffDetail.get(0).toString();
            System.out.println(fileNameOfDiffDetail);
            for (File listFile : Objects.requireNonNull(new File(diffInfoPath+DTPlatform.FILE_SEPARATOR+"info").listFiles())) {
                if(fileNameOfDiffDetail.contains(listFile.getName().replace(".info",""))){
                    uniqueBugFile = listFile;
                    fileNameOfDiffDetail = uniqueBugFile.getName().replace(".info","");
                    break;
                }
            }
            if(!allBugClassSet.contains(fileNameOfDiffDetail)){
                allBugClassSet.add(fileNameOfDiffDetail);
                FileWriter fileWriter = new FileWriter(allBugClassFile,true);
                fileWriter.write(fileNameOfDiffDetail+"\n");
                fileWriter.flush();
                fileWriter.close();
            }

            if(!lastFileName.equals(fileNameOfDiffDetail)){
                lastFileName = fileNameOfDiffDetail;
                uniqueBugList.clear();
            }
            if(!uniqueBugList.contains(diffDetail.get(1).toString())){
                uniqueBugList.add(diffDetail.get(1).toString());
                assert uniqueBugFile != null;
                FileWriter fileWriter = new FileWriter(uniqueBugFile,true);
                fileWriter.write(diffDetail.get(1).toString()+"\n");
                fileWriter.flush();
                fileWriter.close();
            }
        }
//        File timeList = new File(diffInfoPath+DTPlatform.FILE_SEPARATOR+"..\\exeTime");
//        List<String> names =new ArrayList<>();
//        for (File file1 : Objects.requireNonNull(timeList.listFiles())) {
//            names.add(file1.getName());
//        }
//        File covInfo =new File(diffInfoPath+DTPlatform.FILE_SEPARATOR+"info");
//        for (File file1 : Objects.requireNonNull(covInfo.listFiles())) {
//            for (String s : names) {
//                if(s.split("@")[0].equals(file1.getName().replace(".info",""))){
//                    file1.renameTo(new File(file1.getAbsolutePath().replace(file1.getName(), s)));
//                }
//            }
//        }
//        for (File listFile : Objects.requireNonNull(covInfo.listFiles())) {
//            if(!listFile.getName().contains("@")){
//                listFile.delete();
//            }
//        }
    }

    private static void reset(){
        diffInfoPath = "";
        uniqueBugList.clear();
    }
}
