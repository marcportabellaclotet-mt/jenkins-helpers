println('Building')
withFolderProperties {
  RESOURCE_BUILD_URL = env.RESOURCE_BUILD_URL
}
pipeline {
  agent any
  stages {
    stage('Hello') {
      steps {
        script {
          checkout([$class: 'GitSCM',
            branches: [[name: params.build_branch ]],
            userRemoteConfigs: [[url: RESOURCE_BUILD_URL ]]
          ])
          sh ''' find . '''
        }
      }
    }
  }
}
