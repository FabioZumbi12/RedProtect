# Build e install rapido (pula javadoc, sources e assinatura gpg)
mvn clean install "-Dmaven.javadoc.skip=true" "-Dmaven.source.skip=true" "-Dgpg.skip=true"

