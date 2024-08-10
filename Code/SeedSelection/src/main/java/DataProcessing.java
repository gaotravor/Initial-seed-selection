import dataProcessing.Processing4PISS;
import dataProcessing.ASTFeature;
import dataProcessing.CFGFeature;
import dataProcessing.utils.exportSourceCode;
import utils.DTPlatform;
import dataProcessing.BitMap;

import java.io.IOException;

public class DataProcessing {
    public static String rootPath = System.getProperty("user.dir")+DTPlatform.FILE_SEPARATOR+".."+DTPlatform.FILE_SEPARATOR+"..";
    public static void main(String[] args) throws IOException, ClassNotFoundException {
        String projectName = args[0];
        String entry = args[1];
        rootPath = rootPath + DTPlatform.FILE_SEPARATOR + "Data";

        if (entry.equals("CISS")){
            CISS(projectName);
        }else  if (entry.equals("PISS")){
            PISS(projectName);
        }else  if (entry.equals("FISSAST")){
            FISSAST(projectName);
        }else if (entry.equals("FISSCFG")){
            FISSCFG(projectName);
        }else if (entry.equals("all")){
            CISS(projectName);
            PISS(projectName);
            FISSAST(projectName);
            FISSCFG(projectName);
        }
    }
    public static void CISS(String project) throws IOException, ClassNotFoundException {
        long start = 0;
        long end = 0;
        BitMap.bitMapMode = BitMap.BRANCH_COVERAGE_BITMAP;

        start = System.currentTimeMillis();
        BitMap.covInfo2bitMap(rootPath+DTPlatform.FILE_SEPARATOR+"covInfo"+DTPlatform.FILE_SEPARATOR+project);
        end = System.currentTimeMillis();
        System.out.println("branch@"+project+": "+(end-start)/1000.0+"s");
    }
    public static void PISS(String project) throws IOException {
        long start = 0;
        long end = 0;
        start = System.currentTimeMillis();
        Processing4PISS.initAndAnalysis(rootPath+DTPlatform.FILE_SEPARATOR+"bugInfo"+DTPlatform.FILE_SEPARATOR+project);
        BitMap.uniqueBugInfo2bitMap(rootPath+DTPlatform.FILE_SEPARATOR+"bugInfo"+DTPlatform.FILE_SEPARATOR+project);
        end = System.currentTimeMillis();
        System.out.println("bug@"+project+": "+(end-start)/1000.0+"s");
    }

    public static void FISSAST(String project) throws IOException {
        // If the source code already exists at Data/tmp/HotspotTests-Java/code, you can comment this function
//        exportSourceCode.run(rootPath, project);
        long start = 0;
        long end = 0;
        start = System.currentTimeMillis();
        ASTFeature.extract(rootPath, project);
        end = System.currentTimeMillis();
        System.out.println("ast@"+project+": "+(end-start)/1000.0+"s");
    }
    public static void FISSCFG(String project) throws IOException {
        long start = 0;
        long end = 0;
        start = System.currentTimeMillis();
        CFGFeature.extract(rootPath, project);
        end = System.currentTimeMillis();
        System.out.println("cfg@"+project+": "+(end-start)/1000.0+"s");
    }
}
