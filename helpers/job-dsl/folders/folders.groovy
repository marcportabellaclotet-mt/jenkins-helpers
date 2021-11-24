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
FOLDERS_CONFIG_PATH = envVars['folders_config_path']
if (! FOLDERS_CONFIG_PATH.find()){
  println "------ INFO ------"
  println "Enviroment variable [folders_config_path] not defined."
  println "You can define this value in jenkins global properties | environment variables"
  println "Using default value (helpers/job-dsl/services/folders/files/${JENKINS_SERVER_NAME}/)."
  println "------------------"
  FOLDERS_YAML_PATH = "helpers/job-dsl/folders/files/${JENKINS_SERVER_NAME}/"
} else {
  FOLDERS_YAML_PATH = "${FOLDERS_CONFIG_PATH}/"
  println "Using Service Folders Yaml Path : ${FOLDERS_YAML_PATH}/"
}

FOLDERS_CONFIG_PATH = "${FOLDERS_YAML_PATH}folders.config"
FOLDER_CONFIG = new Yaml().load(readFileFromWorkspace(FOLDERS_CONFIG_PATH))

MAIN_FOLDER      = FOLDER_CONFIG.config.main_folder
MAIN_FOLDER_DESC = FOLDER_CONFIG.config.main_folder_description
MAIN_FOLDER_NAME = FOLDER_CONFIG.config.main_folder_display_name

hudson.FilePath workspace = hudson.model.Executor.currentExecutor().getCurrentWorkspace()
cwd = hudson.model.Executor.currentExecutor().getCurrentWorkspace().absolutize()
yamlFiles = new FilePath(cwd, FOLDERS_YAML_PATH).list('*.yaml')

yamlFiles.each { file ->
  servicesList = new Yaml().load(readFileFromWorkspace(file.getRemote()))
  folder(MAIN_FOLDER) {
    displayName(MAIN_FOLDER_DESC)
    description(MAIN_FOLDER_NAME)
    authorization {
      if (FOLDER_CONFIG.config.allow_all_auth_users) {
        permission('hudson.model.Item.Read', 'authenticated')
      }
      FOLDER_CONFIG.config.allowed_users.each{allowed_user->
        permission('hudson.model.Item.Read', allowed_user)
      }
      FOLDER_CONFIG.config.allowed_groups.each{allowed_group->
        permission('hudson.model.Item.Read', allowed_group)
      }
    }
  }

  servicesList.each{serviceName, serviceConfig->
    folder(MAIN_FOLDER + '/' + serviceName ) {
      displayName(serviceName)
      description(serviceName)
      if (serviceConfig.find() ) {
        properties {
          folderProperties {
            properties {
              serviceConfig.folder_properties.each {element->
                stringProperty {
                  key(element.key)
                  if (element.value != null){
                    value(element.value)
                  }else{
                    value('')
                  }
                }
              }
            }
          }
          authorizationMatrix {
            inheritanceStrategy {
                nonInheriting()
            }
          }
        }
        authorization {
          if ( serviceConfig.owner.find() ) {
                permission("hudson.model.Item.Build:${serviceConfig.owner}")
                permission("hudson.model.Item.Read:${serviceConfig.owner}")
                permission("hudson.model.Item.Cancel:${serviceConfig.owner}")
                permission("hudson.model.View.Read:${serviceConfig.owner}")
                permission("hudson.model.Item.Configure:${serviceConfig.owner}")
              }
          if ( serviceConfig.allowed_users.find() ) {
            serviceConfig.allowed_users.each{allowed_users->
              permission("hudson.model.Item.Build:${allowed_users}")
              permission("hudson.model.Item.Read:${allowed_users}")
              permission("hudson.model.Item.Cancel:${allowed_users}")
              permission("hudson.model.View.Read:${allowed_users}")
            }
          }
          if ( serviceConfig.allowed_ro_users.find() ) {
            serviceConfig.allowed_ro_users.each{allowed_gh_ro_users->
              permission("hudson.model.Item.Read:${allowed_gh_ro_users}")
              permission("hudson.model.View.Read:${allowed_gh_ro_users}")
            }
          }
          if ( serviceConfig.group_owner.find() ) {
            permission("hudson.model.Item.Build:${serviceConfig.group_owner}")
            permission("hudson.model.Item.Read:${serviceConfig.group_owner}")
            permission("hudson.model.Item.Cancel:${serviceConfig.group_owner}")
            permission("hudson.model.View.Read:${serviceConfig.group_owner}")
            permission("hudson.model.Item.Configure:${serviceConfig.group_owner}")
          }
          if ( serviceConfig.allowed_groups.find() ) {
            serviceConfig.allowed_groups.each{allowed_group->
              permission("hudson.model.Item.Build:${allowed_group}")
              permission("hudson.model.Item.Read:${allowed_group}")
              permission("hudson.model.Item.Cancel:${allowed_group}")
              permission("hudson.model.View.Read:${allowed_group}")
            }
          }
        }
      }
    }
  }
}
