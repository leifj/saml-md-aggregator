#!/usr/bin/env bash

PRG="$0"
DIR=`dirname $PRG`
PIN=secret123

cat<<EOX>$DIR/../etc/mdx.properties
#mdx.signer.provider.class=sun.security.pkcs11.SunPKCS11
#mdx.signer.config=mdx.cfg
#mdx.signer.provider.type=PKCS11
mdx.signer.keystore=\${jetty.home}/credentials/mdx.jks
mdx.signer.provider.class=
mdx.signer.config=
mdx.signer.provider.type=JKS
mdx.signer.alias=mdx
mdx.signer.validity=
mdx.signer.cacheduration=
mdx.signer.pin=$PIN
mdx.neo4j.directory=\${jetty.home}/db
mdx.scanner.delay=2000
mdx.scanner.period=10000
mdx.store.url=\${jetty.home}/metadata
EOX