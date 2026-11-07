# Minecraft Plugin: VPB Port Gun

VPB is a Paper Minecraft plugin that adds a port gun: a tagged crossbow that teleports its holder toward a nearby block. This lightweight Minecraft plugin uses a ray trace to find a destination and requires no particles or entities.

## Install

1. Build the project with Java 21 and Maven by running `mvn package`.
2. Copy `target/plugin.jar` into your Paper server’s `plugins` folder.
3. Start or restart the server.

## Use

1. Run `/portgun` as a player with the `portgun.give` permission, or use its alias `/pg`.
2. Hold the Port Gun and right-click while aiming at a block within 32 blocks.
3. The gun teleports you to the adjacent space if that space and the block above it are empty. It has a 1.2-second cooldown.

The plugin targets Paper. It is not a Bedrock add-on. The `portgun.use` permission is enabled by default; `portgun.give` is restricted to operators by default.

---

More Minecraft plugin projects by EnderDevelopment: https://github.com/EnderDevelopment

Project page: https://dash.enderdevelopment.com/community/0147d804-75f3-4725-a446-e22f6da0a234?utm_source=github&utm_medium=readme&utm_campaign=auto_publish&utm_content=0147d804-75f3-4725-a446-e22f6da0a234
