import org.junit.Test;

import static org.junit.Assert.*;

public class JunitGradesTest2 {

    @Test
    public void grade() {

        Grades g = new Grades();
        String mark= g.Grade(50, 50);
        assertEquals("Pass,C",mark);
    }
}