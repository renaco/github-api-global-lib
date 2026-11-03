def call(Map config=[:]) {
  // only transition when the build has completed without errors
  if( currentBuild.currentResult != 'SUCCESS' ) {
    echo "jiraTransitionIssue: build result is ${currentBuild.currentResult}, skipping transition"
    return
  }
  if( !config.transitionName?.trim() ) {
    error 'jiraTransitionIssue: transitionName is required'
  }
  def issueId = config.issueId ?: getIssueIdFromCommitMessage()
  if( !issueId?.trim() ) {
    echo 'jiraTransitionIssue: no issue id found, skipping transition'
    return
  }
  // transition ids differ per workflow, so look the id up by name
  def rawTransitions = sh(returnStdout: true, script: 'curl -s -u $JIRA_CREDENTIALS -H "Accept: application/json" $JIRA_URL/rest/api/2/issue/'+issueId+'/transitions')
  def transitionId = findTransitionId(rawTransitions, config.transitionName)
  if( !transitionId ) {
    echo "jiraTransitionIssue: transition '${config.transitionName}' not available for ${issueId}, skipping"
    return
  }
  def rawBody = libraryResource 'com/planetpope/api/jira/transitionIssue.json'
  def binding = [
    transitionId: "${transitionId}"
  ]
  def render = renderTemplate(rawBody,binding)
  sh('curl -D- -u $JIRA_CREDENTIALS -X POST --data "'+render+'" -H "Content-Type: application/json" $JIRA_URL/rest/api/2/issue/'+issueId+'/transitions')
}

@NonCPS
def findTransitionId(String rawTransitions, String transitionName) {
  def transitions = new groovy.json.JsonSlurperClassic().parseText(rawTransitions)?.transitions
  return transitions?.find { it.name?.equalsIgnoreCase(transitionName) }?.id
}
