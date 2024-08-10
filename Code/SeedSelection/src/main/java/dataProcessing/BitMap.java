package dataProcessing;

import dataProcessing.utils.CovRecord;
import utils.DTPlatform;
import utils.ProgressBar;

import java.io.*;
import java.util.*;

public class BitMap {
    String fileName; // 生成此 bitmap 的文件
    double exeTime; // 执行文件所需时间
    List<Integer> bits = new ArrayList<>(); // 存储所有的 bit
    long counter = 0; // 计数器，每32(int长度)个bit添加新的Integer，并且重新计数
    static List<String> allBranchId = new ArrayList<>();


    public static final int LINE_COVERAGE_BITMAP = 0;
    public static final int FUNCTION_COVERAGE_BITMAP = 1;
    public static final int BRANCH_COVERAGE_BITMAP = 2;

    public static int bitMapMode = LINE_COVERAGE_BITMAP; // 使用什么信息构建 bitmap


    public static void uniqueBugInfo2bitMap(String path) throws IOException{
        List<String> allUniqueBug = new ArrayList<>();
        BitMap allBitMap = null;

        // 遍历获取全部的uniqueBug
        File file = new File(path+ DTPlatform.FILE_SEPARATOR+"info");
        for (File listFile : Objects.requireNonNull(file.listFiles())) {
            if(listFile.getName().contains(".info")){
                BufferedReader bufferedReader = new BufferedReader(new FileReader(listFile));
                String line = bufferedReader.readLine();
                while(line!=null){
                    if(!allUniqueBug.contains(line)){
                        allUniqueBug.add(line);
                    }
                    line=bufferedReader.readLine();
                }
            }
        }
        Collections.sort(allUniqueBug);

        // 对每一个测试用例构建bitMap
        for (File listFile : Objects.requireNonNull(file.listFiles())) {
            if(listFile.getName().contains(".info")){
                List<String> uniqueBugList = new ArrayList<>();

                // 输出 uniqueBug 基本信息
                System.out.println(listFile.getName());
                BufferedReader bufferedReader = new BufferedReader(new FileReader(listFile));
                String line = bufferedReader.readLine();
                while(line!=null){
                    System.out.print(line+";");
                    uniqueBugList.add(line);
                    line=bufferedReader.readLine();
                }
                System.out.println();

                // 使用文件中的 uniqueBug 信息构建 bitMap
                String fileName = listFile.getName().split("@")[0];
                double exeTime = 0;
                if (listFile.getName().contains("@")){
                    exeTime = Long.parseLong(listFile.getName().split("@")[1].split("\\.")[0]);
                }

                BitMap bitMap =new BitMap(uniqueBugList,allUniqueBug,fileName,exeTime);

                if(allBitMap == null){
                    allBitMap = new BitMap(bitMap.getBits().size(),"allBitMap",0);
                }
                allBitMap = BitMap.add(allBitMap,bitMap,"allBitMap",0);

                new File(path+"\\bitmap").mkdirs();
                bitMap.saveBitMap(path+"\\bitmap\\"+listFile.getName().replace(".info",".bitmap"));
            }
        }
    }


    public static void covInfo2bitMap(String path) throws IOException, ClassNotFoundException {
        String rootPath = path;
        BitMap allBitMap = null;
        File file = new File(rootPath+DTPlatform.FILE_SEPARATOR+"info");
        if(bitMapMode == BRANCH_COVERAGE_BITMAP){
            ProgressBar progressBar = new ProgressBar(Objects.requireNonNull(file.listFiles()).length);
            for (File listFile : Objects.requireNonNull(file.listFiles())) {
                progressBar.showBar();
                if (listFile.getName().contains(".info")) {
                    if(allBranchId.size() == 0){
                        System.out.println(CovAnalyzer.initAndAnalysis(listFile.getAbsolutePath()));
                        for (String fileName : CovAnalyzer.getCovRecordList().keySet()) {
                            CovRecord covRecord = CovAnalyzer.getCovRecordList().get(fileName);
                            for (String branchId : covRecord.BRDAs.keySet()) {
                                allBranchId.add(fileName+"@@"+branchId);
                            }
                        }
                    }else {
                        List<String> branchIds = new ArrayList<>();
                        System.out.println(CovAnalyzer.initAndAnalysis(listFile.getAbsolutePath()));
                        for (String fileName : CovAnalyzer.getCovRecordList().keySet()) {
                            CovRecord covRecord = CovAnalyzer.getCovRecordList().get(fileName);
                            for (String branchId : covRecord.BRDAs.keySet()) {
                                branchIds.add(fileName+"@@"+branchId);
                            }
                        }
                        allBranchId.retainAll(branchIds);
                    }
                    System.out.println(allBranchId.size());
                }
            }

        }

        for (File listFile : Objects.requireNonNull(file.listFiles())) {
            if(listFile.getName().contains(".info")){

                // 输出 covInfo 基本信息
                System.out.println(listFile.getName());
                System.out.println(CovAnalyzer.initAndAnalysis(listFile.getAbsolutePath()));

                // 使用 covInfo 信息构建 bitMap
                String fileName = listFile.getName().split("@")[0];
                double exeTime = 0;
                if (fileName.contains("@")){
                    exeTime = Long.parseLong(listFile.getName().split("@")[1].split("\\.")[0]);
                }
                BitMap bitMap = new BitMap(CovAnalyzer.getCovRecordList(),fileName,exeTime);

                if(allBitMap == null){
                    allBitMap = new BitMap(bitMap.getBits().size(),"allBitMap",0);
                }
                allBitMap = BitMap.add(allBitMap,bitMap,"allBitMap",0);

                System.out.println(allBitMap.hitCount());
                // 分模式保存到对应的文件夹
                if(bitMapMode == LINE_COVERAGE_BITMAP){
                    new File(rootPath+"\\lineBitMap").mkdirs();
                    bitMap.saveBitMap(rootPath+"\\lineBitMap\\"+listFile.getName().replace(".info",".bitmap"));
                }
                if(bitMapMode == BRANCH_COVERAGE_BITMAP){
                    new File(rootPath+"\\branchBitMap").mkdirs();
                    bitMap.saveBitMap(rootPath+"\\branchBitMap\\"+listFile.getName().replace(".info",".bitmap"));
                }
            }
        }
    }

