package FlightIns;

public class FlightInsurance {
    public int ComputeInsurance(boolean sportsEquipment, boolean musicEquipment)
    {
        int    insurance;
        if (sportsEquipment == true && musicEquipment == true)
            insurance = 20;
        else if((sportsEquipment == true && musicEquipment == false)  || (sportsEquipment == false && musicEquipment == true))
            insurance = 10;
        else
            insurance = 5;
        return insurance;

    }
}
