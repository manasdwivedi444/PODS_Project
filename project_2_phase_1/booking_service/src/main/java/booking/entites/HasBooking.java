package booking.entites;

import akka.actor.typed.ActorRef;

public class HasBooking {
	private int show_id;
	private int user_id;
	private boolean valid;
	private Counter counter;
	private ActorRef<Boolean> replyTo;
	
	public HasBooking(int show_id,int user_id,Counter counter,ActorRef<Boolean> replyTo) {
		this.show_id=show_id;
		this.user_id=user_id;
		this.valid=false;
		this.counter=counter;
		this.replyTo=replyTo;
	}
	
	public int getUserId(){
		return this.user_id;
	}
	
	public int getShowId() {
		return this.show_id;
	}
	
	public boolean getVaild() {
		return this.valid;
	}
	
	public void setValid(boolean valid) {
		this.valid=valid;
	}
	
	public Counter getCounter() {
		return this.counter;
	}
	
	public ActorRef<Boolean> getReplyTo() {
		return this.replyTo;
	}

}
