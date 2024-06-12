EC-ZeroTrust Plugin

EC-ZeroTrust



Plugin version 1.0.0

Revised on Wed Jun 12 10:59:47 ICT 2024


* * *


Contents



*   [Overview](#overview)
*   [Plugin Configurations](#plugin-configurations)
*   [Plugin Procedures](#plugin-procedures)
    *   [UpdateCdroCredentialThroughJwtRequest](#updatecdrocredentialthroughjwtrequest)
    *   [getCdroCredentialAndRunStep](#getcdrocredentialandrunstep)
    *   [IssueJwtAndStoreInProperty](#issuejwtandstoreinproperty)

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
| Check Connection? | If checked, a connection endpoint and credentials will be tested before save. The configuration will not be saved if the test fails. |
| Debug Level | This option sets debug level for logs. If info is selected, only summary information will be shown, for debug, there will be some debug information and for trace the whole requests and responses will be shown. |

## Plugin Procedures

**IMPORTANT** Note that the names of **Required** parameters are marked in *bold** in the parameter description table for each procedure.




## UpdateCdroCredentialThroughJwtRequest

Update CDRO Credential with Zero Trust JWT token authentication process

### UpdateCdroCredentialThroughJwtRequest Parameters

| Parameter | Description |
| --- | --- |
| **Configuration Name** | Previously defined configuration for the plugin |
| **credentialProjectName** | The project name of the CDRO credential to be updated. |
| **credentialName** | The name of the CDRO credential to be updated. |
| **secretPath** | The path to the secret to read, such as secret/data/my-secret. |



## getCdroCredentialAndRunStep

Get CDRO Credential with Zero Trust JWT token authentication process, and pass the credential to the next step

### getCdroCredentialAndRunStep Parameters

| Parameter | Description |
| --- | --- |
| **Configuration Name** | Previously defined configuration for the plugin |
| **secretPath** | The path to the secret to read, such as secret/data/my-secret. |
| shellOfStepCommandToRun | The shell of the command to run after getting the credential<br>(note: the credential name will always be zt_credential), e.g. <br>import com.electriccloud.client.groovy.ElectricFlow<br>import groovy.json.JsonSlurper<br>ElectricFlow ef = new ElectricFlow()<br>def password=ef.getFullCredential(credentialName: "zt_credential").credential.password<br>def secretMap = new JsonSlurper().parseText(password)<br> |
| **stepCommandToRun** | The command to run after getting the credential |



## IssueJwtAndStoreInProperty

Issue JWT token and store in a property for later usage

### IssueJwtAndStoreInProperty Parameters

| Parameter | Description |
| --- | --- |
| **Configuration Name** | Previously defined configuration for the plugin |
| **propertyPath** | The path of the CDRO property to store the JWT token. |
