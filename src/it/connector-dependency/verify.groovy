
def assertFileExist(filePath){
    File file = new File(basedir,filePath );
    if(!file.exists() || file.length() == 0) {
        throw new FileNotFoundException("Missing '"+filePath+"'");
    }
}

// =========== Main ================

// definition file
assertFileExist("connectors-def/connector-starwars.def");

// properties file
assertFileExist("connectors-def/connector-starwars.properties");

// PNG file
assertFileExist("connectors-def/connector.png");

// implementation file
assertFileExist("connectors-impl/connector-starwars.impl");

// Jar dependencies
assertFileExist("lib");
File[] jars = new File(basedir,"lib").listFiles();
assertFileExist("target/connector-starwars-1.0.0-SNAPSHOT/classpath");
File[] expectedJars = new File(basedir,"target/connector-starwars-1.0.0-SNAPSHOT/classpath").listFiles();
if(jars.length != expectedJars.length){
    throw new RuntimeException("Some expected dependency jars are missing in lib folder.");
}
