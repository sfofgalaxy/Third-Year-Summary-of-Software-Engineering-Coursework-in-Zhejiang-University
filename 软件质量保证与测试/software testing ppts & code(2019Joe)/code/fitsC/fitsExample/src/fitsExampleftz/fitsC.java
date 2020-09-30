package fitsExampleftz;

public class fitsC {

    Status fits(int passengers, boolean comfortFlag) {
        Status rv = Status.FAILURE;
        int availableSeats = 120;
        if (comfortFlag)
            availableSeats = availableSeats - 40;
        if ((passengers > 0) && (passengers <= availableSeats))
            rv = Status.SUCCESS;
        else if (passengers < 0)
            rv = Status.ERROR;
        return rv;

    }
        public enum Status {FAILURE, SUCCESS, ERROR};

}
