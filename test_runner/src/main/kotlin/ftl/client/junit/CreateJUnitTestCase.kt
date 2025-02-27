package ftl.client.junit

import com.google.api.services.toolresults.model.StackTrace
import com.google.api.services.toolresults.model.TestCase
import com.google.testing.model.ToolResultsStep
import ftl.api.JUnitTest
import ftl.reports.api.format
import ftl.reports.api.millis

internal fun createJUnitTestCases(
    testCases: List<TestCase>,
    toolResultsStep: ToolResultsStep,
): List<JUnitTest.Case> = testCases.map { testCase ->
    createJUnitTestCase(
        testCase = testCase,
        toolResultsStep = toolResultsStep,
    )
}

private fun createJUnitTestCase(
    testCase: TestCase,
    toolResultsStep: ToolResultsStep,
): JUnitTest.Case {
    val stackTraces = mapOf(
        testCase.status to testCase.stackTraces?.map(StackTrace::getException)
    )
    return JUnitTest.Case(
        name = testCase.testCaseReference.name,
        classname = testCase.testCaseReference.className,
        time = testCase.elapsedTime.millis().format(),
        failures = stackTraces["failed"],
        errors = stackTraces["error"],
        // skipped = true is represented by null. skipped = false is "absent"
        skipped = if (testCase.status == "skipped") null else "absent"
    ).apply {
        webLink = getWebLink(toolResultsStep, testCase.testCaseId)
        if (testCase.flaky) {
            flaky = true
        }
    }
}

private fun getWebLink(toolResultsStep: ToolResultsStep, testCaseId: String): String {
    return "https://console.firebase.google.com/project/${toolResultsStep.projectId}/" +
        "testlab/histories/${toolResultsStep.historyId}/" +
        "matrices/${toolResultsStep.executionId}/" +
        "executions/${toolResultsStep.stepId}/" +
        "testcases/$testCaseId"
}
