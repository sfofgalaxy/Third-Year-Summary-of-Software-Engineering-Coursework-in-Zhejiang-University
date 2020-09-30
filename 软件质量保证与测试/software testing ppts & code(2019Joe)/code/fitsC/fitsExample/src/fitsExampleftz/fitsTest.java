package fitsExampleftz;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import fitsExampleftz.fitsC.Status;

class fitsTest {

	@Test
	void test() {
	     fitsC ft=new fitsC();
	     Status actual=ft.fits(60,true);
	       assertEquals(actual,fitsC.Status.SUCCESS);
	}
}
