##### About
Sblock draws inspiration for its gameplay from HomeStuck's Sburb.

##### Issues
If you have an issue relevant to Sblock, please [open a ticket](https://github.com/SblockCo/Sblock/issues/new).

##### Contributing
We accept pull requests that fit in with our vision for the project. Please attempt to match our formatting style.

##### Compiling
* Compile Spigot using [BuildTools](https://www.spigotmc.org/wiki/buildtools/)
* Install spigot.jar into your local repository

        mvn install:install-file -Dfile=path/to/spigot.jar -DgroupId=org.spigotmc \
            -DartifactId=spigot -Dversion=<version> -Dpackaging=jar
* Install [Project Lombok](https://projectlombok.org/).
* Clone the repository and run `mvn clean install`
