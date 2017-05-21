[![Dependency Status](https://www.versioneye.com/user/projects/58b6b03c9fd69a004b1920f1/badge.svg?style=flat-square)](https://www.versioneye.com/user/projects/58b6b03c9fd69a004b1920f1)
##### About
Easterlyn is the brainchild of Edtheminecrafter. The core plugin, originally designed for the Sblock server, provides an extensive set of modules and functions enhancing gameplay.

##### Issues
If you have a relevant issue, please [open a ticket](https://github.com/Easterlyn/Easterlyn/issues/new).

##### Contributing
We accept pull requests that fit in with our vision for the project. Please attempt to match our formatting style.

##### Compiling
* Compile Spigot using [BuildTools](https://www.spigotmc.org/wiki/buildtools/)
* Install spigot.jar into your local repository

        mvn install:install-file -Dfile=path/to/spigot.jar -DgroupId=org.spigotmc \
            -DartifactId=spigot -Dversion=<version> -Dpackaging=jar
* Clone the repository and run `mvn clean install`
