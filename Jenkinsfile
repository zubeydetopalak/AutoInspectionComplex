pipeline {
    agent any

    tools {
        maven 'Maven-3' 
    }
    environment {
        PATH = "/Applications/Docker.app/Contents/Resources/bin:/usr/local/bin:/usr/bin:/bin:/usr/sbin:/sbin:$PATH"
    }

    stages {
        stage('1. Checkout Code') {
            steps {
                echo 'Githubdan kodlar çekiliyor...'
                git branch: 'main', url: 'https://github.com/zubeydetopalak/AutoInspectionComplex.git'
            }
        }

        stage('2. Backend Unit Tests') {
            steps {
                echo 'Backend birim testleri çalıştırılıyor...'
                dir('backend') {
                    sh 'mvn test'
                }
            }
            post {
                always {
                    junit 'backend/target/surefire-reports/*.xml'
                }
            }
        }

        stage('3. Backend Integration Tests') {
            steps {
                echo 'Backend entegrasyon testleri çalıştırılıyor...'
                dir('backend') {
                    sh 'mvn verify -DskipUnitTests'
                }
            }
        }

        stage('4. Docker Build & Up') {
            steps {
                script {
                    echo 'Sistem temizleniyor (Volume verileri dahil siliniyor)...'
                    
                    sh 'docker compose down -v || true' 
                    
                    sh 'docker compose build --no-cache' 
                    
                    echo 'Sistem başlatılıyor...'
                    sh 'docker compose up -d'
                    
                    echo 'Servislerin (Spring & DB) tam açılması için 60sn bekleniyor...'
                    sleep 60 
                }
            }
        }

        stage('5. Selenium System Tests') {
            steps {
                echo 'Selenium test paketi çalıştırılıyor...'
                dir('system-tests') {
                    sh 'mvn clean test'
                }
            }
            post {
                always {
                    junit 'system-tests/target/surefire-reports/*.xml'
                }
            }
        }
    }

    post {
        always {
            script {
                echo 'Süreç bitti.'
            }
        }
    }
}
