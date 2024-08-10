package seedSelection.coverage;

import utils.BitMap;

import java.util.Comparator;
import java.util.List;

public class CISS_P extends CISS {



    public CISS_P(List<BitMap> bitMapList, double budget, double originSize) {
        super(budget, originSize);
        this.bitMapList = bitMapList;
        allBitMap = new BitMap(bitMapList.get(0).getBits().size(),"allBitMap",0);
    }

    @Override
    public void reduce(){
        sort();
        for (BitMap bitMap : bitMapList) {
            if(BitMap.subtract(bitMap, allBitMap,"temp",0).hitCount() != 0){
                allBitMap = BitMap.add(bitMap, allBitMap, "allBitMap",0);
                sumTime += bitMap.getExeTime();
                fileNameList.add(bitMap.getFileName());
            }
            allBitMap = BitMap.add(bitMap, allBitMap, "allBitMap",0);
            sumTime += bitMap.getExeTime();
            fileNameList.add(bitMap.getFileName());
        }
    }

    protected void sort(){
        bitMapList.sort(new Comparator<BitMap>() {
            @Override
            public int compare(BitMap t1, BitMap t2) {
                return Long.compare(t2.hitCount(),t1.hitCount());
            }
        });
    }
}