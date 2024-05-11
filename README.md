一个执行shell 命令的java springboot服务

curl --location 'http://localhost:8080/login' --form 'username="admin"' --form 'password="12345678"'

curl --location 'http://localhost:8080/shell/runSyncCmd' \
--header 'Content-Type: application/json' \
--header 'Cookie: JSESSIONID=326352753BDA9F612B875FCCCBA246E5' \
--data '{"cmd": "/bin/sh", "timeout": 1, "params": ["-c", "pwd&&ls -lht&&echo '\''hello,world'\''"], "env": {}}'
