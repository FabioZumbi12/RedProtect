# Package rapido (gera os JARs sem instalar no ~/.m2, pulando javadoc, sources e assinatura gpg)
mvn clean package "-Dmaven.javadoc.skip=true" "-Dmaven.source.skip=true" "-Dgpg.skip=true"

