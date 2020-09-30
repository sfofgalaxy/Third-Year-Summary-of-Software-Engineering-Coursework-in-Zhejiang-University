import org.junit.Test;

import static org.junit.Assert.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class JunitGradesTest1 {

    @Test
    public void grade() {
        Grades g=new Grades();
        String mark = g.Grade(-1,-1);
        assertEquals("Marks out of range", mark);
    }
}