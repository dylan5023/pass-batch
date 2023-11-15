# start background, recreate
db-up:
	docker-compose up -d --force-recreate

# delete volume
db-down:
	docker-compose down -v