import com.cloudbees.flowpdf.*
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.security.Keys
import org.bouncycastle.jce.provider.BouncyCastleProvider
import io.github.jopenlibs.vault.Vault;
import io.github.jopenlibs.vault.VaultConfig;
import io.github.jopenlibs.vault.VaultException;
import io.github.jopenlibs.vault.response.AuthResponse;
import io.github.jopenlibs.vault.response.LogicalResponse;
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import groovy.yaml.YamlSlurper
import groovy.yaml.YamlBuilder

import java.security.Key
import java.security.KeyFactory
import java.security.PrivateKey
import java.security.Security
import java.security.spec.PKCS8EncodedKeySpec
import javax.crypto.spec.SecretKeySpec
import java.util.Base64

import com.electriccloud.client.groovy.ElectricFlow
import com.electriccloud.client.groovy.models.Credential

Security.addProvider(new BouncyCastleProvider())

/**
* ZeroTrust
*/
class ZeroTrust extends FlowPlugin {

    @Override
    Map<String, Object> pluginInfo() {
        return [
                pluginName     : '@PLUGIN_KEY@',
                pluginVersion  : '@PLUGIN_VERSION@',
                configFields   : ['config'],
                configLocations: ['ec_plugin_cfgs'],
                defaultConfigValues: [:]
        ]
    }
/** This is a special method for checking connection during configuration creation
    */
    def checkConnection(StepParameters p, StepResult sr) {
        // Use this pre-defined method to check connection parameters
        try {
            // Put some checks here
            def config = context.configValues
            log.info(config)
            // Getting parameters:
            // log.info config.asMap.get('config')
            // log.info config.asMap.get('desc')
            // log.info config.asMap.get('endpoint')
            // log.info config.asMap.get('role')
            // log.info config.asMap.get('credential')
            // assert config.getRequiredCredential("credential").secretValue == "secret"
            def algorithm =  config.asMap.get('algorithm')
            def provider = config.asMap.get('provider')
            def issuer = config.asMap.get('issuer')
            def tokenLifeTime = config.asMap.get('tokenLifeTime')
            def endpoint = config.asMap.get('endpoint')
            def privateKeyString = config.getRequiredCredential("credential").secretValue //private key
            def customClaims = config.asMap.get('testConnectionClaims')
            def role = config.asMap.get('role')
            def namespace = config.asMap.get('namespace')
            log.info "Namespace: ${namespace}"

            JsonSlurper jsonSlurper = new JsonSlurper()
            Map<String, Object> fullClaims = jsonSlurper.parseText(customClaims)

            long nowSeconds = System.currentTimeMillis()/1000
            long expSeconds = nowSeconds + tokenLifeTime.toInteger()

            Map<String, Object> updateClaims = [ iss: issuer, iat: nowSeconds, exp: expSeconds]
            fullClaims.putAll(updateClaims)
            fullClaims = processTemplate(fullClaims, role, namespace, endpoint)

            String jwt = createJWT(privateKeyString, algorithm, fullClaims)
            //println "Generated JWT with $algorithm: $jwt"
            println "JWT successfully generated."

            // Vault config
            VaultConfig vaultConfig = new VaultConfig()
                    .address(endpoint)

            if(namespace != null && !namespace.isEmpty()) {
                vaultConfig = vaultConfig.nameSpace(namespace)
            }
            vaultConfig = vaultConfig.build()
            final Vault vault = Vault.create(vaultConfig);
            // login using JWT
            AuthResponse response = vault.auth().loginByJwt(provider, role, jwt);
            String token = response.getAuthClientToken();
            println "Got token from Vault."
            //println("Vault Token: " + token);

        }  catch (Throwable e) {
            // Set this property to show the error in the UI
            sr.setOutcomeProperty("/myJob/configError", e.message + System.lineSeparator())
            sr.apply()
            throw e
        }
    }

