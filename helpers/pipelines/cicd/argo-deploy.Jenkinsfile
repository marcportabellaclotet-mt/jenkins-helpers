withFolderProperties {
  ENVIRONMENT            = env.ENVIRONMENT
  RESOURCE_DEPLOY_URL    = env.RESOURCE_DEPLOY_URL
  RESOURCE_DEPLOY_BRANCH = env.RESOURCE_DEPLOY_BRANCH ?: 'main'
}
boolean manifestChanged
pipeline {
  agent any
  stages {
    stage('Update Check') {
      environment {
        GIT_AUTH = credentials('github-jenkins-user-token')
      }
      steps {
        script {
          checkout([$class: 'GitSCM',
              branches: [[name: RESOURCE_DEPLOY_BRANCH ]],
              userRemoteConfigs: [[url: RESOURCE_DEPLOY_URL ]]
          ])
          manifestFile = params.tag_manifest_path.replace('environment', ENVIRONMENT)
          currentYaml = readYaml file: manifestFile
          jsonUpdateManifest = readJSON text: params.json_updates ?: '{}'
          currentYamlContent = writeYaml returnText: true, data: currentYaml
          replacementMap = [:]
          jsonUpdateManifest.each { k, v ->
              replacementMap["${k}"] = v
          }
          replacementMap.each { k, v ->
              tokens = k.tokenize('.')
              last = tokens.init().inject(currentYaml) { a, t -> a[t] }
              last[tokens.last()] = v
          }
          desiredYamlContent = writeYaml returnText: true, data: currentYaml

          if (desiredYamlContent != currentYamlContent) {
              manifestChanged = true
              writeYaml file: manifestFile, data: currentYaml, overwrite: true
              sh """
                git config user.name 'jenkins automation'
                git config user.email 'automation@users.noreply.com'
                git add . && git commit -am "[Jenkins CI] Update Manifests"
                git config --local credential.helper "!f() { echo username=\\$GIT_AUTH_USR; echo password=\\$GIT_AUTH_PSW; }; f"
                git push origin HEAD:$RESOURCE_DEPLOY_BRANCH
              """
          }
        }
      }
    }
    stage('Update Manifest') {
      when {
          expression { manifestChanged }
      }
      steps {
          println('canvis')
      }
    }
  }
  post {
    always {
      script {
        println('Info')
      }
    }
  }
  options {
    disableConcurrentBuilds()
    skipDefaultCheckout()
  }
}
