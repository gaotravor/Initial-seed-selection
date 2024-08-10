package seedSelection.coverage;

import seedSelection.SubSet;
import utils.BitMap;
import ilog.concert.IloException;

import java.util.List;

public abstract class CISS extends SubSet {
    protected BitMap allBitMap;
    protected List<BitMap> bitMapList;


    public CISS(double budget, double originSize) {
        super(budget, originSize);
    }

    protected void sort() {

    }

    /**
     * 对 bitMapList 进行约简，要求记录子集的
     * 所有文件名 fileNames
     * 每个运行一次所需时间 sumTime
     * 被选择的测试用例的和 allBitMap
     */
    public abstract void reduce() throws IloException;


    public BitMap getAllBitMap() {
        return allBitMap;
    }
}
