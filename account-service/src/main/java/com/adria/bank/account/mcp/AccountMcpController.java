package com.adria.bank.account.mcp;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/mcp")
@RequiredArgsConstructor
public class AccountMcpController {

    private final AccountMcpTools mcpTools;

    @GetMapping("/tools")
    public ResponseEntity<List<Map<String, Object>>> listTools() {
        return ResponseEntity.ok(List.of(
                Map.of(
                        "name", "getAccountBalance",
                        "description", "Consulter le solde, la devise et le statut d'un compte bancaire",
                        "inputSchema", Map.of(
                                "type", "object",
                                "properties", Map.of("accountId", Map.of("type", "string", "description", "UUID du compte bancaire")),
                                "required", List.of("accountId")
                        )
                ),
                Map.of(
                        "name", "checkAccountStatus",
                        "description", "Vérifier si un compte est actif (ACTIVE) ou bloqué (BLOCKED)",
                        "inputSchema", Map.of(
                                "type", "object",
                                "properties", Map.of("accountId", Map.of("type", "string", "description", "UUID du compte bancaire")),
                                "required", List.of("accountId")
                        )
                ),
                Map.of(
                        "name", "debitAccount",
                        "description", "Débiter un montant sur un compte bancaire actif",
                        "inputSchema", Map.of(
                                "type", "object",
                                "properties", Map.of(
                                        "accountId", Map.of("type", "string", "description", "UUID du compte"),
                                        "amount", Map.of("type", "number", "description", "Montant à débiter")
                                ),
                                "required", List.of("accountId", "amount")
                        )
                ),
                Map.of(
                        "name", "creditAccount",
                        "description", "Créditer un montant sur un compte bancaire actif",
                        "inputSchema", Map.of(
                                "type", "object",
                                "properties", Map.of(
                                        "accountId", Map.of("type", "string", "description", "UUID du compte"),
                                        "amount", Map.of("type", "number", "description", "Montant à créditer")
                                ),
                                "required", List.of("accountId", "amount")
                        )
                )
        ));
    }

    @PostMapping("/tools/{toolName}")
    public ResponseEntity<?> executeTool(@PathVariable String toolName, @RequestBody(required = false) Map<String, Object> arguments) {
        if (arguments == null) {
            arguments = Map.of();
        }
        return switch (toolName) {
            case "getAccountBalance" -> {
                UUID id = UUID.fromString(arguments.get("accountId").toString());
                yield ResponseEntity.ok(Map.of("result", mcpTools.getAccountBalance(id)));
            }
            case "checkAccountStatus" -> {
                UUID id = UUID.fromString(arguments.get("accountId").toString());
                yield ResponseEntity.ok(Map.of("result", mcpTools.checkAccountStatus(id)));
            }
            case "debitAccount" -> {
                UUID id = UUID.fromString(arguments.get("accountId").toString());
                double amount = Double.parseDouble(arguments.get("amount").toString());
                yield ResponseEntity.ok(mcpTools.debitAccount(id, amount));
            }
            case "creditAccount" -> {
                UUID id = UUID.fromString(arguments.get("accountId").toString());
                double amount = Double.parseDouble(arguments.get("amount").toString());
                yield ResponseEntity.ok(mcpTools.creditAccount(id, amount));
            }
            default -> ResponseEntity.badRequest().body(Map.of("error", "Unknown MCP tool: " + toolName));
        };
    }
}

