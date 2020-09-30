package CIns;

import org.testng.annotations.Test;

import static org.testng.Assert.*;

public class CarInsuranceTest {

    @org.testng.annotations.Test
    public void testCarInsurance() {
        CarInsurance CI=new CarInsurance();
        int PremiumExpected=500;
        int PremiumActual;
        PremiumActual = CI.CarIns(40, 'M', true);
        assertEquals(PremiumActual,PremiumExpected);
    }


}