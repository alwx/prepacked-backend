VERSION = 11
IMAGE_NAME = alwxdev/prepacked-backend

development-deps-up:
	docker-compose -f deployments/docker-compose.development.yml up --build

development-deps-down:
	docker-compose -f deployments/docker-compose.development.yml down

uberjar:
	clojure -T:build uberjar :project backend

build-docker-image: uberjar
	docker build --platform linux/amd64 --rm -t $(IMAGE_NAME):$(VERSION) -t $(IMAGE_NAME):latest -f Dockerfile .
	docker push $(IMAGE_NAME):$(VERSION)
	docker push $(IMAGE_NAME):latest
