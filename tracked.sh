#!/bin/bash

curl -s -X 'GET' "$EXTERNAL_URL/api/v1/project" -H 'accept: application/json' \
  -H "X-Api-Key: $API_KEY" | jq -r '.[] | select(.lastBomImport) | [.uuid, .lastBomImport|tostring] | join(" ")' | while read -ra line
do
  curl -s -X 'GET' "$EXTERNAL_URL/api/v1/finding/project/${line[0]}?suppressed=false" -H 'accept: application/json' \
  -H "X-Api-Key: $API_KEY" | jq "[.[] | select(.attribution.attributedOn-1000 > ${line[1]}) | {at: .attribution.attributedOn, vulnId: .vulnerability.vulnId, severity: .vulnerability.severity, purl: .component.purl}]"
done | jq -s 'reduce .[] as $x ([]; . + $x)'
