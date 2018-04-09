FROM centos

#install wget
RUN yum -y install wget
EXPOSE 8080

WORKDIR /opt

#install jre 8
RUN wget --no-check-certificate --no-cookies --header "Cookie: oraclelicense=accept-securebackup-cookie" http://download.oracle.com/otn-pub/java/jdk/8u40-b26/jre-8u40-linux-x64.rpm
RUN rpm --install jre-8u40-linux-x64.rpm

#install maven
#RUN wget http://ftp.nluug.nl/internet/apache/maven/maven-3/3.3.1/binaries/apache-maven-3.3.1-bin.tar.gz
#RUN tar -zxvf apache-maven-3.3.1-bin.tar.gz

#add application
ADD target/battlecamp-server-1.0-SNAPSHOT.war /opt/battlecamp-server-1.0-SNAPSHOT.war

#run application
CMD java -jar /opt/battlecamp-server-1.0-SNAPSHOT.war
