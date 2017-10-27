FROM elastest/test-etm-alpinegitjava

CMD git clone https://github.com/EduJGURJC/logstashsender; cd logstashsender; mvn clean package -DskipTests; cd target; exec java -jar $(ls | grep ".*\.jar$");
