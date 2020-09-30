package CIns;

public class CarInsurance {
    public int CarIns (int age, char gender, boolean married) {
        int premium;
        if ((age<16) || (age>65) || (gender!='M' &&	gender!='F')) //if invalid age or gender
            premium=0;
        else {
            premium=500;
            if ((age<25) && (gender=='M') && (!married)) //if under 25
                //if ((age<25) && (gender=='M') || (!married)) //LCR mutation
                premium += 1500;
            else {       // if over 25
                if (married || gender=='F')
                    //if (!married || gender=='F')  //ROR mutation testing
                    premium -= 200;
                if ((age>=45) && (age<=65))
                    premium -= 100;
            }
        }
        return premium;
    }
}
