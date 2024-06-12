$[/myProject/groovy/scripts/preamble.groovy.ignore]

ZeroTrust plugin = new ZeroTrust()
plugin.runStep('$[/myProcedure/name]', 'checkConnection', 'checkConnection')