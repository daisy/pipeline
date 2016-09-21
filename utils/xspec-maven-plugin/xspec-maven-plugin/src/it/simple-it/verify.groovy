File reportsDir = new File( basedir, "target/surefire-reports");

assert new File( reportsDir, "OUT-test.txt" ).isFile()
assert new File( reportsDir, "XSPEC-test.xml" ).isFile()
assert new File( reportsDir, "HTML-test.html" ).isFile()
assert new File( reportsDir, "TEST-test.xml" ).isFile()
