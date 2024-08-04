package booking.entites;

public class Counter {
    private int count;
    
    public Counter(){
    	count=0;
    }
    
    public int getCount() {
    	return this.count;
    }
    
    public void increment() {
    	this.count++;
    }
    
}
