package api.ldapsubscription.controller;

import api.ldapsubscription.model.Snssai;
import api.ldapsubscription.model.Subscription;
import api.ldapsubscription.service.LdapService;
import org.ldaptive.LdapException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class SubscriptionController {
    @GetMapping("/subscription/{subscriptionId}")
    public ResponseEntity<?> getSubscription(@PathVariable("subscriptionId") UUID subscriptionId,
                                             @RequestParam(required = false) List<String> subscribedEvents,
                                             @RequestParam(required = false) String snssai) throws LdapException {
        Subscription subscription = LdapService.ldapSearch(subscriptionId);
        if (subscription == null) {
            return new ResponseEntity<>("Could not find subscription with ID: " + subscriptionId, HttpStatus.NOT_FOUND);
        }
        if (subscribedEvents != null && !subscription.getSubscribedEvents().containsAll(subscribedEvents)) {
            return new ResponseEntity<>("EVENT_NOT_ALLOWED", HttpStatus.FORBIDDEN);
        }
        if (snssai != null) {
            String[] snssaiParts = snssai.split("-");
            if (snssaiParts.length != 2) {
                return new ResponseEntity<>("Invalid snssai parameter", HttpStatus.BAD_REQUEST);
            }
            Snssai subscriptionSnssai = subscription.getSnssai();
            if (subscriptionSnssai == null || !subscriptionSnssai.getSst().toString().equals(snssaiParts[0]) || !subscriptionSnssai.getSd().equals(snssaiParts[1])) {
                return new ResponseEntity<>("SNSSAI_NOT_ALLOWED", HttpStatus.FORBIDDEN);
            }
        }
        if (subscription.getExpiry().isBefore(LocalDateTime.now())) {
            LdapService.ldapDelete(subscriptionId);
            return new ResponseEntity<>("RECORD_EXPIRED", HttpStatus.FORBIDDEN);
        }
        return new ResponseEntity<>(subscription, HttpStatus.OK);
    }

    @DeleteMapping("/subscription/{subscriptionId}")
    public ResponseEntity<String> deleteSubscription(@PathVariable("subscriptionId") UUID subscriptionId) throws LdapException {
        if (LdapService.ldapDelete(subscriptionId) == false) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    String.format("Could not find subscription with id: %s", subscriptionId)
            );
        }
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/subscription")
    public ResponseEntity<Subscription> addSubscription(@RequestBody Subscription subscription) throws LdapException {
        if (subscription.getSupis() == null || subscription.getSnssai() == null || subscription.getDnn() == null || subscription.getEventFilters() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Parameters supis, snssai, dnn or eventFilters do not exist");
        }
        if (subscription.getExpiry() != null && subscription.getExpiry().isBefore(LocalDateTime.now())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "EXPIRY_NOT_VALID: Expiry date has already passed");
        }
        subscription.setSubscriptionId(UUID.randomUUID());
        if (LdapService.ldapAdd(subscription) == false) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Could not save the subscription.");
        }
        HttpHeaders headers = new HttpHeaders();
        headers.add("Location", String.format("%s/ntsctsf-time-sync/%s/subscriptions/%s", "{apiRoot}", "{apiVersion}", subscription.getSubscriptionId()));
        return new ResponseEntity<>(subscription, headers, HttpStatus.CREATED);
    }
}