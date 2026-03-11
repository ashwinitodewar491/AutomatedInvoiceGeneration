pipeline {
        agent {
            label 'Intranet-staging'
        }


    options {
        buildDiscarder(logRotator(
            daysToKeepStr: '1',
            numToKeepStr: '2'
        ))
        timestamps()
        disableConcurrentBuilds()
    }

    parameters {
        string(name: 'PROJECT_NAMES', defaultValue: '445', description: 'Project ID for leave report')
        string(name: 'BASE_URL', defaultValue: 'https://pg-stage-intranet.joshsoftware.com', description: 'Application base URL')
        string(name: 'LOGIN_EMAIL', defaultValue: 'pooja@joshsoftware.com', description: 'Login email')
        password(name: 'LOGIN_PASSWORD', defaultValue: 'josh123', description: 'Login password')
        choice(name: 'RUN_TYPE', choices: ['STANDARD', 'RETRY'], description: 'Execution type')
    }

    stages {

        stage('Checkout Code') {
            steps {
                git branch: 'main',
                    url: 'https://github.com/ashwinitodewar491/AutomatedInvoiceGeneration.git'
            }
        }

        stage('Build & Execute Tests') {
            steps {
            sh '''
            mvn clean test \
              "-DPROJECT_NAMES=$PROJECT_NAMES" \
              "-DBASE_URL=$BASE_URL" \
              "-DLOGIN_EMAIL=$LOGIN_EMAIL" \
              "-DLOGIN_PASSWORD=$LOGIN_PASSWORD" \
              "-DRUN_TYPE=$RUN_TYPE"
            '''
            }
        }
    }

    post {
        always {
            archiveArtifacts artifacts: 'Leave_Summary_Report_*.xlsx', fingerprint: true
        }
    }
}