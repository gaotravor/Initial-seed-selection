package seedSelection.prefuzz;

import seedSelection.SubSet;
import utils.BitMap;

import java.util.Comparator;
import java.util.List;

public class PISS extends SubSet {
    protected BitMap allBitMap;
    protected List<BitMap> bitMapList;


    public PISS(List<BitMap> bitMapList, double budget, double originSize) {
        super(budget, originSize);
        this.bitMapList = bitMapList;
        allBitMap = new BitMap(bitMapList.get(0).getBits().size(),"allBitMap",0);
    }

    @Override
    public void reduce(){
        sort();
        for (BitMap bitMap : bitMapList) {
            allBitMap = BitMap.add(bitMap, allBitMap, "allBitMap",0);
            sumTime += bitMap.getExeTime();
            fileNameList.add(bitMap.getFileName());
        }
    }

    protected void sort(){
        bitMapList.sort(new Comparator<BitMap>() {
            @Override
            public int compare(BitMap t1, BitMap t2) {
                return (int) (t2.hitCount() - t1.hitCount());
            }
        });
    }


    public BitMap getAllBitMap() {
        return allBitMap;
    }
}
