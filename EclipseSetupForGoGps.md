# Introduction #

This tutorial covers the basics of the process, from downloading Eclipse, Mercurial, and the needed plugins “MercurialEclipse” and “Maven Integration for Eclipse (Juno and newer)”, to running goGPS within the IDE.
If you already have Eclipse, Mercurial and these plugins installed and you are using them, most likely you will not need to read this tutorial for running goGPS Java within Eclipse.


# Details #

First of all you need to install the Mercurial command binaries; go to the Mercurial website (http://mercurial.selenic.com/), download and install a version suitable for your system.
_NOTE_: on some Windows installations, you might have to manually add the Mercurial installation folder to the PATH environment variable (e.g. C:\Program Files\Mercurial)

![http://wiki.gogps.googlecode.com/hg/img/EclipseSetupForGoGps/00a.png](http://wiki.gogps.googlecode.com/hg/img/EclipseSetupForGoGps/00a.png)

To check the result of the installation, try to execute a Mercurial command in the terminal. For example, type “hg” in the terminal and execute it.  If the below result appears, the installation was successful.

![http://wiki.gogps.googlecode.com/hg/img/EclipseSetupForGoGps/00-01a.png](http://wiki.gogps.googlecode.com/hg/img/EclipseSetupForGoGps/00-01a.png)

After the Mercurial installation, download and install Eclipse (here we are using “Eclipse Standard” version Kepler), run it, access the workbench and click on “Help” – “Eclipse Marketplace…”.

![http://wiki.gogps.googlecode.com/hg/img/EclipseSetupForGoGps/01a.png](http://wiki.gogps.googlecode.com/hg/img/EclipseSetupForGoGps/01a.png)

In the “Search” box that appears, input “Mercurial” and click the “Go” button.
After the search result appears, click the “Install” button in the “MercurialEclipse” section.

![http://wiki.gogps.googlecode.com/hg/img/EclipseSetupForGoGps/02a.png](http://wiki.gogps.googlecode.com/hg/img/EclipseSetupForGoGps/02a.png)

Select “MercurialEclipse” and just go through the following plugin installation windows.

![http://wiki.gogps.googlecode.com/hg/img/EclipseSetupForGoGps/03a.png](http://wiki.gogps.googlecode.com/hg/img/EclipseSetupForGoGps/03a.png)

After having restarted Eclipse as requested, you are ready to install the Maven plugin. Follow the same procedure as with the first plugin, but searching “Maven” (note that some recent versions of Eclipse may have the Maven plugin already installed)

![http://wiki.gogps.googlecode.com/hg/img/EclipseSetupForGoGps/04a.png](http://wiki.gogps.googlecode.com/hg/img/EclipseSetupForGoGps/04a.png)

Click the “Install” button in “Maven Integration for Eclipse (Juno and newer)” and go through the following windows.

![http://wiki.gogps.googlecode.com/hg/img/EclipseSetupForGoGps/05a.png](http://wiki.gogps.googlecode.com/hg/img/EclipseSetupForGoGps/05a.png)

Please note that installing these two Eclipse plugins does not necessarily mean that your system is already equipped with Mercurial and Maven. Depending on which operating system you are using, you might already have them installed or not. Please check your system and install the two software packages if necessary.

In case you are working behind a proxy server, you will need to setup Eclipse, Mercurial and Maven accordingly. Depending on the operating system that you are using, this might be done in different ways, so if you have problems with this step, feel free to ask in the gogps-discuss mailing list.

Once the two plugins are installed and working, you can import goGPS Java by clicking on “File” – “Import…”, opening the “Mercurial” folder and selecting “Clone Existing Mercurial Repository”.

![http://wiki.gogps.googlecode.com/hg/img/EclipseSetupForGoGps/06.png](http://wiki.gogps.googlecode.com/hg/img/EclipseSetupForGoGps/06.png)
![http://wiki.gogps.googlecode.com/hg/img/EclipseSetupForGoGps/07.png](http://wiki.gogps.googlecode.com/hg/img/EclipseSetupForGoGps/07.png)

The “Mercurial clone repository wizard” will appear, in which it is needed to enter "https://code.google.com/p/gogps/" as the “Repository location” URL and “gogps” in “Clone directory name”.

![http://wiki.gogps.googlecode.com/hg/img/EclipseSetupForGoGps/08.png](http://wiki.gogps.googlecode.com/hg/img/EclipseSetupForGoGps/08.png)

Click “Next” and select the “default” branch.

![http://wiki.gogps.googlecode.com/hg/img/EclipseSetupForGoGps/09.png](http://wiki.gogps.googlecode.com/hg/img/EclipseSetupForGoGps/09.png)

Follow the wizard screens until the code is imported. It will be shown in the “Package Explorer” panel of Eclipse.

![http://wiki.gogps.googlecode.com/hg/img/EclipseSetupForGoGps/10.png](http://wiki.gogps.googlecode.com/hg/img/EclipseSetupForGoGps/10.png)

Note that the procedure above has created a “gogps” folder in your Eclipse workspace, containing a folder “src” and a file “pom.xml”. Now create “data” and “test” folders inside “gogps”.

![http://wiki.gogps.googlecode.com/hg/img/EclipseSetupForGoGps/11.png](http://wiki.gogps.googlecode.com/hg/img/EclipseSetupForGoGps/11.png)

Within the “data” folder you can place the RINEX files contained in the archive “data.zip” available in the “Downloads” tab on goGPS Google code webpage (http://code.google.com/p/gogps/downloads/list).

![http://wiki.gogps.googlecode.com/hg/img/EclipseSetupForGoGps/12.png](http://wiki.gogps.googlecode.com/hg/img/EclipseSetupForGoGps/12.png)

If you have your own RINEX data available, you can place them in the “data” folder, but keep in mind that you will need to modify the paths leading to these files in “TestGoGPS.java”.

It is then needed to set the “.project” and “.classpath” files as follows. They must be placed in the “gogps” folder, together with the “pom.xml” file.

#### .project ####

```
<?xml version="1.0" encoding="UTF-8"?>
<projectDescription>
	<name>gogps</name>
	<comment></comment>
	<projects>
	</projects>
	<buildSpec>
		<buildCommand>
			<name>org.eclipse.jdt.core.javabuilder</name>
			<arguments>
			</arguments>
		</buildCommand>
	</buildSpec>
	<natures>
		<nature>org.eclipse.jdt.core.javanature</nature>
	</natures>
	<linkedResources>
		<link>
			<name>gogpsproject</name>
			<type>2</type>
	<locationURI>PROJECT_LOC/src/test/java/org/gogpsproject</locationURI>
		</link>
	</linkedResources>
</projectDescription>
```

#### .classpath ####

```
<?xml version="1.0" encoding="UTF-8"?>
<classpath>
	<classpathentry including="**/*.java" kind="src" output="target/classes" path="src/main/java"/>
	<classpathentry excluding="**" kind="src" output="target/classes" path="src/main/resources"/>
	<classpathentry kind="src" output="target/test-classes" path="src/test/java"/>
	<classpathentry kind="con" path="org.eclipse.jdt.launching.JRE_CONTAINER/org.eclipse.jdt.internal.debug.ui.launcher.StandardVMType/JavaSE-1.6"/>
	<classpathentry kind="output" path="target/classes"/>
</classpath>
```

After having prepared the “.project” and “.classpath” files, please close and restart Eclipse, you should see something like the following picture.

![http://wiki.gogps.googlecode.com/hg/img/EclipseSetupForGoGps/13.png](http://wiki.gogps.googlecode.com/hg/img/EclipseSetupForGoGps/13.png)

It is now time to set the project to use Maven, and to do that you can right-click on the project folder within the “Package Explorer” panel and select “Configure” – “Convert to Maven Project”.

![http://wiki.gogps.googlecode.com/hg/img/EclipseSetupForGoGps/14.png](http://wiki.gogps.googlecode.com/hg/img/EclipseSetupForGoGps/14.png)

Maven will download the dependencies needed by goGPS and setup the project, and after this process the “gogps” project folder in the “Package Explorer” panel should look like the following figure.

![http://wiki.gogps.googlecode.com/hg/img/EclipseSetupForGoGps/15.png](http://wiki.gogps.googlecode.com/hg/img/EclipseSetupForGoGps/15.png)

Let’s now create a run configuration to execute goGPS Java in post-processing. In the menu “Run”, select “Run Configurations…”.

![http://wiki.gogps.googlecode.com/hg/img/EclipseSetupForGoGps/16.png](http://wiki.gogps.googlecode.com/hg/img/EclipseSetupForGoGps/16.png)

Create a new configuration for a “Java Application”, name it “TestGoGPS” (or anything you like) and in the “Main” tab enter or browse to “gogps” as “Project” and “org.gogpsproject.TestGoGPS” as “Main Class”.

![http://wiki.gogps.googlecode.com/hg/img/EclipseSetupForGoGps/17.png](http://wiki.gogps.googlecode.com/hg/img/EclipseSetupForGoGps/17.png)

Apply, close the “Run Configurations” window and try to run goGPS by selecting “Run” – “Run As” – “TestGoGPS” or simply by clicking the green button with a white arrow as displayed in the figure.

![http://wiki.gogps.googlecode.com/hg/img/EclipseSetupForGoGps/18.png](http://wiki.gogps.googlecode.com/hg/img/EclipseSetupForGoGps/18.png)

If everything went well, you should see an output like in the following figure and a KML file with the result should have been created in the “test” folder.

![http://wiki.gogps.googlecode.com/hg/img/EclipseSetupForGoGps/19a.png](http://wiki.gogps.googlecode.com/hg/img/EclipseSetupForGoGps/19a.png)

In order to be able to run real-time applications that require a serial connection to a COM port (e.g. log data from a receiver connected to a USB port), you will also need to download the Rxtx library (http://rxtx.qbang.org/wiki/index.php/Download). Installation instructions can be found here: http://rxtx.qbang.org/wiki/index.php/Installation (NOTE: you only need to install the driver libraries, e.g. rxtxSerial.dll for Windows and librxtxSerial.so for Linux).
A copy of the RXTX sources and compiled binaries used in the current version of goGPS Java is stored here: https://code.google.com/p/gogps/downloads/list