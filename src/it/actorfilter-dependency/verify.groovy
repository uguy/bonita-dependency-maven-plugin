
def assertFileExist(filePath){
    File file = new File(basedir,filePath );
    if(!file.exists() || file.length() == 0) {
        throw new FileNotFoundException("Missing '"+filePath+"'");
    }
}

// =========== Main ================

// definition file
assertFileExist("filters-def/sample-actorfilter.def");

// properties file
assertFileExist("filters-def/sample-actorfilter.properties");

// PNG file
assertFileExist("filters-def/icon.png");

// implementation file
assertFileExist("filters-impl/sample-actorfilter.impl");

// Jar dependencies
assertFileExist("lib");
File[] jars = new File(basedir,"lib").listFiles();
assertFileExist("target/sample-actorfilter-1.0.0-SNAPSHOT/classpath");
File[] expectedJars = new File(basedir,"target/sample-actorfilter-1.0.0-SNAPSHOT/classpath").listFiles();
if(jars.length != expectedJars.length){
    throw new RuntimeException("Some expected dependency jars are missing in lib folder.");
}
