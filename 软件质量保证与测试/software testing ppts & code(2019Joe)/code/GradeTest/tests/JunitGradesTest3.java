import org.junit.Test;

import static org.junit.Assert.*;

public class JunitGradesTest3 {

    @Test
    public void grade() {
        Grades g = new Grades();
        //String mark= g.Grade(90, 30);
        String mark= g.Grade(90, 50);
        assertEquals("Pass,A",mark);
    }
}