    /**
     * 使用收集的uniqueBug构建bitmap, 注意使用 unqiueBug 构建 bitmap 必须一次性提供全部的测试用例结果
     * @param uniqueBugList 当前测试用例发现的uniqueBug
     * @param allUniqueBug 所有测试用例发现的全部unqiueBug
     * @param fileName 测试用例名字
     * @param exeTime 测试用例执行时间
     */
    public BitMap(List<String> uniqueBugList, List<String> allUniqueBug, String fileName, double exeTime){
        this.fileName = fileName;
        this.exeTime = exeTime;
        for (String uniqueBug : allUniqueBug) {
            if(counter  % 32 == 0){
                counter = 0;
                bits.add(0);
            }
            counter++;
            Integer integer = bits.get(bits.size() - 1);
            integer = integer<<1;
            if(uniqueBugList.contains(uniqueBug)){
                integer = integer | 1;
            }
            bits.set(bits.size() - 1, integer);
        }
        Integer integer = bits.get(bits.size() - 1);
        while (counter % 32 != 0){
            integer = integer<<1;
            counter++;
        }
        bits.set(bits.size() - 1, integer);
    }

    /**
     * 使用收集的覆盖率信息构建bitmap
     * @param covRecordList 测试用例产生的所有 CovRecord
     * @param fileName 测试用例名字
     * @param exeTime 测试用例执行时间
     */
    public BitMap(Map<String, CovRecord> covRecordList, String fileName, double exeTime) {
        this.fileName = fileName;
        this.exeTime = exeTime;
        if(bitMapMode == BRANCH_COVERAGE_BITMAP){
            for (String branchId : allBranchId) {
                String name = branchId.split("@@")[0];
                String id = branchId.split("@@")[1];
                if(counter  % 32 == 0){
                    counter = 0;
                    bits.add(0);
                }
                counter++;
                long covTime = covRecordList.get(name).BRDAs.get(id);
                Integer integer = bits.get(bits.size() - 1);
                integer = integer<<1;
                if( covTime != 0){
                    integer = integer | 1;
                }
                bits.set(bits.size() - 1, integer);
            }
        }else {
            for (String key : covRecordList.keySet()) {
                CovRecord covRecord = covRecordList.get(key);

                if(bitMapMode == LINE_COVERAGE_BITMAP){
                    // 使用行覆盖率生成 bitmap
                    Map<String, Long> DAs = covRecord.DAs;
                    for (String location : DAs.keySet()) {
                        if(counter  % 32 == 0){
                            counter = 0;
                            bits.add(0);
                        }
                        counter++;
                        long covTime = DAs.get(location);
                        Integer integer = bits.get(bits.size() - 1);
                        integer = integer<<1;
                        if( covTime != 0){
                            integer = integer | 1;
                        }
                        bits.set(bits.size() - 1, integer);
                    }
                }
                if(bitMapMode == FUNCTION_COVERAGE_BITMAP){
                    // 使用函数覆盖率生成 bitmap
                    Map<String, Long> FNDAs = covRecord.FNDAs;
                    for (String location : FNDAs.keySet()) {
                        if(counter  % 32 == 0){
                            counter = 0;
                            bits.add(0);
                        }
                        counter++;
                        long covTime = FNDAs.get(location);
                        Integer integer = bits.get(bits.size() - 1);
                        integer = integer<<1;
                        if( covTime != 0){
                            integer = integer | 1;
                        }
                        bits.set(bits.size() - 1, integer);
                    }
                }


            }
        }

        Integer integer = bits.get(bits.size() - 1);
        while (counter % 32 != 0){
            integer = integer<<1;
            counter++;
        }
        bits.set(bits.size() - 1, integer);
    }

