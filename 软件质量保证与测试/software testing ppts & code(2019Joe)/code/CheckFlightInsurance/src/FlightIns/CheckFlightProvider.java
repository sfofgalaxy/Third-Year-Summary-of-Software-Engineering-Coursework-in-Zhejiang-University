package FlightIns;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import static org.testng.Assert.assertEquals;

public class CheckFlightProvider {

    @DataProvider(name = "data-provider")
    public static Object[][] dataProviderMethod() {
        Object[][] data = new Object[2][3];
        data[0][0] = true;data[0][1] = true;data[0][2] = 20;
        data[1][0] = false; data[1][1] = false; data[1][2] = 5;
        return data;
    }
   @Test(dataProvider = "data-provider")
    public void setData(boolean sportsEquipment, boolean musicEquipment,int ExpectedInsurance) {
        FlightInsurance CompIns=new FlightInsurance();
        int ActualInsurance=0;
        assertEquals(CompIns(sportsEquipment, musicEquipment), ExpectedInsurance);
       }

    private int CompIns(boolean sportsEquipment, boolean musicEquipment) {
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








