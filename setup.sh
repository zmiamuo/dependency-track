#!/bin/bash
# Definir EXTERNAL_URL ADMIN_PASSWORD

set -e
set -u
set -o pipefail
# assert for variables
_="$EXTERNAL_URL $ADMIN_PASSWORD"
set -x

curl "${EXTERNAL_URL}/api/v1/user/forceChangePassword" -X POST --data "username=admin&password=admin&newPassword=${ADMIN_PASSWORD}"
ADMIN_JWT=$(curl "${EXTERNAL_URL}/api/v1/user/login" -X POST --data "username=admin&password=${ADMIN_PASSWORD}")
curl "${EXTERNAL_URL}/api/v1/configProperty/aggregate" -X POST -H "Authorization: Bearer ${ADMIN_JWT}" -H 'Content-Type: application/json' --data "[
  {
    \"groupName\": \"access-management\",
    \"propertyName\": \"acl.enabled\",
    \"propertyValue\": \"true\"
  },
  {
    \"groupName\": \"general\",
    \"propertyName\": \"base.url\",
    \"propertyValue\": \"${EXTERNAL_URL}\"
  },
  {
    \"groupName\": \"vuln-source\",
    \"propertyName\": \"nvd.api.enabled\",
    \"propertyValue\": \"true\"
  },{
    \"groupName\": \"vuln-source\",
    \"propertyName\": \"nvd.api.key\",
    \"propertyValue\": \"${NVD_API_KEY}\"
  }
]"
# repositories
MAVEN_REPOS=$(curl "${EXTERNAL_URL}/api/v1/repository/MAVEN" -H "Authorization: Bearer ${ADMIN_JWT}")
curl "${EXTERNAL_URL}/api/v1/repository" -X POST -H "Authorization: Bearer ${ADMIN_JWT}" -H 'Content-Type: application/json' --data "{
    \"type\": \"MAVEN\",
    \"identifier\": \"central\",
    \"url\": \"https://popfr1.repos.tech.orange/artifactory/maven-proxy-asis-apache-releases/\",
    \"enabled\": true,
    \"internal\": false,
    \"uuid\": \"$(echo $MAVEN_REPOS | jq -r '.[] | select(.identifier == "central") | .uuid')\"
}"
curl "${EXTERNAL_URL}/api/v1/repository" -X POST -H "Authorization: Bearer ${ADMIN_JWT}" -H 'Content-Type: application/json' --data "{
    \"type\": \"MAVEN\",
    \"identifier\": \"atlassian-public\",
    \"url\": \"https://popfr1.repos.tech.orange/artifactory/maven-proxy-asis-atlassian-releases/\",
    \"enabled\": true,
    \"internal\": false,
    \"uuid\": \"$(echo $MAVEN_REPOS | jq -r '.[] | select(.identifier == "atlassian-public") | .uuid')\"
}"
curl "${EXTERNAL_URL}/api/v1/repository" -X POST -H "Authorization: Bearer ${ADMIN_JWT}" -H 'Content-Type: application/json' --data "{
    \"type\": \"MAVEN\",
    \"identifier\": \"jboss-releases\",
    \"url\": \"https://popfr1.repos.tech.orange/artifactory/maven-proxy-asis-jboss-releases/\",
    \"enabled\": true,
    \"internal\": false,
    \"uuid\": \"$(echo $MAVEN_REPOS | jq -r '.[] | select(.identifier == "jboss-releases") | .uuid')\"
}"
curl "${EXTERNAL_URL}/api/v1/repository" -X POST -H "Authorization: Bearer ${ADMIN_JWT}" -H 'Content-Type: application/json' --data "{
    \"type\": \"MAVEN\",
    \"identifier\": \"clojars\",
    \"url\": \"https://repo.clojars.org/\",
    \"enabled\": false,
    \"internal\": false,
    \"uuid\": \"$(echo $MAVEN_REPOS | jq -r '.[] | select(.identifier == "clojars") | .uuid')\"
}"
curl "${EXTERNAL_URL}/api/v1/repository" -X POST -H "Authorization: Bearer ${ADMIN_JWT}" -H 'Content-Type: application/json' --data "{
    \"type\": \"MAVEN\",
    \"identifier\": \"google-android\",
    \"url\": \"https://maven.google.com/\",
    \"enabled\": false,
    \"internal\": false,
    \"uuid\": \"$(echo $MAVEN_REPOS | jq -r '.[] | select(.identifier == "google-android") | .uuid')\"
}"
COMPOSER_REPOS=$(curl "${EXTERNAL_URL}/api/v1/repository/COMPOSER" -H "Authorization: Bearer ${ADMIN_JWT}")
curl "${EXTERNAL_URL}/api/v1/repository" -X POST -H "Authorization: Bearer ${ADMIN_JWT}" -H 'Content-Type: application/json' --data "{
    \"type\": \"COMPOSER\",
    \"identifier\": \"packagist\",
    \"url\": \"https://repos.tech.orange/artifactory/api/composer/php-proxy-official/\",
    \"enabled\": false,
    \"internal\": false,
    \"uuid\": \"$(echo $COMPOSER_REPOS | jq -r '.[] | select(.identifier == "packagist") | .uuid')\"
}"
NPM_REPOS=$(curl "${EXTERNAL_URL}/api/v1/repository/NPM" -H "Authorization: Bearer ${ADMIN_JWT}")
curl "${EXTERNAL_URL}/api/v1/repository" -X POST -H "Authorization: Bearer ${ADMIN_JWT}" -H 'Content-Type: application/json' --data "{
    \"type\": \"NPM\",
    \"identifier\": \"npm-public-registry\",
    \"url\": \"https://registry.npmjs.org/\",
    \"enabled\": false,
    \"internal\": false,
    \"uuid\": \"$(echo $NPM_REPOS | jq -r '.[] | select(.identifier == "npm-public-registry") | .uuid')\"
}"

# get admin api key
ADMIN_UUID=$(curl "${EXTERNAL_URL}/api/v1/team" -H "Authorization: Bearer ${ADMIN_JWT}" | jq -r '.[] | select(.name == "Administrators") | .uuid')
curl "${EXTERNAL_URL}/api/v1/team/${ADMIN_UUID}/key" -X PUT -H "Authorization: Bearer ${ADMIN_JWT}"
