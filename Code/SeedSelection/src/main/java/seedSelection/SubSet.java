package seedSelection;

import java.util.ArrayList;
import java.util.List;

abstract public class SubSet {
    protected List<String> fileNameList = new ArrayList<>();
    protected int sumTime = 0;
    protected int targetSize;

    public SubSet(double budget, double originSize) {
        this.targetSize = (int) Math.floor((budget * originSize) / 100);
    }

    public List<String> getFileNameList() {
        return fileNameList;
    }

    public int getSumTime() {
        return sumTime;
    }

    public abstract void reduce() throws Exception;
}
