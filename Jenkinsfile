pipeline {
  agent any

  options {
    buildDiscarder(logRotator(numToKeepStr: '20'))
    disableConcurrentBuilds()
    timestamps()
  }

  parameters {
    booleanParam(
      name: 'DEPLOY_BACKEND',
      defaultValue: false,
      description: 'Deploy backend to the configured server after a successful main-branch build. Enable after Jenkins SSH credentials are ready.'
    )
  }

  environment {
    BACKEND_DIR = 'backend/onboarding-api'
    DEPLOY_HOST = '110.42.239.130'
    DEPLOY_APP_DIR = '/opt/onboarded/onboarding-api'
    DEPLOY_SSH_CREDENTIAL_ID = 'onboarded-prod-ssh'
    SERVICE_NAME = 'onboarding-api'
    HEALTH_URL = 'http://127.0.0.1:8080/api/health'
  }

  stages {
    stage('Environment') {
      steps {
        sh '''
          set -euo pipefail
          java -version
          git --version
          cd "$BACKEND_DIR"
          ./mvnw -version
        '''
      }
    }

    stage('Test') {
      steps {
        dir("${env.BACKEND_DIR}") {
          sh './mvnw -B test'
        }
      }
      post {
        always {
          junit allowEmptyResults: true, testResults: "${env.BACKEND_DIR}/target/surefire-reports/*.xml"
        }
      }
    }

    stage('Package Backend') {
      steps {
        dir("${env.BACKEND_DIR}") {
          sh './mvnw -B -DskipTests package'
        }
        sh '''
          set -euo pipefail
          rm -rf .jenkins-deploy
          mkdir -p .jenkins-deploy
          jar_path=$(find "$BACKEND_DIR/target" -maxdepth 1 -type f -name 'onboarding-api-*.jar' ! -name '*sources.jar' ! -name '*javadoc.jar' | head -n 1)
          test -n "$jar_path"
          cp "$jar_path" .jenkins-deploy/onboarding-api.jar
          cp deploy/jenkins/onboarding-api.service .jenkins-deploy/onboarding-api.service
          cp deploy/jenkins/onboarding-api.env.example .jenkins-deploy/onboarding-api.env.example
          cp scripts/deploy/remote-deploy-backend.sh .jenkins-deploy/remote-deploy-backend.sh
        '''
        archiveArtifacts artifacts: '.jenkins-deploy/**, backend/onboarding-api/target/*.jar', fingerprint: true
      }
    }

    stage('Deploy Backend') {
      when {
        expression {
          def onMain = env.BRANCH_NAME == null || env.BRANCH_NAME == 'main' || env.GIT_BRANCH == 'main' || env.GIT_BRANCH == 'origin/main'
          return params.DEPLOY_BACKEND && onMain
        }
      }
      steps {
        withCredentials([sshUserPrivateKey(
          credentialsId: "${env.DEPLOY_SSH_CREDENTIAL_ID}",
          keyFileVariable: 'DEPLOY_SSH_KEY',
          usernameVariable: 'DEPLOY_SSH_USER'
        )]) {
          sh '''
            set -euo pipefail
            ssh_opts="-i $DEPLOY_SSH_KEY -o StrictHostKeyChecking=accept-new"
            remote="$DEPLOY_SSH_USER@$DEPLOY_HOST"

            ssh $ssh_opts "$remote" "sudo mkdir -p '$DEPLOY_APP_DIR/incoming' && sudo chown -R '$DEPLOY_SSH_USER' '$DEPLOY_APP_DIR/incoming'"
            scp $ssh_opts .jenkins-deploy/onboarding-api.jar "$remote:$DEPLOY_APP_DIR/incoming/onboarding-api.jar"
            scp $ssh_opts .jenkins-deploy/onboarding-api.service "$remote:$DEPLOY_APP_DIR/incoming/onboarding-api.service"
            scp $ssh_opts .jenkins-deploy/onboarding-api.env.example "$remote:$DEPLOY_APP_DIR/incoming/onboarding-api.env.example"
            scp $ssh_opts .jenkins-deploy/remote-deploy-backend.sh "$remote:$DEPLOY_APP_DIR/incoming/remote-deploy-backend.sh"

            ssh $ssh_opts "$remote" "sudo APP_DIR='$DEPLOY_APP_DIR' SERVICE_NAME='$SERVICE_NAME' BUILD_NUMBER='$BUILD_NUMBER' HEALTH_URL='$HEALTH_URL' bash '$DEPLOY_APP_DIR/incoming/remote-deploy-backend.sh'"
          '''
        }
      }
    }
  }

  post {
    success {
      echo "CI/CD finished for ${env.JOB_NAME} #${env.BUILD_NUMBER}"
    }
    failure {
      echo "CI/CD failed for ${env.JOB_NAME} #${env.BUILD_NUMBER}"
    }
  }
}
