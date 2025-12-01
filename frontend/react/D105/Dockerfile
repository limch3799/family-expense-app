FROM node:20-alpine

WORKDIR /app

# package.json과 package-lock.json 복사
COPY package*.json ./

# 의존성 설치
RUN npm install

# 소스 코드 복사
COPY . .

# 빌드 실행
RUN npm run build

# serve 글로벌 설치
RUN npm install -g serve

# 3000 포트 노출
EXPOSE 3000

# serve로 dist 폴더 서빙 (3000 포트)
CMD ["serve", "-s", "dist", "-l", "3000"]