
def traverse( File file ) {
    if(file.exists()){
        if(file.isDirectory()){
            String[] children = file.list()
            for( int i = 0; children != null && i < children.length; i++){
                traverse( new File( file, children[i]) )
            }
        }
        if(file.isFile()){
            if(file.getName().endsWith(".groovy") ){
                if ( containsFX( file ) ){
                    violatingFiles << file
                }
            }
        }
    }else{
        println "File $project.properties.directory cannot be found";
    }
}

def containsFX( File file ){
    String[] fileContents = file.text.split("\n")
    for( String line : fileContents ){
        if ( line.startsWith("import") ){
            if(line.contains("javafx")){
                return true;
            }
        }
    }
    return false;
}

def execute(  ) {

    def scriptDir = getClass().protectionDomain.codeSource.location.path as String
    String loadui = "loadui";
    if( scriptDir.contains( loadui ) ){

        String[] tmp = scriptDir.split( loadui )
        String directory = tmp[0] + loadui
        if( directory.startsWith("/")){
            directory = directory.substring(1)
        }

        traverse ( new File( directory) )
        if( violatingFiles ){
            for( File f : violatingFiles ){
                println f.absolutePath
            }
            println "The above file(s) contains JavaFX imports. This is illegal and breaks agent modularity. This is bad and you should feel bad!"
            fail("breaking the build because there are groovy files that contains JavaFX imports.")
        }
    }
}

violatingFiles = []
execute( )


