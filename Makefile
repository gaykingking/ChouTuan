.PHONY: help dev-up dev-down backend-test android-test android-apk docker-build k3s-deploy

SERVICES := gateway-service order-service product-service notification-service
IMAGE_PREFIX := choutuan
IMAGE_TAG := latest

help:
	@echo "Available targets:"
	@echo "  dev-up        Start backend infrastructure via Docker Compose"
	@echo "  dev-down      Stop backend infrastructure via Docker Compose"
	@echo "  backend-test  Run Maven clean test package for all backend services"
	@echo "  android-test  Run Android debug unit tests"
	@echo "  android-apk   Build Android debug APK"
	@echo "  docker-build  Build Docker images for all 4 backend microservices"
	@echo "  k3s-deploy    Deploy ChouTuan workloads to K3s cluster"

dev-up:
	docker compose -f backend-service/docker-compose.yml up -d

dev-down:
	docker compose -f backend-service/docker-compose.yml down

backend-test:
	mvn -B -f backend-service/pom.xml clean test package

android-test:
	./gradlew testDebugUnitTest

android-apk:
	./gradlew assembleDebug

docker-build:
	@for svc in $(SERVICES); do \
		echo "Building $(IMAGE_PREFIX)/$$svc:$(IMAGE_TAG)..."; \
		docker build -f backend-service/Dockerfile --build-arg SERVICE_NAME=$$svc -t $(IMAGE_PREFIX)/$$svc:$(IMAGE_TAG) backend-service; \
	done

k3s-deploy:
	kubectl apply -f k8s/choutuan-k3s-deployment.yaml
