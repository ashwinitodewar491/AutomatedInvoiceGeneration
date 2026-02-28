pipeline {
    agent any
    options {
        buildDiscarder(logRotator(
            daysToKeepStr: '1',
            numToKeepStr: '2'
        ))
        timestamps()
        disableConcurrentBuilds()
    }
    parameters {
        string(name: 'PROJECT_NAMES', defaultValue: 'Banyan-ops', description: 'Project Name for leave report')
        string(name: 'BASE_URL', defaultValue: 'https://pg-stage-intranet.joshsoftware.com', description: '')
        string(name: 'LOGIN_PASSWORD', defaultValue: 'josh123', description: '')
        string(name: 'LOGIN_EMAIL', defaultValue: 'pooja@joshsoftware.com', description: '')
    }
    stages {
        stage('Checkout Code') {
            steps {
                git branch: 'main',
                    url: 'https://github.com/ashwinitodewar491/AutomatedInvoiceGeneration.git'
            }
        }
        stage('Build & Run Tests') {
            steps {
                sh """
                    mvn clean test \
                    -DPROJECT_NAMES=${params.PROJECT_NAMES} \
                    -DBASE_URL=${params.BASE_URL} \
                    -DLOGIN_EMAIL=${params.LOGIN_EMAIL} \
                    -DLOGIN_PASSWORD=${params.LOGIN_PASSWORD}
                """
            }
        }
    }
    post {
        success {
            echo "Build completed successfully"
        }
        failure {
            echo "Build failed"
        }
        always {
            archiveArtifacts artifacts: 'Leave_Summary_Report_*.xlsx', fingerprint: true
        }
    }
}