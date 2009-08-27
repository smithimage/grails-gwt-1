includeTargets << grailsScript("_GrailsArgParsing")
includeTargets << grailsScript("_GrailsCreateArtifacts")

gwtSrcPath = "src/gwt"

target (default: "Creates a new GWT module.") {
    depends(parseArguments)
    promptForName(type: "")

    // The only argument should be the fully qualified name of the GWT
    // module. First, split it into package and name parts.
    def moduleName = argsMap["params"][0]
    def modulePackage = null
    def pos = moduleName.lastIndexOf('.')
    if (pos != -1) {
        // Extract the name and the package.
        modulePackage = moduleName.substring(0, pos)
        moduleName = moduleName.substring(pos + 1)
    }

    def packagePath = (modulePackage != null ? '/' + modulePackage.replace('.' as char, '/' as char) : '')

    // Now create the module file.
    def targetPath = "${basedir}/${gwtSrcPath}${packagePath}"
    def moduleFile = "${targetPath}/${moduleName}.gwt.xml"
    def templatePath = "${gwtPluginDir}/src/templates/artifacts"
    def templateFile = "${templatePath}/GwtModule.gwt.xml"

    // Check whether the target module exists already.
    if (new File(moduleFile).exists()) {
        // It does, so find out whether the user wants to overwrite
        // the existing copy.
        Ant.input(
            addProperty:"${moduleName}.overwrite",
            message:"GwtModule: ${moduleName} already exists. Overwrite? [y/n]")

        if (Ant.antProject.properties."${moduleName}.overwrite" == "n") {
            // User doesn't want to overwrite, so stop the script.
            return
        }
    }

    // Copy the template module file over, replacing any tokens in the
    // process.
    Ant.copy(file: templateFile, tofile: moduleFile, overwrite: true)
    Ant.replace(file: moduleFile) {
        Ant.replacefilter(token: '@module.package@', value: (modulePackage != null ? modulePackage + '.' : ''))
        Ant.replacefilter(token: '@module.name@', value: moduleName)
    }
//    Ant.replace(file: moduleFile, token: '@module.name@', value: args)
//    Ant.sequential {
//        copy(file: templateFile, tofile: moduleFile, overwrite: true) {
//            filterset {
//                filter(token: '@module.name@', value: 'test')
//                filter(token: '@TEST@', value: 'test2')
//            }
//        }
//    }

    // Now copy the template client entry point over.
    templateFile = "${templatePath}/GwtClientEntryPoint.java"
    def entryPointFile = "${targetPath}/client/${moduleName}.java"

    Ant.copy(file: templateFile, tofile: entryPointFile, overwrite: true)
    Ant.replace(file: entryPointFile) {
        Ant.replacefilter(token: '@module.package@', value: (modulePackage != null ? modulePackage + '.' : ''))
        Ant.replacefilter(token: '@module.name@', value: moduleName)
    }

    event("CreatedFile", [ moduleFile ])
}
