pipeline {
    agent any
    stages {
        stage('Cleanup') {
            steps {
                echo 'Stopping existing containers...'
                sh 'docker-compose down || true'
                sh 'docker image prune -f || true'
            }
        }
        
        stage('Prepare Config Files') {
            steps {
                echo 'Ensuring config files exist as files, not directories...'
                sh '''
                    # 디렉토리로 생성된 경우 제거
                    [ -d promtail-config.yml ] && rm -rf promtail-config.yml
                    
                    # Git에서 파일 체크아웃
                    git checkout HEAD -- promtail-config.yml || echo "promtail-config.yml not in git"
                    
                    ls -la promtail-config.yml
                    cat promtail-config.yml
                '''
            }
}
        
        stage('Build and Deploy') {
            steps {
                echo 'Building and deploying with Docker Compose...'
                sh 'docker-compose up -d --build'
            }
        }
        
        stage('Health Check') {
            steps {
                echo 'Waiting for application to start...'
                sleep(time: 30, unit: 'SECONDS')
                echo 'Checking application health...'
                sh 'curl -f http://localhost:8081/actuator/health || echo "App not ready yet"'
            }
        }
        
        stage('Monitoring Check') {
            steps {
                echo 'Checking monitoring services...'
                sleep(time: 10, unit: 'SECONDS')
                echo 'Checking container status...'
                sh 'docker-compose ps'
                echo 'Checking Prometheus logs...'
                sh 'docker logs prometheus || echo "Prometheus container not running"'
                echo 'Testing Grafana...'
                sh 'curl -f http://localhost:3000/api/health || echo "Grafana not ready yet"'
            }
        }
    }
}