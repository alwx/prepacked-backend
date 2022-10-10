development-deps-up:
	docker-compose -f deployments/docker-compose.development.yml up --build

development-deps-down:
	docker-compose -f deployments/docker-compose.development.yml down