    private Map<String, Object> processTemplate(Map<String, Object> fullClaims, role, namespace, endpoint) {
        fullClaims = fullClaims.collectEntries { k, v -> [k, v.toString().isInteger() ? v : v.toString().replace("<role>", role).replace("<namespace>", namespace).replace("<vault-url>", endpoint)] }
        log.info "Claims: ${fullClaims}"
        fullClaims
    }
// === check connection ends ===


    Key getKey(String keyString, String algorithm) {
        byte[] decodedKey = Base64.getDecoder().decode(keyString.replaceAll("-----\\w+ PRIVATE KEY-----", "").replaceAll("\\s", ""))
        switch (algorithm) {
            case ~/HS256|HS384|HS512/:
                return new SecretKeySpec(decodedKey, "Hmac" + algorithm.substring(2))
            case ~/RS256|RS384|RS512|PS256|PS384|PS512/:
                return getPrivateKey(decodedKey, "RSA")
            case ~/ES256|ES384|ES512/:
                return getPrivateKey(decodedKey, "EC")
            case "EdDSA":
                return getPrivateKey(decodedKey, "Ed25519")
            default:
                throw new IllegalArgumentException("Unsupported algorithm: $algorithm")
        }
    }

    PrivateKey getPrivateKey(byte[] keyBytes, String algorithm) {
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes)
        KeyFactory keyFactory = KeyFactory.getInstance(algorithm)
        return keyFactory.generatePrivate(keySpec)
    }

    String createJWT(String keyString, String algorithm, Map<String, Object> claims) {
        Key key = getKey(keyString, algorithm)
        SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.forName(algorithm)

        return Jwts.builder()
                .setClaims(claims)
                .signWith(key, signatureAlgorithm)
                .compact()
    }

/**
    * updateCdroCredentialThroughJwtRequest - UpdateCdroCredentialThroughJwtRequest/UpdateCdroCredentialThroughJwtRequest
    * Add your code into this method and it will be called when the step runs
    * @param config (required: true)
    * @param customClaims (required: true)
    * @param credentialProjectName (required: true)
    * @param credentialName (required: true)
    */
    def updateCdroCredentialThroughJwtRequest(StepParameters p, StepResult sr) {
        // Use this parameters wrapper for convenient access to your parameters
        UpdateCdroCredentialThroughJwtRequestParameters sp = UpdateCdroCredentialThroughJwtRequestParameters.initParameters(p)

        // Calling logger:
        log.info p.asMap.get('config')
        log.info p.asMap.get('customClaims')
        log.info p.asMap.get('credentialProjectName')
        log.info p.asMap.get('credentialName')

        def config = context.configValues
        def issuer = config.asMap.get('issuer')
        def provider = config.asMap.get('provider')
        def tokenLifeTime = config.asMap.get('tokenLifeTime')
        def algorithm =  config.asMap.get('algorithm')
        def endpoint = config.asMap.get('endpoint')
        def privateKeyString = config.getRequiredCredential("credential").secretValue //private key
        def customClaims = config.asMap.get('customClaims')
        def secretPath = p.asMap.get('secretPath')
        def role = config.asMap.get('role')
        def namespace = config.asMap.get('namespace')
        log.info "Namespace: ${namespace}"

        JsonSlurper jsonSlurper = new JsonSlurper()
        Map<String, Object> fullClaims = jsonSlurper.parseText(customClaims)

        long nowSeconds = System.currentTimeMillis()/1000
        long expSeconds = nowSeconds + tokenLifeTime.toInteger()


        Map<String, Object> updateClaims = ["iss": issuer, iat: nowSeconds, exp: expSeconds]
        fullClaims.putAll(updateClaims)
        fullClaims = processTemplate(fullClaims, role, namespace, endpoint)

        String jwt = createJWT(privateKeyString, algorithm, fullClaims)
        log.trace  "Generated JWT with $algorithm: $jwt"
        log.info  "JWT successfully generated."

        // Vault config
        VaultConfig vaultConfig = new VaultConfig()
                .address(endpoint)
        if(namespace != null && !namespace.isEmpty()) {
            vaultConfig = vaultConfig.nameSpace(namespace)
        }

        vaultConfig = vaultConfig.build()
        Vault vault = Vault.create(vaultConfig);
        // login using JWT
        AuthResponse response = vault.auth().loginByJwt(provider, role, jwt);
        String token = response.getAuthClientToken();
        log.info  "Got token from Vault."
        log.trace  "Vault Token: " + token
        vaultConfig = new VaultConfig()
                .address(endpoint)
                .token(token)
        if(namespace != null && !namespace.isEmpty()) {
            vaultConfig = vaultConfig.nameSpace(namespace)
        }

        vaultConfig = vaultConfig.build()

        vault = vault = Vault.create(vaultConfig)
        // Read the secret
        def mount = config.asMap.get('secret_mount_path')
        log.info "Mount: ${mount}"
        def vaultSecretPath = "${mount}/${secretPath}"
        log.info "vaultSecretPath Path: $vaultSecretPath"
        LogicalResponse logicalResponse = vault.logical()
                .read(vaultSecretPath);

        def secretData = logicalResponse.getData()
        log.trace ("Secret Data: " + secretData)

        ElectricFlow ef = FlowAPI.getEc()
        def credentialProjectName = sp.credentialProjectName
        def credentialName = sp.credentialName

        if(secretData.size() > 2)
            throw new IllegalArgumentException("The secret contains more than two fields. The secret should contain only username and password fields.")
        if(secretData.isEmpty())
            throw new IllegalArgumentException("The secret is empty. The secret should contain username and password fields.")

        def userName="", password=""
        if(secretData.size() == 1){
            userName = secretData.keySet().iterator().next()
            password = secretData.get(userName)
        }

        if(secretData.size() == 2){
            userName = secretData.username
            password = secretData.password
        }
        log.trace ("username: $userName password: $password")

        ef.modifyCredential(projectName: credentialProjectName, credentialName: credentialName, userName: userName, password: password)
        log.info ("Credential $credentialName updated successfully")
        log.info("step UpdateCdroCredentialThroughJwtRequest has been finished")
    }

