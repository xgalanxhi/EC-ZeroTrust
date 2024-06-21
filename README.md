EC-ZeroTrust Plugin

EC-ZeroTrust



Plugin version 1.0.0

Revised on Fri Jun 21 15:21:56 ICT 2024


* * *


Contents



*   [Overview](#overview)
*   [Plugin Configurations](#plugin-configurations)
*   [Plugin Procedures](#plugin-procedures)
    *   [UpdateCdroCredentialThroughJwtRequest](#updatecdrocredentialthroughjwtrequest)
    *   [getCdroCredentialAndRunStep](#getcdrocredentialandrunstep)
    *   [getAuthorizedTokenAndRunStep](#getauthorizedtokenandrunstep)
    *   [issueJwtAndStoreInProperty](#issuejwtandstoreinproperty)
*   [Use Cases](#use-cases)
    *   [1 provide identification to procedure/pipelines](#1-provide-identification-to-procedure/pipelines)
    *   [1 provide identification to external applications (e.g. AAP)](#1-provide-identification-to-external-applications-(e.g.-aap))
*   [Known Issues](#known-issues)
*   [Change Log](#change-log)

## Overview


enable JWT authentication for Zero Trust implementation




## Plugin Configurations

Plugin configurations are sets of parameters that can be applied across some, or all, of the plugin procedures. They can reduce the repetition of common values, create predefined parameter sets, and securely store credentials. Each configuration is given a unique name that is entered in the designated parameter for the plugin procedures that use them.

### Creating Plugin Configurations

*   To create plugin configurations in CloudBees CD/RO, complete the following steps:
*   Navigate to DevOps Essentials  Plugin Management  Plugin configurations.
*   Select Add plugin configuration to create a new configuration.
*   In the New Configuration window, specify a Name for the configuration.
*   Select the Project that the configuration belongs to.
*   Optionally, add a Description for the configuration.
*   Select the appropriate Plugin for the configuration.
*   Configure the parameters per the descriptions below.

Configuration Parameters

| Parameter | Description |
| --- | --- |
| **Configuration Name** | Unique name for the configuration |
| Description | Configuration description |
| **config** | The name for the created configuration |
| desc | Description for the configuration |
| **endpoint** | Hashicorp vault endpoint to connect to. e.g. https://vault.swen.aws.ps.beescloud.com, can be referred in custom claims by using "&lt;vault-url&gt;" |
| **role** | role to use for jwt authentication, can be referred in custom claims by using "&lt;role&gt;" |
| **provider** | Provider of JWT token. |
| **issuer** | The issuer of the JWT token, e.g. https://cd.gettingstarted.swen.aws.ps.beescloud.com/myProject |
| **customClaims** | JSON claims to build the JWT payload |
| **testConnectionClaims** | JSON claims to build the JWT payload for test connection |
| **tokenLifeTime** | The lifetime of the JWT token in seconds |
| **credential** | JWT encryption key |
| **algorithm** | The algorithm to use for JWT token generation |
| **secret_mount_path** | The path to the KV mount containing the secret to read, such as secret. |
| namespace | The namespace to use for the secret. e.g. ns1/ns2, can be referred in custom claims by using "&lt;namespace&gt;" |
| Check Connection? | If checked, a connection endpoint and credentials will be tested before save. The configuration will not be saved if the test fails. |
| Debug Level | This option sets debug level for logs. If info is selected, only summary information will be shown, for debug, there will be some debug information and for trace the whole requests and responses will be shown. |

## Plugin Procedures

**IMPORTANT** Note that the names of **Required** parameters are marked in *bold** in the parameter description table for each procedure.




## UpdateCdroCredentialThroughJwtRequest

Update CDRO Credential with Zero Trust JWT token authentication process, If the secret has one key-value pair, it will set the key as the userName and the value as the password, or if the secret has two key-value pair with key username and password, it will set the value of key username as the username, and set the value of password as password of the credential, or if the secret has more than two key-value pairs, it will store the secret as a JSON string in the password of the credential

### UpdateCdroCredentialThroughJwtRequest Parameters

| Parameter | Description |
| --- | --- |
| **Configuration Name** | Previously defined configuration for the plugin |
| **credentialProjectName** | The project name of the CDRO credential to be updated. |
| **credentialName** | The name of the CDRO credential to be updated. |
| **secretPath** | The path to the secret to read, such as data/my-secret (mount is not included). |



## getCdroCredentialAndRunStep

Get secret with Zero Trust JWT token authentication process, and stored it in the password field of a credential parameter as a JSON string, then pass to the next step

### getCdroCredentialAndRunStep Parameters

| Parameter | Description |
| --- | --- |
| **Configuration Name** | Previously defined configuration for the plugin |
| **secretPath** | The path to the secret to read, such as data/my-secret (mount is not included). |
| shellOfStepCommandToRun | The shell of the command to run after getting the credential<br>(note: the credential name will always be zt_credential), e.g. <br>import com.electriccloud.client.groovy.ElectricFlow<br>import groovy.json.JsonSlurper<br>ElectricFlow ef = new ElectricFlow()<br>def password=ef.getFullCredential(credentialName: "zt_credential").credential.password<br>def secretMap = new JsonSlurper().parseText(password)<br> |
| **stepCommandToRun** | The command to run after getting the credential |



## getAuthorizedTokenAndRunStep

Get authorized token with Zero Trust JWT token authentication process, and stored it in the password field of a credential parameter, then pass to the next step

### getAuthorizedTokenAndRunStep Parameters

| Parameter | Description |
| --- | --- |
| **Configuration Name** | Previously defined configuration for the plugin |
| shellOfStepCommandToRun | The shell of the command to run after getting the credential<br>(note: the credential name will always be zt_credential), e.g. <br>import com.electriccloud.client.groovy.ElectricFlow<br>ElectricFlow ef = new ElectricFlow()<br>def token=ef.getFullCredential(credentialName: "zt_credential").credential.password<br> |
| **stepCommandToRun** | The command to run after getting the credential |



## issueJwtAndStoreInProperty

Issue JWT token and store in a property for later usage

### issueJwtAndStoreInProperty Parameters

| Parameter | Description |
| --- | --- |
| **Configuration Name** | Previously defined configuration for the plugin |
| **propertyPath** | The path of the CDRO property to store the JWT token. |



## Use Cases


### 1 provide identification to procedure/pipelines

### 1 provide identification to external applications (e.g. AAP)




## Known Issues


*   OIDC Discovery or JWKS is not supported yet. Now supports static certificate only



## Change Log


*   1.0 initial release
