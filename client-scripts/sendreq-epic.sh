
curl --location 'http://localhost:8081/api/receive-notification/' \
--header 'Content-Type: application/json' \
--header 'X-Request-ID:'"${1}"'' \
--data "@./subevent-epic.json" 
#--trace-ascii trace-request.log

