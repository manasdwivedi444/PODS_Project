package booking.entites;

import booking.CborSerializable;

public class Counter implements CborSerializable {
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
