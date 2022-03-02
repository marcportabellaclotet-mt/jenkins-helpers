withFolderProperties {
  ENVIRONMENT            = env.ENVIRONMENT
  RESOURCE_DEPLOY_URL     = env.RESOURCE_DEPLOY_URL
}

pipeline {
  agent any
  stages {
    stage('Update Check') {
      steps {
        checkout([$class: 'GitSCM',
            branches: [[name: 'main' ]],
            userRemoteConfigs: [[url: RESOURCE_DEPLOY_URL ]]
        ])
        manifestFile = params.tag_manifest_path.replace('environment', ENVIRONMENT)
        currentYaml = readYaml file: manifestFile
        yamlReplacementMap = params.replacement_map
        currentYamlContent = writeYaml returnText: true, data: currentYaml
        replacementMap = [:]
        yamlReplacementMap.each { k, v ->
            replacementMap["${k}"] = v
        }
        replacementMap.each { k, v ->
            tokens = k.tokenize('.')
            last = tokens.init().inject(currentYaml) { a, t -> a[t] }
            last[tokens.last()] = v
        }
        desiredYamlContent = writeYaml returnText: true, data: currentYaml
        println desiredYamlContent
      }
    }
  }
  post{
    always{
      script {
        println("Info")
      }
    }
  }
  options {
    disableConcurrentBuilds()
    skipDefaultCheckout()
  }
}