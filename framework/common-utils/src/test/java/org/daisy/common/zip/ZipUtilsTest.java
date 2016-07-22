package org.daisy.common.zip;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;


public class ZipUtilsTest {
	//baconised version of lorem ipsum
	private static String BLAH= new String("Bacon ipsum dolor sit amet doner ham shank filet mignon prosciutto, turducken boudin pork pork loin brisket leberkas. Turkey turducken doner pork loin pig beef ribs. Cow swine venison sausage t-bone ham biltong shank short loin prosciutto ham hock. Swine sausage turkey tri-tip andouille leberkas fatback short ribs pancetta salami tongue meatball boudin. Hamburger spare ribs sausage bacon prosciutto salami pig jowl short ribs chuck shank tail. Meatloaf ham hock cow, jowl tenderloin doner flank ribeye fatback spare ribs short loin tongue strip steak.");


	@Test
	public void deflateEmptyString() throws IOException{
		byte buff[]=ZipUtils.deflate("");
		Assert.assertEquals(buff.length,0);
	}
		
	@Test
	public void inflateEmptyBuffer()throws IOException{
		String res=ZipUtils.inflate(new byte[]{});
		Assert.assertTrue(res.isEmpty());
	}
	@Test
	public void goAndBack() throws IOException{
		String res=ZipUtils.inflate(ZipUtils.deflate(BLAH));
		Assert.assertEquals(BLAH,res);
	}
}
