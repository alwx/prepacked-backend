VERSION = 1
IMAGE_NAME = alwxdev/prepacked-backend:$(VERSION)

development-deps-up:
	docker-compose -f deployments/docker-compose.development.yml up --build

development-deps-down:
	docker-compose -f deployments/docker-compose.development.yml down

uberjar:
	clojure -T:build uberjar :project backend

build-docker-image:
	docker build --platform linux/amd64 --rm -t $(IMAGE_NAME) -f Dockerfile .
	docker push $(IMAGE_NAME)