FROM elastest/test-etm-alpinegitjava

COPY logstashsender.jar /logstashsender.jar

CMD cd /; exec java -jar logstashsender.jar
