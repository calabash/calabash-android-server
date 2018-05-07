pipeline {
  agent { label 'android-agent' }

  environment {
    SLACK_COLOR_DANGER  = '#E01563'
    SLACK_COLOR_INFO    = '#6ECADC'
    SLACK_COLOR_WARNING = '#FFC300'
    SLACK_COLOR_GOOD    = '#3EB991'

    PROJECT_NAME = 'Calabash android server'
  }

  stages {
    stage('Prepare') {
      steps {
        slackSend (color: "${env.SLACK_COLOR_INFO}",
                   message: "${env.PROJECT_NAME} [${env.GIT_BRANCH}] #${env.BUILD_NUMBER} *Started* (<${env.BUILD_URL}|Open>)")
      }
    }
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

    aborted {
      echo "Sending 'aborted' message to Slack"
      slackSend (color: "${env.SLACK_COLOR_WARNING}",
                 message: "${env.PROJECT_NAME} [${env.GIT_BRANCH}] #${env.BUILD_NUMBER} *Aborted* after ${currentBuild.durationString} (<${env.BUILD_URL}|Open>)")
    }

    failure {
      echo "Sending 'failed' message to Slack"
      slackSend (color: "${env.SLACK_COLOR_DANGER}",
                 message: "${env.PROJECT_NAME} [${env.GIT_BRANCH}] #${env.BUILD_NUMBER} *Failed* after ${currentBuild.durationString} (<${env.BUILD_URL}|Open>)")
    }

    success {
      echo "Sending 'success' message to Slack"
      slackSend (color: "${env.SLACK_COLOR_GOOD}",
                 message: "${env.PROJECT_NAME} [${env.GIT_BRANCH}] #${env.BUILD_NUMBER} *Success* after ${currentBuild.durationString} (<${env.BUILD_URL}|Open>)")
    }
  }

  options {
    disableConcurrentBuilds()
    timeout(time: 30, unit: 'MINUTES')
    timestamps()
  }
}