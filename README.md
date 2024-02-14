# Dependency Track

* Build
```sh
(cd oidc; mvn package)
cp oidc/target/dependency-track-oidc.jar oidc.jar
(cd provisioning; mvn package)
cp provisioning/target/dependency-track-provisioning.jar provisioning.jar
```
* Run dev:
```sh
cp .env.sample .env
vim .env
docker compose -f docker-compose.dev.yml up -d
sleep 5m
eval "$(echo $(cat .env))" ./setup.sh
```
* Copy the generated api key to `PROVISIONING_TOKEN`
* `docker compose -f docker-compose.dev.yml restart`
