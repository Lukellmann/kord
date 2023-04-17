package dev.kord.rest.service

import dev.kord.rest.json.response.BotGatewayResponse
import dev.kord.rest.json.response.GatewayResponse
import dev.kord.rest.request.RequestHandler
import dev.kord.rest.route.Route

public class GatewayService(requestHandler: RequestHandler) : RestService(requestHandler) {
    public suspend fun getGateway(): GatewayResponse = call(Route.GatewayGet)
    public suspend fun getGatewayBot(): BotGatewayResponse = call(Route.GatewayBotGet)
}
