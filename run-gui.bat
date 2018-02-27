set "MVN=mvn --settings %~dp0/settings.xml -Dworkspace=%~dp0/.maven-workspace -Dcache=%~dp0/.maven-cache -Dorg.ops4j.pax.url.mvn.localRepository=%~dp0/.maven-workspace -Dorg.daisy.org.ops4j.pax.url.mvn.settings=%~dp0/settings.xml"
%MVN% -f %~dp0\assembly\pom.xml clean package -Pdev-launcher || goto :error
%~dp0\assembly\target\dev-launcher\bin\pipeline2.bat gui
:error
exit /b %errorlevel%
