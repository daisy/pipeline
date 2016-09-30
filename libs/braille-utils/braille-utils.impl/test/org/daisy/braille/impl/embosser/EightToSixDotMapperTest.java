package org.daisy.braille.impl.embosser;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
public class EightToSixDotMapperTest {

	@Test
	public void testMapper() {
		EightToSixDotMapper ced = new EightToSixDotMapper(16);
		ced.write("⠀⠀⠀⠁⠀⠂⠀⠃⠀⠄⠀⠅⠀⠆⠀⠇");ced.newLine(1);
		ced.write("⠀⠈⠀⠉⠀⠊⠀⠋⠀⠌⠀⠍⠀⠎⠀⠏");ced.newLine(1);
		ced.write("⠀⠐⠀⠑⠀⠒⠀⠓⠀⠔⠀⠕⠀⠖⠀⠗");ced.newLine(1);
		ced.write("⠀⠘⠀⠙⠀⠚⠀⠛⠀⠜⠀⠝⠀⠞⠀⠟");ced.newLine(1);
		ced.write("⠀⠠⠀⠡⠀⠢⠀⠣⠀⠤⠀⠥⠀⠦⠀⠧");ced.newLine(1);
		ced.write("⠀⠨⠀⠩⠀⠪⠀⠫⠀⠬⠀⠭⠀⠮⠀⠯");ced.newLine(1);
		ced.write("⠀⠰⠀⠱⠀⠲⠀⠳⠀⠴⠀⠵⠀⠶⠀⠷");ced.newLine(1);
		ced.write("⠀⠸⠀⠹⠀⠺⠀⠻⠀⠼⠀⠽⠀⠾⠀⠿");
		ced.flush();

		assertEquals(ced.readLine(), "⠀⠀⠀⠁⠀⠂⠀⠃⠀⠄⠀⠅⠀⠆⠀⠇");
		assertEquals(ced.readLine(), "⠀⠠⠀⠤⠀⠠⠀⠤⠀⠠⠀⠤⠀⠠⠀⠤");
		assertEquals(ced.readLine(), "⠀⠀⠀⠀⠀⠁⠀⠁⠀⠂⠀⠂⠀⠃⠀⠃");
		assertEquals(ced.readLine(), "⠀⠠⠀⠢⠀⠤⠀⠦⠀⠠⠀⠢⠀⠤⠀⠦");
		assertEquals(ced.readLine(), "⠀⠀⠀⠀⠀⠀⠀⠀⠀⠁⠀⠁⠀⠁⠀⠁");
		assertEquals(ced.readLine(), "⠀⠘⠀⠙⠀⠚⠀⠛⠀⠜⠀⠝⠀⠞⠀⠟");
		assertEquals(ced.readLine(), "⠀⠀⠀⠄⠀⠀⠀⠄⠀⠀⠀⠄⠀⠀⠀⠄");
		assertEquals(ced.readLine(), "⠀⠐⠀⠐⠀⠑⠀⠑⠀⠒⠀⠒⠀⠓⠀⠓");
		assertEquals(ced.readLine(), "⠀⠐⠀⠒⠀⠔⠀⠖⠀⠐⠀⠒⠀⠔⠀⠖");
		assertEquals(ced.readLine(), "⠀⠈⠀⠈⠀⠈⠀⠈⠀⠉⠀⠉⠀⠉⠀⠉");
		assertEquals(ced.readLine(), "⠀⠰⠀⠱⠀⠲⠀⠳⠀⠴⠀⠵⠀⠶⠀⠷");
		assertEquals(ced.readLine(), "⠀⠠⠀⠤⠀⠠⠀⠤⠀⠠⠀⠤⠀⠠⠀⠤");
		assertEquals(ced.readLine(), "⠀⠘⠀⠘⠀⠙⠀⠙⠀⠚⠀⠚⠀⠛⠀⠛");

		ced.write("⠀⡀⠀⡁⠀⡂⠀⡃⠀⡄⠀⡅⠀⡆⠀⡇");ced.newLine(1);
		ced.write("⠀⡈⠀⡉⠀⡊⠀⡋⠀⡌⠀⡍⠀⡎⠀⡏");ced.newLine(1);
		ced.write("⠀⡐⠀⡑⠀⡒⠀⡓⠀⡔⠀⡕⠀⡖⠀⡗");ced.newLine(1);
		ced.write("⠀⡘⠀⡙⠀⡚⠀⡛⠀⡜⠀⡝⠀⡞⠀⡟");ced.newLine(1);
		ced.write("⠀⡠⠀⡡⠀⡢⠀⡣⠀⡤⠀⡥⠀⡦⠀⡧");ced.newLine(1);
		ced.write("⠀⡨⠀⡩⠀⡪⠀⡫⠀⡬⠀⡭⠀⡮⠀⡯");ced.newLine(1);
		ced.write("⠀⡰⠀⡱⠀⡲⠀⡳⠀⡴⠀⡵⠀⡶⠀⡷");ced.newLine(1);
		ced.write("⠀⡸⠀⡹⠀⡺⠀⡻⠀⡼⠀⡽⠀⡾⠀⡿");
		ced.flush();

		assertEquals(ced.readLine(), "⠀⠀⠀⠁⠀⠂⠀⠃⠀⠄⠀⠅⠀⠆⠀⠇");
		assertEquals(ced.readLine(), "⠀⠡⠀⠥⠀⠡⠀⠥⠀⠡⠀⠥⠀⠡⠀⠥");
		assertEquals(ced.readLine(), "⠀⠄⠀⠄⠀⠅⠀⠅⠀⠆⠀⠆⠀⠇⠀⠇");
		assertEquals(ced.readLine(), "⠀⠠⠀⠢⠀⠤⠀⠦⠀⠠⠀⠢⠀⠤⠀⠦");
		assertEquals(ced.readLine(), "⠀⠂⠀⠂⠀⠂⠀⠂⠀⠃⠀⠃⠀⠃⠀⠃");
		assertEquals(ced.readLine(), "⠀⠘⠀⠙⠀⠚⠀⠛⠀⠜⠀⠝⠀⠞⠀⠟");
		assertEquals(ced.readLine(), "⠀⠁⠀⠅⠀⠁⠀⠅⠀⠁⠀⠅⠀⠁⠀⠅");
		assertEquals(ced.readLine(), "⠀⠔⠀⠔⠀⠕⠀⠕⠀⠖⠀⠖⠀⠗⠀⠗");
		assertEquals(ced.readLine(), "⠀⠐⠀⠒⠀⠔⠀⠖⠀⠐⠀⠒⠀⠔⠀⠖");
		assertEquals(ced.readLine(), "⠀⠊⠀⠊⠀⠊⠀⠊⠀⠋⠀⠋⠀⠋⠀⠋");
		assertEquals(ced.readLine(), "⠀⠰⠀⠱⠀⠲⠀⠳⠀⠴⠀⠵⠀⠶⠀⠷");
		assertEquals(ced.readLine(), "⠀⠡⠀⠥⠀⠡⠀⠥⠀⠡⠀⠥⠀⠡⠀⠥");
		assertEquals(ced.readLine(), "⠀⠜⠀⠜⠀⠝⠀⠝⠀⠞⠀⠞⠀⠟⠀⠟");

		ced.write("⠀⢀⠀⢁⠀⢂⠀⢃⠀⢄⠀⢅⠀⢆⠀⢇");ced.newLine(1);
		ced.write("⠀⢈⠀⢉⠀⢊⠀⢋⠀⢌⠀⢍⠀⢎⠀⢏");ced.newLine(1);
		ced.write("⠀⢐⠀⢑⠀⢒⠀⢓⠀⢔⠀⢕⠀⢖⠀⢗");ced.newLine(1);
		ced.write("⠀⢘⠀⢙⠀⢚⠀⢛⠀⢜⠀⢝⠀⢞⠀⢟");ced.newLine(1);
		ced.write("⠀⢠⠀⢡⠀⢢⠀⢣⠀⢤⠀⢥⠀⢦⠀⢧");ced.newLine(1);
		ced.write("⠀⢨⠀⢩⠀⢪⠀⢫⠀⢬⠀⢭⠀⢮⠀⢯");ced.newLine(1);
		ced.write("⠀⢰⠀⢱⠀⢲⠀⢳⠀⢴⠀⢵⠀⢶⠀⢷");ced.newLine(1);
		ced.write("⠀⢸⠀⢹⠀⢺⠀⢻⠀⢼⠀⢽⠀⢾⠀⢿");
		ced.flush();

		assertEquals(ced.readLine(), "⠀⠀⠀⠁⠀⠂⠀⠃⠀⠄⠀⠅⠀⠆⠀⠇");
		assertEquals(ced.readLine(), "⠀⠨⠀⠬⠀⠨⠀⠬⠀⠨⠀⠬⠀⠨⠀⠬");
		assertEquals(ced.readLine(), "⠀⠠⠀⠠⠀⠡⠀⠡⠀⠢⠀⠢⠀⠣⠀⠣");
		assertEquals(ced.readLine(), "⠀⠠⠀⠢⠀⠤⠀⠦⠀⠠⠀⠢⠀⠤⠀⠦");
		assertEquals(ced.readLine(), "⠀⠐⠀⠐⠀⠐⠀⠐⠀⠑⠀⠑⠀⠑⠀⠑");
		assertEquals(ced.readLine(), "⠀⠘⠀⠙⠀⠚⠀⠛⠀⠜⠀⠝⠀⠞⠀⠟");
		assertEquals(ced.readLine(), "⠀⠈⠀⠌⠀⠈⠀⠌⠀⠈⠀⠌⠀⠈⠀⠌");
		assertEquals(ced.readLine(), "⠀⠰⠀⠰⠀⠱⠀⠱⠀⠲⠀⠲⠀⠳⠀⠳");
		assertEquals(ced.readLine(), "⠀⠐⠀⠒⠀⠔⠀⠖⠀⠐⠀⠒⠀⠔⠀⠖");
		assertEquals(ced.readLine(), "⠀⠘⠀⠘⠀⠘⠀⠘⠀⠙⠀⠙⠀⠙⠀⠙");
		assertEquals(ced.readLine(), "⠀⠰⠀⠱⠀⠲⠀⠳⠀⠴⠀⠵⠀⠶⠀⠷");
		assertEquals(ced.readLine(), "⠀⠨⠀⠬⠀⠨⠀⠬⠀⠨⠀⠬⠀⠨⠀⠬");
		assertEquals(ced.readLine(), "⠀⠸⠀⠸⠀⠹⠀⠹⠀⠺⠀⠺⠀⠻⠀⠻");

		ced.write("⠀⣀⠀⣁⠀⣂⠀⣃⠀⣄⠀⣅⠀⣆⠀⣇");ced.newLine(1);
		ced.write("⠀⣈⠀⣉⠀⣊⠀⣋⠀⣌⠀⣍⠀⣎⠀⣏");ced.newLine(1);
		ced.write("⠀⣐⠀⣑⠀⣒⠀⣓⠀⣔⠀⣕⠀⣖⠀⣗");ced.newLine(1);
		ced.write("⠀⣘⠀⣙⠀⣚⠀⣛⠀⣜⠀⣝⠀⣞⠀⣟");ced.newLine(1);
		ced.write("⠀⣠⠀⣡⠀⣢⠀⣣⠀⣤⠀⣥⠀⣦⠀⣧");ced.newLine(1);
		ced.write("⠀⣨⠀⣩⠀⣪⠀⣫⠀⣬⠀⣭⠀⣮⠀⣯");ced.newLine(1);
		ced.write("⠀⣰⠀⣱⠀⣲⠀⣳⠀⣴⠀⣵⠀⣶⠀⣷");ced.newLine(1);
		ced.write("⠀⣸⠀⣹⠀⣺⠀⣻⠀⣼⠀⣽⠀⣾⠀⣿");
		ced.flush();

		assertEquals(ced.readLine(), "⠀⠀⠀⠁⠀⠂⠀⠃⠀⠄⠀⠅⠀⠆⠀⠇");
		assertEquals(ced.readLine(), "⠀⠩⠀⠭⠀⠩⠀⠭⠀⠩⠀⠭⠀⠩⠀⠭");
		assertEquals(ced.readLine(), "⠀⠤⠀⠤⠀⠥⠀⠥⠀⠦⠀⠦⠀⠧⠀⠧");
		assertEquals(ced.readLine(), "⠀⠠⠀⠢⠀⠤⠀⠦⠀⠠⠀⠢⠀⠤⠀⠦");
		assertEquals(ced.readLine(), "⠀⠒⠀⠒⠀⠒⠀⠒⠀⠓⠀⠓⠀⠓⠀⠓");
		assertEquals(ced.readLine(), "⠀⠘⠀⠙⠀⠚⠀⠛⠀⠜⠀⠝⠀⠞⠀⠟");
		assertEquals(ced.readLine(), "⠀⠉⠀⠍⠀⠉⠀⠍⠀⠉⠀⠍⠀⠉⠀⠍");
		assertEquals(ced.readLine(), "⠀⠴⠀⠴⠀⠵⠀⠵⠀⠶⠀⠶⠀⠷⠀⠷");
		assertEquals(ced.readLine(), "⠀⠐⠀⠒⠀⠔⠀⠖⠀⠐⠀⠒⠀⠔⠀⠖");
		assertEquals(ced.readLine(), "⠀⠚⠀⠚⠀⠚⠀⠚⠀⠛⠀⠛⠀⠛⠀⠛");
		assertEquals(ced.readLine(), "⠀⠰⠀⠱⠀⠲⠀⠳⠀⠴⠀⠵⠀⠶⠀⠷");
		assertEquals(ced.readLine(), "⠀⠩⠀⠭⠀⠩⠀⠭⠀⠩⠀⠭⠀⠩⠀⠭");
		assertEquals(ced.readLine(), "⠀⠼⠀⠼⠀⠽⠀⠽⠀⠾⠀⠾⠀⠿⠀⠿");

	}

}
