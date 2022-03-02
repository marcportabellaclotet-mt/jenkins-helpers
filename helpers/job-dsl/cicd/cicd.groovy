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
    def resourceList = serviceConfig.resources
    resourceList.each{resourceName,resourceConfig->
      def resourcesFolderProperties = resourceConfig.folder_properties
      def resourceNameFolderJob = MAIN_FOLDER + '/' +
                                  serviceName + '/' +
                                  CICD_FOLDER_NAME + '/' +
                                  resourceName

      def createBuildJob = resourceConfig.create_build_job
      def buildServiceRepoURL = resourceConfig.build_service_repo
      def buildJobJenkinsfilePath = resourceConfig.jenkinsfile_build_path
      def buildJobJenkinsfileBranch = resourceConfig.jenkinsfile_build_branch

      def deployServiceRepoURL = resourceConfig.deploy_service_repo
      def deployJobJenkinsfilePath = resourceConfig.jenkinsfile_deploy_path
      def deployJobJenkinsfileBranch = resourceConfig.jenkinsfile_deploy_branch
      def createDeployJob = resourceConfig.create_deploy_job

      folder(resourceNameFolderJob) {
        displayName(resourceName)
        description(resourceName)
        properties {
          folderProperties {
            properties {
              resourcesFolderProperties.each {element->
                stringProperty {
                  key(element.key)
                  value(element.value)
                }
              }
            }
          }
        }
      }
      if (createBuildJob){
        def buildJobName = MAIN_FOLDER + '/' +
                           serviceName + '/' +
                           CICD_FOLDER_NAME + '/' +
                           resourceName + '/Build'
        pipelineJob(buildJobName) {
          displayName('Build')
          properties {
            disableConcurrentBuilds()
          }
          logRotator {
            numToKeep(10)
          }
          if ( buildJobParameters.find()) {
            def buildJobParameters = resourceConfig.build_job_parameters.params
            parameters {
              resourceBuildEnv = []
              buildJobParameters.each{param ->
                if (param.type == "choice") {
                  choiceParam(param.name , param.list, param.description )
                  if ( param.name == "build_environment"){
                    resourceBuildEnv = param.list
                  }
                }else if (param.type == "string") {
                  stringParam(param.name , param.default_value , param.description )
                }else if (param.type == "boolean") {
                  booleanParam(param.name , param.default_value , param.description )
                }
              }
            }
          }
          definition {
            cpsScm {
              scm {
                git {
                  remote {
                    url(buildServiceRepoURL)
                      credentials('githubJenkinsUSerToken')
                    }
                  branches(buildJobJenkinsfileBranch)
                }
              }
              scriptPath(buildJobJenkinsfilePath)
            }
          }
        }
      }
    }
  }
}
