package net.corda.solarsystem

import com.google.gson.GsonBuilder
import java.time.Duration
import java.util.UUID
import kong.unirest.HttpResponse
import kong.unirest.JsonNode
import kong.unirest.Unirest
import kong.unirest.json.JSONObject
import net.corda.solarsystem.flows.LaunchProbeFlowJava
import net.corda.test.dev.network.Credentials
import net.corda.test.dev.network.TestNetwork
import net.corda.test.dev.network.withFlow
import net.corda.test.dev.network.x500Name
import org.apache.http.HttpStatus
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test

class LaunchProbeFlowJavaTest {

    companion object {
        @JvmStatic
        @BeforeAll
        fun setup() {
            TestNetwork.forNetwork("solar-system").verify {
                hasNode("earth").withFlow<LaunchProbeFlowJava>()
                hasNode("mars").withFlow<LaunchProbeFlowJava>()
                hasNode("pluto").withFlow<LaunchProbeFlowJava>()
            }
        }
    }

    @Test
    fun `Start Launch Probe Flow`() {

        TestNetwork.forNetwork("solar-system").use {

            val pluto = getNode("pluto")
            getNode("earth").httpRpc(Credentials("earthling", "password")) {
                val clientId = "client-${UUID.randomUUID()}"
                val flowId = with(startFlow(
                    flowName = LaunchProbeFlowJava::class.java.name,
                    clientId = clientId,
                    parametersInJson = launchProbeFlowParams(
                        message = "Hello pluto",
                        target = pluto.x500Name.toString(),
                        planetaryOnly = false
                    )
                )){
                    Assertions.assertThat(status).isEqualTo(HttpStatus.SC_OK)
                    Assertions.assertThat(body.`object`.get("clientId")).isEqualTo(clientId)
                    val flowId = body.`object`.get("flowId") as JSONObject
                    Assertions.assertThat(flowId).isNotNull
                    flowId.get("uuid") as String
                }

                eventually {
                    with(retrieveOutcome(flowId)) {
                        Assertions.assertThat(status).isEqualTo(HttpStatus.SC_OK)
                        Assertions.assertThat(body.`object`.get("status")).isEqualTo("COMPLETED")
                    }
                }
            }
        }
    }

    @Test
    fun `Start Launch Probe Flow - Planetary Only True`() {

        TestNetwork.forNetwork("solar-system").use {

            val pluto = getNode("pluto")
            getNode("earth").httpRpc(Credentials("earthling", "password")) {
                val clientId = "client-${UUID.randomUUID()}"
                val flowId = with(startFlow(
                    flowName = LaunchProbeFlowJava::class.java.name,
                    clientId = clientId,
                    parametersInJson = launchProbeFlowParams(
                        message = "Hello pluto",
                        target = pluto.x500Name.toString(),
                        planetaryOnly = true
                    )
                )){
                    Assertions.assertThat(status).isEqualTo(HttpStatus.SC_OK)
                    Assertions.assertThat(body.`object`.get("clientId")).isEqualTo(clientId)
                    val flowId = body.`object`.get("flowId") as JSONObject
                    Assertions.assertThat(flowId).isNotNull
                    flowId.get("uuid") as String
                }

                eventually {
                    with(retrieveOutcome(flowId)) {
                        Assertions.assertThat(status).isEqualTo(HttpStatus.SC_OK)
                        Assertions.assertThat(body.`object`.get("status")).isEqualTo("FAILED")
                    }
                }
            }
        }
    }

    private fun launchProbeFlowParams(message: String, target: String, planetaryOnly: Boolean): String {
        return GsonBuilder()
            .create()
            .toJson(mapOf("message" to message, "target" to target, "planetaryOnly" to planetaryOnly.toString()))
    }

    private fun startFlow(
        flowName: String,
        clientId: String = "client-${UUID.randomUUID()}",
        parametersInJson: String
    ): HttpResponse<JsonNode> {
        val body = mapOf(
            "rpcStartFlowRequest" to
                    mapOf(
                        "flowName" to flowName,
                        "clientId" to clientId,
                        "parameters" to mapOf("parametersInJson" to parametersInJson)
                    )
        )
        val request = Unirest.post("flowstarter/startflow")
            .header("Content-Type", "application/json")
            .body(body)

        return request.asJson()
    }

    private fun retrieveOutcome(flowId: String): HttpResponse<JsonNode> {
        val request = Unirest.get("flowstarter/flowoutcome/$flowId").header("Content-Type", "application/json")
        return request.asJson()
    }

    private inline fun <R> eventually(
        duration: Duration = Duration.ofSeconds(5),
        waitBetween: Duration = Duration.ofMillis(100),
        waitBefore: Duration = waitBetween,
        test: () -> R
    ): R {
        val end = System.nanoTime() + duration.toNanos()
        var times = 0
        var lastFailure: AssertionError? = null

        if (!waitBefore.isZero) Thread.sleep(waitBefore.toMillis())

        while (System.nanoTime() < end) {
            try {
                return test()
            } catch (e: AssertionError) {
                if (!waitBetween.isZero) Thread.sleep(waitBetween.toMillis())
                lastFailure = e
            }
            times++
        }

        throw AssertionError("Test failed with \"${lastFailure?.message}\" after $duration; attempted $times times")
    }
}