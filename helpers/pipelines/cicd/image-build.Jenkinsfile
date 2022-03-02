withFolderProperties {
  RESOURCE_BUILD_URL = env.RESOURCE_BUILD_URL
}

podManifest = '''
kind: Pod
spec:
  containers:
  - name: builder
    image: marcportabellaclotet/kaniko-project:executor-v1.6.0-debug
    command: ['cat']
    tty: true
'''

pipeline {
  agent {
    kubernetes {
      yaml podManifest
    }
  }
  stages {
    stage('Image Build') {
      steps {
        script {
          container('builder') {
            checkout([$class: 'GitSCM',
                branches: [[name: params.build_branch ]],
                userRemoteConfigs: [[url: RESOURCE_BUILD_URL ]]
            ])
            imageDestination = "marcportabellaclotet/prometheus-example-app:${params.image_tag}"
            withCredentials([string(credentialsId: 'dockerauth', variable: 'dockerauth')]) {
              sh """ echo '{"auths":{"https://index.docker.io/v1/":{"auth":"${dockerauth}"}}}' |tee /kaniko/.docker/config.json """
            }
            env.pushResult = sh(script: """
              /kaniko/executor -f Dockerfile -c . --insecure \
              --skip-tls-verify --cache=true --log-format=text \
              --destination=${imageDestination}
            """, returnStatus: true)
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
