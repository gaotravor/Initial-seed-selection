package utils;

public class ProgressBar {
    private long count = 0;
    private long max = 0;

    public static void main(String[] args) {
        ProgressBar progressBar = new ProgressBar(1000);
        for(int i=0;i<1000;i++){
            progressBar.showBar(i+1);
        }
    }
    public ProgressBar(long max){
        count = 0;
        this.max = max;
    }

    public void reset(long max){
        count = 0;
        this.max = max;
    }

    public void showBar(){
        count++;
        System.out.print((int)Math.floor((count+1.0)/max*100));
        System.out.print("/100");
        for (int i=0;i<100;i++){
            if(i<(count+1.0)/max*100-1){
                System.out.print("*");
            }else {
                System.out.print("-");
            }
        }
        System.out.print(count);
        System.out.print("/");
        System.out.print(max);
        System.out.println();
    }

    public void showBar(long count){
        System.out.print((int)Math.floor((count+1.0)/max*100));
        System.out.print("/100");
        for (int i=0;i<100;i++){
            if(i<(count+1.0)/max*100-1){
                System.out.print("*");
            }else {
                System.out.print("-");
            }
        }
        System.out.print(count);
        System.out.print("/");
        System.out.print(max);
        System.out.println();

    }

}