/**
    * issueJwtAndStoreInProperty - IssueJwtAndStoreInProperty/IssueJwtAndStoreInProperty
    * Add your code into this method and it will be called when the step runs
    * @param config (required: true)
    * @param customClaims (required: true)
    * @param propertyPath (required: true)
    */
    def issueJwtAndStoreInProperty(StepParameters p, StepResult sr) {
        // Use this parameters wrapper for convenient access to your parameters
        IssueJwtAndStoreInPropertyParameters sp = IssueJwtAndStoreInPropertyParameters.initParameters(p)

        // Calling logger:
        log.info p.asMap.get('config')
        log.info p.asMap.get('customClaims')
        log.info p.asMap.get('propertyPath')

        def config = context.configValues
        def algorithm =  config.asMap.get('algorithm')
        def provider = config.asMap.get('provider')
        def issuer = config.asMap.get('issuer')
        def tokenLifeTime = config.asMap.get('tokenLifeTime')
        def endpoint = config.asMap.get('endpoint')
        def privateKeyString = config.getRequiredCredential("credential").secretValue //private key
        def customClaims = config.asMap.get('customClaims')
        JsonSlurper jsonSlurper = new JsonSlurper()
        Map<String, Object> fullClaims = jsonSlurper.parseText(customClaims)

        long nowSeconds = System.currentTimeMillis()/1000
        long expSeconds = nowSeconds + tokenLifeTime.toInteger()

        Map<String, Object> updateClaims = [iss: issuer, iat: nowSeconds, exp: expSeconds]
        fullClaims.putAll(updateClaims)
        log.info "Claims: ${fullClaims}"

        String jwt = createJWT(privateKeyString, algorithm, fullClaims)
        log.trace  "Generated JWT with $algorithm: $jwt"
        log.info  "JWT successfully generated."

        // Setting job step summary to the config name
        def propertyPath = sp.propertyPath
        sr.setOutcomeProperty(propertyPath, jwt)
        sr.apply()
        log.info("step IssueJwtAndStoreInProperty has been finished")
    }

