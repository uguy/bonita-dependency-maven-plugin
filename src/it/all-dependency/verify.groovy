
def assertFileExist(filePath){
    File file = new File(basedir,filePath );
    if(!file.exists() || file.length() == 0) {
        throw new FileNotFoundException("Missing '"+filePath+"'");
    }
}

// =========== Main ================

// definition file
assertFileExist("connectors-def/connector-starwars.def");
assertFileExist("filters-def/sample-actorfilter.def");


