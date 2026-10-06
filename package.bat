@echo off
mvn clean package -Dmaven.javadoc.skip=true -Dmaven.source.skip=true -Dgpg.skip=true

