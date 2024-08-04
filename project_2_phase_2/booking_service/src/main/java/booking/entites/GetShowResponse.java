package booking.entites;

import booking.CborSerializable;

public class GetShowResponse implements CborSerializable {
   private boolean valid;
   private Show show;
   
   public GetShowResponse(boolean valid, Show show){
	   this.valid=valid;
	   this.show=show;
   }
   
   public boolean getValid() {
	   return this.valid;
   }
   
   public Show getShow() {
	   return this.show;
   }
   
}
