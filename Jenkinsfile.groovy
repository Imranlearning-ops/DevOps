pipeline{
    agent any
    tools {
        maven "Maven3.9"
        jdk "JDK17"
    }
    stages{
        stage('Fetch code'){
         steps{

          git branch:'atom', url:'https://github.com/hkhcoder/vprofile-project.git'
        }
    }
        stage('unit test'){
            steps{
                sh 'mvn test'
            }
        }
        stage('Build'){
            steps{
                sh 'mvn install -DskipTests'
            }
            post{
                success{
                    echo "Archiving Artifacts"
                    archiveArtifacts artifacts:'**/*.war'
                }
            }
        }
        stage('Checkstyle Analysis') {
            steps{
                sh 'mvn checkstyle:checkstyle'
            }
        }
        stage('Sonar Code Analysis') {
            environment{
                scannerHome=tool 'sonar6.2'
            }
            steps{
                withSonarQubeEnv('sonarserver') {
                    sh ''' 
                    ${scannerHome}/bin/sonar-scanner \
                        -Dsonar.projectKey=vprofile \
                        -Dsonar.projectName=vprofile \
                        -Dsonar.projectVersion=1.0 \
                        -Dsonar.sources=src \
                        -Dsonar.java.binaries=target/test-classes/com/visualpathit/account/controllerTest/ \
                        -Dsonar.junit.reportsPath=target/surefire-reports/ \
                        -Dsonar.jacoco.reportsPath=target/jacoco.exec \
                        -Dsonar.java.checkstyle.reportPaths=target/checkstyle-result.xml'''

                }
            }
        }
        stage('Upload Artifacts'){
            steps{
                nexusArtifactUploader (
                    nexusVersion: 'nexus3',
                    protocol:'http',
                    nexusUrl:'172.31.19.228:8081',
                    groupId:'QA',
                    version:"${env.BUILD_ID}-${env.BUILD_TIMESTAMP}",
                    repository: 'DevOps_Training',
                    credentialsId: 'nexususer',
                    artifacts: [
                        [
                            artifactId:'vproapp',
                            classifier:'',
                            file:'target/vprofile-v2.war',
                            type:'war'
                        ]
                    ]
                )
            

            }

        }
    }
}