pipeline {
    agent any

    // Jenkins > Manage Jenkins > Tools kısmında Maven'a verdiğin isim
    tools {
        maven 'Maven-3' 
    }
    //zübeyde topalak com
    // --- EKLENEN KISIM: DOCKER YOLU ---
    // Jenkins'in senin bilgisayarındaki Docker'ı bulması için bu yolu gösteriyoruz.
    environment {
        PATH = "/Applications/Docker.app/Contents/Resources/bin:/usr/local/bin:/usr/bin:/bin:/usr/sbin:/sbin:$PATH"
    }
    // ----------------------------------

    stages {
        // --- 1. AŞAMA: KODLARI ÇEKME ---
        stage('1. Checkout Code') {
            steps {
                echo 'Githubdan kodlar çekiliyor...'
                git branch: 'main', url: 'https://github.com/zubeydetopalak/AutoInspectionComplex.git'
            }
        }

        // --- 2. AŞAMA: BACKEND BİRİM TESTLERİ (Docker Öncesi) ---
        stage('2. Backend Unit Tests') {
            steps {
                echo 'Backend birim testleri çalıştırılıyor...'
                dir('backend') {
                    // Testleri çalıştır ama paketlemeyle uğraşma
                    sh 'mvn test'
                }
            }
            post {
                always {
                    // Raporu Jenkins'e sun
                    junit 'backend/target/surefire-reports/*.xml'
                }
            }
        }

        // --- 3. AŞAMA: BACKEND ENTEGRASYON TESTLERİ ---
        stage('3. Backend Integration Tests') {
            steps {
                echo 'Backend entegrasyon testleri çalıştırılıyor...'
                dir('backend') {
                    // Unit testleri atla, sadece entegrasyonu yap
                    sh 'mvn verify -DskipUnitTests'
                }
            }
        }

        // --- 4. AŞAMA: SİSTEMİ AYAĞA KALDIRMA (Docker Compose) ---
        stage('4. Docker Build & Up') {
            steps {
                script {
                    echo 'Sistem temizleniyor (Volume verileri dahil siliniyor)...'
                    
                    // BURASI DEĞİŞTİ: yanına -v ekledik.
                    // -v: Veritabanı kayıtlarını tutan volume'ü de siler.
                    sh 'docker compose down -v || true' 
                    
                    // --no-cache: Eski image'ı kullanma, zorla yeni build al
                    sh 'docker compose build --no-cache' 
                    
                    echo 'Sistem başlatılıyor...'
                    sh 'docker compose up -d'
                    
                    echo 'Servislerin (Spring & DB) tam açılması için 60sn bekleniyor...'
                    sleep 60 
                }
            }
        }

        // --- 5. AŞAMA: SELENIUM TESTLERİ (System Tests) ---
        stage('5. Selenium System Tests') {
            steps {
                echo 'Selenium test paketi çalıştırılıyor...'
                // Senin ekran görüntüsündeki klasöre giriyoruz
                dir('system-tests') {
                    // Bu komut src/test/java altındaki (selenium paketi dahil) TÜM testleri çalıştırır
                    sh 'mvn clean test'
                }
            }
            post {
                always {
                    // Selenium raporlarını Jenkins'e sun
                    junit 'system-tests/target/surefire-reports/*.xml'
                }
            }
        }
    }

    // --- TEMİZLİK ---
    post {
        always {
            script {
                echo 'Süreç bitti, Docker kapatılıyor...'
                sh 'docker compose down -v'
            }
        }
    }
}
