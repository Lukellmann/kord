@file:GenerateKordEnum(
    "OAuth2Scope", valueType = STRING, valueName = "name",
    docUrl = "https://discord.com/developers/docs/topics/oauth2#shared-resources-oauth2-scopes",
    entries = [
        Entry(
            "ActivitiesRead", stringValue = "activities.read",
            kDoc = "Allows your app to fetch data from a user's \"Now Playing/Recently Played\" list - not currently " +
                "available for apps.",
        ),
        Entry(
            "ActivitiesWrite", stringValue = "activities.write",
            kDoc = "Allows your app to update a user's activity - requires Discord approval.",
        ),
        Entry(
            "ApplicationsBuildsRead", stringValue = "applications.builds.read",
            kDoc = "Allows your app to read build data for a user's applications.",
        ),
        Entry(
            "ApplicationsBuildsUpload", stringValue = "applications.builds.upload",
            kDoc = "Allows your app to upload/update builds for a user's applications - requires Discord approval.",
        ),
        Entry(
            "ApplicationsCommands", stringValue = "applications.commands",
            kDoc = "Allows your app to use commands in a guild.",
        ),
        Entry(
            "ApplicationsCommandsUpdate", stringValue = "applications.commands.update",
            kDoc = "Allows your app to update its commands using a Bearer token - [client·credentials·grant]" +
                "(https://discord.com/developers/docs/topics/oauth2#client-credentials-grant) only.",
        ),
        Entry(
            "ApplicationsCommandsPermissionsUpdate", stringValue = "applications.commands.permissions.update",
            kDoc = "Allows your app to update permissions for its commands in a guild a user has permissions to.",
        ),
        Entry(
            "ApplicationsEntitlements", stringValue = "applications.entitlements",
            kDoc = "Allows your app to read entitlements for a user's applications.",
        ),
        Entry(
            "ApplicationsStoreUpdate", stringValue = "applications.store.update",
            kDoc = "Allows your app to read and update store data (SKUs, store listings, achievements, etc.) for a " +
                "user's applications.",
        ),
        Entry(
            "Bot", stringValue = "bot",
            kDoc = "For OAuth2 bots, this puts the bot in the user's selected guild by default.",
        ),
        Entry(
            "Connections", stringValue = "connections",
            kDoc = "Allows `/users/@me/connections` to return linked third-party accounts.",
        ),
        Entry(
            "DmChannelsRead", stringValue = "dm_channels.read",
            kDoc = "Allows your app to see information about the user's DMs and group DMs - requires Discord approval.",
        ),
        Entry("Email", stringValue = "email", kDoc = "Enables `/users/@me` to return an `email`."),
        Entry("GdmJoin", stringValue = "gdm.join", kDoc = "Allows your app to join users to a group dm."),
        Entry(
            "Guilds", stringValue = "guilds",
            kDoc = "Allows `/users/@me/guilds` to return basic information about all of a user's guilds.",
        ),
        Entry(
            "GuildsJoin", stringValue = "guilds.join",
            kDoc = "Allows `/guilds/{guild.id}/members/{user.id}` to be used for joining users to a guild.",
        ),
        Entry(
            "GuildsMembersRead", stringValue = "guilds.members.read",
            kDoc = "Allows `/users/@me/guilds/{guild.id}/member` to return a user's member information in a guild.",
        ),
        Entry("Identify", stringValue = "identify", kDoc = "Allows `/users/@me` without `email`."),
        Entry(
            "MessagesRead", stringValue = "messages.read",
            kDoc = "For local rpc server api access, this allows you to read messages from all client channels " +
                "(otherwise restricted to channels/guilds your app creates).",
        ),
        Entry(
            "RelationshipsRead", stringValue = "relationships.read",
            kDoc = "Allows your app to know a user's friends and implicit relationships - requires Discord approval.",
        ),
        Entry(
            "RoleConnectionsWrite", stringValue = "role_connections.write",
            kDoc = "Allows your app to update a user's connection and metadata for the app.",
        ),
        Entry(
            "Rpc", stringValue = "rpc",
            kDoc = "For local rpc server access, this allows you to control a user's local Discord client - requires " +
                "Discord approval.",
        ),
        Entry(
            "RpcActivitiesWrite", stringValue = "rpc.activities.write",
            kDoc = "For local rpc server access, this allows you to update a user's activity - requires Discord " +
                "approval.",
        ),
        Entry(
            "RpcNotificationsRead", stringValue = "rpc.notifications.read",
            kDoc = "For local rpc server access, this allows you to receive notifications pushed out to the user - " +
                "requires Discord approval.",
        ),
        Entry(
            "RpcVoiceRead", stringValue = "rpc.voice.read",
            kDoc = "For local rpc server access, this allows you to read a user's voice settings and listen for " +
                "voice events - requires Discord approval.",
        ),
        Entry(
            "RpcVoiceWrite", stringValue = "rpc.voice.write",
            kDoc = "For local rpc server access, this allows you to update a user's voice settings - requires " +
                "Discord approval.",
        ),
        Entry(
            "Voice", stringValue = "voice",
            kDoc = "Allows your app to connect to voice on user's behalf and see all the voice members - requires " +
                "Discord approval.",
        ),
        Entry(
            "WebhookIncoming", stringValue = "webhook.incoming",
            kDoc = "This generates a webhook that is returned in the oauth token response for authorization code " +
                "grants.",
        ),
    ],
)

package dev.kord.common.entity

import dev.kord.ksp.GenerateKordEnum
import dev.kord.ksp.GenerateKordEnum.Entry
import dev.kord.ksp.GenerateKordEnum.ValueType.STRING
