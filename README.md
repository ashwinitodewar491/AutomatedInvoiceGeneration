cURL to trigger build remotely via jenkins

curl --location --request POST 'http://<JenkinsHost>/job/InvoiceAutomation/buildWithParameters' \
--header 'Authorization: Basic QXNod2luaUpvc2g6MTFmMDUxM2ZjY2JlZmE3ZWRhNzMwNTJlMmEyMGI4NzM5OQ==' \
--header 'Content-Type: application/x-www-form-urlencoded' \
--header 'Cookie: JSESSIONID.82657ebe=node0jaf8xs74dk7amu5ll9os5gho8.node0' \
--data-urlencode 'PROJECT_ID=445' \
--data-urlencode 'RUN_TYPE=STANDARD' \
--data-urlencode 'BASE_URL=https://pg-stage-intranet.joshsoftware.com' \
--data-urlencode 'LOGIN_EMAIL=pooja@joshsoftware.com' \
--data-urlencode 'LOGIN_PASSWORD=josh123'

Basic Authentication have been used 
Pass jenkins user name and API token from jenkins in authentication
For Email - configure directly to jenkins