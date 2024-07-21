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
     
                sh 'mvn clean'
            }
        }
        stage('Maven Compile') {
            steps {
     
                sh 'mvn compile'
            }
        }
	stage('Build Backend') {
            steps {
                sh "docker build -t azizsnoussi/rest-api-test-generator ."
            }
        }
	    stage('Build Frontend') {
            steps {
                dir('frontend') {
                    sh "docker build -t azizsnoussi/rtg-ng-frontend ."
                }
            }
	}
    	 stage('Docker Login') {
     	    steps {
        withCredentials([usernamePassword(credentialsId: 'dockercred', usernameVariable: 'DOCKERHUB_USERNAME', passwordVariable: 'DOCKERHUB_PASSWORD')]) {
          sh 'docker login -u azizsnoussi -p $DOCKERHUB_PASSWORD'
        }
      }
    }   
	 stage('Docker Push') {
      	    steps {
               sh "docker push azizsnoussi/rest-api-test-generator"
      }
    }

    stage('docker compose') {
            steps {
                sh "docker-compose up -d "
            }
	}

 
}
}




