package utils;

import java.io.*;
import java.util.*;

public class BitMap {
    String fileName; // 生成此 bitmap 的文件
    double exeTime; // 执行文件所需时间
    List<Integer> bits = new ArrayList<>(); // 存储所有的 bit

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
