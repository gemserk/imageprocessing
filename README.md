Description
------------

Image Processing Library, for now just Color Bleeding to remove artifacts when using sprites.

Introduction
------------

To generate an executable jar you need to download the project and execute:

	mvn package 

That will create an executable jar named imageprocessing-0.0.2-SNAPSHOT-jar-with-dependencies.jar in the folder target that could be used later to process images.

Usage
------------

To run the image processing tool from the jar, just run:

	java -jar imageprocessing-0.0.2-SNAPSHOT-jar-with-dependencies.jar

And it will show the Usage with the following options:

* -debug: Writes debug images without alpha. Default: false
* -dir: Directory containing the images. Required
* -maxiterations: Max iterations to run the algorithm. Default: 2147483647
* -numthreads: Number of threads to use. Default: 8
* -outdir: Directory to use for the output (copy if not overriding and debug image if debug enabled)
* -overwrite: Overwrite Source Images. Default: false

Usage from code
------------

In case you want to use the processor directly from code, you can run:

	mvn install

And then include the dependency in your maven project as:

	<dependency>
		<groupId>com.gemserk.utils.imageprocessing</groupId>
		<artifactId>imageprocessing</artifactId>
		<version>0.0.2-SNAPSHOT</version>
	</dependency>

Or if your project is not maven, just copy the imageprocessing-0.0.2-SNAPSHOT.jar file from /target in your project.
 
After including the project, just add the following code:

	Options options = new Options();
	options.dir = "/my/directory";
	ColorBleedingCLI.execute(options);

Contributing
------------

Feel free to add issues, make forks or contact us directly if you have suggestions, bug reports or enhancements.

