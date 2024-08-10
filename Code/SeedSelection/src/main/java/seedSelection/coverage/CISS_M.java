package seedSelection.coverage;

import utils.BitMap;

import java.util.*;

public class CISS_M extends CISS {


    public CISS_M(List<BitMap> bitMapList, double budget, double originSize) {
        super(budget, originSize);
        this.bitMapList = bitMapList;
        allBitMap = new BitMap(bitMapList.get(0).getBits().size(),"allBitMap",0);
    }

    @Override
    public void reduce(){
        Map<String,Long> name2count = new HashMap<>();
        for (BitMap bitMap : bitMapList) {
            name2count.put(bitMap.getFileName(),bitMap.hitCount());
        }
        while (true){
            sort();

            if(bitMapList.get(0).hitCount() == 0){
                break;
            }
            allBitMap = BitMap.add(allBitMap,bitMapList.get(0),"allBitMap",0);
            sumTime += bitMapList.get(0).getExeTime();
            fileNameList.add(bitMapList.get(0).getFileName());
            bitMapList.remove(0);
            List<BitMap> temp = new ArrayList<>();
            for (BitMap bitMap : bitMapList) {
                temp.add(BitMap.subtract(bitMap,allBitMap,bitMap.getFileName(),bitMap.getExeTime()));
            }
            bitMapList = temp;
        }
        // name2count 按照value排序
        List<Map.Entry<String, Long>> list = new ArrayList<>(name2count.entrySet());
        list.sort((o1, o2) -> (int) (o2.getValue() - o1.getValue()));
        for (Map.Entry<String, Long> entry : list) {
            fileNameList.add(entry.getKey());
        }
    }

    protected void sort(){
        bitMapList.sort(new Comparator<BitMap>() {
            @Override
            public int compare(BitMap t1, BitMap t2) {
                return Long.compare(t2.hitCount(), t1.hitCount());
            }
        });
    }
}
