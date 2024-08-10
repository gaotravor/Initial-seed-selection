package dataProcessing.utils;

import utils.DTPlatform;
import spoon.reflect.declaration.CtClass;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

// 将测试用例对应的源码进行保存
public class exportSourceCode {

    public static void run(String rootPath, String project) throws IOException {
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

        // 使用 spoon 分析整个项目源码
        String srcPath = rootPath+DTPlatform.FILE_SEPARATOR+"benchmarks"+DTPlatform.FILE_SEPARATOR+project+DTPlatform.FILE_SEPARATOR+"src";

        List<String> dependencies = new ArrayList<>();
        for (File jar : Objects.requireNonNull(new File(srcPath.replace("src", "lib")).listFiles())) {
            dependencies.add(jar.getAbsolutePath());
        }
        dependencies.add("libs"+DTPlatform.FILE_SEPARATOR+"rt.jar");
        Integer complianceLevel = 8;
        LibSpoonUseful spoonUseful = new LibSpoonUseful(srcPath, dependencies.toArray(new String[0]), complianceLevel, true);

        // 处理每一个testcase
        List<String> testcases = getTestCases(new File(rootPath+DTPlatform.FILE_SEPARATOR+"benchmarks"+DTPlatform.FILE_SEPARATOR+project+DTPlatform.FILE_SEPARATOR+"testcases.txt"));

        String outPath = rootPath+DTPlatform.FILE_SEPARATOR+"tmp"+DTPlatform.FILE_SEPARATOR+project+DTPlatform.FILE_SEPARATOR+"code";
        new File(outPath).mkdirs();
        for (String testcase : testcases) {
            CtClass target = spoonUseful.getClass(testcase);
            File outFile =new File(outPath+DTPlatform.FILE_SEPARATOR+testcase+".java");
            FileWriter fileWriter = new FileWriter(outFile);
            fileWriter.write(target.toString());
            fileWriter.flush();
            fileWriter.close();
        }
    }
    /**
     * 从 testcases.txt 文件中获取测试用例名
     * @param testcases 测试用例文件
     * @return 测试用例名 列表
     */
    private static List<String> getTestCases(File testcases) throws IOException {
        List<String> result = new ArrayList<>();
        BufferedReader bufferedReader = new BufferedReader(new FileReader(testcases));
        String line = bufferedReader.readLine();
        while(line!=null){
            result.add(line);
            line=bufferedReader.readLine();
        }
        return result;
    }

    public static void CollectProject(String rootPath, String project) throws IOException {
        String CollectProjectPath = rootPath + DTPlatform.FILE_SEPARATOR + "benchmarks" + DTPlatform.FILE_SEPARATOR + project;
        for (File subProject : Objects.requireNonNull(new File(CollectProjectPath).listFiles())) {
            List<String> packages = new ArrayList<>();
            packages.add("main");
            packages.add("test");
            for (String aPackage : packages) {
                String srcPath = subProject.getAbsolutePath()+DTPlatform.FILE_SEPARATOR+"src";

                List<String> dependencies = new ArrayList<>();


                for (File jar : Objects.requireNonNull(new File(subProject.getAbsolutePath()+DTPlatform.FILE_SEPARATOR+"lib").listFiles())) {
                    dependencies.add(jar.getAbsolutePath());
                }
                dependencies.add("libs"+DTPlatform.FILE_SEPARATOR+"rt.jar");
                Integer complianceLevel = 8;
                LibSpoonUseful spoonUseful = null;
                srcPath = srcPath+DTPlatform.FILE_SEPARATOR+aPackage+DTPlatform.FILE_SEPARATOR+"java";
                System.out.println(srcPath);
                if (!new File(srcPath).exists()){
                    continue;
                }
                try {
                    spoonUseful = new LibSpoonUseful(srcPath, dependencies.toArray(new String[0]), complianceLevel, true);
                }catch (Error e){
                    System.out.println("ProjectLoadError:"+subProject.getName());
                    e.printStackTrace();
                    continue;
                }



                // 处理每一个testcase
                List<String> testcases = getTestCases(new File(subProject.getAbsolutePath()+DTPlatform.FILE_SEPARATOR+"testcases.txt"));

                String outPath = rootPath+DTPlatform.FILE_SEPARATOR+"tmp"+DTPlatform.FILE_SEPARATOR+project+DTPlatform.FILE_SEPARATOR+"code";
                new File(outPath).mkdirs();
                for (String testcase : testcases) {
                    CtClass target = spoonUseful.getClass(testcase);
                    if (target==null){
                        System.out.println("ClassNotFindError:"+subProject.getName()+" "+testcase);
                        continue;
                    }
                    File outFile =new File(outPath+DTPlatform.FILE_SEPARATOR+testcase+".java");
                    FileWriter fileWriter = new FileWriter(outFile);
                    fileWriter.write(target.toString());
                    fileWriter.flush();
                    fileWriter.close();
                }
            }
        }

    }
}
