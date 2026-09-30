---
name: create-quarkus-mcp-server
description: >-
  Génère (ou étend) un serveur MCP en Quarkus conforme aux conventions SCIAM.
  À utiliser dès qu'on demande de créer un serveur MCP en Java/Quarkus.
---

# Version

Utilise la version https://github.com/quarkiverse/quarkus-mcp-server/releases/tag/2.0.1 , > JDK 25

## Structure imposée
package : org.sebi


## Extension & transport
Toujours http jamais stdio

## Primitives & annotations
A tool description is required for each @Tool and each @ToolArg.
It must always return a ToolResponse. 

## Référence
https://docs.quarkiverse.io/quarkus-mcp-server/dev/
