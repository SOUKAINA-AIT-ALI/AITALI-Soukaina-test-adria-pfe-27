package com.adria.bank.transaction.mcp;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/mcp")
@RequiredArgsConstructor
public class TransactionMcpController {

    private final TransactionMcpTools mcpTools;

    @GetMapping("/tools")
    public ResponseEntity<List<Map<String, Object>>> listTools() {
        return ResponseEntity.ok(List.of(
                Map.of(
                        "name", "createTransaction",
                        "description", "Enregistrer une transaction bancaire sur un compte",
                        "inputSchema", Map.of(
                                "type", "object",
                                "properties", Map.of(
                                        "accountId", Map.of("type", "string", "description", "UUID du compte"),
                                        "amount", Map.of("type", "number", "description", "Montant de la transaction"),
                                        "currency", Map.of("type", "string", "description", "Devise de la transaction (ex: MAD, EUR)"),
                                        "merchantCategory", Map.of("type", "string", "description", "Catégorie commerçant"),
                                        "country", Map.of("type", "string", "description", "Code pays (ex: MA, FR)")
                                ),
                                "required", List.of("accountId", "amount", "currency")
                        )
                ),
                Map.of(
                        "name", "transferMoney",
                        "description", "Effectuer un virement bancaire d'un compte source vers un compte destination",
                        "inputSchema", Map.of(
                                "type", "object",
                                "properties", Map.of(
                                        "sourceAccountId", Map.of("type", "string", "description", "UUID compte émetteur"),
                                        "targetAccountId", Map.of("type", "string", "description", "UUID compte destinataire"),
                                        "amount", Map.of("type", "number", "description", "Montant du virement"),
                                        "currency", Map.of("type", "string", "description", "Devise du virement")
                                ),
                                "required", List.of("sourceAccountId", "targetAccountId", "amount", "currency")
                        )
                ),
                Map.of(
                        "name", "getTransactionsByAccount",
                        "description", "Consulter l'historique des transactions d'un compte bancaire",
                        "inputSchema", Map.of(
                                "type", "object",
                                "properties", Map.of(
                                        "accountId", Map.of("type", "string", "description", "UUID du compte")
                                ),
                                "required", List.of("accountId")
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
            case "createTransaction" -> {
                UUID accountId = UUID.fromString(arguments.get("accountId").toString());
                BigDecimal amount = new BigDecimal(arguments.get("amount").toString());
                String currency = arguments.get("currency").toString();
                String category = arguments.getOrDefault("merchantCategory", "RETAIL").toString();
                String country = arguments.getOrDefault("country", "MA").toString();
                yield ResponseEntity.ok(mcpTools.createTransaction(accountId, amount, currency, category, country));
            }
            case "transferMoney" -> {
                UUID from = UUID.fromString(arguments.get("sourceAccountId").toString());
                UUID to = UUID.fromString(arguments.get("targetAccountId").toString());
                BigDecimal amount = new BigDecimal(arguments.get("amount").toString());
                String currency = arguments.get("currency").toString();
                yield ResponseEntity.ok(Map.of("result", mcpTools.transferMoney(from, to, amount, currency)));
            }
            case "getTransactionsByAccount" -> {
                UUID accountId = UUID.fromString(arguments.get("accountId").toString());
                yield ResponseEntity.ok(mcpTools.getTransactionsByAccount(accountId));
            }
            default -> ResponseEntity.badRequest().body(Map.of("error", "Unknown MCP tool: " + toolName));
        };
    }
}

