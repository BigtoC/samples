package net.corda.ct.flow.test

import net.corda.core.messaging.startFlow
import net.corda.core.utilities.getOrThrow
import net.corda.ct.flow.IssueAndMoveConfidentialTokensFlow
import net.corda.testing.common.internal.testNetworkParameters
import net.corda.testing.core.DUMMY_BANK_A_NAME
import net.corda.testing.core.DUMMY_BANK_B_NAME
import net.corda.testing.driver.DriverParameters
import net.corda.testing.driver.driver
import net.corda.testing.node.TestCordapp
import org.junit.Test

class ConfidentialTokenFlowTests {
    @Test
    fun `issue token test`() {
        val nodeALegalName = DUMMY_BANK_A_NAME
        val nodeBLegalName = DUMMY_BANK_B_NAME

        driver(DriverParameters(isDebug = true,
                networkParameters = testNetworkParameters(minimumPlatformVersion = 4),
                startNodesInProcess = true,
                cordappsForAllNodes = listOf(
                        TestCordapp.findCordapp("net.corda.ct.contract"),
                        TestCordapp.findCordapp("net.corda.ct.flow")
                )
        )) {
            val nodeAFuture = startNode(providedName = nodeALegalName)
            val nodeBFuture = startNode(providedName = nodeBLegalName)

            val (nodeA, nodeB) = listOf(nodeAFuture, nodeBFuture).map { it.getOrThrow() }

            val counterParty = nodeB.nodeInfo.legalIdentities.first()

            val flowHandle = nodeA.rpc.startFlow(::IssueAndMoveConfidentialTokensFlow, counterParty, 100, 10)
            val stx = flowHandle.returnValue.get()
        }
    }
}