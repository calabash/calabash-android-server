pipeline {
  agent any
  stages {
    stage('Build') {
      steps {
        sh 'bin/build.sh'
      }
    }
    stage('Test') {
      parallel {
        stage('Run emulator') {
          steps {
            sh 'bin/start_emulator.sh'
          }
        }
        stage('Compile and execute tests') {
          steps {
            sh '''
cd server/integration-tests
./run_and_compile.sh
'''
          }
        }
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