pipeline {
    agent any

    stages {
         stage('Display pom.xml') {
            steps {
                script {
                    def pomContent = readFile('pom.xml')
                    echo "Content of pom.xml:\n${pomContent}"
                }
            }
        }

	stage('Maven Clean') {
            steps {
     
                sh 'mvn clean install'
            }
        }
        stage('Maven Compile') {
            steps {
     
                sh 'mvn compile'
            }
        }

 
}
}




