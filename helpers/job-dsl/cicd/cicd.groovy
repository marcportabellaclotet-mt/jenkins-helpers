@Grab('org.yaml:snakeyaml:1.17')
import org.yaml.snakeyaml.Yaml
import groovy.io.FileType
import hudson.FilePath
import jenkins.model.Jenkins

instance = Jenkins.getInstance()
globalNodeProperties = instance.getGlobalNodeProperties()
envVarsNodePropertyList = globalNodeProperties.getAll(hudson.slaves.EnvironmentVariablesNodeProperty.class)
envVars = envVarsNodePropertyList.get(0).getEnvVars()

JENKINS_SERVER_NAME = InetAddress.localHost.canonicalHostName
CICD_CONFIG_PATH = envVars['cicd_config_path']
if (! CICD_CONFIG_PATH.find()){
  println "------ INFO ------"
  println "Enviroment variable [cicd_config_path] not defined."
  println "You can define this value in jenkins global properties | environment variables"
  println "Using default value (helpers/job-dsl/services/folders/files/${JENKINS_SERVER_NAME}/)."
  println "------------------"
  CICD_YAML_PATH = "helpers/job-dsl/cicd/files/${JENKINS_SERVER_NAME}/"
} else {
  CICD_YAML_PATH = "${CICD_CONFIG_PATH}/"
  println "Using Service Folders Yaml Path : ${CICD_YAML_PATH}/"
}

CICD_CONFIG_PATH = "${CICD_YAML_PATH}cicd.config"
CICD_CONFIG = new Yaml().load(readFileFromWorkspace(CICD_CONFIG_PATH))

MAIN_FOLDER      = CICD_CONFIG.config.main_folder
CICD_FOLDER_NAME = CICD_CONFIG.config.cicd_folder

hudson.FilePath workspace = hudson.model.Executor.currentExecutor().getCurrentWorkspace()
cwd = hudson.model.Executor.currentExecutor().getCurrentWorkspace().absolutize()
yamlFiles = new FilePath(cwd, CICD_YAML_PATH).list('*.yaml')
yamlFiles.each { file ->
  servicesList = new Yaml().load(readFileFromWorkspace(file.getRemote()))
  servicesList.each{serviceName, serviceConfig->
    def servicesFolderProperties = serviceConfig.folder_properties
    def resourceList = serviceConfig.resources
    def deploymentsFolderJob = MAIN_FOLDER + '/' +
                                serviceName + '/' +
                                CICD_FOLDER_NAME
    folder(deploymentsFolderJob) {
      displayName(CICD_FOLDER_NAME)
      description(CICD_FOLDER_NAME)
      properties {
        folderProperties {
          properties {
            servicesFolderProperties.each {element->
              stringProperty {
                key(element.key)
                value(element.value)
              }
            }
          }
        }
      }
    }
  }
}
