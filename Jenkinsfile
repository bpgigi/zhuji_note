pipeline {
    agent any

    environment {
        ANDROID_HOME      = "${env.ANDROID_HOME ?: 'C:\\Users\\l\\AppData\\Local\\Android\\Sdk'}"
        JAVA_HOME         = "${env.JAVA_HOME ?: 'D:\\Softwares\\AndroidStudio\\jbr'}"
        GRADLE_USER_HOME  = "D:\\Android\\.gradle"
    }

    options {
        timeout(time: 45, unit: 'MINUTES')
        timestamps()
        buildDiscarder(logRotator(numToKeepStr: '10'))
    }

    stages {
        stage('Checkout') { steps { checkout scm } }

        stage('Setup') {
            steps {
                bat 'echo sdk.dir=%ANDROID_HOME:\\=/% > local.properties'
                bat '"%JAVA_HOME%\\bin\\java" -version'
            }
        }

        stage('Build APK') {
            steps { bat '.\\gradlew.bat :app:assembleDebug --no-daemon' }
        }

        stage('Three-Stage Tests + JaCoCo') {
            steps {
                bat '.\\gradlew.bat :app:jacocoStage1Report :app:jacocoStage2Report :app:jacocoStage3Report :app:jacocoCumulativeReport --no-daemon'
            }
            post {
                always {
                    junit allowEmptyResults: true, testResults: 'app/build/test-results/**/*.xml'
                    archiveArtifacts artifacts: 'app/build/reports/coverage/**', fingerprint: true, allowEmptyArchive: true
                    publishHTML(target: [
                        reportDir: 'app/build/reports/coverage/cumulative/html',
                        reportFiles: 'index.html',
                        reportName: 'JaCoCo Cumulative',
                        keepAll: true,
                        alwaysLinkToLastBuild: true,
                        allowMissing: true,
                    ])
                }
            }
        }

        stage('Archive APK') {
            steps {
                archiveArtifacts artifacts: 'app/build/outputs/apk/debug/*.apk', fingerprint: true
            }
        }
    }

    post {
        success { echo 'ZhujiNote build OK!' }
        failure { echo 'ZhujiNote build failed.' }
    }
}
