pipeline {
  agent any
  stages {
    stage('Execute build script') {
      steps {
        sh '''#!/usr/bin/env bash

bin/ci/jenkins.sh'''
      }
    }
  }
}