    /**
     * 构建临时bitmap
     * @param size bitmap大小
     * @param fileName 测试用例名字
     * @param exeTime 测试用例执行时间
     */
    public BitMap(int size,String fileName, double exeTime) {
        this.fileName = fileName;
        this.exeTime = exeTime;
        for (int i=0;i<size;i++){
            bits.add(0);
        }
    }

    /**
     * 从文件读取 bitmap 信息
     * @param filePath 文件路径
     * @param fileName 测试用例名字
     * @param exeTime 测试用例执行时间
     * @throws IOException 文件读取
     * @throws ClassNotFoundException readObject
     */
    public BitMap(String filePath, String fileName, double exeTime) throws IOException, ClassNotFoundException {
        File file = new File(filePath);
        this.fileName = fileName;
        this.exeTime = exeTime;
        FileInputStream in = new FileInputStream(file);
        ObjectInputStream objIn = new ObjectInputStream(in);
        bits = (List<Integer>) objIn.readObject();
        objIn.close();
    }

    /**
     * 保存收集的bitmap信息
     * @param filePath 文件路径
     * @throws IOException write
     */
    public void saveBitMap(String filePath) throws IOException {
        File file = new File(filePath);
        file.createNewFile();
        FileOutputStream out = new FileOutputStream(file);
        ObjectOutputStream objOut=new ObjectOutputStream(out);
        objOut.writeObject(bits);
        objOut.flush();
        objOut.close();
        fileName = file.getName();
    }

    /**
     * 返回总覆盖数
     * @return bitmap中为 1 的个数
     */
    public long hitCount(){
        return showBitMap().replace("0","").replace("\n","").length();
    }

    /**
     * 展示bitmap
     * @return bitmap全部信息的字符串
     */
    public String showBitMap(){
        StringBuilder result = new StringBuilder();
        for (Integer bit : bits) {
            String s = Integer.toUnsignedString(bit, 2);
            for (int i=0;i<32 - s.length();i++){
                result.append("0");
            }
            result.append(s).append("\n");
        }
        return result.toString();
    }

    /**
     * 两个bitmap相减，计算规则如下
     * 1 1 0 0
     * 1 0 0 1
     *
     * 0 1 0 0
     * @param left left bitmap
     * @param right right bitmap
     * @return result bitmap
     */
    public static BitMap subtract(BitMap left, BitMap right,String fileName, double exeTime){
        BitMap bitMap = new BitMap(left.bits.size(),fileName,exeTime);
        bitMap.bits.clear();
        for(int i = 0;i<left.bits.size();i++){
            bitMap.bits.add(subtract(left.bits.get(i),right.bits.get(i)));
        }
        return bitMap;
    }
    private static Integer subtract(Integer a, Integer b){
        return (a ^ b) & a;
    }

    /**
     * 两个bitmap相加，计算规则如下
     * 1 1 0 0
     * 1 0 0 1
     *
     * 1 1 0 1
     * @param left left bitmap
     * @param right right bitmap
     * @param fileName 文件名
     * @param exeTime 执行时间
     * @return result bitmap
     */
    public static BitMap add(BitMap left, BitMap right,String fileName,double exeTime){
        BitMap bitMap = new BitMap(left.bits.size(),fileName,exeTime);
        bitMap.bits.clear();
        for(int i = 0;i<left.bits.size();i++){
            bitMap.bits.add( left.bits.get(i) | right.bits.get(i) );
        }
        return bitMap;
    }

    public List<Integer> getBits() {
        return bits;
    }

    public String getFileName() {
        return fileName;
    }

    public double getExeTime() {
        return exeTime;
    }

    /**
     * 获得 index 指向的具体 bit
     * @param index
     * @return
     */
    public boolean getBit(int index){
        if(index < 0 ){
            throw new ArrayIndexOutOfBoundsException();
        }
        if(index >= bits.size() * 32){
            throw new ArrayIndexOutOfBoundsException();
        }
        int listIndex = index / 32;
        int bitIndex = index % 32;
        String s = Integer.toUnsignedString(bits.get(listIndex),2);
        while (s.length() < 32){
            s = "0" + s;
        }
        char c = s.charAt(bitIndex);
        return c == '1';
    }

    /**
     * 获得全部的具体 bit
     * @return
     */
    public List<Boolean> getAllBit(){
        List<Boolean> result = new ArrayList<>();
        for(int i=0;i<bits.size() * 32;i++){
            result.add(getBit(i));
        }
        return result;
    }
}
