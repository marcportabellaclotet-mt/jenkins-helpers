println('Building')
withFolderProperties {
  RESOURCE_BUILD_URL = env.RESOURCE_BUILD_URL
}

def yamldefinition = '''
kind: Pod
spec:
  containers:
  - name: builder
    image: busybox:latest
    command: ['cat']
    tty: true
'''

pipeline {
  agent {
    kubernetes {
      yaml yamldefinition
    }
  }
  stages {
    stage('Hello') {
      steps {
        script {
          container('builder'){
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
  options {
    disableConcurrentBuilds()
    skipDefaultCheckout()
  }
}
