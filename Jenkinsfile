pipeline {
  agent any
  stages {
    stage('Execute build script') {
      steps {
        sh 'bin/ci/jenkins.sh'
      }
    }
  }
  post {
    always {
      junit 'server/integration-tests/calabash-test-suite/test_report/*.xml'
    }
  }
  options {
    disableConcurrentBuilds()
    timestamps()
  }
}