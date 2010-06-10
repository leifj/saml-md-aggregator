
This is an implementation of the MDX specification. Here is how to get started:

0. Build saml-md-aggregator:

   mvn

1. Unpack a bunch of SAML metadata, one EntityDescriptor per file in a directory. The
   filename minus the extension (must be .xml) will be the identifier of each 
   EntityDescriptor.

2. Use keytool to generate a keystore with a signing key in it - eg

   keytool -genkeypair -alias mdx -keypass secret123 -keystore mdx.jks

3. Run the standalone webapp:

   java -Dmdx.directory=/path/to/metadata \
        -Dmdx.signer.alias=mdx \
        -Dmdx.signer.pin=secret123 \
        -Dmdx.signer.validity=3600 -jar target/saml-md-aggregator-standalone.jar   

4. Reference an entity

   wget -qO- http://localhost:8080/entity/idp.example.com.xml

   Note that the EntityDescriptor has a validUntil of 1h (3600 seconds) into the future.