/**
    * getCdroCredentialAndRunStep - getCdroCredentialAndRunStep/getCdroCredentialAndRunStep
    * Add your code into this method and it will be called when the step runs
    * @param config (required: true)
    * @param customClaims (required: true)
    * @param shellOfStepCommandToRun (required: true)
    * @param stepCommandToRun (required: true)
    */
    def getCdroCredentialAndRunStep(StepParameters p, StepResult sr) {
        // Use this parameters wrapper for convenient access to your parameters
        GetCdroCredentialAndRunStepParameters sp = GetCdroCredentialAndRunStepParameters.initParameters(p)

        // Calling logger:
        log.info p.asMap.get('config')
        log.info p.asMap.get('customClaims')
        log.info p.asMap.get('stepCommandToRun')
        log.info p.asMap.get('shellOfStepCommandToRun')

        def config = context.configValues
        def algorithm =  config.asMap.get('algorithm')
        def provider = config.asMap.get('provider')
        def issuer = config.asMap.get('issuer')
        def tokenLifeTime = config.asMap.get('tokenLifeTime')
        def endpoint = config.asMap.get('endpoint')
        def privateKeyString = config.getRequiredCredential("credential").secretValue //private key
        def customClaims = config.asMap.get('customClaims')
        def secretPath = p.asMap.get('secretPath')
        def role = config.asMap.get('role')
        def namespace = config.asMap.get('namespace')

        JsonSlurper jsonSlurper = new JsonSlurper()
        Map<String, Object> fullClaims = jsonSlurper.parseText(customClaims)

        long nowSeconds = System.currentTimeMillis()/1000
        long expSeconds = nowSeconds + tokenLifeTime.toInteger()

        Map<String, Object> updateClaims = [ iss: issuer, iat: nowSeconds, exp: expSeconds]
        fullClaims.putAll(updateClaims)
        fullClaims = processTemplate(fullClaims, role, namespace, endpoint)

        String jwt = createJWT(privateKeyString, algorithm, fullClaims)
        log.trace  "Generated JWT with $algorithm: $jwt"
        log.info  "JWT successfully generated."

        // Vault config
        VaultConfig vaultConfig = new VaultConfig()
                .address(endpoint)
        if(namespace != null && !namespace.isEmpty()) {
            vaultConfig = vaultConfig.nameSpace(namespace)
        }

        vaultConfig = vaultConfig.build()
        Vault vault = Vault.create(vaultConfig);
        AuthResponse response = vault.auth().loginByJwt(provider, role, jwt);
        String token = response.getAuthClientToken();
        log.info  "Got token from Vault."
        log.trace  "Vault Token: " + token
        vaultConfig = new VaultConfig()
                .address(endpoint)
                .token(token)
        if(namespace != null && !namespace.isEmpty()) {
            vaultConfig = vaultConfig.nameSpace(namespace)
        }

        vaultConfig = vaultConfig.build()

        vault = vault = Vault.create(vaultConfig)
        // Read the secret
        def mount = config.asMap.get('secret_mount_path')
        log.info "Mount: ${mount}"
        def vaultSecretPath = "${mount}/${secretPath}"
        log. info "vaultSecretPath Path: $vaultSecretPath"
        LogicalResponse logicalResponse = vault.logical()
                .read(vaultSecretPath);

        log.trace ("Secret Data: " + logicalResponse.getData())

        // create dynamic job step and run it with the secret passed as a parameter
        def shellToRun = sp.shellOfStepCommandToRun ?: ""
        def commandToRun = sp.stepCommandToRun
        ElectricFlow ef = FlowAPI.getEc()
        String password= JsonOutput.toJson(logicalResponse.getData())

        def arg = new Credential(
                credentialName: "zt_credential",
                password:password
        )
        ef.createJobStep(
                jobStepName: "ZeroTrust",
                shell: shellToRun,
                command: commandToRun,
                credentials: [arg]
        )
        sr.apply()
        log.info("step getCdroCredentialAndRunStep has been finished")
    }

// === step ends ===

}